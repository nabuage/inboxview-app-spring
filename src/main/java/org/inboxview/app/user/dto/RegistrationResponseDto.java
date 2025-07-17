package org.inboxview.app.user.dto;

public record RegistrationResponseDto(
    String username,
    String email,
    String firstName,
    String lastName,
    String phone
) {
    
}
