package com.teoryman.blogmanager.comment;

import com.teoryman.blogmanager.auth.roleaccess.PostAuthorizationService;
import com.teoryman.blogmanager.common.exception.ResourceNotFoundException;
import com.teoryman.blogmanager.comment.dto.CommentRequest;
import com.teoryman.blogmanager.comment.dto.CommentResponse;
import com.teoryman.blogmanager.post.Post;
import com.teoryman.blogmanager.post.PostRepository;
import com.teoryman.blogmanager.user.User;
import com.teoryman.blogmanager.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
  @Autowired private CommentRepository commentRepository;
  @Autowired private CommentMapper commentMapper;
  @Autowired private PostRepository postRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private PostAuthorizationService  postAuthorizationService;

  @Transactional
  public CommentResponse createComment(String postId, CommentRequest request, String userId) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

    User author = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Comment comment = new Comment();
    comment.setContent(request.getContent());
    comment.setPost(post);
    comment.setAuthor(author);

    Comment saved = commentRepository.save(comment);
    return commentMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<CommentResponse> getCommentsByPost(String postId) {
    if (!postRepository.existsById(postId)) {
      throw new ResourceNotFoundException("Post not found with id: " + postId);
    }
    List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    return commentMapper.toListResponse(comments);
  }

  @Transactional(readOnly = true)
  public List<CommentResponse> getCommentsByAuthor(String authorId) {
    if (!userRepository.existsById(authorId)) {
      throw new ResourceNotFoundException("User not found");
    }
    List<Comment> comments = commentRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    return commentMapper.toListResponse(comments);
  }

  @Transactional(readOnly = true)
  public CommentResponse getCommentById(String commentId) {
    Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
    return commentMapper.toResponse(comment);
  }

  @Transactional
  public CommentResponse updateComment(String commentId, CommentRequest request, String userId) {
    Comment comment = findCommentById(commentId);
    validateCommentOwnership(comment, userId);
    comment.setContent(request.getContent());
    Comment saved = commentRepository.save(comment);
    return commentMapper.toResponse(saved);
  }

  @Transactional
  public CommentResponse deleteComment(String commentId, String userId) {
    Comment comment = findCommentById(commentId);
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!postAuthorizationService.canDeleteComment(comment, user)) {
      throw new AccessDeniedException("You are not allowed to delete this comment");
    }

    CommentResponse response = commentMapper.toResponse(comment);
    commentRepository.delete(comment);
    return response;
  }

  private Comment findCommentById(String commentId) {
    return commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
  }

  private void validateCommentOwnership(Comment comment, String userId) {
    if (!comment.getAuthor().getId().equals(userId)) {
      throw new AccessDeniedException("You are not authorized to perform this action on this comment");
    }
  }
}

