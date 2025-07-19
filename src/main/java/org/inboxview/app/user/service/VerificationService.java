package org.inboxview.app.user.service;

import java.util.Date;
import java.util.UUID;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.entity.UserVerification;
import org.inboxview.app.user.repository.UserRepository;
import org.inboxview.app.user.repository.UserVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final JavaMailSender mailSender;
    private final static String SUBJECT = "Email verification";
    private final static String BODY = "Here's your link to verify your email: %s/registration/verify?id=%s&code=%s";
    private final static int MAX_ATTEMPT_COUNT = 10;
    private final static String INVALID_CODE_ERROR = "Invalid code.";

    @Value("${spring.mail.username}")
    private String FROM;

    @Value("${app.url}")
    private String url;

    public void sendEmailVerification(Long userId) {
        String code = generateEmailToken(userId);
        var user = userRepository.findById(userId).orElseGet(null);
        var email = user != null ? user.getEmail() : "";
        var guid = user != null ? user.getGuid() : "";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(FROM);
        message.setSubject(SUBJECT);
        message.setText(BODY.formatted(url, guid, code));

        mailSender.send(message);
    }

    private String generateEmailToken(Long userId) {
        String code = UUID.randomUUID().toString();

        UserVerification verification = new UserVerification();
        verification.setUserId(userId);
        verification.setCode(code);
        verification.setAttemptCount(0L);

        userVerificationRepository.save(verification);

        return code;
    }

    @Transactional
    public User verifyEmail(String id, String code) {
        return userRepository.findByGuid(id).map(user -> {
                return userVerificationRepository.findByUserId(user.getId())
                    .map(verification -> {
                        if (verification.getAttemptCount() < MAX_ATTEMPT_COUNT &&
                            verification.getCode().equals(code)
                        ) {
                            Date dateVerified = new Date();
                            verification.setDateVerified(dateVerified);
                            userVerificationRepository.save(verification);
                            user.setDateVerified(dateVerified);
                            userRepository.save(user);
                            return user;
                        }
                        else {
                            verification.setAttemptCount(verification.getAttemptCount() + 1);
                            userVerificationRepository.save(verification);
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_CODE_ERROR);
                        }                        
                    })
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_CODE_ERROR));
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_CODE_ERROR));
    }
}
