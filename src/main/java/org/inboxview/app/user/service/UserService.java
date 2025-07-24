package org.inboxview.app.user.service;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.mapper.UserMapper;
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
    private final UserMapper userMapper;   

    public UserDto getByUsername(String username) {
        var user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> 
                new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_ERROR)
            );
        
        return userMapper.toDto(user);
    }
}
