package com.attendance.authService.Testing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/auth")
public class TestController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/verify-email")
    public String verify(@RequestParam String token){
        try {
            ResponseEntity<?> response = restTemplate.postForEntity(
                    "http://localhost:8080/api/auth/verify-email?token="+token,
                    null,
                    String.class
            );

            if(response.getStatusCode() == HttpStatus.FOUND){
                return "Successful";
            }else {
                return "Fai";
            }
        } catch (Exception e) {
            e.getStackTrace();
            return "error";
        }
    }
}
