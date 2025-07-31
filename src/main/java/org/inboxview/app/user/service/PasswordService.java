package org.inboxview.app.user.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.inboxview.app.error.InvalidRequest;
import org.inboxview.app.user.dto.PasswordResetRequestDto;
import org.inboxview.app.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private static final String SUBJECT = "Password Reset";
    private static final String BODY = "Here's your link to reset your password: %sapi/password/reset?code=%s";
    private static final String SUBJECT_VERIFY_PASSWORD_RESET = "Password Reset Confirmation";
    private static final String BODY_VERIFY_PASSWORD_RESET = "Your password was reset successfully.";
    private static final String PASSWORD_NOT_EQUAL_ERROR = "Password and Password Confirmation must be the same.";
    private static final int MAX_COUNT = 10;
    private static final long MAX_MINNUTES = 10;
    private final UserRepository userRepository;
    private final MessageSenderService messageSenderService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.url}")
    private String url;

    public void emailResetLink(String username) {
        var userE = userRepository.findByUsername(username);

        if (userE.isPresent()) {
            var user = userE.get();
            user.setPasswordResetToken(UUID.randomUUID().toString());
            user.setPasswordResetDateRequested(OffsetDateTime.now());
            user.setPasswordResetCount(0L);

            userRepository.save(user);

            messageSenderService.sendEmail(user.getEmail(), SUBJECT, BODY.formatted(url, user.getPasswordResetToken()));
        }
    }

    public void reset(PasswordResetRequestDto request) {
        if (request.password() != request.passwordConfirmation()) {
            throw new InvalidRequest(PASSWORD_NOT_EQUAL_ERROR);
        }

        var userE = userRepository.findByGuidAndPasswordResetToken(request.username(), request.token());

        if (userE.isPresent()) {
            var user = userE.get();
            
            if (user.getPasswordResetCount() != null &&
                user.getPasswordResetCount() <= MAX_COUNT &&
                user.getPasswordResetDateRequested() != null &&
                user.getPasswordResetDateRequested().plus(Duration.ofMinutes(MAX_MINNUTES)).isAfter(OffsetDateTime.now())
            ) {
                user.setPassword(passwordEncoder.encode(request.password()));
                user.setPasswordDateReset(OffsetDateTime.now());

                userRepository.save(user);

                messageSenderService.sendEmail(user.getEmail(), SUBJECT_VERIFY_PASSWORD_RESET, BODY_VERIFY_PASSWORD_RESET);
            }
        }        
    }
}
