package org.inboxview.app.user.service;

import org.inboxview.app.user.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final static String USER_NOT_FOUND_ERROR = "Username is not found.";
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
            .findByUsername(username).map(user -> {
                return User.builder()
                    .username(username)
                    .password(user.getPassword())
                    .build();
            }).orElseThrow(() -> new UsernameNotFoundException(
                USER_NOT_FOUND_ERROR
            ));
    }    
}
