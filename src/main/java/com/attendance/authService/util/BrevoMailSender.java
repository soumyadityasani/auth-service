package com.attendance.authService.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BrevoMailSender {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);
            headers.set("accept", "application/json");

            Map<String, Object> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);

            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", to);

            Map<String, Object> payload = new HashMap<>();
            payload.put("sender", sender);
            payload.put("to", List.of(recipient));
            payload.put("subject", subject);
            payload.put("htmlContent", htmlContent);

            String body = objectMapper.writeValueAsString(payload);  // ✅ proper JSON, handles escaping automatically

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            restTemplate.postForObject("https://api.brevo.com/v3/smtp/email", request, String.class);
            return true;

        } catch (Exception e) {
            System.out.println("Brevo email send failed: " + e.getMessage());
            return false;
        }
    }
}