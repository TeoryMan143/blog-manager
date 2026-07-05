package com.teoryman.blogmanager.user;

import com.teoryman.blogmanager.BaseTestContainerIntegrationTest;
import com.teoryman.blogmanager.common.exception.DuplicateUserException;
import com.teoryman.blogmanager.user.dto.UserRequest;
import com.teoryman.blogmanager.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Service Integration Tests")
class UserServiceIntegrationTest extends BaseTestContainerIntegrationTest {

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("Should create new user successfully")
  void testCreateUserSuccess() {
    UserRequest request = new UserRequest();
    request.setUsername("newuser");
    request.setEmail("newuser@example.com");
    request.setPassword("password123");

    UserResponse response = userService.createUser(request);

    assertNotNull(response);
    assertEquals("newuser", response.getUsername());
    assertEquals("newuser@example.com", response.getEmail());

    assertTrue(userRepository.existsByUsername("newuser"));
  }

  @Test
  @DisplayName("Should throw DuplicateUserException when username exists")
  void testCreateUserWithDuplicateUsername() {
    UserRequest request1 = new UserRequest();
    request1.setUsername("duplicate");
    request1.setEmail("first@example.com");
    request1.setPassword("password123");

    userService.createUser(request1);

    UserRequest request2 = new UserRequest();
    request2.setUsername("duplicate");
    request2.setEmail("second@example.com");
    request2.setPassword("password123");

    assertThrows(DuplicateUserException.class, () -> userService.createUser(request2));
  }

  @Test
  @DisplayName("Should throw DuplicateUserException when email exists")
  void testCreateUserWithDuplicateEmail() {
    UserRequest request1 = new UserRequest();
    request1.setUsername("user1");
    request1.setEmail("duplicate@example.com");
    request1.setPassword("password123");

    userService.createUser(request1);

    UserRequest request2 = new UserRequest();
    request2.setUsername("user2");
    request2.setEmail("duplicate@example.com");
    request2.setPassword("password123");

    assertThrows(DuplicateUserException.class, () -> userService.createUser(request2));
  }

  @Test
  @DisplayName("Should load user by username successfully")
  void testLoadUserByUsernameSuccess() {
    User user = new User();
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setPassword("password");
    userRepository.save(user);

    var foundUser = userService.loadUserByUsername("testuser");

    assertNotNull(foundUser);
    assertEquals("testuser", foundUser.getUsername());
  }

  @Test
  @DisplayName("Should throw UsernameNotFoundException when user not found")
  void testLoadUserByUsernameNotFound() {
    assertThrows(UsernameNotFoundException.class,
            () -> userService.loadUserByUsername("nonexistent"));
  }

  @Test
  @DisplayName("Should create multiple users successfully")
  void testCreateMultipleUsers() {
    UserRequest request1 = new UserRequest();
    request1.setUsername("user1");
    request1.setEmail("user1@example.com");
    request1.setPassword("password123");

    UserRequest request2 = new UserRequest();
    request2.setUsername("user2");
    request2.setEmail("user2@example.com");
    request2.setPassword("password123");

    UserResponse response1 = userService.createUser(request1);
    UserResponse response2 = userService.createUser(request2);

    assertNotNull(response1.getId());
    assertNotNull(response2.getId());
    assertNotEquals(response1.getId(), response2.getId());
  }
}
