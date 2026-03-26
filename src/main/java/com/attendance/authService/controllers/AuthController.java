package com.attendance.authService.controllers;

import com.attendance.authService.dto.*;
import com.attendance.authService.services.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    @PostMapping("/register-faculty")
    public ResponseEntity<ApiResponseDto<SignUpResponseDto>> registerUser(@Valid @RequestBody FacultySignUpRequestDto requestDto){
        return authService.registerUser(requestDto);
    }

    @PostMapping("/register-student")
    public ResponseEntity<ApiResponseDto<SignUpResponseDto>> registerStudent(@Valid @RequestBody StudentSignUpRequestDto requestDto){
        return authService.registerStudent(requestDto);
    }

//    @PreAuthorize("hasAuthority('MANAGE_USER')")
//    @PostMapping("/create-account")
//    public ResponseEntity<ApiResponseDto<SignUpResponseDto>> createAccount(@Valid @RequestBody BaseSignUpRequestDto requestDto, Authentication auth){
//        return authService.createAccount(requestDto,auth);
//    }

    @PreAuthorize("hasAuthority('MANAGE_USER')")
    @PostMapping("/delete-user-by-username")
    public ResponseEntity<ApiResponseDto<?>> deleteUserByUsername(@Valid @RequestBody UsernameRequestDto requestDto, Authentication auth){
        return authService.deleteUserByUsername(requestDto, auth);
    }

    @PreAuthorize("hasAuthority('MANAGE_USER')")
    @PostMapping("/delete-all-user-by-role")
    public ResponseEntity<ApiResponseDto<String>> deleteAllUserByRole(@Valid @RequestBody RoleRequestDto requestDto){
        return authService.deleteAllUserByRole(requestDto);
    }

//    @GetMapping("/verify-email-link")
//    public ResponseEntity<ApiResponseDto<String>> sendEmail(@RequestParam @Email @Size(max = 50, message = "MAX 50 DIGIT") String email) throws IOException {
//        return authService.sentVerifyEmail(email);
//    }
//
//    @PostMapping("/verify-email-link")
//    public ResponseEntity<ApiResponseDto<VerificationResponseDto>> verifyEmail(@RequestParam @Size(max=36 ,message = "MAX 36 CHARACTER") String token){
//        return authService.verifyEmail(token);
//    }

    @GetMapping("/verify-email-code")
    public ResponseEntity<ApiResponseDto<String>> sendEmailCode(@Valid @RequestParam @Email @Size(max = 50, message = "MAX 50 DIGIT") String email){
        return authService.sendOtpToEmail(email);
    }

    @PostMapping("/verify-email-code")
    public ResponseEntity<ApiResponseDto<VerificationResponseDto>> verifyEmailCode(@Valid @RequestParam @Email @Size(max = 50, message = "MAX 50 DIGIT") String email, @RequestParam @Size(max=6 ,message = "MAX 6 CHARACTER") String code){
        return authService.verifyEmailOtp(email,code);
    }

//    @PostMapping("/sent-otp")
//    public ResponseEntity<ApiResponseDto<String>> sentOtp(@RequestParam @Size(max = 10, message = "MAX 10 DIGIT") String contact){
//        return authService.sendOtpToContact(contact);
//    }
//
//    @PostMapping("/verify-otp")
//    public ResponseEntity<ApiResponseDto<VerificationResponseDto>> verifyOtp(@RequestParam @Size(max = 10, message = "MAX 10 DIGIT") String contact, @RequestParam @Size(max=6 ,message = "MAX 6 CHARACTER") String otp){
//        return authService.verifyContactOtp(contact, otp);
//   }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto requestDto){
        return authService.loginUser(requestDto);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDto<ProfileResponseDto>> getCurrentUserProfile(Authentication auth){
        return authService.profileUser(auth);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponseDto<UpdateUserResponseDto>> updateUserProfile(@Valid @RequestBody UpdateUserRequestDto requestDto, Authentication authentication){
        return authService.updateUser(requestDto,authentication);
    }

    @PreAuthorize("hasAuthority('MANAGE_USER')")
    @PutMapping("/update-student")
    public ResponseEntity<ApiResponseDto<String>> updateStudent(@Valid @RequestBody UpdateStudentRequestDto requestDto, Authentication authentication){
        return authService.updateStudent(requestDto,authentication);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponseDto<ChangePasswordResponseDto>> changePassword(@Valid @RequestBody ChangePasswordRequestDto requestDto, Authentication auth){
        return authService.changePassword(requestDto.getPassword(),requestDto.getNewPassword(),auth);
    }

    @PostMapping("/forgot-password-email")
    public ResponseEntity<ApiResponseDto<String>> forgotPassword(@RequestParam @Email @Size(max = 50, message = "MAX 50 DIGIT") String email){
        return authService.forgotPasswordEmail(email);
    }

    @PostMapping("/verify-password-email")
    public ResponseEntity<ApiResponseDto<?>> verifyforgotPassword(@RequestParam @Email @Size(max = 50, message = "MAX 50 DIGIT") String email, @RequestParam @Size(max = 10, message = "MAX 10 DIGIT") String otp){
        return authService.verifyEmailOTp(email,otp);
    }

    @PreAuthorize("hasAuthority('MANAGE_ROLE')")
    @PostMapping("/remove-roles-from-user")
    public ResponseEntity<ApiResponseDto<?>> removeRoleFromUser(@Valid @RequestBody AssignOrRemoveRoleFromUserRequestDto requestDto){
        return authService.removeRoleFromUser(requestDto);
    }

    @PreAuthorize("hasAuthority('MANAGE_ROLE')")
    @PostMapping("/assign-roles-to-user")
    public ResponseEntity<ApiResponseDto<?>> assignRoleToUser(@RequestBody AssignOrRemoveRoleFromUserRequestDto requestDto){
        return authService.assignRolesToUser(requestDto);
    }

    @GetMapping("/get-student-count")
    public ResponseEntity<ApiResponseDto<Long>> getStudentCounts(@RequestBody GetStudentCountDto requestDto){
        return authService.getStudentCount(requestDto);
    }

    @GetMapping("/health-check")
    public ResponseEntity<ApiResponseDto<String>> healthCheck(){
        return authService.healthCheck();
    }

}
