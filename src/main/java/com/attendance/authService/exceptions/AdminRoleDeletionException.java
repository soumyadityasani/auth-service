package com.attendance.authService.exceptions;

public class AdminRoleDeletionException extends RuntimeException{

    public AdminRoleDeletionException(String errorCode){
        super(errorCode);
    }
}
