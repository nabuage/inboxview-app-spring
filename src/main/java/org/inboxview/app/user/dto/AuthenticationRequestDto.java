package org.inboxview.app.user.dto;

public record AuthenticationRequestDto(
    String username,
    String password
) {
    
}
