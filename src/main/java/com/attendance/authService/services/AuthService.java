package com.attendance.authService.services;

import com.attendance.authService.dto.*;
import com.attendance.authService.entity.PendingEmail;
import com.attendance.authService.entity.Student;
import com.attendance.authService.entity.User;
import com.attendance.authService.entity.UserRole;
import com.attendance.authService.enums.ErrorCodeEnum;
import com.attendance.authService.enums.MessagesEnum;
import com.attendance.authService.exceptions.AdminRoleDeletionException;
import com.attendance.authService.exceptions.RoleNotFoundException;
import com.attendance.authService.exceptions.RoleResponseException;
import com.attendance.authService.exceptions.UserNotFoundException;
import com.attendance.authService.network.RoleClient;
import com.attendance.authService.repo.PendingEmailRepo;
import com.attendance.authService.repo.StudentRepo;
import com.attendance.authService.repo.UserRepo;
import com.attendance.authService.repo.UserRoleRepo;
import com.attendance.authService.util.MyUserDetails;
import com.attendance.authService.util.PasswordGenerator;
import com.attendance.authService.util.ScheduleJobs;
import com.attendance.authService.util.StudentDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AuthService {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private EEmailService emailService;

    @Autowired
    private EmailService emailOtpService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PendingEmailRepo pendingEmailRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleClient roleClient;

    @Autowired
    private StudentDetails studentDetails;

    @Autowired
    private StudentRepo studentRepo;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ScheduleJobs scheduleJobs;

    @Autowired
    private UserRoleRepo userRoleRepo;


    @Value("${frontened.registerUrl}")
    private String frontendUrl;

    @Transactional
    public ResponseEntity<ApiResponseDto<SignUpResponseDto>> registerUser(
            @Valid FacultySignUpRequestDto requestDto) {

        // CHECK EMAIL
        if (userRepo.findByEmail(requestDto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponseDto<>(false,
                            MessagesEnum.EMAIL_ALREADY_EXIST.getMessage(),
                            null,
                            LocalDateTime.now()));
        }

        // REMOVE DUPLICATE ROLES
        List<String> uniqueRoles = requestDto.getRole().stream()
                .distinct()
                .toList();

        // FETCH ROLES
        ApiResponseDto<List<RoleResponseDto>> roleResponse;
        try {
            roleResponse = roleClient.getRolesByNames(uniqueRoles);
        } catch (Exception e) {
            throw new RuntimeException("Role service unavailable");
        }

        List<RoleResponseDto> roles = roleResponse.getData();

        if (roles == null || roles.isEmpty()) {
            throw new RoleNotFoundException(ErrorCodeEnum.S_404.getMessage());
        }

        // CREATE USER (NO STUDENT FIELDS)
        User user = new User();
        user.setUserId(requestDto.getUserId());
        user.setUsername(requestDto.getUsername());
        user.setDepartment(requestDto.getDepartment());
        user.setEmail(requestDto.getEmail());
        user.setContact(requestDto.getContact());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        // CREATE USER ROLES (ATTACH TO USER)
        List<UserRole> userRoles = new ArrayList<>();

        for (RoleResponseDto role : roles) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);          // 🔥 important (FK)
            userRole.setRoleId(role.getId());

            userRoles.add(userRole);
        }

        user.setUserRoles(userRoles);

        // ✅ SINGLE SAVE (CASCADE)
        User savedUser = userRepo.save(user);

        // RESPONSE
        return ResponseEntity.ok(
                new ApiResponseDto<>(true,
                        MessagesEnum.USER_REGISTERED_SUCCESSFUL.getMessage(),
                        new SignUpResponseDto(savedUser.getId()),
                        LocalDateTime.now())
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<ApiResponseDto<SignUpResponseDto>> registerStudent(
            @Valid StudentSignUpRequestDto requestDto) {

        if (userRepo.findByEmail(requestDto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponseDto<>(false,
                            MessagesEnum.EMAIL_ALREADY_EXIST.getMessage(),
                            null,
                            LocalDateTime.now()));
        }

        List<String> uniqueRoles = requestDto.getRole().stream().distinct().toList();
        List<RoleResponseDto> roles = roleClient.getRolesByNames(uniqueRoles).getData();

        if (roles == null || roles.isEmpty()) {
            throw new RoleNotFoundException(ErrorCodeEnum.S_404.getMessage());
        }

        User user = new User();
        user.setUserId(requestDto.getStudentId());
        user.setUsername(requestDto.getUsername());
        user.setEmail(requestDto.getEmail());
        user.setContact(requestDto.getContact());
        user.setDepartment(requestDto.getDepartment());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        Student student = new Student();
        student.setStudentId(requestDto.getStudentId());
        student.setCollegeRoll(requestDto.getCollegeRoll());
        student.setDepartment(requestDto.getDepartment());
        student.setSemester(requestDto.getSemester());

        int semester = Integer.parseInt(requestDto.getSemester());

        student.setAdmissionYear(
                String.valueOf(studentDetails.calculateAdmissionYear(semester))
        );

        student.setAcademicYear(
                studentDetails.calculateAcademicYear(semester)
        );

        student.setUser(user);
        user.setStudent(student);

        List<UserRole> userRoles = new ArrayList<>();

        for (RoleResponseDto role : roles) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRoleId(role.getId());
            userRoles.add(userRole);
        }

        user.setUserRoles(userRoles);

        // ✅ SAVE
        User savedUser = userRepo.save(user);

        // 🔥 REDIS INCREMENT (ADD THIS)
        try {
            String redisKey = student.getDepartment() + ":" +
                    student.getAcademicYear() + ":" +
                    student.getSemester();


            Long val = redisTemplate.opsForHash().increment("student:count", redisKey, 1);

            if (val != null && val < 0) {
                redisTemplate.opsForHash().put("student:count", redisKey, 1);
            }

        } catch (Exception e) {
            // optional: log error, don't break flow
            System.out.println("Redis update failed: " + e.getMessage());
        }

        return ResponseEntity.ok(
                new ApiResponseDto<>(true,
                        "STUDENT REGISTER SUCCESSFUL",
                        new SignUpResponseDto(savedUser.getId()),
                        LocalDateTime.now())
        );
    }

