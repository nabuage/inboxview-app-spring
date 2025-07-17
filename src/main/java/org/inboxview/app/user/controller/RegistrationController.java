package org.inboxview.app.user.controller;

import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.dto.RegistrationResponseDto;
import org.inboxview.app.user.mapper.RegistrationMapper;
import org.inboxview.app.user.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {
    private final UserService registrationService;
    private final RegistrationMapper registrationMapper;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDto> registerUser(
        @Valid @RequestBody final RegistrationRequestDto request
    ) {
        final var registeredUser = registrationService.register(request);

        return ResponseEntity.ok(registrationMapper.toRegistrationResponse(registeredUser));
    }    
}
