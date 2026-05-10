package com.attendance.authService.exceptions;

import lombok.Data;

@Data
public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String errorCode) {
        super(errorCode);


    }
}
