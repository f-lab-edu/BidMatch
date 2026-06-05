package com.project.bidmatch.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.bidmatch.common.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  // 인증 안 된 사용자가 보호된 자원에 접근했을 때 어떻게 응답할지를 정의함
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    // ErrorResponse 레코드를 재사용해서 일관된 응답 만들기
    ErrorResponse body = ErrorResponse.of(401, "로그인이 필요합니다");
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
