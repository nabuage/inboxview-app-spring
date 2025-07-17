package org.inboxview.app.user.service;

import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.mapper.RegistrationMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final static String USER_NOT_FOUND_ERROR = "User not found.";
    private final static String USER_EXIST_ERROR = "Username or email already exists.";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegistrationRequestDto request) {
        if (userRepository.existsByUsername(request.username()) ||
            userRepository.existsByEmail(request.email())) {
                throw new ValidationException(
                    USER_EXIST_ERROR
                );
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        
        return userRepository.save(user);
    }

    public User getByUsername(String username) {
        return userRepository
            .findByUsername(username)
            .orElseThrow(() -> 
                new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ERROR)
            );
    }
}
