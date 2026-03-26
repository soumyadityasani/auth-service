package com.attendance.authService.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {
        @Autowired
        private JavaMailSender mailSender;

        @Autowired
        private TemplateEngine templateEngine;



    // 🔹 Generic mail sender
    @Async
    private void sendEmailAsync(String to, String subject, String templateName, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);

            String html = templateEngine.process(templateName, context);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            // 🔥 replace with logger later
            System.out.println("Email send failed: " + e.getMessage());
        }
    }

    // ✅ Password Reset Email (Async)
    public void sendChangePasswordEmailAsync(String to, String password) {
        Context context = new Context();
        context.setVariable("password", password);

        sendEmailAsync(
                to,
                "New Password",
                "forgotPasswordEmail",
                context
        );
    }

    // ✅ OTP Email
    public void sendOtpForForgotPassword(String to, String otp) {

        Context context = new Context();
        context.setVariable("otp", otp);

        sendEmailAsync(
                to,
                "Your OTP for Forgot Password",
                "otpEmail",   // separate template
                context
        );
    }

    // ✅ OTP Email
    public void sendOtpForVerification(String to, String otp) {

        Context context = new Context();
        context.setVariable("otp", otp);

        sendEmailAsync(
                to,
                "Verification",
                "verificationEmail",   // separate template
                context
        );
    }
}


