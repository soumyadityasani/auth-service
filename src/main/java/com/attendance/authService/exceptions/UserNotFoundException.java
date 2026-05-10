package com.attendance.authService.exceptions;

import lombok.Data;

@Data
public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(String errorCode){
        super(errorCode);
    }

}
