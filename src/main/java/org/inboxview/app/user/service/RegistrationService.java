package org.inboxview.app.user.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.inboxview.app.error.DuplicateException;
import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final static String USER_EXIST_ERROR = "Username already exists.";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;

    @Transactional
    public User register(RegistrationRequestDto request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateException(
                USER_EXIST_ERROR
            );
        }
        
        User registeredUser = userRepository.save(
                User.builder()
                    .guid(UUID.randomUUID().toString())
                    .username(request.username())
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .dateAdded(OffsetDateTime.now())
                    .build()
            );

        verificationService.sendEmailVerification(registeredUser.getId());
        
        return registeredUser;
    }
    
}