//    @Transactional(rollbackFor = Exception.class)
//    public ResponseEntity<ApiResponseDto<SignUpResponseDto>> createAccount(BaseSignUpRequestDto requestDto, Authentication auth) {
//
//
//        //GET THE USER
//        Optional<User> userOptional = userRepo.findByEmail(requestDto.getEmail());
//
//        //CHECKING DB FOR UNIQUE EMAIL IN USER DB
//        if (userOptional.isPresent()) {
//
//            //BUILD RESPONSE
//            ApiResponseDto<SignUpResponseDto> resonseDto= ApiResponseDto.<SignUpResponseDto>builder()
//                    .success(false)
//                    .message(MessagesEnum.EMAIL_ALREADY_EXIST.getMessage())
//                    .data(new SignUpResponseDto(userOptional.get().getId()))
//                    .timeStamp(LocalDateTime.now())
//                    .build();
//
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(resonseDto);
//        }
//
//        //FETCH ROLE FROM DB
////            Role role = roleRepo.findByRole(requestDto.getRole()).
////                    orElseThrow(() ->
////                            new RoleNotFoundException(ErrorCodeEnum.S_404.getMessage()));
//
////            ApiResponseDto<RoleResponseDto> roleResponse=roleClient.getRoleByName(requestDto.getRole());
//
//        // ✅ REMOVE DUPLICATES
//        List<String> uniqueRoles = requestDto.getRole().stream()
//                .distinct()
//                .toList();
//
//        // ✅ FEIGN CALL
//        ApiResponseDto<List<RoleResponseDto>> roleResponse;
//        try {
//            roleResponse = roleClient.getRolesByNames(uniqueRoles);
//        } catch (Exception e) {
//            throw new RuntimeException("Role service unavailable");
//        }
//
//
//        List<RoleResponseDto> roles=roleResponse.getData();
//
//        if(roles==null || roles.isEmpty()){
//            throw new RoleNotFoundException(ErrorCodeEnum.S_404.getMessage());
//        }
//
//        // SAVE THE USER
//        User user = new User();
//        user.setStudentId(requestDto.getStudentId());
//        user.setUsername(requestDto.getUsername());
//        user.setCollegeRoll(requestDto.getCollegeRoll());
//        user.setDepartment(requestDto.getDepartment());
//        user.setEmail(requestDto.getEmail());
//        user.setContact(requestDto.getContact());
//        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
////            user.setRole(role.getId());
//
//        User savedUser = userRepo.save(user);  //Ensure UUID generated
//
//        List<UserRole> userRoles= roles.stream()
//                .map(role-> new UserRole(savedUser,role.getId()))
//                .toList();
//
//        userRoleRepo.saveAll(userRoles);
//
//        //BUILD RESPONSE
//        ApiResponseDto<SignUpResponseDto> resonseDto= ApiResponseDto.<SignUpResponseDto>builder()
//                .success(true)
//                .message(MessagesEnum.USER_REGISTERED_SUCCESSFUL.getMessage())
//                .data(new SignUpResponseDto( user.getId()))
//                .timeStamp(LocalDateTime.now())
//                .build();
//
//        return ResponseEntity.ok(resonseDto);
//    }

    @Transactional
    public ResponseEntity<ApiResponseDto<String>> sentVerifyEmail(String email){

        try {

            if (pendingEmailRepo.existsById(email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponseDto<>(false,MessagesEnum.EMAIL_PENDING_VERIFICATION.getMessage(), null,LocalDateTime.now()));
            }

            //GENERATE TOKEN
            String token = UUID.randomUUID().toString();

            //EMAIL SEND
            boolean status = emailService.sendVerificationEmailLink(email, token);

            if (status) {

                PendingEmail emailVerification = PendingEmail.builder()
                        .email(email)
                        .pendingEmail(null)
                        .emailVerificationToken(token)
                        .tokenExpiry(LocalDateTime.now().plusMinutes(15))
                        .build();

                pendingEmailRepo.save(emailVerification);

                return ResponseEntity.ok(new ApiResponseDto<>(true,MessagesEnum.EMAIL_VERIFICATION_SEND.getMessage(), null,LocalDateTime.now()));
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(false,MessagesEnum.FAILED_TO_SEND_EMAIL_VERIFICATION.getMessage(), null,LocalDateTime.now()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(false,MessagesEnum.FAILED_TO_SEND_EMAIL_VERIFICATION.getMessage(), null,LocalDateTime.now()));
        }
    }

    @Transactional
    public ResponseEntity<ApiResponseDto<VerificationResponseDto>> verifyEmail(String token){

        try {
            Optional<PendingEmail> optional = pendingEmailRepo.findByEmailVerificationToken(token);

            if (optional.isEmpty()) {

                VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                        .type("EMAIL")
                        .verified(false)
                        .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(false,MessagesEnum.INVALID_OR_EXPIRED_TOKEN.getMessage(), responseVerificationDto,LocalDateTime.now()));
            }

            PendingEmail verification = optional.get();

            if (verification.getTokenExpiry().isBefore(LocalDateTime.now())) {

                VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                        .type("EMAIL")
                        .verified(false)
                        .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(false,MessagesEnum.INVALID_OR_EXPIRED_TOKEN.getMessage(), responseVerificationDto,LocalDateTime.now()));
            }

            if (verification.getEmailVerificationToken().equals(token)) {
                pendingEmailRepo.deleteById(verification.getEmail());

                VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                        .type("EMAIL")
                        .verified(true)
                        .build();

                return ResponseEntity.status(HttpStatus.FOUND).body(new ApiResponseDto<>(true,MessagesEnum.EMAIL_VERIFIED.getMessage(), responseVerificationDto,LocalDateTime.now() ));

            } else {

                VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                        .type("EMAIL")
                        .verified(false)
                        .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(false,MessagesEnum.INVALID_OR_EXPIRED_TOKEN.getMessage(), responseVerificationDto,LocalDateTime.now()));
            }
        }catch (Exception e){
            e.printStackTrace();

            VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                    .type("EMAIL")
                    .verified(false)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(false,MessagesEnum.INVALID_OR_EXPIRED_TOKEN.getMessage(), responseVerificationDto,LocalDateTime.now()));
        }

    }

    public ResponseEntity<ApiResponseDto<String>> sendOtpToContact(String contact){
        boolean sent = otpService.sendOtp(contact);

        if(sent){
            return ResponseEntity.ok(new ApiResponseDto<>(true,MessagesEnum.OTP_SENT_SUCCESSFUL.getMessage(), null,LocalDateTime.now()));
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDto<>(false,MessagesEnum.FAILED_TO_SEND_OTP.getMessage(), null,LocalDateTime.now()));
        }
    }

    public ResponseEntity<ApiResponseDto<VerificationResponseDto>> verifyContactOtp(String contact, String otp){
        boolean verified= otpService.verifyOtp(contact,otp);

        Map<String, String> response = new HashMap<>();

        if(verified){

            VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                    .type("CONTACT")
                    .verified(true)
                    .build();
            return ResponseEntity.status(HttpStatus.FOUND).body(new ApiResponseDto<>(true,MessagesEnum.OTP_VERIFIED.getMessage(), responseVerificationDto,LocalDateTime.now()));

        }else {

            VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                    .type("CONTACT")
                    .verified(false)
                    .build();

            return ResponseEntity.status(HttpStatus.FOUND).body(new ApiResponseDto<>(false,MessagesEnum.INVALID_OR_EXPIRED_OTP.getMessage(), responseVerificationDto,LocalDateTime.now()));
        }
    }

    //✅
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> loginUser(LoginRequestDto requestDto) {

            //TOKEN CREATION: YOU WRAP THE RAW CREDENTIALS (HERE: EMAIL & PASSWORD FROM YOUR DTO) INTO A UsernamePasswordAuthenticationToken
            //AuthenticationManager : YOU HAND THAT TOKEN TO YOUR AuthenticationManager (TYPICALLY A ProviderManager), THE MANAGER ITERATES THROUGH ITS CONFIGURED AuthenticationProviders (e.g. A DaoAuthenticationProvider FOR DATABASE-BACKENED USERS)
            //Throws: AuthenticationException -> if authentication fails
            //CREDENTIAL VERIFICATION: THE CHOSEN PROVIDER LOADS THE USER’S DETAILS VIA YOUR UserDetailsService (E.G. BY EMAIL). COMPARES THE PRESENTED PASSWORD (FROM THE TOKEN) AGAINST THE STORED, ENCODED PASSWORD (VIA YOUR PASSWORDENCODER). CHECKS FOR ACCOUNT STATUS (LOCKED, EXPIRED, ETC.).
            //AUTHENTICATED TOKEN RETURNED: IF ALL CHECKS PASS, THE PROVIDER BUILDS A NEW, AUTHENTICATED USERNAMEPASSWORDAUTHENTICATIONTOKEN (NOW CONTAINING THE USERDETAILS AS ITS PRINCIPAL AND THE USER’S GRANTED AUTHORITIES). THAT GETS RETURNED FROM AUTHENTICATE(...)
            //SECURITYCONTEXT UPDATE (OFTEN IMPLICIT): IN A TYPICAL FILTER CHAIN, SPRING SECURITY WILL TAKE THAT RETURNED AUTHENTICATION AND STORE IT IN THE SECURITYCONTEXTHOLDER, MAKING IT AVAILABLE THROUGHOUT THE REQUEST (AND FOR FUTURE AUTHORIZATION CHECKS)
        Authentication authentication;

        try {
            authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getEmail(),
                            requestDto.getPassword()
                    )
            );
        } catch (Exception e) {

            ApiResponseDto<LoginResponseDto> responseDto = ApiResponseDto.<LoginResponseDto>builder()
                    .success(false)
                    .message(MessagesEnum.FAILED_TO_LOGIN.getMessage())
                    .data(null)
                    .timeStamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
        }

        // ✅ Extract authenticated user
        MyUserDetails user = (MyUserDetails) authentication.getPrincipal();

        List<String> roles = user.getRoles();
        List<String> permissions = user.getPermissions();

        // ✅ Safety checks (should not fail if service layer is correct)
        if (roles == null || roles.isEmpty()) {
            throw new RuntimeException("User has no roles");
        }

        if (permissions == null || permissions.isEmpty()) {
            throw new RuntimeException("User has no permissions");
        }

        // ✅ Generate JWT with full claims
        String token = jwtService.generateToken(
                user.getUsername(),
                roles
        );

        // ✅ Response
        ApiResponseDto<LoginResponseDto> responseDto = ApiResponseDto.<LoginResponseDto>builder()
                .success(true)
                .message(MessagesEnum.LOGIN_SUCCESSFUL.getMessage())
                .data(new LoginResponseDto(token, roles)) // return all roles
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(responseDto);

    }

    public ResponseEntity<ApiResponseDto<ProfileResponseDto>> profileUser(Authentication auth) {

        // ❌ UNAUTHENTICATED USER
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto<>(
                            false,
                            MessagesEnum.UNAUTHORISED_USER.getMessage(),
                            null,
                            LocalDateTime.now()
                    ));
        }

        // ✅ AUTHENTICATED USER
        MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

        List<String> roles = userDetails.getRoles();

        // 🔥 FETCH USER FROM DB (IMPORTANT for relational mapping)
        User user = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("USER NOT FOUND"));

        // 🔥 FETCH STUDENT (via relationship)
        Student student = user.getStudent();

        // ✅ BUILD STUDENT PROFILE (only if exists)
        StudentProfileDto studentProfileDto = null;

        if (student != null) {
            studentProfileDto = StudentProfileDto.builder()
                    .studentId(student.getStudentId())
                    .collegeRoll(student.getCollegeRoll())
                    .admissionYear(student.getAdmissionYear())
                    .academicYear(student.getAcademicYear())
                    .semester(student.getSemester())
                    .build();
        }

        // ✅ BUILD PROFILE RESPONSE
        ProfileResponseDto profileResponseDto = ProfileResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .department(user.getDepartment())
                .email(user.getEmail())
                .contact(user.getContact())
                .role(roles)
                .studentProfile(studentProfileDto) // 🔥 important
                .build();

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        true,
                        MessagesEnum.USER_PROFILE.getMessage(),
                        profileResponseDto,
                        LocalDateTime.now()
                )
        );
    }

    public ResponseEntity<ApiResponseDto<UpdateUserResponseDto>> updateUser(UpdateUserRequestDto updateRequestDto, Authentication authentication) {

        boolean isContactChang=false;
        boolean isEmailChang=false;

        //VERIFY AUTHENTICATION OBJ
        if(authentication==null || !authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponseDto<>(false,MessagesEnum.UNAUTHORISED_USER.getMessage(), null,LocalDateTime.now()));
        }

        MyUserDetails userDetails=(MyUserDetails) authentication.getPrincipal();

        User user=userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(()->new UserNotFoundException( ErrorCodeEnum.S_404.getMessage()));

        // Update only if new values are provided (non-null)
        if (updateRequestDto.getUserId() != null && !updateRequestDto.getUserId().isBlank()) {
            user.setUserId(updateRequestDto.getUserId());
        }

        // Update only if new values are provided (non-null)
        if (updateRequestDto.getUsername() != null && !updateRequestDto.getUsername().isBlank()) {
            user.setUsername(updateRequestDto.getUsername());
        }

        if(updateRequestDto.getEmail()!=null && !updateRequestDto.getEmail().isBlank() && !updateRequestDto.getEmail().equals(userDetails.getUsername())){
            user.setEmail(updateRequestDto.getEmail());

            isEmailChang=true;
        }

        if (updateRequestDto.getContact() != null && !updateRequestDto.getContact().isBlank()) {
            user.setContact(updateRequestDto.getContact());

            isContactChang=true;
        }

        // Save only after conditional updates
        userRepo.save(user);

        if(isEmailChang && isContactChang){

            UpdateUserResponseDto responseDto=UpdateUserResponseDto.builder()
                    .isEmailChanged(true)
                    .isContactChanged(true)
                    .build();

            return ResponseEntity.ok(new ApiResponseDto<>(true,MessagesEnum.USER_PROFILE_UPDATED_SUCCESSFUL.getMessage(), responseDto,LocalDateTime.now()));
        } else if (isEmailChang) {
            UpdateUserResponseDto responseDto=UpdateUserResponseDto.builder()
                    .isEmailChanged(true)
                    .isContactChanged(false)
                    .build();

            return ResponseEntity.ok(new ApiResponseDto<>(true,MessagesEnum.USER_PROFILE_UPDATED_SUCCESSFUL.getMessage(), responseDto,LocalDateTime.now()));
        }else if(isContactChang){
            UpdateUserResponseDto responseDto=UpdateUserResponseDto.builder()
                    .isEmailChanged(false)
                    .isContactChanged(true)
                    .build();

            return ResponseEntity.ok(new ApiResponseDto<>(true,MessagesEnum.USER_PROFILE_UPDATED_SUCCESSFUL.getMessage(), responseDto,LocalDateTime.now()));
        }else {
            UpdateUserResponseDto responseDto=UpdateUserResponseDto.builder()
                    .isEmailChanged(false)
                    .isContactChanged(false)
                    .build();

            return ResponseEntity.ok(new ApiResponseDto<>(true,MessagesEnum.USER_PROFILE_UPDATED_SUCCESSFUL.getMessage(), responseDto,LocalDateTime.now()));
        }
    }

    @Transactional
    public ResponseEntity<ApiResponseDto<String>> updateStudent(
            UpdateStudentRequestDto dto,
            Authentication authentication) {

        // ✅ Authentication check
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto<>(false, "UNAUTHORIZED", null, LocalDateTime.now()));
        }

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
//
//        boolean isAdmin = userDetails.getRoles().contains("ADMIN");
//        boolean isStudent = userDetails.getRoles().contains("STUDENT");
//        boolean hasManageUserPermission= userDetails.getPermissions().contains("MANAGE_USER");

