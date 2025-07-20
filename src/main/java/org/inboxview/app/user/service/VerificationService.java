package org.inboxview.app.user.service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.entity.UserVerification;
import org.inboxview.app.user.repository.UserRepository;
import org.inboxview.app.user.repository.UserVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
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
    private final static String BODY = "Here's your link to verify your email: %sapi/registration/email/verify?id=%s&code=%s";
    private final static int MAX_ATTEMPT_COUNT = 10;
    private final static Long MAX_SECONDS_EXPIRATION = 86400L;
    private final static String INVALID_CODE_ERROR = "Invalid code.";
    private final static String USER_NOT_ERROR = "Not found.";
    private final static String ALREADY_VERIFIED_ERROR = "Email already verified.";

    @Value("${spring.mail.username}")
    private String FROM;

    @Value("${app.url}")
    private String url;

    @Async
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
        verification.setDateAdded(OffsetDateTime.now());

        userVerificationRepository.save(verification);

        return code;
    }

    @Transactional
    public User verifyEmail(String userGuid, String code) {
        return userRepository.findByGuid(userGuid).map(user -> {
                return userVerificationRepository.findByUserId(user.getId())
                    .map(verification -> {
                        OffsetDateTime dateVerified = OffsetDateTime.now();
                        Long secondsBetweenDateAddedAndNow = ChronoUnit.SECONDS.between(dateVerified, verification.getDateAdded());

                        if (verification.getDateVerified() == null &&
                            verification.getAttemptCount() <= MAX_ATTEMPT_COUNT &&
                            verification.getCode().equals(code) &&
                            secondsBetweenDateAddedAndNow <= MAX_SECONDS_EXPIRATION
                        ) {                            

                            verification.setDateVerified(dateVerified);
                            userVerificationRepository.save(verification);

                            user.setDateVerified(dateVerified);
                            user.setDateUpdated(dateVerified);
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

    @Transactional
    public void resendEmailVerification(String userGuid) {
        var user = userRepository.findByGuid(userGuid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_ERROR));

        if (user.getDateVerified() != null) {
            userVerificationRepository.deleteByUserId(user.getId());

            sendEmailVerification(user.getId());
        }
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ALREADY_VERIFIED_ERROR);
        }
    }
}
