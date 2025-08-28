package com.attendance.authService.enums;

public enum ErrorCodeEnum {

    S_401("S_401"),
    S_400("S_400"),
    S_404("S_404");

    private final String message;

    ErrorCodeEnum(String message) {
        this.message=message;
    }

    public String getMessage(){
        return message;
    }
}
