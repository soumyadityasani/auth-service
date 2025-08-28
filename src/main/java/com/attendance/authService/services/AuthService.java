package com.attendance.authService.services;

import com.attendance.authService.dto.*;
import com.attendance.authService.entity.PendingEmail;
import com.attendance.authService.entity.User;
import com.attendance.authService.enums.ErrorCodeEnum;
import com.attendance.authService.enums.MessagesEnum;
import com.attendance.authService.exceptions.EmailSendFailException;
import com.attendance.authService.exceptions.RoleNotFoundException;
import com.attendance.authService.exceptions.UserNotFoundException;
import com.attendance.authService.network.RoleClient;
import com.attendance.authService.repo.PendingEmailRepo;
import com.attendance.authService.repo.UserRepo;
import com.attendance.authService.util.MyUserDetails;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class AuthService {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ForgotPasswordEmailService emailOtpService;

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


    @Value("${frontened.registerUrl}")
    private String frontendUrl;

    public ResponseEntity<ApiResonseDto<SignUpResponseDto>> registerUser(SignUpRequestDto requestDto) {


            //GET THE USER
            Optional<User> userOptional = userRepo.findByEmail(requestDto.getEmail());

            //CHECKING DB FOR UNIQUE EMAIL IN USER DB
            if (userOptional.isPresent()) {

                //BUILD RESPONSE
                ApiResonseDto<SignUpResponseDto> resonseDto=ApiResonseDto.<SignUpResponseDto>builder()
                        .success(false)
                        .message(MessagesEnum.EMAIL_ALREADY_EXIST.getMessage())
                        .data(new SignUpResponseDto(userOptional.get().getId()))
                        .timeStamp(LocalDateTime.now())
                        .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(resonseDto);
            }

            //FETCH ROLE FROM DB
//            Role role = roleRepo.findByRole(requestDto.getRole()).
//                    orElseThrow(() ->
//                            new RoleNotFoundException(ErrorCodeEnum.S_404.getMessage()));

            ApiResonseDto<RoleResponseDto> roleResponse=roleClient.getRoleByName(requestDto.getRole());

            RoleResponseDto role=roleResponse.getData();


            // SAVE THE USER
            User user = new User();
            user.setStudentId(requestDto.getStudentId());
            user.setUsername(requestDto.getUsername());
            user.setCollegeRoll(requestDto.getCollegeRoll());
            user.setDepartment(requestDto.getDepartment());
            user.setEmail(requestDto.getEmail());
            user.setContact(requestDto.getContact());
            user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
            user.setRole(role.getId());
            userRepo.save(user);

        //BUILD RESPONSE
        ApiResonseDto<SignUpResponseDto> resonseDto=ApiResonseDto.<SignUpResponseDto>builder()
                .success(true)
                .message(MessagesEnum.USER_REGISTERED_SUCCESSFUL.getMessage())
                .data(new SignUpResponseDto( user.getId()))
                .timeStamp(LocalDateTime.now())
                .build();

            return ResponseEntity.ok(resonseDto);

    }

    public ResponseEntity<ApiResonseDto<SignUpResponseDto>> createAccount(SignUpRequestDto requestDto,Authentication auth) {

        //UNAUTHENTICATED USER
        if(auth==null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResonseDto<>(false,MessagesEnum.UNAUTHORISED_USER.getMessage(), null,LocalDateTime.now()));
        }

        //GET THE USER
        Optional<User> userOptional = userRepo.findByEmail(requestDto.getEmail());

        //CHECKING DB FOR UNIQUE EMAIL IN USER DB
        if (userOptional.isPresent()) {

            //BUILD RESPONSE
            ApiResonseDto<SignUpResponseDto> resonseDto=ApiResonseDto.<SignUpResponseDto>builder()
                    .success(false)
                    .message(MessagesEnum.EMAIL_ALREADY_EXIST.getMessage())
                    .data(new SignUpResponseDto(userOptional.get().getId()))
                    .timeStamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.CONFLICT).body(resonseDto);
        }

        //FETCH ROLE FROM DB
