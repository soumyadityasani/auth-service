package com.attendance.authService.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BrevoMailSender {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            String escapedHtml = htmlContent.replace("\"", "\\\"").replace("\n", "");

            String body = """
                {
                  "sender": { "name": "%s", "email": "%s" },
                  "to": [{ "email": "%s" }],
                  "subject": "%s",
                  "htmlContent": "%s"
                }
                """.formatted(senderName, senderEmail, to, subject, escapedHtml);

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            restTemplate.postForObject("https://api.brevo.com/v3/smtp/email", request, String.class);
            return true;

        } catch (Exception e) {
            System.out.println("Brevo email send failed: " + e.getMessage());
            return false;
        }
    }
}
