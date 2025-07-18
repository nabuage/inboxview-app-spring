package org.inboxview.app.user.service;

import java.util.UUID;

import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final static String USER_EXIST_ERROR = "Username or email already exists.";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final VerificationService verificationService;

    @Transactional
    public User register(RegistrationRequestDto request) {
        if (userRepository.existsByUsername(request.username()) ||
            userRepository.existsByEmail(request.email())) {
                throw new ValidationException(
                    USER_EXIST_ERROR
                );
        }

        User user = new User();
        user.setGuid(UUID.randomUUID().toString());
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        
        User registeredUser = userRepository.save(user);

        verificationService.sendEmailVerification(registeredUser.getId());
        
        return registeredUser;
    }
    
}
