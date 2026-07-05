package com.teoryman.blogmanager.user;

import com.teoryman.blogmanager.BaseTestContainerIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Repository Integration Tests")
class UserRepositoryTest extends BaseTestContainerIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPassword("hashedpassword");
  }

  @Test
  @DisplayName("Should save and retrieve user by username")
  void testSaveAndFindByUsername() {
    userRepository.save(testUser);

    Optional<User> found = userRepository.findByUsername("testuser");

    assertTrue(found.isPresent());
    assertEquals("testuser", found.get().getUsername());
    assertEquals("test@example.com", found.get().getEmail());
  }

  @Test
  @DisplayName("Should return empty when user not found by username")
  void testFindByUsernameNotFound() {
    Optional<User> found = userRepository.findByUsername("nonexistent");

    assertFalse(found.isPresent());
  }

  @Test
  @DisplayName("Should check if username exists")
  void testExistsByUsername() {
    userRepository.save(testUser);

    assertTrue(userRepository.existsByUsername("testuser"));
    assertFalse(userRepository.existsByUsername("nonexistent"));
  }

  @Test
  @DisplayName("Should check if email exists")
  void testExistsByEmail() {
    userRepository.save(testUser);

    assertTrue(userRepository.existsByEmail("test@example.com"));
    assertFalse(userRepository.existsByEmail("notfound@example.com"));
  }


  @Test
  @DisplayName("Should update user")
  void testUpdateUser() {
    User saved = userRepository.save(testUser);
    saved.setEmail("newemail@example.com");

    User updated = userRepository.save(saved);

    assertEquals("newemail@example.com", updated.getEmail());
    assertEquals(saved.getId(), updated.getId());
  }

  @Test
  @DisplayName("Should delete user")
  void testDeleteUser() {
    User saved = userRepository.save(testUser);

    userRepository.delete(saved);

    assertFalse(userRepository.findById(saved.getId()).isPresent());
  }

  @Test
  @DisplayName("Should persist user with UUID generated ID")
  void testUserIdGeneration() {
    User saved = userRepository.save(testUser);

    assertNotNull(saved.getId());
    assertFalse(saved.getId().isEmpty());
  }
}
