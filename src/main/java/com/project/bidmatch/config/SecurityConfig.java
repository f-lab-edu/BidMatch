package com.project.bidmatch.config;

import com.project.bidmatch.auth.handler.JsonAuthFailureHandler;
import com.project.bidmatch.auth.handler.JsonAuthSuccessHandler;
import com.project.bidmatch.auth.handler.JsonAuthenticationEntryPoint;
import com.project.bidmatch.auth.handler.JsonLogoutSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JsonAuthSuccessHandler successHandler;
  private final JsonAuthFailureHandler failureHandler;
  private final JsonLogoutSuccessHandler logoutSuccessHandler;
  private final JsonAuthenticationEntryPoint authenticationEntryPoint;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/auth/login",
                "/api/auth/signup",
                "/h2-console/**"
            ).permitAll()
            .anyRequest().authenticated()
        )

        // 로그인 설정
        .formLogin(form -> form
            .loginProcessingUrl("/api/auth/login")
            .usernameParameter("email")
            .passwordParameter("password")
            .successHandler(successHandler)
            .failureHandler(failureHandler)
        )

        // 로그아웃 설정
        .logout(logout -> logout
            .logoutUrl("/api/auth/logout")
            .logoutSuccessHandler(logoutSuccessHandler)
            .invalidateHttpSession(true)
            .deleteCookies("BIDMATCH_SESSION")
        )

        // 예외 처리
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(authenticationEntryPoint))

        // 세션 정책
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false)
        );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}