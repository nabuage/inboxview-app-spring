package org.inboxview.app.user.service;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final static String USER_NOT_FOUND_ERROR = "User is not found.";
    private final UserRepository userRepository; 
    private final UserMapper userMapper;   

    public UserDto getByUsername(String username) {
        var user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> 
                new UsernameNotFoundException(USER_NOT_FOUND_ERROR)
            );
        
        return userMapper.toDto(user);
    }
}
