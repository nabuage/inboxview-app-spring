package org.inboxview.app.user.dto;

public record RegistrationResponseDto(
    String id,
    String username,
    String email,
    String firstName,
    String lastName,
    String phone,
    boolean emailVerificationRequired
) {
    
}
