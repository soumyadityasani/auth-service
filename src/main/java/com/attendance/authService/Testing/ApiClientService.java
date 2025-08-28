package com.attendance.authService.Testing;

import com.attendance.authService.dto.LoginRequestDto;
import com.attendance.authService.dto.LoginResponseDto;
import com.attendance.authService.dto.SignUpRequestDto;
import com.attendance.authService.dto.UpdateUserRequestDto;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiClientService {


    private final RestTemplate restTemplate;

    private String jwtToken;

    public ApiClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String registerUser(SignUpRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SignUpRequestDto> request = new HttpEntity<>(requestDto, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:8080/api/auth/register",
                    request,
                    String.class
            );

            return response.getStatusCode() == HttpStatus.CREATED
                    ? "Registration successful"
                    : "Registration failed";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        String url = "http://localhost:8080/api/auth/login";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequestDto, headers);

        ResponseEntity<LoginResponseDto> response = restTemplate.postForEntity(
                url, request, LoginResponseDto.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            jwtToken = response.getBody().getToken();  // save token
            return response.getBody();
        } else {
            throw new RuntimeException("Login failed: " + response.getStatusCode());
        }
    }

    public HttpHeaders getAuthorizedHeaders() {
        if (jwtToken == null) {
            throw new IllegalStateException("User not authenticated");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        return headers;
    }

    public ResponseEntity<?> updateUser(UpdateUserRequestDto requestDto, String jwtToken){
        String url = "http://localhost:8080/api/auth/update";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UpdateUserRequestDto> entity = new HttpEntity<>(requestDto, headers);

        return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }


    public UpdateUserRequestDto getCurrentUser(String jwtToken) {
        String url = "http://localhost:8080/api/auth/profile"; // Your GET endpoint for user profile

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UpdateUserRequestDto> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, UpdateUserRequestDto.class);

        return response.getBody();
    }



}
