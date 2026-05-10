package com.attendance.authService.enums;

public enum ExceptionEnum {
    USER_NOT_FOUND("USER NOT FOUND"),
    FAIL_TO_SEND_OTP("FAIL TO SENT OTP"),
    ROLE_NOT_FOUND(" ROLE NOT FOUND "),
    VALIDATION_EXCEPTION("VALIDATION EXCEPTION"),
    INVALID_EMAIL_OR_PASSWORD("INVALID EMAIL OR PASSWORD");

    private final String message;

    ExceptionEnum(String message) {
        this.message=message;
    }

    public String getMessage(){
        return message;
    }
}
