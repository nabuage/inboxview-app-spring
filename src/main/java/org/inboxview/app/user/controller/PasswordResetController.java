package org.inboxview.app.user.controller;

import org.inboxview.app.user.dto.PasswordResetRequestDto;
import org.inboxview.app.user.service.PasswordService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordService passwordService;

    @PostMapping("/email-reset")
    public ResponseEntity<Void> emailReset(@RequestBody String username) {
        passwordService.emailResetLink(username);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset(@RequestBody PasswordResetRequestDto request) {
        passwordService.reset(request);
        
        return ResponseEntity.noContent().build();
    }
    
}
