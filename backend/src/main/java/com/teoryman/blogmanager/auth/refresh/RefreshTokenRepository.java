package com.teoryman.blogmanager.auth.refresh;

import com.teoryman.blogmanager.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
  Optional<RefreshToken> findByToken(String token);

  void deleteByUser(User user);
}