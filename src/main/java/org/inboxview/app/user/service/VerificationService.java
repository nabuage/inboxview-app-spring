package org.inboxview.app.user.service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.inboxview.app.error.DuplicateException;
import org.inboxview.app.error.NotFoundException;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.UserVerification;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.inboxview.app.user.repository.UserVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final UserMapper userMapper;
    private final MessageSenderService messageSenderService;
    private static final String SUBJECT = "Email verification";
    private static final String BODY = "Here's your link to verify your email: %sapi/registration/email/verify?id=%s&code=%s";
    private static final int MAX_ATTEMPT_COUNT = 10;
    private static final Long MAX_SECONDS_EXPIRATION = 86400L;
    private static final String INVALID_CODE_ERROR = "Invalid code.";
    private static final String USER_NOT_FOUND_ERROR = "User is not found.";
    private static final String ALREADY_VERIFIED_ERROR = "Email already verified.";

    @Value("${app.from-email}")
    private String FROM;

    @Value("${app.url}")
    private String url;

    @Async
    public void sendEmailVerification(Long userId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ERROR));
        String code = generateEmailToken(userId);        
        var email = user.getEmail();
        var guid = user.getGuid();

        messageSenderService.sendEmail(email, SUBJECT, BODY.formatted(url, guid, code));
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
    public UserDto verifyEmail(String userGuid, String code) {
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

                            return userMapper.toDto(user);
                        }
                        else {
                            verification.setAttemptCount(verification.getAttemptCount() + 1);
                            userVerificationRepository.save(verification);

                            throw new NotFoundException(INVALID_CODE_ERROR);
                        }                        
                    })
                    .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ERROR));
            })
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ERROR));
    }

    @Transactional
    public void resendEmailVerification(String userGuid) {
        var user = userRepository.findByGuid(userGuid)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ERROR));

        if (user.getDateVerified() == null) {
            userVerificationRepository.setDateDeletedByUserId(user.getId(), OffsetDateTime.now());

            sendEmailVerification(user.getId());
        }
        else {
            throw new DuplicateException(ALREADY_VERIFIED_ERROR);
        }
    }
}
