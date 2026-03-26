package com.attendance.authService.exceptions;

public class OtpResponseException extends RuntimeException {
    public OtpResponseException(String message) {
        super(message);
    }
}
