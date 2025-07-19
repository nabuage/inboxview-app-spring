package org.inboxview.app.user.dto;

public record UserDto(
    String email,
    String usernname,
    String firstName,
    String lastName,
    String phone,
    boolean isVerified
) {
    
}
