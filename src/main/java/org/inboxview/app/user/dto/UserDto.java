package org.inboxview.app.user.dto;

public record UserDto(
    String email,
    String username,
    String firstName,
    String lastName,
    String phone,
    boolean isVerified
) {
    
}
