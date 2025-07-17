package org.inboxview.app.user.controller;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getUser(
        final Authentication authentication
    ) {
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(
            userMapper.toDto(userService.getByUsername(authentication.getName()))
        );
    }
}
