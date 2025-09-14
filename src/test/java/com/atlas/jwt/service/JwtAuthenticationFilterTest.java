package com.atlas.jwt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.atlas.config.jwt.JwtAuthenticationFilter;
import com.atlas.config.jwt.JwtExceptions.JwtTokenParsingException;
import com.atlas.config.jwt.JwtTokenProvider;
import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtTokenProvider jwtTokenProvider;

    @Mock private UserService userService;

    @Mock private HttpServletRequest request;

    @Mock private HttpServletResponse response;

    @Mock private FilterChain filterChain;

    @Mock private SecurityContext securityContext;

    @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserEntity testUser;
    private String validJwtToken;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId(123L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        validJwtToken = "eyJhbGciOiJIUzUxMiJ9.validtoken.signature";

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doFilterInternal_shouldAuthenticateUserWithValidJwtCookie() throws Exception {
        Cookie jwtCookie = new Cookie("jwt", validJwtToken);
        Cookie[] cookies = {jwtCookie};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtTokenProvider.validateToken(validJwtToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validJwtToken)).thenReturn(123L);
        when(jwtTokenProvider.getEmailFromToken(validJwtToken)).thenReturn("test@example.com");
        when(userService.findById(123L)).thenReturn(Optional.of(testUser));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenProvider).validateToken(validJwtToken);
        verify(jwtTokenProvider).getUserIdFromToken(validJwtToken);
        verify(jwtTokenProvider).getEmailFromToken(validJwtToken);
        verify(userService).findById(123L);
        verify(securityContext).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);

        verify(securityContext)
                .setAuthentication(
                        argThat(
                                auth -> {
                                    assertThat(auth.getPrincipal()).isEqualTo("test@example.com");
                                    assertThat(auth.getCredentials()).isNull();
                                    assertThat(auth.getAuthorities())
                                            .extracting(GrantedAuthority::getAuthority)
                                            .containsExactly("ROLE_USER");
                                    assertThat(auth.isAuthenticated()).isTrue();

                                    assertThat(auth.getDetails()).isEqualTo(testUser);

                                    return true;
                                }));
    }

    @Test
    void doFilterInternal_shouldNotAuthenticateWhenNoJwtCookie() throws Exception {
        when(request.getCookies()).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenProvider, never()).validateToken(any());
        verify(userService, never()).findById(any());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticateWhenJwtCookieNotPresent() throws Exception {
        Cookie otherCookie = new Cookie("session", "sessionvalue");
        Cookie[] cookies = {otherCookie};

        when(request.getCookies()).thenReturn(cookies);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenProvider, never()).validateToken(any());
        verify(userService, never()).findById(any());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticateWhenJwtInvalid() throws Exception {
        Cookie jwtCookie = new Cookie("jwt", "invalid.jwt.token");
        Cookie[] cookies = {jwtCookie};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtTokenProvider.validateToken("invalid.jwt.token")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenProvider).validateToken("invalid.jwt.token");
        verify(jwtTokenProvider, never()).getUserIdFromToken(any());
        verify(userService, never()).findById(any());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticateWhenUserNotFoundInDatabase() throws Exception {
        Cookie jwtCookie = new Cookie("jwt", validJwtToken);
        Cookie[] cookies = {jwtCookie};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtTokenProvider.validateToken(validJwtToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validJwtToken)).thenReturn(123L);
        when(jwtTokenProvider.getEmailFromToken(validJwtToken)).thenReturn("test@example.com");
        when(userService.findById(123L)).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenProvider).validateToken(validJwtToken);
        verify(jwtTokenProvider).getUserIdFromToken(validJwtToken);
        verify(userService).findById(123L);
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContextOnJwtException() throws Exception {
        Cookie jwtCookie = new Cookie("jwt", validJwtToken);
        Cookie[] cookies = {jwtCookie};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtTokenProvider.validateToken(validJwtToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validJwtToken))
                .thenThrow(new JwtTokenParsingException("Token parsing failed"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenProvider).validateToken(validJwtToken);
        verify(jwtTokenProvider).getUserIdFromToken(validJwtToken);
        verify(userService, never()).findById(any());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldClearContextOnUserServiceException() throws Exception {
        Cookie jwtCookie = new Cookie("jwt", validJwtToken);
        Cookie[] cookies = {jwtCookie};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtTokenProvider.validateToken(validJwtToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validJwtToken)).thenReturn(123L);
        when(jwtTokenProvider.getEmailFromToken(validJwtToken)).thenReturn("test@example.com");
        when(userService.findById(123L)).thenThrow(new RuntimeException("Database error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenProvider).validateToken(validJwtToken);
        verify(userService).findById(123L);
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldHandleMultipleCookiesAndFindJwt() throws Exception {
        Cookie sessionCookie = new Cookie("session", "sessionvalue");
        Cookie jwtCookie = new Cookie("jwt", validJwtToken);
        Cookie csrfCookie = new Cookie("csrf", "csrfvalue");
        Cookie[] cookies = {sessionCookie, jwtCookie, csrfCookie};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtTokenProvider.validateToken(validJwtToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validJwtToken)).thenReturn(123L);
        when(jwtTokenProvider.getEmailFromToken(validJwtToken)).thenReturn("test@example.com");
        when(userService.findById(123L)).thenReturn(Optional.of(testUser));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenProvider).validateToken(validJwtToken);
        verify(userService).findById(123L);
        verify(securityContext).setAuthentication(any(Authentication.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotFilter_shouldReturnTrueForOAuth2Endpoints() throws Exception {
        when(request.getServletPath()).thenReturn("/oauth2/authorization/google");

        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    void shouldNotFilter_shouldReturnTrueForLoginOAuth2Endpoints() throws Exception {
        when(request.getServletPath()).thenReturn("/login/oauth2/code/google");

        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    void shouldNotFilter_shouldReturnTrueForAuthLoginEndpoint() throws Exception {
        when(request.getServletPath()).thenReturn("/api/auth/login");

        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    void shouldNotFilter_shouldReturnTrueForErrorEndpoints() throws Exception {
        when(request.getServletPath()).thenReturn("/error");

        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    void shouldNotFilter_shouldReturnTrueForHealthEndpoints() throws Exception {
        when(request.getServletPath()).thenReturn("/actuator/health");

        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        assertThat(shouldNotFilter).isFalse();
    }

    @Test
    void shouldNotFilter_shouldReturnFalseForProtectedEndpoints() throws Exception {
        when(request.getServletPath()).thenReturn("/api/user/profile");

        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        assertThat(shouldNotFilter).isFalse();
    }

    @Test
    void shouldNotFilter_shouldReturnFalseForApiEndpoints() throws Exception {
        when(request.getServletPath()).thenReturn("/api/some/protected/endpoint");

        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        assertThat(shouldNotFilter).isFalse();
    }

    @Test
    void doFilterInternal_shouldAlwaysContinueFilterChain() throws Exception {
        when(request.getCookies()).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        reset(filterChain);
        Cookie jwtCookie = new Cookie("jwt", validJwtToken);
        when(request.getCookies()).thenReturn(new Cookie[] {jwtCookie});
        when(jwtTokenProvider.validateToken(validJwtToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validJwtToken)).thenReturn(123L);
        when(jwtTokenProvider.getEmailFromToken(validJwtToken)).thenReturn("test@example.com");
        when(userService.findById(123L)).thenReturn(Optional.of(testUser));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }
}
