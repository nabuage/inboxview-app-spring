package org.inboxview.app.user.service;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

import org.inboxview.app.user.entity.UserVerification;
import org.inboxview.app.user.repository.UserRepository;
import org.inboxview.app.user.repository.UserVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom;
    private final static String SUBJECT = "Verifcation Code";
    private final static String BODY = "Here is your verification code: %s";

    @Value("${spring.mail.username}")
    private String FROM;

    public void sendEmailVerification(Long userId) {
        String code = generateEmailToken(userId);
        String email = getEmail(userId);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(FROM);
        message.setSubject(SUBJECT);
        message.setText(BODY.formatted(code));

        mailSender.send(message);
    }

    private String generateEmailToken(Long userId) {
        String code = String.valueOf(secureRandom.nextInt(100000, 1000000));

        UserVerification verification = new UserVerification();
        verification.setUserId(userId);
        verification.setCode(String.valueOf(code));
        verification.setAttemptCount(0L);

        userVerificationRepository.save(verification);

        return code;
    }

    private String getEmail(Long userId) {
        return userRepository.findById(userId).get().getEmail();
    }
}
