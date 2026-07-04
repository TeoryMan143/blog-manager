package com.teoryman.blogmanager.auth;

import com.teoryman.blogmanager.auth.dto.*;
import com.teoryman.blogmanager.auth.refresh.RefreshToken;
import com.teoryman.blogmanager.auth.refresh.RefreshTokenService;
import com.teoryman.blogmanager.common.response.ApiResponse;
import com.teoryman.blogmanager.user.UserService;
import com.teoryman.blogmanager.user.dto.UserRequest;
import com.teoryman.blogmanager.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired private AuthenticationManager authManager;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private RefreshTokenService refreshTokenService;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserService userService;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
    authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
    );

    String accessToken = jwtUtil.generateToken(req.getUsername());
    RefreshToken refreshToken = refreshTokenService.create(req.getUsername());

    return ResponseEntity.ok(new ApiResponse<>(new AuthResponse(accessToken, refreshToken.getToken()), null));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@Valid @RequestBody RefreshRequest req) {
    String requestToken = req.getRefreshToken();

    RefreshToken refreshToken = refreshTokenService.validate(requestToken);
    String newAccessToken = jwtUtil.generateToken(
            refreshToken.getUser().getUsername()
    );

    return ResponseEntity.ok(new ApiResponse<>(new RefreshResponse(newAccessToken), null));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestBody Map<String, String> body) {
    refreshTokenService.deleteByUser(body.get("username"));
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest req) {
    String passwordHash = passwordEncoder.encode(req.getPassword());
    UserRequest userRequest = new UserRequest(req.getUsername(), req.getEmail(), passwordHash);
    UserResponse userRes = userService.createUser(userRequest);

    String accessToken = jwtUtil.generateToken(userRes.getUsername());
    RefreshToken refreshToken = refreshTokenService.create(userRes.getUsername());

    RegisterResponse res = new RegisterResponse(userRes,  accessToken, refreshToken.getToken());
    return ResponseEntity.ok(new ApiResponse<>(res, null));
  }
}