package org.inboxview.app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.inboxview.app.error.DuplicateException;
import org.inboxview.app.error.NotFoundException;
import org.inboxview.app.user.dto.UserDto;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.entity.UserVerification;
import org.inboxview.app.user.mapper.UserMapper;
import org.inboxview.app.user.repository.UserRepository;
import org.inboxview.app.user.repository.UserVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.internet.MimeMessage;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class VerificationServiceTest {
    static final String FROM = "george@inboxview.com";
    static final String URL = "http://localhost:8080/";
    static final String SUBJECT = "Email verification";
    static final String BODY = "Here's your link to verify your email: %sapi/registration/email/verify?id=%s&code=%s";
    static final String USER_NOT_FOUND_ERROR = "User is not found.";
    static final String INVALID_CODE_ERROR = "Invalid code.";
    static final String ALREADY_VERIFIED_ERROR = "Email already verified.";

    @InjectMocks
    VerificationService verificationService;

    @Mock
    UserVerificationRepository userVerificationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    JavaMailSender javaMailSender;

    @Spy
    UserMapper userMapper;
    
    User user;
    UserDto userDto;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(verificationService, "FROM", FROM);
        ReflectionTestUtils.setField(verificationService, "url", URL);

        user = User.builder()
            .id(1L)
            .guid(UUID.randomUUID().toString())
            .username("username")
            .password("password")
            .email("george@inboxview.com")
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
    public void testSendEmailVerificationReturnsSuccess() {
        var userId = 1L;
        var userVerification = UserVerification.builder()
            .userId(userId)
            .code(UUID.randomUUID().toString())
            .attemptCount(0L)
            .build();

        when(userVerificationRepository.save(any())).thenReturn(userVerification);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        verificationService.sendEmailVerification(user.getId());

        verify(userVerificationRepository, times(1)).save(any());
        verify(userRepository, times(1)).findById(anyLong());
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendEmailVerificationReturnsNotFoundException() {
        when(userRepository.findById(anyLong())).thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));

        Exception result = assertThrows(NotFoundException.class, () -> {
            verificationService.sendEmailVerification(user.getId());
        });

        assertThat(result.getMessage()).isEqualTo(USER_NOT_FOUND_ERROR);

        verify(userRepository, times(1)).findById(anyLong());
        verify(userVerificationRepository, never()).save(any());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testVerifyEmailReturnsSuccess() {
        var userId = 1L;
        var userVerification = UserVerification.builder()
            .userId(userId)
            .code(UUID.randomUUID().toString())
            .attemptCount(0L)
            .dateAdded(OffsetDateTime.now())
            .build();
        // userDto = UserDto.builder()
        //     .email(user.getEmail())
        //     .username(user.getUsername())
        //     .firstName(user.getFirstName())
        //     .lastName(user.getLastName())
        //     .phone(user.getPhone())
        //     .isVerified(Boolean.TRUE)
        //     .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Optional.of(user));
        when(userVerificationRepository.findByUserId(anyLong())).thenReturn(Optional.of(userVerification));
        when(userVerificationRepository.save(any())).thenReturn(userVerification);
        // when(userMapper.toDto(any())).thenReturn(userDto);
        
        var result = verificationService.verifyEmail(user.getGuid(), userVerification.getCode());

        assertThat(result.isVerified()).isTrue();

        verify(userRepository, times(1)).findByGuid(anyString());
        verify(userVerificationRepository, times(1)).findByUserId(anyLong());
        verify(userVerificationRepository, times(1)).save(any());
        verify(userMapper, times(1)).toDto(any());
    }

    @Test
    public void testVerifyEmailReturnsUserNotFound() {
        var userId = 1L;
        var userVerification = UserVerification.builder()
            .userId(userId)
            .code(UUID.randomUUID().toString())
            .attemptCount(0L)
            .dateAdded(OffsetDateTime.now())
            .build();

        when(userRepository.findByGuid(anyString())).thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));
        
        Exception result = assertThrows(NotFoundException.class, () -> {
            verificationService.verifyEmail(user.getGuid(), userVerification.getCode());
        });

        assertThat(result.getMessage()).isEqualTo(USER_NOT_FOUND_ERROR);

        verify(userRepository, times(1)).findByGuid(anyString());
        verify(userVerificationRepository, never()).findByUserId(anyLong());
        verify(userVerificationRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    public void testVerifyEmailReturnsUserIdNotFound() {
        var userId = 1L;
        var userVerification = UserVerification.builder()
            .userId(userId)
            .code(UUID.randomUUID().toString())
            .attemptCount(0L)
            .dateAdded(OffsetDateTime.now())
            .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Optional.of(user));
        when(userVerificationRepository.findByUserId(anyLong())).thenThrow(new NotFoundException(USER_NOT_FOUND_ERROR));
        
        Exception result = assertThrows(NotFoundException.class, () -> {
            verificationService.verifyEmail(user.getGuid(), userVerification.getCode());
        });

        assertThat(result.getMessage()).isEqualTo(USER_NOT_FOUND_ERROR);

        verify(userRepository, times(1)).findByGuid(anyString());
        verify(userVerificationRepository, times(1)).findByUserId(anyLong());
        verify(userVerificationRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    public void testVerifyEmailReturnsInvalidCodeMoreThanMaxAttemptCount() {
        var userId = 1L;
        var userVerification = UserVerification.builder()
            .userId(userId)
            .code(UUID.randomUUID().toString())
            .attemptCount(11L)
            .dateAdded(OffsetDateTime.now())
            .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Optional.of(user));
        when(userVerificationRepository.findByUserId(anyLong())).thenReturn(Optional.of(userVerification));

        Exception result = assertThrows(NotFoundException.class, () -> {
            verificationService.verifyEmail(user.getGuid(), userVerification.getCode());
        });

        assertThat(result.getMessage()).isEqualTo(INVALID_CODE_ERROR);

        verify(userRepository, times(1)).findByGuid(anyString());
        verify(userVerificationRepository, times(1)).findByUserId(anyLong());
        verify(userVerificationRepository, times(1)).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    public void testVerifyEmailReturnsInvalidCodeMoreThanMaxSecondsExpiration() {
        var userId = 1L;
        var userVerification = UserVerification.builder()
            .userId(userId)
            .code(UUID.randomUUID().toString())
            .attemptCount(0L)
            .dateAdded(OffsetDateTime.now().plusSeconds(86400L + 1000L))
            .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Optional.of(user));
        when(userVerificationRepository.findByUserId(anyLong())).thenReturn(Optional.of(userVerification));

        Exception result = assertThrows(NotFoundException.class, () -> {
            verificationService.verifyEmail(user.getGuid(), userVerification.getCode());
        });

        assertThat(result.getMessage()).isEqualTo(INVALID_CODE_ERROR);

        verify(userRepository, times(1)).findByGuid(anyString());
        verify(userVerificationRepository, times(1)).findByUserId(anyLong());
        verify(userVerificationRepository, times(1)).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    public void testResendEmailVerificationReturnsSuccess() {
        var userVerification = UserVerification.builder()
            .userId(user.getId())
            .code(UUID.randomUUID().toString())
            .attemptCount(0L)
            .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Optional.of(user));
        when(userVerificationRepository.save(any())).thenReturn(userVerification);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        verificationService.resendEmailVerification(user.getGuid());

        verify(userRepository, times(1)).findByGuid(anyString());
        verify(userVerificationRepository, times(1)).save(any());
        verify(userRepository, times(1)).findById(anyLong());
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testResendEmailVerificationReturnsNotFoundException() {
        when(userRepository.findByGuid(anyString())).thenReturn(Optional.empty());

        Exception result = assertThrows(NotFoundException.class, () -> {
            verificationService.resendEmailVerification(user.getGuid());
        });
        
        assertThat(result.getMessage()).isEqualTo(USER_NOT_FOUND_ERROR);

        verify(userRepository, times(1)).findByGuid(anyString());
    }

    @Test
    public void testResendEmailVerificationReturnsDuplicateException() {
        user = User.builder()
            .id(1L)
            .guid(UUID.randomUUID().toString())
            .username("username")
            .password("password")
            .email("george@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .dateVerified(OffsetDateTime.now())
            .build();

        when(userRepository.findByGuid(anyString())).thenReturn(Optional.of(user));

        Exception result = assertThrows(DuplicateException.class, () -> {
            verificationService.resendEmailVerification(user.getGuid());
        });
        
        assertThat(result.getMessage()).isEqualTo(ALREADY_VERIFIED_ERROR);

        verify(userRepository, times(1)).findByGuid(anyString());
    }
}
