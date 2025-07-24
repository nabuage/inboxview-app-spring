package org.inboxview.app.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class UserControllerTest extends BaseControllerTest {
    @MockitoBean
    UserService userService;

    private static final String USERNAME = "username";
    private UserDto user;

    @BeforeEach
    public void setup() {
        user = UserDto.builder()
            .username("username")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();
    }

    @Test
    @WithMockUser(username = USERNAME)
    public void testGetByUsernameReturnsSuccess() throws Exception {
        when(userService.getByUsername(any())).thenReturn(user);

        mockMvc.perform(
            get("/api/user/me")            
        )
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(user.username()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(user.email()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(user.firstName()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(user.lastName()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value(user.phone()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.isVerified").value(Boolean.FALSE));

        verify(userService, times(1)).getByUsername(USERNAME);
    }

    @Test
    public void testGetByUsernameReturnsUnauthorized() throws Exception {
        mockMvc.perform(
            get("/api/user/me")            
        )
        .andExpect(status().isUnauthorized());
        
        verify(userService, times(0)).getByUsername(USERNAME);
    }
}
