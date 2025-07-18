package org.inboxview.app.user.service;

import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final static String USER_NOT_FOUND_ERROR = "User not found.";
    private final UserRepository userRepository;    

    public User getByUsername(String username) {
        return userRepository
            .findByUsername(username)
            .orElseThrow(() -> 
                new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ERROR)
            );
    }
}
