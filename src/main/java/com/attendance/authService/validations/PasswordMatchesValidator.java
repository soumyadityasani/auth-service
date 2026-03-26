package com.attendance.authService.validations;

import com.attendance.authService.dto.BaseSignUpRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, BaseSignUpRequestDto> {
    @Override
    public boolean isValid(BaseSignUpRequestDto signupRequestDto, ConstraintValidatorContext context) {
        if(signupRequestDto.getPassword()==null || signupRequestDto.getConfirmPassword()==null){
            return false;
        }

        boolean valid = signupRequestDto.getPassword().equals(signupRequestDto.getConfirmPassword());

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Passwords do not match")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }

        return valid;
    }
}