//        // ❌ No valid role
//        if (!isAdmin && !isStudent && !hasManageUserPermission) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(new ApiResponseDto<>(false, "NO PERMISSION", null, LocalDateTime.now()));
//        }

        // ✅ Logged-in user
        User loggedInUser = userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("USER NOT FOUND"));

        Student studentToUpdate= studentRepo.findByStudentId(dto.getOldStudentId().trim())
                    .orElseThrow(() -> new UserNotFoundException("STUDENT NOT FOUND"));

        User userToUpdate= userRepo.findByStudent(studentToUpdate)
                   .orElseThrow(() -> new UserNotFoundException("USER NOT FOUND"));

//        // ================================
//        // 🔹 ADMIN FLOW (update anyone)
//        // ================================
//        if (isAdmin || hasManageUserPermission) {
//
//            if (dto.getOldStudentId() == null || dto.getOldStudentId().isBlank()) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body(new ApiResponseDto<>(false,
//                                "STUDENT ID REQUIRED FOR ADMIN UPDATE",
//                                null,
//                                LocalDateTime.now()));
//            }
//
//            studentToUpdate = studentRepo.findByStudentId(dto.getOldStudentId().trim())
//                    .orElseThrow(() -> new UserNotFoundException("STUDENT NOT FOUND"));
//
//            userToUpdate = userRepo.findByStudent(studentToUpdate)
//                    .orElseThrow(() -> new UserNotFoundException("USER NOT FOUND"));
//        }
//
//        // ================================
//        // 🔹 STUDENT FLOW (self update only)
//        // ================================
//        else {
//
//            studentToUpdate = loggedInUser.getStudent();
//            userToUpdate = loggedInUser;
//
//            // ❌ Prevent student from changing identity
//            if (dto.getStudentId() != null &&
//                    !dto.getStudentId().trim().equalsIgnoreCase(studentToUpdate.getStudentId())) {
//
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(new ApiResponseDto<>(false,
//                                "YOU CANNOT CHANGE OTHER STUDENT PROFILE",
//                                null,
//                                LocalDateTime.now()));
//            }
//        }

        // ================================
        // 🔥 STORE OLD VALUES (Redis use)
        // ================================
        String oldDept = studentToUpdate.getDepartment();
        String oldYear = studentToUpdate.getAcademicYear();
        String oldSem = studentToUpdate.getSemester();

        boolean countRelevantChanged = false;
//        boolean otherChanged = false;

        // ================================
        // ✅ FIELD UPDATES
        // ================================

        if (dto.getDepartment() != null &&
                !dto.getDepartment().equals(studentToUpdate.getDepartment())) {

            studentToUpdate.setDepartment(dto.getDepartment());
            userToUpdate.setDepartment(dto.getDepartment());
            countRelevantChanged = true;
        }

        if (dto.getCollegeRoll() != null &&
                !dto.getCollegeRoll().trim().equalsIgnoreCase(studentToUpdate.getCollegeRoll())) {

            studentToUpdate.setCollegeRoll(dto.getCollegeRoll().trim());
            countRelevantChanged = true;
        }

        // 🔹 Only admin can change studentId
        if (dto.getStudentId() != null &&
                !dto.getStudentId().trim().equalsIgnoreCase(studentToUpdate.getStudentId())) {

            studentToUpdate.setStudentId(dto.getStudentId().trim());
            countRelevantChanged = true;
        }

        // ❌ Nothing changed
        if (!countRelevantChanged ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(true, "NOTHING TO UPDATE", null, LocalDateTime.now()));
        }

        // ================================
        // ✅ SAVE (IMPORTANT FIX)
        // ================================
        userRepo.save(userToUpdate);

        // ================================
        // 🔥 REDIS COUNT UPDATE
        // ================================
        String newKey = studentToUpdate.getDepartment() + ":" +
                studentToUpdate.getAcademicYear() + ":" +
                studentToUpdate.getSemester();

        String oldKey = oldDept + ":" + oldYear + ":" + oldSem;

        if (!oldKey.equals(newKey)) {

            Long val = redisTemplate.opsForHash()
                    .increment("student:count", oldKey, -1);

            if (val != null && val < 0) {
                redisTemplate.opsForHash().put("student:count", oldKey, 0);
            }

            redisTemplate.opsForHash()
                    .increment("student:count", newKey, 1);
        }

        // ================================
        // ✅ RESPONSE
        // ================================
        return ResponseEntity.ok(
                new ApiResponseDto<>(true, "STUDENT UPDATED", null, LocalDateTime.now())
        );
    }

    public ResponseEntity<ApiResponseDto<ChangePasswordResponseDto>> changePassword(String password, String newPassword, Authentication auth) {
        try {
            MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

            if (passwordEncoder.matches(password, userDetails.getPassword())) {

                User user=userRepo.findByEmail(userDetails.getUsername())
                        .orElseThrow(()->new UserNotFoundException(ErrorCodeEnum.S_404.getMessage()));


                user.setPassword(passwordEncoder.encode(newPassword));

                userRepo.save(user);

                return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponseDto<>(true,MessagesEnum.PASSWORD_CHANGE.getMessage(), new ChangePasswordResponseDto(true),LocalDateTime.now()));
            }

            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponseDto<>(false,MessagesEnum.PASSWORD_NOT_CHANGE.getMessage(), new ChangePasswordResponseDto(false),LocalDateTime.now()));
        }catch (Exception e){

            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponseDto<>(false,MessagesEnum.PASSWORD_NOT_CHANGE.getMessage(), new ChangePasswordResponseDto(false),LocalDateTime.now()));
        }
    }

    public ResponseEntity<ApiResponseDto<String>> forgotPasswordEmail(String email) {

        User user=userRepo.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException(ErrorCodeEnum.S_404.getMessage()));

            Random rand = new Random();

        String otp = otpService.generateAndSaveOtp(email);

        emailOtpService.sendOtpForForgotPassword(email, otp);


        return ResponseEntity.ok(new ApiResponseDto<>(true,"OTP SEND TO "+ email, null,LocalDateTime.now()));

    }

    @Transactional
    public ResponseEntity<ApiResponseDto<?>> verifyEmailOTp(String email, String otp){

        User user=userRepo.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException(ErrorCodeEnum.S_404.getMessage()));

        otpService.validateOtp(email, otp);

        String password= PasswordGenerator.generatePassword(10);

        user.setPassword(passwordEncoder.encode(password));

        emailOtpService.sendChangePasswordEmailAsync(email,password);



        userRepo.save(user);

        return ResponseEntity.ok(
                new ApiResponseDto<>(true, "NEW PASSWORD SENT TO EMAIL", null, LocalDateTime.now())
        );

    }

    public ResponseEntity<ApiResponseDto<String>> sendOtpToEmail(
            @Email @Size(max = 50, message = "MAX 50 DIGIT") String email) {

        Optional<User> userOptional = userRepo.findByEmail(email);

        if (userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponseDto<>(
                            false,
                            MessagesEnum.EMAIL_ALREADY_EXIST.getMessage(),
                            userOptional.get().getId().toString(),
                            LocalDateTime.now()
                    ));
        }

        // ✅ Generate OTP
        String otp = otpService.generateAndSaveOtp("OTP:EMAIL_VERIFY:" + email);

        // ✅ Async email (non-blocking)
        emailOtpService.sendOtpForVerification(email, otp);

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        true,
                        MessagesEnum.EMAIL_VERIFICATION_CODE_SEND.getMessage(),
                        null,
                        LocalDateTime.now()
                )
        );
    }

    public ResponseEntity<ApiResponseDto<VerificationResponseDto>> verifyEmailOtp(
            @Email @Size(max = 50, message = "MAX 50 DIGIT") String email,
            @Size(max = 6, message = "MAX 6 CHARACTER") String code) {

        try {
            // ✅ Validate OTP from Redis
            otpService.validateOtp("OTP:EMAIL_VERIFY:" + email, code);

            VerificationResponseDto response = VerificationResponseDto.builder()
                    .type(MessagesEnum.EMAIL_VERIFICATION.getMessage())
                    .verified(true)
                    .build();

            return ResponseEntity.ok(
                    new ApiResponseDto<>(
                            true,
                            MessagesEnum.EMAIL_VERIFICATION_SUCCESSFUL.getMessage(),
                            response,
                            LocalDateTime.now()
                    )
            );

        } catch (Exception e) {

            VerificationResponseDto response = VerificationResponseDto.builder()
                    .type(MessagesEnum.EMAIL_VERIFICATION.getMessage())
                    .verified(false)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(
                            false,
                            MessagesEnum.EMAIL_VERIFICATION_FAIL.getMessage(),
                            response,
                            LocalDateTime.now()
                    ));
        }
    }

    @Transactional
    public ResponseEntity<ApiResponseDto<String>> deleteAllUserByRole(RoleRequestDto requestDto) {

        // ✅ 1. Get Role from Role Service
        ApiResponseDto<RoleResponseDto> responseDto = roleClient.getRoleByName(requestDto.getRole());


        RoleResponseDto role = Optional.ofNullable(responseDto.getData())
                .orElseThrow(() -> new RoleNotFoundException(ErrorCodeEnum.S_404.getMessage()));


        Long roleId = role.getId();

        // ✅ 2. Get User IDs having this role
        List<UUID> userIds = userRoleRepo.findUserIdsByRoleId(roleId);

        if (userIds == null || userIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponseDto<>(
                            true,
                            "NO USERS FOUND BY THAT ROLE",
                            null,
                            LocalDateTime.now()
                    )
            );
        }

