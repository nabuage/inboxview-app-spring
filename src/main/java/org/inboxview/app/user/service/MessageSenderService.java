package org.inboxview.app.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageSenderService {
    private final JavaMailSender mailSender;

    @Value("${app.from-email}")
    private String FROM;

    public void sendEmail(String email, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(FROM);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
