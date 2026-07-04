package com.teoryman.blogmanager.auth;

import com.teoryman.blogmanager.common.response.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                  FilterChain chain) throws ServletException, IOException {
    String header = req.getHeader("Authorization");

    if (header == null || !header.startsWith("Bearer ")) {
      chain.doFilter(req, res);
      return;
    }

    String token = header.substring(7);

    try {
      String username = jwtUtil.extractUsername(token);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails user = userDetailsService.loadUserByUsername(username);
        if (jwtUtil.isValid(token, user)) {
          UsernamePasswordAuthenticationToken auth =
                  new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
          auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      }
      chain.doFilter(req, res);
    } catch (ExpiredJwtException e) {
      writeError(res, "Access token has expired");
    } catch (JwtException | IllegalArgumentException e) {
      writeError(res, "Invalid access token");
    }
  }

  private void writeError(HttpServletResponse res, String message) throws IOException {
    SecurityContextHolder.clearContext();
    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    res.setContentType("application/json");
    ApiResponse<?> body = new ApiResponse<>(null, message, LocalDateTime.now());
    res.getWriter().write(objectMapper.writeValueAsString(body));
  }
}