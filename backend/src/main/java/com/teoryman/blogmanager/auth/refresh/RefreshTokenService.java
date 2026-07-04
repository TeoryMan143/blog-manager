package com.teoryman.blogmanager.auth.refresh;

import com.teoryman.blogmanager.common.exception.ExpiredTokenException;
import com.teoryman.blogmanager.common.exception.InvalidTokenException;
import com.teoryman.blogmanager.common.exception.ResourceNotFoundException;
import com.teoryman.blogmanager.user.User;
import com.teoryman.blogmanager.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
  @Autowired
  private RefreshTokenRepository repo;
  @Autowired
  private UserRepository userRepo;

  public RefreshToken create(String username) {
    User user = userRepo.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("No user with username: " + username));

    // delete old token if exists
    repo.deleteByUser(user);

    RefreshToken token = RefreshToken.builder()
            .user(user)
            .token(UUID.randomUUID().toString())
            .expiresAt(Instant.now().plusSeconds(60 * 60 * 24 * 7)) // 7 days
            .build();

    return repo.save(token);
  }

  public RefreshToken validate(String token) {
    RefreshToken refreshToken = repo.findByToken(token)
            .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

    if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
      repo.delete(refreshToken);
      throw new ExpiredTokenException("Refresh token expired, please login again");
    }

    return refreshToken;
  }

  public void deleteByUser(String username) {
    User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("No user with username: " + username));
    repo.deleteByUser(user);
  }
}