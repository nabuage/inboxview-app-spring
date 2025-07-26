package org.inboxview.app.user.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.inboxview.app.config.JwtService;
import org.inboxview.app.user.dto.AuthenticationRequestDto;
import org.inboxview.app.user.entity.RefreshToken;
import org.inboxview.app.user.entity.User;
import org.inboxview.app.user.repository.RefreshTokenRepository;
import org.inboxview.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class AuthenticationServiceTest {
    private static final String jwtToken = "jwt-token";
    private static final String BAD_CREDENTIALS_EXCEPTION = "Invalid credentials.";

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    JwtService jwtService;

    @Mock
    Authentication authentication;

    @Mock
    UserRepository userRepository;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    User user;
    AuthenticationRequestDto request;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(authenticationService, "ttl", Duration.ofDays(1));

        user = User.builder()
            .id(1L)
            .username("username")
            .password("password")
            .email("email@inboxview.com")
            .firstName("firstname")
            .lastName("lastname")
            .build();

        request = AuthenticationRequestDto.builder()
            .username("username")
            .password("password")
            .build();
    }

    @Test
    public void testAuthenticateReturnsSuccess() {
        String refreshTokenGuid = UUID.randomUUID().toString();
        var authToken = UsernamePasswordAuthenticationToken
            .unauthenticated(request.username(), request.password());

        when(authenticationManager.authenticate(authentication)).thenReturn(authToken);
        when(jwtService.generateToken(request.username())).thenReturn(jwtToken);
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(paramters -> {
                RefreshToken refreshToken = (RefreshToken) paramters.getArgument(0);
                refreshToken.setGuid(refreshTokenGuid);
                return refreshToken;
            }
        );

        var result = authenticationService.authenticate(request);

        assertThat(result.accessToken()).isEqualTo(jwtToken);
        assertThat(result.refreshToken()).isEqualTo(refreshTokenGuid);

        verify(refreshTokenRepository, times(1)).save(any());
    }

    @Test
    public void testAuthenticateReturnsBadCredentialsException() {
        var authToken = UsernamePasswordAuthenticationToken
            .unauthenticated(request.username(), request.password());

        when(authenticationManager.authenticate(authentication)).thenReturn(authToken);
        when(jwtService.generateToken(request.username())).thenReturn(jwtToken);
        when(userRepository.findByUsername(request.username())).thenThrow(new BadCredentialsException(BAD_CREDENTIALS_EXCEPTION));

        Exception result = assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(request);
        });

        assertThat(result.getMessage()).isEqualTo(BAD_CREDENTIALS_EXCEPTION);

        verify(userRepository, times(1)).findByUsername(any());
    }

    @Test
    public void testRefreshTokenReturnsSuccess() {
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(user.getId())
            .guid(UUID.randomUUID().toString())
            .dateAdded(OffsetDateTime.now())
            .expirationDate(OffsetDateTime.now().plus(Duration.ofDays(1)))
            .build();

        when(refreshTokenRepository.findByGuidAndExpirationDateAfter(any(), any())).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(request.username())).thenReturn(jwtToken);
        
        var result = authenticationService.refreshToken(refreshToken.getGuid());

        assertThat(result.accessToken()).isEqualTo(jwtToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken.getGuid());

        verify(jwtService, times(1)).generateToken(any());
    }

    @Test
    public void testRefreshTokenReturnsBadCredentialsExceptionOnExpiredToken() {
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(user.getId())
            .guid(UUID.randomUUID().toString())
            .dateAdded(OffsetDateTime.now())
            .expirationDate(OffsetDateTime.now().plus(Duration.ofDays(1)))
            .build();

        when(refreshTokenRepository.findByGuidAndExpirationDateAfter(any(), any())).thenThrow(new BadCredentialsException(BAD_CREDENTIALS_EXCEPTION));
        
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authenticationService.refreshToken(refreshToken.getGuid());
        });

        assertThat(exception.getMessage()).isEqualTo(BAD_CREDENTIALS_EXCEPTION);

        verify(refreshTokenRepository, times(1)).findByGuidAndExpirationDateAfter(any(), any());
    }

    @Test
    public void testRefreshTokenReturnsBadCredentialsExceptionOnFindById() {
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(user.getId())
            .guid(UUID.randomUUID().toString())
            .dateAdded(OffsetDateTime.now())
            .expirationDate(OffsetDateTime.now().plus(Duration.ofDays(1)))
            .build();

        when(refreshTokenRepository.findByGuidAndExpirationDateAfter(any(), any())).thenReturn(Optional.of(refreshToken));
        when(userRepository.findById(anyLong())).thenThrow(new BadCredentialsException(BAD_CREDENTIALS_EXCEPTION));

        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            authenticationService.refreshToken(refreshToken.getGuid());
        });

        assertThat(exception.getMessage()).isEqualTo(BAD_CREDENTIALS_EXCEPTION);

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testRevokeRefreshToken() {
        String refreshToken = UUID.randomUUID().toString();

        authenticationService.revokeRefreshToken(refreshToken);

        verify(refreshTokenRepository, times(1)).deleteByGuid(refreshToken);
    }
    
}
