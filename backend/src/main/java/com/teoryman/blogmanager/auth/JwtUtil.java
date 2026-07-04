package com.teoryman.blogmanager.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
  @Value("${jwt.secret}")
  private String secret;

  private SecretKey getKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }

  public String generateToken(String username) {
    return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 minutes
            .signWith(getKey())
            .compact();
  }

  public String extractUsername(String token) {
    return Jwts.parser().verifyWith(getKey()).build()
            .parseSignedClaims(token).getPayload().getSubject();
  }

  public boolean isValid(String token, UserDetails user) {
    return extractUsername(token).equals(user.getUsername())
            && !isExpired(token);
  }

  private boolean isExpired(String token) {
    return Jwts.parser().verifyWith(getKey()).build()
            .parseSignedClaims(token).getPayload().getExpiration().before(new Date());
  }
}