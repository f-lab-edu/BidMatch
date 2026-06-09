package com.project.bidmatch.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.bidmatch.service.CustomUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonAuthSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper; // 직렬화를 위해

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> body = Map.of(
        "message", "로그인 성공",
        "userId", principal.getId(),
        "email", principal.getEmail()
    );

    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
