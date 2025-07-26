package org.inboxview.app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.inboxview.app.user.dto.RegistrationRequestDto;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class RegistrationServiceTest {
    @InjectMocks
    RegistrationService registrationService;

    @Mock
    VerificationService verificationService;

    @Mock
    UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    UserMapper userMapper;

    User user;
    UserDto userDto;
    RegistrationRequestDto request;

    @BeforeEach
    public void setup() {
        request = RegistrationRequestDto.builder()
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();

        user = User.builder()
            .id(1L)
            .username(request.username())
            .password(request.password())
            .email(request.email())
            .firstName(request.firstName())
            .lastName(request.lastName())
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
    public void testRegisterReturnsSuccess() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(verificationService).sendEmailVerification(anyLong());
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userMapper.toDto(user)).thenReturn(userDto);

        var result = registrationService.register(request);

        assertThat(result).isEqualTo(userDto);
    }
    
}
