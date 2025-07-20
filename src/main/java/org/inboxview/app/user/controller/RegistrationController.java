package org.inboxview.app.user.controller;

import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.dto.VerifyResendRequestDto;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.service.RegistrationService;
import org.inboxview.app.user.service.VerificationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;
    private final VerificationService verificationService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(
        @Valid @RequestBody final RegistrationRequestDto request
    ) {
        final var registeredUser = registrationService.register(request);

        return ResponseEntity.ok(userMapper.toDto(registeredUser));
    }

    @GetMapping("/email/verify")
    public ResponseEntity<UserDto> verifyEmail(
        @RequestParam String id,
        @RequestParam String code
    ) {
        final var verifiedUser = verificationService.verifyEmail(id, code);
        
        return ResponseEntity.ok(userMapper.toDto(verifiedUser));
    }

    @PostMapping("/email/resend-verify")
    public ResponseEntity<Void> resendEmailVerify(@RequestBody VerifyResendRequestDto request) {
        verificationService.resendEmailVerification(request.id());
        
        return ResponseEntity.noContent().build();
    }
}
