package com.teoryman.blogmanager.user;

import com.teoryman.blogmanager.common.exception.DuplicateUserException;
import com.teoryman.blogmanager.user.dto.UserRequest;
import com.teoryman.blogmanager.user.dto.UserResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserService implements UserDetailsService {
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserMapper userMapper;

  public UserResponse createUser(UserRequest userRequest) {
    if (userRepository.existsByUsername(userRequest.getUsername())) {
      throw new DuplicateUserException("Username already exists: " + userRequest.getUsername());
    }

    if (userRepository.existsByEmail(userRequest.getEmail())) {
      throw new DuplicateUserException("Email already exists: " + userRequest.getEmail());
    }

    User newUser = userMapper.toEntity(userRequest);
    userRepository.save(newUser);
    return userMapper.toResponse(newUser);
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }
}
