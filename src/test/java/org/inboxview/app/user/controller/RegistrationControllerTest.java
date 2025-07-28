package org.inboxview.app.user.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.inboxview.app.error.DuplicateException;
import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class RegistrationControllerTest extends BaseControllerTest {
    private final String PASSWORD = "password";

    @MockitoBean
    private RegistrationService registrationService;

    private UserDto user;
    private String jsonRequest;

    @BeforeEach
    public void setup() {
        user = UserDto.builder()
            .username("username")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();

        jsonRequest = """
                {
                    "username": "%s",
                    "password": "%s",
                    "email": "%s",
                    "firstName": "%s",
                    "lastName": "%s"
                }
            """.formatted(
                user.username(),
                PASSWORD,
                user.email(),
                user.firstName(),
                user.lastName()
            );
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        when(registrationService.register(any(RegistrationRequestDto.class))).thenReturn(user);

        mockMvc.perform(
            post("/api/registration/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest)
        )
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(user.username()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(user.email()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(user.firstName()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(user.lastName()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value(user.phone()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.isVerified").value(Boolean.FALSE));

        verify(registrationService, times(1)).register(any());
    }

    @Test
    public void testRegisterReturnsDuplicateException() throws Exception {
        when(registrationService.register(any())).thenThrow(new DuplicateException("Username already exists"));

        mockMvc.perform(
                post("/api/registration/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
            )
            .andExpect(status().is4xxClientError());
    }
}
