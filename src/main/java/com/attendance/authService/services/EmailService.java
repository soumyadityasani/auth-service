package com.attendance.authService.services;

import com.attendance.authService.entity.PendingEmail;
import com.attendance.authService.repo.PendingEmailRepo;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Service
public class EmailService {

    @Autowired
    private PendingEmailRepo pendingEmailRepo;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${email.verifyUrl}")
    String emailVerifyUrl;

    public boolean sendVerificationEmailLink(String to, String token) {

        if (!emailVerifyUrl.startsWith("http://") && !emailVerifyUrl.startsWith("https://")) {
            throw new IllegalArgumentException("Invalid frontend URL");
        }

        String separator = emailVerifyUrl.contains("?") ? "&" : "?";
        String verificationUrl = emailVerifyUrl + separator + "token=" + token;

       try {
           MimeMessage message = mailSender.createMimeMessage();
           MimeMessageHelper helper = new MimeMessageHelper(message, true);
           helper.setTo(to);
           helper.setSubject("Email Verification");
           Context context = new Context();
           context.setVariable("verificationUrl", verificationUrl);
           String html = templateEngine.process("verification-email", context);
           helper.setText(html, true);
           mailSender.send(message);

           return true;
       }catch (Exception e){
           e.printStackTrace();

           return false;
       }

    }

    public boolean sendVerificationEmailCode(String to, String code) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Email Verification Code");
            Context context = new Context();
            context.setVariable("code", code);
            String html = templateEngine.process("verification-email", context);
            helper.setText(html, true);
            mailSender.send(message);

            return true;
        }catch (Exception e){
            e.printStackTrace();

            return false;
        }

    }

    public boolean verifyEmailCode(String email, String code){
        PendingEmail pendingEmail=pendingEmailRepo.findById(email)
                .orElse(null);

        if(pendingEmail!=null && pendingEmail.getEmailVerificationToken().equals(code) && pendingEmail.getTokenExpiry().isAfter(LocalDateTime.now())){
            pendingEmailRepo.deleteById(email);

            return true;
        }

        return false;
    }
}
