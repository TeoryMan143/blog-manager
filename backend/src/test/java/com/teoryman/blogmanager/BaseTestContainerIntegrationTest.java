package com.teoryman.blogmanager;

import com.teoryman.blogmanager.comment.CommentRepository;
import com.teoryman.blogmanager.post.PostRepository;
import com.teoryman.blogmanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseTestContainerIntegrationTest {

  static PostgreSQLContainer postgres;

  static {
    postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("blog_manager_test")
            .withUsername("test_user")
            .withPassword("test_password");
    postgres.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
  }

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private UserRepository userRepository;


  @BeforeEach
  void cleanDatabase() {
    commentRepository.deleteAll();
    postRepository.deleteAll();
    userRepository.deleteAll();
  }
}
