package com.attendance.authService.exceptions;

import com.attendance.authService.dto.ApiResonseDto;
import com.attendance.authService.dto.ExceptionRespone;
import com.attendance.authService.enums.ErrorCodeEnum;
import com.attendance.authService.enums.ExceptionEnum;
import com.attendance.authService.enums.MessagesEnum;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResonseDto<ExceptionRespone>> userNotFound(UserNotFoundException e){
        ApiResonseDto<ExceptionRespone> responseDto = ApiResonseDto.<ExceptionRespone>builder()
                .success(false)
                .message(ExceptionEnum.USER_NOT_FOUND.getMessage())
                .data(new ExceptionRespone(e.getMessage()))
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(responseDto,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailSendFailException.class)
    private ResponseEntity<ApiResonseDto<ExceptionRespone>> emailSendFail(EmailSendFailException e){
        ApiResonseDto<ExceptionRespone> responseDto = ApiResonseDto.<ExceptionRespone>builder()
                .success(false)
                .message(ExceptionEnum.FAIL_TO_SEND_OTP.getMessage())
                .data(new ExceptionRespone(e.getMessage()))
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(responseDto,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiResonseDto<ExceptionRespone>> roleNotFound(RoleNotFoundException e){
        ApiResonseDto<ExceptionRespone> responseDto = ApiResonseDto.<ExceptionRespone>builder()
                .success(false)
                .message(ExceptionEnum.ROLE_NOT_FOUND.getMessage())
                .data(new ExceptionRespone(e.getMessage()))
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(responseDto,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResonseDto<ExceptionRespone>> handleBadCredentials(BadCredentialsException ex) {
        ApiResonseDto<ExceptionRespone> responseDto = ApiResonseDto.<ExceptionRespone>builder()
                .success(false)
                .message(ExceptionEnum.INVALID_EMAIL_OR_PASSWORD.getMessage())
                .data(new ExceptionRespone(ErrorCodeEnum.S_401.getMessage()))
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(responseDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> usernameNotFound(UsernameNotFoundException exception){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MessagesEnum.USER_NOT_FOUND.getMessage()+MessagesEnum.CREDENTIALS_DOESNT_MATCH.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResonseDto<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        });

        ApiResonseDto<Map<String, String>> response = ApiResonseDto.<Map<String, String>>builder()
                .success(false)
                .message(ExceptionEnum.VALIDATION_EXCEPTION.getMessage())
                .data(errors)
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResonseDto<String>> handleConstraintViolation(ConstraintViolationException ex) {
        ApiResonseDto<String> response = ApiResonseDto.<String>builder()
                .success(false)
                .message(ExceptionEnum.VALIDATION_EXCEPTION.getMessage())
                .data(ex.getMessage())
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//
//        ex.getBindingResult().getAllErrors().forEach(error -> {
//            if (error instanceof FieldError fieldError) {
//                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
//            } else {
//                errors.put(error.getObjectName(), error.getDefaultMessage());
//            }
//        });
//
//        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
//    }
//
//
//
//
//
//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
//        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
//    }
}
