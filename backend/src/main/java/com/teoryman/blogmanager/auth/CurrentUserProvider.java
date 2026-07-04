package com.teoryman.blogmanager.auth;

import com.teoryman.blogmanager.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()
            || !(authentication.getPrincipal() instanceof User user)) {
      throw new IllegalStateException("No authenticated user found");
    }

    return user;
  }
}