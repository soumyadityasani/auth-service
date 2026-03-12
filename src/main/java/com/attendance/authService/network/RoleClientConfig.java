package com.attendance.authService.network;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class RoleClientConfig {

    @Value("${auth.secret}")
    private  String auth_secret;

    @Bean
    private RequestInterceptor requestInterceptor(){
        return requestTemplate -> {
            requestTemplate.header("X-INTERNAL-SECRET",auth_secret);
        };
    }
}
