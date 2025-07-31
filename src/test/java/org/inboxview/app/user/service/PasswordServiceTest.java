package org.inboxview.app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.inboxview.app.error.InvalidRequest;
import org.inboxview.app.user.dto.PasswordResetRequestDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@EnableAutoConfiguration
public class PasswordServiceTest {
    private static final String URL = "http://localhost:8080";
    private static final String PASSWORD_NOT_EQUAL_ERROR = "Password and Password Confirmation must be the same.";
    
    @InjectMocks
    PasswordService passwordService;

    @Mock
    UserRepository userRepository;

    @Mock
    MessageSenderService messageSenderService;

    @Mock
    PasswordEncoder passwordEncoder;

    User user;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(passwordService, "url", URL);

        user = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .dateVerified(OffsetDateTime.now())
            .build();
    }

    @Test
    public void testEmailResetLinkIsSent() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        doNothing().when(messageSenderService).sendEmail(anyString(), anyString(), anyString());

        passwordService.emailResetLink(user.getUsername());

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userRepository, times(1)).save(any());
        verify(messageSenderService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testEmailResetLinkIsNotSent() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        passwordService.emailResetLink(user.getUsername());

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userRepository, never()).save(any());
        verify(messageSenderService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testResetIsSuccess() {
        var request = PasswordResetRequestDto.builder()
            .password("password")
            .passwordConfirmation("password")
            .username("username")
            .token("token")
            .build();
        var userPasswordReset = user = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .dateVerified(OffsetDateTime.now())
            .passwordResetCount(0L)
            .passwordResetDateRequested(OffsetDateTime.now())
            .build();
        var encodedPassword = "encoded-password";

        when(userRepository.findByGuidAndPasswordResetToken(anyString(), anyString())).thenReturn(Optional.of(userPasswordReset));
        when(userRepository.save(any())).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        doNothing().when(messageSenderService).sendEmail(anyString(), anyString(), anyString());

        passwordService.reset(request);

        verify(userRepository, times(1)).findByGuidAndPasswordResetToken(anyString(), anyString());
        verify(userRepository, times(1)).save(any());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(messageSenderService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testResetIsOverPasswordResetCount() {
        var request = PasswordResetRequestDto.builder()
            .password("password")
            .passwordConfirmation("password")
            .username("username")
            .token("token")
            .build();
        var userPasswordReset = user = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .dateVerified(OffsetDateTime.now())
            .passwordResetCount(11L)
            .passwordResetDateRequested(OffsetDateTime.now())
            .build();

        when(userRepository.findByGuidAndPasswordResetToken(anyString(), anyString())).thenReturn(Optional.of(userPasswordReset));

        passwordService.reset(request);

        verify(userRepository, times(1)).findByGuidAndPasswordResetToken(anyString(), anyString());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(messageSenderService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testResetIsOverPasswordResetDateRequested() {
        var request = PasswordResetRequestDto.builder()
            .password("password")
            .passwordConfirmation("password")
            .username("username")
            .token("token")
            .build();
        var userPasswordReset = user = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .dateVerified(OffsetDateTime.now())
            .passwordResetCount(9L)
            .passwordResetDateRequested(OffsetDateTime.now().minus(Duration.ofMinutes(11)))
            .build();

        when(userRepository.findByGuidAndPasswordResetToken(anyString(), anyString())).thenReturn(Optional.of(userPasswordReset));

        passwordService.reset(request);

        verify(userRepository, times(1)).findByGuidAndPasswordResetToken(anyString(), anyString());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(messageSenderService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testResetIsFailure() {
        var request = PasswordResetRequestDto.builder()
            .password("password")
            .passwordConfirmation("password")
            .username("username")
            .token("token")
            .build();

        when(userRepository.findByGuidAndPasswordResetToken(anyString(), anyString())).thenReturn(Optional.empty());
        passwordService.reset(request);

        verify(userRepository, times(1)).findByGuidAndPasswordResetToken(anyString(), anyString());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(messageSenderService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testResetIsPasswordNotEqualPasswordConfirmation() {
        var request = PasswordResetRequestDto.builder()
            .password("password")
            .passwordConfirmation("password1")
            .username("username")
            .token("token")
            .build();

        Exception result = assertThrows(InvalidRequest.class, () -> {
            passwordService.reset(request);
        });

        assertThat(result.getMessage()).isEqualTo(PASSWORD_NOT_EQUAL_ERROR);

        verify(userRepository, never()).findByGuidAndPasswordResetToken(anyString(), anyString());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(messageSenderService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