//            Role role = roleRepo.findByRole(requestDto.getRole()).
//                    orElseThrow(() ->
//                            new RoleNotFoundException(ErrorCodeEnum.S_404.getMessage()));

        ApiResonseDto<RoleResponseDto> roleResponse=roleClient.getRoleByName(requestDto.getRole());

        RoleResponseDto role=roleResponse.getData();


        // SAVE THE USER
        User user = new User();
        user.setStudentId(requestDto.getStudentId());
        user.setUsername(requestDto.getUsername());
        user.setCollegeRoll(requestDto.getCollegeRoll());
        user.setDepartment(requestDto.getDepartment());
        user.setEmail(requestDto.getEmail());
        user.setContact(requestDto.getContact());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setRole(role.getId());
        userRepo.save(user);

        //BUILD RESPONSE
        ApiResonseDto<SignUpResponseDto> resonseDto=ApiResonseDto.<SignUpResponseDto>builder()
                .success(true)
                .message(MessagesEnum.USER_REGISTERED_SUCCESSFUL.getMessage())
                .data(new SignUpResponseDto( user.getId()))
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(resonseDto);

    }

    @Transactional
    public ResponseEntity<ApiResonseDto<String>> sentVerifyEmail(String email){

        try {

            if (pendingEmailRepo.existsById(email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResonseDto<>(false,MessagesEnum.EMAIL_PENDING_VERIFICATION.getMessage(), null,LocalDateTime.now()));
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

                return ResponseEntity.ok(new ApiResonseDto<>(true,MessagesEnum.EMAIL_VERIFICATION_SEND.getMessage(), null,LocalDateTime.now()));
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResonseDto<>(false,MessagesEnum.FAILED_TO_SEND_EMAIL_VERIFICATION.getMessage(), null,LocalDateTime.now()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResonseDto<>(false,MessagesEnum.FAILED_TO_SEND_EMAIL_VERIFICATION.getMessage(), null,LocalDateTime.now()));
        }
    }

    @Transactional
    public ResponseEntity<ApiResonseDto<VerificationResponseDto>> verifyEmail(String token){

        try {
            Optional<PendingEmail> optional = pendingEmailRepo.findByEmailVerificationToken(token);

            if (optional.isEmpty()) {

                VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                        .type("EMAIL")
                        .verified(false)
                        .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResonseDto<>(false,MessagesEnum.INVALID_OR_EXPIRED_TOKEN.getMessage(), responseVerificationDto,LocalDateTime.now()));
            }

            PendingEmail verification = optional.get();

            if (verification.getTokenExpiry().isBefore(LocalDateTime.now())) {

                VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                        .type("EMAIL")
                        .verified(false)
                        .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResonseDto<>(false,MessagesEnum.INVALID_OR_EXPIRED_TOKEN.getMessage(), responseVerificationDto,LocalDateTime.now()));
            }

            if (verification.getEmailVerificationToken().equals(token)) {
                pendingEmailRepo.deleteById(verification.getEmail());

                VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                        .type("EMAIL")
                        .verified(true)
                        .build();

                return ResponseEntity.status(HttpStatus.FOUND).body(new ApiResonseDto<>(true,MessagesEnum.EMAIL_VERIFIED.getMessage(), responseVerificationDto,LocalDateTime.now() ));

            } else {

                VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                        .type("EMAIL")
                        .verified(false)
                        .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResonseDto<>(false,MessagesEnum.INVALID_OR_EXPIRED_TOKEN.getMessage(), responseVerificationDto,LocalDateTime.now()));
            }
        }catch (Exception e){
            e.printStackTrace();

            VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                    .type("EMAIL")
                    .verified(false)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResonseDto<>(false,MessagesEnum.INVALID_OR_EXPIRED_TOKEN.getMessage(), responseVerificationDto,LocalDateTime.now()));
        }

    }

    public ResponseEntity<ApiResonseDto<String>> sendOtpToContact(String contact){
        boolean sent = otpService.sendOtp(contact);

        if(sent){
            return ResponseEntity.ok(new ApiResonseDto<>(true,MessagesEnum.OTP_SENT_SUCCESSFUL.getMessage(), null,LocalDateTime.now()));
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResonseDto<>(false,MessagesEnum.FAILED_TO_SEND_OTP.getMessage(), null,LocalDateTime.now()));
        }
    }

    public ResponseEntity<ApiResonseDto<VerificationResponseDto>> verifyContactOtp(String contact,String otp){
        boolean verified= otpService.verifyOtp(contact,otp);

        Map<String, String> response = new HashMap<>();

        if(verified){

            VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                    .type("CONTACT")
                    .verified(true)
                    .build();
            return ResponseEntity.status(HttpStatus.FOUND).body(new ApiResonseDto<>(true,MessagesEnum.OTP_VERIFIED.getMessage(), responseVerificationDto,LocalDateTime.now()));

        }else {

            VerificationResponseDto responseVerificationDto=VerificationResponseDto.builder()
                    .type("CONTACT")
                    .verified(false)
                    .build();

            return ResponseEntity.status(HttpStatus.FOUND).body(new ApiResonseDto<>(false,MessagesEnum.INVALID_OR_EXPIRED_OTP.getMessage(), responseVerificationDto,LocalDateTime.now()));
        }
    }

    public ResponseEntity<ApiResonseDto<LoginResponseDto>> loginUser(LoginRequestDto requestDto) {

            //TOKEN CREATION: YOU WRAP THE RAW CREDENTIALS (HERE: EMAIL & PASSWORD FROM YOUR DTO) INTO A UsernamePasswordAuthenticationToken
            //AuthenticationManager : YOU HAND THAT TOKEN TO YOUR AuthenticationManager (TYPICALLY A ProviderManager), THE MANAGER ITERATES THROUGH ITS CONFIGURED AuthenticationProviders (e.g. A DaoAuthenticationProvider FOR DATABASE-BACKENED USERS)
            //Throws: AuthenticationException -> if authentication fails
            //CREDENTIAL VERIFICATION: THE CHOSEN PROVIDER LOADS THE USER’S DETAILS VIA YOUR UserDetailsService (E.G. BY EMAIL). COMPARES THE PRESENTED PASSWORD (FROM THE TOKEN) AGAINST THE STORED, ENCODED PASSWORD (VIA YOUR PASSWORDENCODER). CHECKS FOR ACCOUNT STATUS (LOCKED, EXPIRED, ETC.).
            //AUTHENTICATED TOKEN RETURNED: IF ALL CHECKS PASS, THE PROVIDER BUILDS A NEW, AUTHENTICATED USERNAMEPASSWORDAUTHENTICATIONTOKEN (NOW CONTAINING THE USERDETAILS AS ITS PRINCIPAL AND THE USER’S GRANTED AUTHORITIES). THAT GETS RETURNED FROM AUTHENTICATE(...)
            //SECURITYCONTEXT UPDATE (OFTEN IMPLICIT): IN A TYPICAL FILTER CHAIN, SPRING SECURITY WILL TAKE THAT RETURNED AUTHENTICATION AND STORE IT IN THE SECURITYCONTEXTHOLDER, MAKING IT AVAILABLE THROUGHOUT THE REQUEST (AND FOR FUTURE AUTHORIZATION CHECKS)
            Authentication authentication=authManager.authenticate(new UsernamePasswordAuthenticationToken(requestDto.getEmail(),requestDto.getPassword()));

            MyUserDetails user=(MyUserDetails) authentication.getPrincipal();

            ApiResonseDto<RoleResponseDto> roleResponse=roleClient.getRoleById(user.getRoleLong());

            String role = roleResponse.getData().getRole();

            if(!role.equals(requestDto.getRole())){
                ApiResonseDto<LoginResponseDto> resonseDto=ApiResonseDto.<LoginResponseDto>builder()
                        .success(false)
                        .message(MessagesEnum.FAILED_TO_LOGIN.getMessage())
                        .data(null)
                        .timeStamp(LocalDateTime.now())
                        .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resonseDto);

            }

            //GENERATE Jwt TOKEN
            String token= jwtService.generateToken(user.getUsername());

        ApiResonseDto<LoginResponseDto> resonseDto=ApiResonseDto.<LoginResponseDto>builder()
                    .success(true)
                    .message(MessagesEnum.LOGIN_SUCCESSFUL.getMessage())
                    .data(new LoginResponseDto(token,user.getRole()))
                    .timeStamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(resonseDto);

    }

    public ResponseEntity<ApiResonseDto<ProfileResponseDto>> profileUser(Authentication auth){

        //UNAUTHENTICATED USER
        if(auth==null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResonseDto<>(false,MessagesEnum.UNAUTHORISED_USER.getMessage(), null,LocalDateTime.now()));
        }

        MyUserDetails userDetails=(MyUserDetails) auth.getPrincipal();

        //BUILD THE PROFILERESPONSEDTO
        ProfileResponseDto profileResponseDto=ProfileResponseDto.builder()
                .studentId(userDetails.getStudentId())
                .username(userDetails.getFullName())
                .collegeRoll(userDetails.getCollegeRoll())
                .department(userDetails.getDepartment())
                .email(userDetails.getUsername())
                .contact(userDetails.getContact())
                .role(userDetails.getRole())
                .build();

        return ResponseEntity.ok(new ApiResonseDto<>(true,MessagesEnum.USER_PROFILE.getMessage(), profileResponseDto,LocalDateTime.now()));

    }

    public ResponseEntity<ApiResonseDto<UpdateUserResponseDto>> updateUser(UpdateUserRequestDto updateRequestDto, Authentication authentication) {

        boolean isContactChang=false;
        boolean isEmailChang=false;

        //VERIFY AUTHENTICATION OBJ
        if(authentication==null || !authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResonseDto<>(false,MessagesEnum.UNAUTHORISED_USER.getMessage(), null,LocalDateTime.now()));
        }

        MyUserDetails userDetails=(MyUserDetails) authentication.getPrincipal();

        User user=userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(()->new UserNotFoundException( ErrorCodeEnum.S_404.getMessage()));

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

            return ResponseEntity.ok(new ApiResonseDto<>(true,MessagesEnum.USER_PROFILE_UPDATED_SUCCESSFUL.getMessage(), responseDto,LocalDateTime.now()));
        } else if (isEmailChang) {
            UpdateUserResponseDto responseDto=UpdateUserResponseDto.builder()
                    .isEmailChanged(true)
                    .isContactChanged(false)
                    .build();

            return ResponseEntity.ok(new ApiResonseDto<>(true,MessagesEnum.USER_PROFILE_UPDATED_SUCCESSFUL.getMessage(), responseDto,LocalDateTime.now()));
        }else if(isContactChang){
            UpdateUserResponseDto responseDto=UpdateUserResponseDto.builder()
                    .isEmailChanged(false)
                    .isContactChanged(true)
                    .build();

            return ResponseEntity.ok(new ApiResonseDto<>(true,MessagesEnum.USER_PROFILE_UPDATED_SUCCESSFUL.getMessage(), responseDto,LocalDateTime.now()));
        }else {
            UpdateUserResponseDto responseDto=UpdateUserResponseDto.builder()
                    .isEmailChanged(false)
                    .isContactChanged(false)
                    .build();

            return ResponseEntity.ok(new ApiResonseDto<>(true,MessagesEnum.USER_PROFILE_UPDATED_SUCCESSFUL.getMessage(), responseDto,LocalDateTime.now()));
        }
    }

    public ResponseEntity<ApiResonseDto<ChangePasswordResponseDto>> changePassword(String password, String newPassword, Authentication auth) {
        try {
            MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();

            if (passwordEncoder.matches(password, userDetails.getPassword())) {

                User user=userRepo.findByEmail(userDetails.getUsername())
                        .orElseThrow(()->new UserNotFoundException(ErrorCodeEnum.S_404.getMessage()));


                user.setPassword(passwordEncoder.encode(newPassword));

                userRepo.save(user);

                return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResonseDto<>(true,MessagesEnum.PASSWORD_CHANGE.getMessage(), new ChangePasswordResponseDto(true),LocalDateTime.now()));
            }

            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResonseDto<>(false,MessagesEnum.PASSWORD_NOT_CHANGE.getMessage(), new ChangePasswordResponseDto(false),LocalDateTime.now()));
        }catch (Exception e){

            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResonseDto<>(false,MessagesEnum.PASSWORD_NOT_CHANGE.getMessage(), new ChangePasswordResponseDto(false),LocalDateTime.now()));
        }
    }

    public ResponseEntity<ApiResonseDto<String>> forgotPasswordEmail(String email) {

        User user=userRepo.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException(ErrorCodeEnum.S_404.getMessage()));

            Random rand = new Random();
            String password = String.valueOf(rand.nextInt(9000) + 1000);

           boolean status=emailOtpService.sendChangePasswordEmail(email,password);

           if(!status){
               throw  new EmailSendFailException( ErrorCodeEnum.S_400.getMessage());
           }

           user.setPassword(passwordEncoder.encode(password));

           userRepo.save(user);

           return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResonseDto<>(true,MessagesEnum.NEW_PASSWORD_SEND.getMessage(), null,LocalDateTime.now()));

    }

    public ResponseEntity<ApiResonseDto<String>> sendOtpToEmail(@Email @Size(max = 50, message = "MAX 50 DIGIT") String email) {

        Optional<User> userOptional=userRepo.findByEmail(email);

        if(userOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResonseDto<>(false,MessagesEnum.EMAIL_ALREADY_EXIST.getMessage(),userOptional.get().getId().toString(),LocalDateTime.now()));
        }

        Random rand = new Random();
        String emailCode = String.valueOf(rand.nextInt(9000) + 1000);

        boolean status=emailService.sendVerificationEmailCode(email,emailCode);

        if(status){
            pendingEmailRepo.save(new PendingEmail(email,null,emailCode,LocalDateTime.now().plusMinutes(5)));

            ApiResonseDto<String> resonseDto=ApiResonseDto.<String>builder()
                    .success(true)
                    .message(MessagesEnum.EMAIL_VERIFICATION_CODE_SEND.getMessage())
                    .data(null)
                    .timeStamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(resonseDto);
        }

        ApiResonseDto<String> resonseDto=ApiResonseDto.<String>builder()
                .success(false)
                .message(MessagesEnum.FAIL_TO_SEND_EMAIL_VERIFICATION_CODE.getMessage())
                .data(null)
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resonseDto);

    }

    public ResponseEntity<ApiResonseDto<VerificationResponseDto>> verifyEmailOtp(@Email @Size(max = 50, message = "MAX 50 DIGIT") String email, @Size(max=6 ,message = "MAX 6 CHARACTER") String code) {
        boolean verified=emailService.verifyEmailCode(email,code);

        if(verified){
            VerificationResponseDto verificationResponseDto=VerificationResponseDto.builder()
                    .type(MessagesEnum.EMAIL_VERIFICATION.getMessage())
                    .verified(true)
                    .build();


            return ResponseEntity.ok(new ApiResonseDto<>(true,MessagesEnum.EMAIL_VERIFICATION_SUCCESSFUL.getMessage(), verificationResponseDto,LocalDateTime.now()));
        }

        VerificationResponseDto verificationResponseDto=VerificationResponseDto.builder()
                .type(MessagesEnum.EMAIL_VERIFICATION.getMessage())
                .verified(false)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResonseDto<>(false,MessagesEnum.EMAIL_VERIFICATION_FAIL.getMessage(),  verificationResponseDto,LocalDateTime.now()));


    }

    public ResponseEntity<ApiResonseDto<String>> deleteAllUserByRole(RoleRequestDto requestDto) {

        ApiResonseDto<RoleResponseDto> resonseDto=roleClient.getRoleByName(requestDto.getRole());

        RoleResponseDto role=Optional.ofNullable(resonseDto.getData()).orElseThrow(()-> new RoleNotFoundException(ErrorCodeEnum.S_404.getMessage()));

        int deleteUser=userRepo.deleteAllUserByRoleId(role.getId()).orElseThrow(RuntimeException::new);

        return ResponseEntity.ok(new ApiResonseDto<>(true,MessagesEnum.USER_DELETE_SUCCESSFUL.getMessage(), MessagesEnum.ALL_USER_DELETE_BY_ROLE.format(deleteUser,role.getRole()),  LocalDateTime.now()));

    }

    public ResponseEntity<ApiResonseDto<?>> deleteUserByUsername(UsernameRequestDto requestDto) {
        User user=userRepo.findByUsername(requestDto.getUsername()).orElseThrow(()->new UserNotFoundException(ErrorCodeEnum.S_404.getMessage()));

        userRepo.delete(user);

        return ResponseEntity.ok(new ApiResonseDto<>(true,MessagesEnum.USER_DELETE_SUCCESSFUL.getMessage(), null,LocalDateTime.now() ));
    }
}
