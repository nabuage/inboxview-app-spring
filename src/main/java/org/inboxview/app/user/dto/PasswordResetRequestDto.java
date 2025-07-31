package org.inboxview.app.user.dto;

import lombok.Builder;

@Builder
public record PasswordResetRequestDto(
    String username,
    String password,
    String passwordConfirmation,
    String token
) {
    
}