//        // ✅ 3. Delete from UserRole (mapping table)
//        userRoleRepo.deleteByRoleId(roleId);

        // ✅ 4. Delete Users
        int deletedUsers = userRepo.deleteAllByIdIn(userIds);

        // ✅ 5. Response
        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        true,
                        MessagesEnum.USER_DELETE_SUCCESSFUL.getMessage(),
                        MessagesEnum.ALL_USER_DELETE_BY_ROLE.format(deletedUsers, role.getRole()),
                        LocalDateTime.now()
                )
        );

    }

    @Transactional
    public ResponseEntity<ApiResponseDto<?>> deleteUserByUsername(UsernameRequestDto requestDto, Authentication auth) {

        if(auth==null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponseDto<>(false,MessagesEnum.UNAUTHORISED_USER.getMessage(), null,LocalDateTime.now()));
        }

        User user=userRepo.findByUsername(requestDto.getUsername()).orElseThrow(()->new UserNotFoundException(ErrorCodeEnum.S_404.getMessage()));

        // 🔥 Step 1: delete all mappings manually
//        userRoleRepo.deleteByUserId(user.getId());

        userRepo.delete(user);

        return ResponseEntity.ok(new ApiResponseDto<>(true,MessagesEnum.USER_DELETE_SUCCESSFUL.getMessage(), null,LocalDateTime.now() ));
    }

    public ResponseEntity<ApiResponseDto<String>> healthCheck() {
        return ResponseEntity.ok(new ApiResponseDto<>(true,MessagesEnum.HEALTHY.getMessage(), MessagesEnum.HEALTHY.getMessage(), LocalDateTime.now()));
    }

    @Transactional
    public ResponseEntity<ApiResponseDto<?>> removeRoleFromUser(AssignOrRemoveRoleFromUserRequestDto requestDto) {

        //FIND THE USER BY USERNAME
        User user = userRepo.findByUsername(requestDto.getUsername())
                .orElseThrow(() -> new UserNotFoundException(ErrorCodeEnum.S_404.getMessage()));

        //TAKE THE LIST OF EXISTING ROLES OF THAT USER
        List<UserRole> userRoles = user.getUserRoles();

        //CALL FEIGN CLIENT TO GET ALL DETAILS ABOUT LIST OF ROLES
        ApiResponseDto<List<RoleResponseDto>> roleResponse= roleClient.getRolesByNames(requestDto.getRoles());

        //CHECK IF ADMIN ROLE IF PRESENT TO DELETE , RESTRICT IT
        boolean isAdminPresent = Optional.ofNullable(roleResponse.getData())
                .orElseThrow(()-> new RoleResponseException(roleResponse.getMessage()))
                .stream()
                .anyMatch(role -> role.getRole().equalsIgnoreCase("ADMIN"));

        if (isAdminPresent) {
            throw new AdminRoleDeletionException(ErrorCodeEnum.S_403.name());
        }

        //USING OPTIONAL TO ASSUME THAT roleResponse.getData() IS NOT NULL , BUT IF NULL THEN THROW EXCEPTION THAT COME FROM ROLE PERMISSION SERVICE IN MEGGASE OF RESPONSE
        //USING STREAM PERORMING SOME PROCESSING TASK TO CONVERT TO LIST OF LONG OF ROLE IDS THAT REMOVE
        List<Long> roleIdsToRemove= Optional.of(roleResponse.getData())
                .orElseThrow(()-> new RoleResponseException(roleResponse.getMessage()))
                .stream()
                .map(RoleResponseDto::getId).toList();

        //CRITICAL CHECK
        if(userRoles.size()<=roleIdsToRemove.size()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponseDto.builder()
                            .success(false)
                            .message("CANNOT BE DELETED ALL ROLES")
                            .data(null)
                            .timeStamp(LocalDateTime.now())
                            .build()
            );
        }

        //CONVERT TO SET FOR FAST LOOK UP O(n)
        Set<Long> rolesIdsToRemoveSet= new HashSet<>(roleIdsToRemove);

        userRoles.removeIf(ur -> rolesIdsToRemoveSet.contains(ur.getRoleId()));

        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .success(true)
                        .message("USER DEASSIGNED FROM ROLE(S) SUCCESSFUL ")
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build()
        );
    }

    public ResponseEntity<ApiResponseDto<?>> assignRolesToUser(AssignOrRemoveRoleFromUserRequestDto requestDto) {

        //FIND THE USER BY USERNAME
        User user = userRepo.findByUsername(requestDto.getUsername())
                .orElseThrow(() -> new UserNotFoundException(ErrorCodeEnum.S_404.getMessage()));

        //TAKE THE LIST OF EXISTING ROLES OF THAT USER
        List<UserRole> userRoles = user.getUserRoles();

        //TAKE THE LIST OF EXISTING ROLES LONG OF THAT USER
        Set<Long> existingRoleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toSet());

        //CALL FEIGN CLIENT TO GET ALL DETAILS ABOUT LIST OF ROLES
        ApiResponseDto<List<RoleResponseDto>> roleResponse= roleClient.getRolesByNames(requestDto.getRoles());

        //CHECK IF ADMIN ROLE IF PRESENT TO DELETE , RESTRICT IT
        boolean isAdminPresent = Optional.ofNullable(roleResponse.getData())
                .orElseThrow(()-> new RoleResponseException(roleResponse.getMessage()))
                .stream()
                .anyMatch(role -> role.getRole().equalsIgnoreCase("ADMIN"));

        if (isAdminPresent) {
            throw new AdminRoleDeletionException(ErrorCodeEnum.S_403.name());
        }

        //USING OPTIONAL TO ASSUME THAT roleResponse.getData() IS NOT NULL , BUT IF NULL THEN THROW EXCEPTION THAT COME FROM ROLE PERMISSION SERVICE IN MEGGASE OF RESPONSE
        //USING STREAM PERORMING SOME PROCESSING TASK TO CONVERT TO LIST OF LONG OF ROLE IDS THAT REMOVE
        List<RoleResponseDto> roles= Optional.of(roleResponse.getData())
                .orElseThrow(()-> new RoleResponseException(roleResponse.getMessage()))
                .stream()
                .filter(role-> !existingRoleIds.contains(role.getId()))
                .toList();

        List<RoleResponseDto> newRoles = roles.stream()
                .filter(role -> !existingRoleIds.contains(role.getId()))
                .toList();

        List<UserRole> newUserRoles = newRoles
                .stream()
                .map(role -> UserRole.builder()
                        .user(user)
                        .roleId(role.getId())
                        .build())
                .toList();

        if(newUserRoles.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseDto.builder()
                            .success(false)
                            .message("NO NEW ROLES ASSIGNED")
                            .data(null)
                            .timeStamp(LocalDateTime.now())
                            .build());
        }

        userRoles.addAll(newUserRoles);
        userRepo.save(user);

        return ResponseEntity.ok(
                ApiResponseDto.builder()
                        .success(true)
                        .message(newRoles.size()+" NEW ROLES ASSIGNED SUCCESSFUL ")
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build());
    }

    public ResponseEntity<ApiResponseDto<Long>> getStudentCount(String department, String academic_year,String semester) {

//        String department= requestDto.getDepartment();
//        String academic_year= requestDto.getAcademicYear();
//        String semester= requestDto.getSemester();

        try {

            Boolean exists = redisTemplate.hasKey("student:count");

            // 🔥 Redis wiped → try rebuild with lock
            if (Boolean.FALSE.equals(exists)) {

                Boolean acquired = redisTemplate.opsForValue()
                        .setIfAbsent("student:count:lock", "1", Duration.ofMinutes(5));

                if (Boolean.TRUE.equals(acquired)) {
                    try {
                        scheduleJobs.preloadStudentCountsScheduled();
                    } finally {
                        redisTemplate.delete("student:count:lock");
                    }
                }
            }

            String key = department + ":" + academic_year + ":" + semester;

            Object value = redisTemplate.opsForHash().get("student:count", key);

            Long result;

            // 🔥 fallback (single key rebuild)
            if (value == null) {

                result = studentRepo.countByDepartmentAndAcademicYearAndSemester(
                        department, academic_year, semester
                );

                redisTemplate.opsForHash().put("student:count", key, result);

            } else {
                result = Long.parseLong(value.toString());
            }

            return ResponseEntity.ok(
                    new ApiResponseDto<>(
                            true,
                            "STUDENT COUNT FETCHED SUCCESSFULLY",
                            result,
                            LocalDateTime.now()
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponseDto<>(
                            false,
                            "FAILED TO FETCH STUDENT COUNT: " ,
                            null,
                            LocalDateTime.now()
                    )
            );
        }
    }


    public ResponseEntity<ApiResponseDto<List<String>>> getAcademicYear(String department, String semester) {

        // ❌ VALIDATION
        if (department == null || department.isBlank() ||
                semester == null || semester.isBlank()) {

            return ResponseEntity.badRequest().body(
                    new ApiResponseDto<>(
                            false,
                            "Department and Semester are required",
                            null,
                            LocalDateTime.now()
                    )
            );
        }

        // 🔥 NORMALIZATION (important for consistency)
        String dept = department.trim().toUpperCase();
        String sem = semester.trim();

        // ✅ FETCH FROM DB
        List<String> academicYears = studentRepo.findAcademicYears(dept, sem);

        // ❌ NO DATA FOUND
        if (academicYears.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponseDto<>(
                            false,
                            "No academic years found",
                            null,
                            LocalDateTime.now()
                    )
            );
        }

        // ✅ SUCCESS
        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        true,
                        "Academic years fetched successfully",
                        academicYears,
                        LocalDateTime.now()
                )
        );
    }

