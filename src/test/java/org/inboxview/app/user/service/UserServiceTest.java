package org.inboxview.app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class UserServiceTest {
    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    User user;
    UserDto userDto;

    @BeforeEach
    public void setup() {        
        user = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();

        userDto = UserDto.builder()
            .email(user.getEmail())
            .username(user.getUsername())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .isVerified(Boolean.FALSE)
            .build();
    }

    @Test
    public void testGetByUsernameReturnsSuccess() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        var result = userService.getByUsername(user.getUsername());

        assertThat(result).isEqualTo(userDto);

        verify(userRepository, times(1)).findByUsername(anyString());
    }

    @Test
    public void testRegisterReturnsUsernameNotFoundException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Exception result = assertThrows(UsernameNotFoundException.class, () -> {
            userService.getByUsername(user.getUsername());
        });

        assertThat(result.getMessage()).isEqualTo("User is not found.");

        verify(userRepository, times(1)).findByUsername(anyString());
    }
}
