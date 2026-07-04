package com.teoryman.blogmanager.auth;

import com.teoryman.blogmanager.common.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RestAuthEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest req, HttpServletResponse res,
                       AuthenticationException authException) throws IOException {
    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    res.setContentType("application/json");
    ApiResponse<?> body = new ApiResponse<>(null, "Authentication required", LocalDateTime.now());
    res.getWriter().write(objectMapper.writeValueAsString(body));
  }
}