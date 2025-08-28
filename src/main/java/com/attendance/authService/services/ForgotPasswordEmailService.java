package com.attendance.authService.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class ForgotPasswordEmailService {
        @Autowired
        private JavaMailSender mailSender;

        @Autowired
        private TemplateEngine templateEngine;


        public boolean sendChangePasswordEmail(String to, String password) {

            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setTo(to);
                helper.setSubject("New Password");
                Context context = new Context();
                context.setVariable("password", password);
                String html = templateEngine.process("forgotPasswordEmail", context);
                helper.setText(html, true);
                mailSender.send(message);

                return true;
            }catch (Exception e){
                e.printStackTrace();

                return false;
            }
        }
}


