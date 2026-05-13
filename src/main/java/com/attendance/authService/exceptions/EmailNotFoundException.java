package com.attendance.authService.exceptions;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String errCode) {
        super(errCode);
    }
}
