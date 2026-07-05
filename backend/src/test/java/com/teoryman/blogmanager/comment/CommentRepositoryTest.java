package com.teoryman.blogmanager.comment;

import com.teoryman.blogmanager.BaseTestContainerIntegrationTest;
import com.teoryman.blogmanager.post.Post;
import com.teoryman.blogmanager.post.PostRepository;
import com.teoryman.blogmanager.user.User;
import com.teoryman.blogmanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Comment Repository Integration Tests")
class CommentRepositoryTest extends BaseTestContainerIntegrationTest {

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private UserRepository userRepository;

  private User author;
  private Post post;
  private Comment testComment;

  @BeforeEach
  void setUp() {
    author = new User();
    author.setUsername("commenter");
    author.setEmail("commenter@example.com");
    author.setPassword("password");
    author = userRepository.save(author);

    post = new Post();
    post.setTitle("Test Post");
    post.setContent("Post Content");
    post.setAuthor(author);
    post = postRepository.save(post);

    testComment = new Comment();
    testComment.setContent("Test Comment");
    testComment.setAuthor(author);
    testComment.setPost(post);
  }

  @Test
  @DisplayName("Should save and retrieve comment")
  void testSaveAndFindComment() {
    Comment saved = commentRepository.save(testComment);

    var found = commentRepository.findById(saved.getId());

    assertTrue(found.isPresent());
    assertEquals("Test Comment", found.get().getContent());
  }

  @Test
  @DisplayName("Should find all comments by post")
  void testFindCommentsByPost() {
    Comment comment1 = new Comment();
    comment1.setContent("Comment 1");
    comment1.setAuthor(author);
    comment1.setPost(post);
    commentRepository.save(comment1);

    Comment comment2 = new Comment();
    comment2.setContent("Comment 2");
    comment2.setAuthor(author);
    comment2.setPost(post);
    commentRepository.save(comment2);

    List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(post.getId());

    assertEquals(2, comments.size());
  }

  @Test
  @DisplayName("Should find all comments by author")
  void testFindCommentsByAuthor() {
    Comment comment1 = new Comment();
    comment1.setContent("My Comment 1");
    comment1.setAuthor(author);
    comment1.setPost(post);
    commentRepository.save(comment1);

    Comment comment2 = new Comment();
    comment2.setContent("My Comment 2");
    comment2.setAuthor(author);
    comment2.setPost(post);
    commentRepository.save(comment2);

    List<Comment> comments = commentRepository.findByAuthorIdOrderByCreatedAtDesc(author.getId());

    assertEquals(2, comments.size());
  }

  @Test
  @DisplayName("Should set createdAt timestamp on save")
  void testCreatedAtTimestamp() {
    Comment saved = commentRepository.save(testComment);

    assertNotNull(saved.getCreatedAt());
  }

  @Test
  @DisplayName("Should update comment")
  void testUpdateComment() {
    Comment saved = commentRepository.save(testComment);
    saved.setContent("Updated Comment");

    Comment updated = commentRepository.save(saved);

    assertEquals("Updated Comment", updated.getContent());
  }

  @Test
  @DisplayName("Should delete comment")
  void testDeleteComment() {
    Comment saved = commentRepository.save(testComment);

    commentRepository.delete(saved);

    assertFalse(commentRepository.findById(saved.getId()).isPresent());
  }

  @Test
  @DisplayName("Should maintain relationship with post and author")
  void testCommentRelationships() {
    Comment saved = commentRepository.save(testComment);

    Comment retrieved = commentRepository.findById(saved.getId()).get();

    assertNotNull(retrieved.getPost());
    assertNotNull(retrieved.getAuthor());
    assertEquals(post.getId(), retrieved.getPost().getId());
    assertEquals(author.getId(), retrieved.getAuthor().getId());
  }

  @Test
  @DisplayName("Should generate UUID for comment ID")
  void testCommentIdGeneration() {
    Comment saved = commentRepository.save(testComment);

    assertNotNull(saved.getId());
    assertFalse(saved.getId().isEmpty());
  }
}
