package com.teoryman.blogmanager.post;

import com.teoryman.blogmanager.BaseTestContainerIntegrationTest;
import com.teoryman.blogmanager.user.User;
import com.teoryman.blogmanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Post Repository Integration Tests")
class PostRepositoryTest extends BaseTestContainerIntegrationTest {

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private UserRepository userRepository;

  private User author;
  private Post testPost;

  @BeforeEach
  void setUp() {
    postRepository.deleteAll();
    userRepository.deleteAll();

    author = new User();
    author.setUsername("author");
    author.setEmail("author@example.com");
    author.setPassword("password");
    author = userRepository.save(author);

    testPost = new Post();
    testPost.setTitle("Test Post");
    testPost.setContent("Test Content");
    testPost.setAuthor(author);
  }

  @Test
  @DisplayName("Should save and retrieve post")
  void testSaveAndFindPost() {
    Post saved = postRepository.save(testPost);

    var found = postRepository.findById(saved.getId());

    assertTrue(found.isPresent());
    assertEquals("Test Post", found.get().getTitle());
    assertEquals("Test Content", found.get().getContent());
  }

  @Test
  @DisplayName("Should set createdAt timestamp on save")
  void testCreatedAtTimestamp() {
    Post saved = postRepository.save(testPost);

    assertNotNull(saved.getCreatedAt());
  }

  @Test
  @DisplayName("Should set updatedAt timestamp on update")
  void testUpdatedAtTimestamp() {
    Post saved = postRepository.save(testPost);
    LocalDateTime initialUpdatedAt = saved.getUpdatedAt();

    saved.setTitle("Updated Title");
    Post updated = postRepository.save(saved);

    assertNotNull(updated.getUpdatedAt());
  }

  @Test
  @DisplayName("Should find all posts")
  void testFindAllPosts() {
    Post post1 = new Post();
    post1.setTitle("Post 1");
    post1.setContent("Content 1");
    post1.setAuthor(author);
    postRepository.save(post1);

    Post post2 = new Post();
    post2.setTitle("Post 2");
    post2.setContent("Content 2");
    post2.setAuthor(author);
    postRepository.save(post2);

    List<Post> posts = postRepository.findAll();

    assertTrue(posts.size() >= 2);
  }

  @Test
  @DisplayName("Should update post")
  void testUpdatePost() {
    Post saved = postRepository.save(testPost);
    saved.setTitle("Updated Title");
    saved.setContent("Updated Content");

    Post updated = postRepository.save(saved);

    assertEquals("Updated Title", updated.getTitle());
    assertEquals("Updated Content", updated.getContent());
  }

  @Test
  @DisplayName("Should delete post")
  void testDeletePost() {
    Post saved = postRepository.save(testPost);

    postRepository.delete(saved);

    assertFalse(postRepository.findById(saved.getId()).isPresent());
  }

  @Test
  @DisplayName("Should generate UUID for post ID")
  void testPostIdGeneration() {
    Post saved = postRepository.save(testPost);

    assertNotNull(saved.getId());
    assertFalse(saved.getId().isEmpty());
  }

  @Test
  @DisplayName("Should maintain relationship with author")
  void testPostAuthorRelationship() {
    Post saved = postRepository.save(testPost);

    Post retrieved = postRepository.findById(saved.getId()).get();

    assertNotNull(retrieved.getAuthor());
    assertEquals(author.getId(), retrieved.getAuthor().getId());
    assertEquals("author", retrieved.getAuthor().getUsername());
  }
}
