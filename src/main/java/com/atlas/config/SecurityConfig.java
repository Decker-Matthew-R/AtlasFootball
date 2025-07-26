package com.atlas.config;

import com.atlas.config.jwt.JwtAuthenticationFilter;
import com.atlas.config.oauthHandlers.OAuth2AuthenticationFailureHandler;
import com.atlas.config.oauthHandlers.OAuth2AuthenticationSuccessHandler;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    SecurityFilterChain web(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(
                        csrf ->
                                csrf.csrfTokenRepository(
                                                CookieCsrfTokenRepository.withHttpOnlyFalse())
                                        .csrfTokenRequestHandler(
                                                new CsrfTokenRequestAttributeHandler())
                                        .ignoringRequestMatchers(
                                                "/oauth2/**",
                                                "/login/oauth2/code/**",
                                                "/api/save-metric",
                                                "/api/auth/**",
                                                "/api/test/**"))
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(
                        (authorize ->
                                authorize
                                        .requestMatchers(
                                                "/",
                                                "/index.html",
                                                "/static/**",
                                                "/assets/**",
                                                "/favicon.ico",
                                                "/manifest.json",
                                                "/robots.txt",
                                                "/*.js",
                                                "/*.css",
                                                "/error",
                                                "/oauth2/**",
                                                "/api/public/**",
                                                "/api/save-metric",
                                                "/api/fixtures/**")
                                        .permitAll()
                                        .requestMatchers("/api/test/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated()))
                .oauth2Login(
                        oauth2 ->
                                oauth2.successHandler(oAuth2AuthenticationSuccessHandler)
                                        .failureHandler(oAuth2AuthenticationFailureHandler))
                .exceptionHandling(
                        exceptions ->
                                exceptions.authenticationEntryPoint(
                                        (request, response, authException) -> {
                                            if (request.getRequestURI().startsWith("/api/")
                                                    || request.getRequestURI().equals("/api")) {
                                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                            } else {
                                                response.sendRedirect(
                                                        "/oauth2/authorization/google");
                                            }
                                        }));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
