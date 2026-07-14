package com.example.discordclone.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendVerificationCode(String to, String code) {
        System.out.println("=========================================");
        System.out.println("SENDER EMAIL VERIFICATION CODE TO: " + to);
        System.out.println("CODE IS: " + code);
        System.out.println("=========================================");

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("no-reply@discordclone.com");
                message.setTo(to);
                message.setSubject("Verification Code - Discord Clone");
                message.setText("Welcome! Your confirmation code: " + code);
                mailSender.send(message);
            } catch (Exception e) {
                System.err.println("Could not send real email. Error: " + e.getMessage());
            }
        }
    }
}