//    public ResponseEntity<ApiResponseDto<List<ProfileResponseDto>>> getAllFaculty() {
//
//    }

    public ResponseEntity<ApiResponseDto<Page<StudentResponseDto>>> getAllStudents(String dept, String year, String sem,Pageable pageable) {
        try {
            // 1. Fetch data
            Page<StudentResponseDto> studentsPage = studentRepo.findByFilter(dept,year,sem,pageable);

            // 2. Handle Empty Case (Optional: Return 204 No Content or just empty Page)
            if (studentsPage.isEmpty()) {

                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                        new ApiResponseDto<>(
                                false,
                                "NO CONTENT BR O",
                                null,
                                LocalDateTime.now()
                        )
                );
            }


            return ResponseEntity.ok().body(
                    new ApiResponseDto<>(
                            true,
                            "SUCCESS FETCHED",
                            studentsPage,
                            LocalDateTime.now()
                    )
            );

        } catch (org.springframework.data.mapping.PropertyReferenceException e) {
            // 3. Handle Invalid Sort Field (e.g., Android sent a field name that doesn't exist)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    new ApiResponseDto<>(
                            false,
                            "INVALID SORT ",
                            null,
                            LocalDateTime.now()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponseDto<>(
                            false,
                            "SERVER PROB lME",
                            null,
                            LocalDateTime.now()
                    )
            );
        }
    }
}
