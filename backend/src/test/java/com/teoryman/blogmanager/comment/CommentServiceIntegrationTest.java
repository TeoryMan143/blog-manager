package com.teoryman.blogmanager.comment;

import com.teoryman.blogmanager.BaseTestContainerIntegrationTest;
import com.teoryman.blogmanager.comment.dto.CommentRequest;
import com.teoryman.blogmanager.post.Post;
import com.teoryman.blogmanager.post.PostRepository;
import com.teoryman.blogmanager.user.User;
import com.teoryman.blogmanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Comment Service Integration Tests")
class CommentServiceIntegrationTest extends BaseTestContainerIntegrationTest {

  @Autowired
  private CommentService commentService;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private UserRepository userRepository;

  private User author;
  private Post post;

  @BeforeEach
  void setUp() {
    author = new User();
    author.setUsername("author");
    author.setEmail("author@example.com");
    author.setPassword("password");
    author = userRepository.save(author);

    post = new Post();
    post.setTitle("Test Post");
    post.setContent("Post Content");
    post.setAuthor(author);
    post = postRepository.save(post);
  }

  @Test
  @DisplayName("Should create comment successfully")
  void testCreateComment() {
    CommentRequest request = new CommentRequest();
    request.setContent("New Comment");

    var response = commentService.createComment(post.getId(), request, author.getId());

    assertNotNull(response.getId());
    assertEquals("New Comment", response.getContent());
  }

  @Test
  @DisplayName("Should get all comments for post")
  void testGetCommentsByPost() {
    CommentRequest request1 = new CommentRequest();
    request1.setContent("Comment 1");
    commentService.createComment(post.getId(), request1, author.getId());

    CommentRequest request2 = new CommentRequest();
    request2.setContent("Comment 2");
    commentService.createComment(post.getId(), request2, author.getId());

    List<com.teoryman.blogmanager.comment.dto.CommentResponse> comments = commentService.getCommentsByPost(post.getId());

    assertEquals(2, comments.size());
  }

  @Test
  @DisplayName("Should get all comments by author")
  void testGetCommentsByAuthor() {
    CommentRequest request1 = new CommentRequest();
    request1.setContent("My Comment 1");
    commentService.createComment(post.getId(), request1, author.getId());

    CommentRequest request2 = new CommentRequest();
    request2.setContent("My Comment 2");
    commentService.createComment(post.getId(), request2, author.getId());

    List<com.teoryman.blogmanager.comment.dto.CommentResponse> comments = commentService.getCommentsByAuthor(author.getId());

    assertEquals(2, comments.size());
  }

  @Test
  @DisplayName("Should update comment successfully")
  void testUpdateComment() {
    CommentRequest createRequest = new CommentRequest();
    createRequest.setContent("Original Comment");
    var created = commentService.createComment(post.getId(), createRequest, author.getId());

    CommentRequest updateRequest = new CommentRequest();
    updateRequest.setContent("Updated Comment");
    var updated = commentService.updateComment(created.getId(), updateRequest, author.getId());

    assertEquals("Updated Comment", updated.getContent());
  }

  @Test
  @DisplayName("Should delete comment successfully")
  void testDeleteComment() {
    CommentRequest request = new CommentRequest();
    request.setContent("Comment to Delete");
    var created = commentService.createComment(post.getId(), request, author.getId());

    commentService.deleteComment(created.getId(), author.getId());

    assertFalse(commentRepository.existsById(created.getId()));
  }

  @Test
  @DisplayName("Should get comment by ID")
  void testGetCommentById() {
    CommentRequest request = new CommentRequest();
    request.setContent("Test Comment");
    var created = commentService.createComment(post.getId(), request, author.getId());

    var retrieved = commentService.getCommentById(created.getId());

    assertNotNull(retrieved);
    assertEquals("Test Comment", retrieved.getContent());
  }
}
