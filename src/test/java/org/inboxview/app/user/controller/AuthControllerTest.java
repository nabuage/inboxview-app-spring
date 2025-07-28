package org.inboxview.app.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.inboxview.app.user.dto.AuthenticationRequestDto;
import org.inboxview.app.user.dto.AuthenticationResponseDto;
import org.inboxview.app.user.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest extends BaseControllerTest {
    @MockitoBean
    private AuthenticationService authenticationService;

    private AuthenticationRequestDto request;
    private String jsonRequest;

    @BeforeEach
    public void setup() {
        request = AuthenticationRequestDto.builder()
            .username("username")
            .password("password")
            .build();

        jsonRequest = """
            {
                "username": "%s",
                "password": "%s"
            }
            """.formatted(
                request.username(),
                request.password()
            );
    }

    @Test
    public void testAuthenticateReturnsSuccess() throws Exception {
        AuthenticationResponseDto response = AuthenticationResponseDto.builder()
            .accessToken("token")
            .refreshToken("refreshtoken")
            .build();
        
        when(authenticationService.authenticate(any(AuthenticationRequestDto.class))).thenReturn(response);

        mockMvc.perform(
            post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest)
        )
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value(response.accessToken()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value(response.refreshToken()));

        verify(authenticationService, times(1)).authenticate(any());
    }

    @Test
    public void tesAuthenticateReturnsBadCredentialsException() throws Exception {
        String error = "Login failed.";
        when(authenticationService.authenticate(any())).thenThrow(new BadCredentialsException(error));

        mockMvc.perform(
                post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value(error));

        verify(authenticationService, times(1)).authenticate(any());
    }
    
}
