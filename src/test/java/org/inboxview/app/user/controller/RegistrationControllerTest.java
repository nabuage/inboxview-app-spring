package org.inboxview.app.user.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.inboxview.app.error.DuplicateException;
import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
// @Import(RegistrationMapper.class)
public class RegistrationControllerTest extends BaseControllerTest {

    @MockitoBean
    private RegistrationService registrationService;

    private User user;
    private String jsonRequest;

    @BeforeEach
    public void setup() {
        user = User.builder()
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .dateAdded(OffsetDateTime.now())
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
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName()
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
            .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(user.getUsername()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(user.getEmail()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(user.getFirstName()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(user.getLastName()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value(user.getPhone()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.isVerified").value(Boolean.FALSE));

        verify(registrationService, times(1)).register(any());
    }

    @Test
    public void testRegisterReturnsDuplicateException() throws Exception {
        doThrow(new DuplicateException("Username already exists")).when(registrationService).register(any());

        mockMvc.perform(
                post("/api/registration/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
            )
            .andExpect(status().is4xxClientError());

        verify(registrationService, times(1)).register(any());
    }
}
