package com.attendance.authService.exceptions;

import lombok.Data;

@Data
public class EmailSendFailException extends RuntimeException {

    public EmailSendFailException(String errorCode) {
        super(errorCode);

    }
}
