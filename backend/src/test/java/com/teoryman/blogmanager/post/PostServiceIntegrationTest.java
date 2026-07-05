package com.teoryman.blogmanager.post;

import com.teoryman.blogmanager.BaseTestContainerIntegrationTest;
import com.teoryman.blogmanager.user.User;
import com.teoryman.blogmanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Post Service Integration Tests")
class PostServiceIntegrationTest extends BaseTestContainerIntegrationTest {

  @Autowired
  private PostService postService;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private UserRepository userRepository;

  private User author;

  @BeforeEach
  void setUp() {
    author = new User();
    author.setUsername("author");
    author.setEmail("author@example.com");
    author.setPassword("password");
    author = userRepository.save(author);
  }

  @Test
  @DisplayName("Should get all posts")
  void testGetAllPosts() {
    Post post1 = new Post();
    post1.setTitle("Post 1");
    post1.setContent("Content 1");
    post1.setAuthor(author);

    Post post2 = new Post();
    post2.setTitle("Post 2");
    post2.setContent("Content 2");
    post2.setAuthor(author);

    postRepository.save(post1);
    postRepository.save(post2);

    var posts = postService.getAllPosts();

    assertTrue(posts.size() >= 2);
  }

  @Test
  @DisplayName("Should get post by ID")
  void testGetPostById() {
    Post post = new Post();
    post.setTitle("Test Post");
    post.setContent("Test Content");
    post.setAuthor(author);
    Post saved = postRepository.save(post);

    var retrieved = postService.getPostById(saved.getId());

    assertNotNull(retrieved);
    assertEquals("Test Post", retrieved.getTitle());
  }

  @Test
  @DisplayName("Should create post successfully")
  void testCreatePost() {
    com.teoryman.blogmanager.post.dto.PostRequest request = new com.teoryman.blogmanager.post.dto.PostRequest();
    request.setTitle("New Post");
    request.setContent("New Content");

    var response = postService.addPost(request, author.getId());

    assertNotNull(response.getId());
    assertEquals("New Post", response.getTitle());
    assertTrue(postRepository.existsById(response.getId()));
  }

  @Test
  @DisplayName("Should update post successfully")
  void testUpdatePost() {
    Post post = new Post();
    post.setTitle("Original Title");
    post.setContent("Original Content");
    post.setAuthor(author);
    Post saved = postRepository.save(post);

    com.teoryman.blogmanager.post.dto.PostRequest request = new com.teoryman.blogmanager.post.dto.PostRequest();
    request.setTitle("Updated Title");
    request.setContent("Updated Content");
    
    var updated = postService.updatePost(saved.getId(), request, author.getId());

    assertEquals("Updated Title", updated.getTitle());
    assertEquals("Updated Content", updated.getContent());
  }

  @Test
  @DisplayName("Should delete post successfully")
  void testDeletePost() {
    Post post = new Post();
    post.setTitle("Post to Delete");
    post.setContent("Content");
    post.setAuthor(author);
    Post saved = postRepository.save(post);

    postService.deletePost(saved.getId(), author.getId());

    assertFalse(postRepository.existsById(saved.getId()));
  }
}
