package com.teoryman.blogmanager.post;

import com.teoryman.blogmanager.auth.roleaccess.PostAuthorizationService;
import com.teoryman.blogmanager.auth.roleaccess.PostPermission;
import com.teoryman.blogmanager.auth.roleaccess.PostPermissionRepository;
import com.teoryman.blogmanager.auth.roleaccess.PostPermissionType;
import com.teoryman.blogmanager.auth.roleaccess.dto.GrantAccessRequest;
import com.teoryman.blogmanager.common.exception.ResourceNotFoundException;
import com.teoryman.blogmanager.post.dto.PostRequest;
import com.teoryman.blogmanager.post.dto.PostResponse;
import com.teoryman.blogmanager.user.User;
import com.teoryman.blogmanager.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private PostMapper postMapper;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PostAuthorizationService postAuthorizationService;
  @Autowired
  private PostPermissionRepository postPermissionRepository;

  private User findUserById(String id) throws ResourceNotFoundException {
    return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  @Transactional
  public PostResponse addPost(PostRequest post, String userId) {
    User author = findUserById(userId);

    Post created = postMapper.toEntity(post);
    created.setAuthor(author);
    postRepository.save(created);
    return new PostResponse(created);
  }

  private Post findPostById(String id) throws ResourceNotFoundException {
    return postRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
  }

  @Transactional
  public PostResponse updatePost(String id, PostRequest request, String userId) {
    Post existing = findPostById(id);
    User user = findUserById(userId);

    if (!postAuthorizationService.canModifyPost(existing, user)) {
      throw new AccessDeniedException("You are not allowed to update this post");
    }

    existing.setTitle(request.getTitle());
    existing.setContent(request.getContent());

    Post saved = postRepository.save(existing);
    return postMapper.toResponse(saved);
  }

  @Transactional
  public PostResponse deletePost(String id, String userId) {
    Post existing = findPostById(id);
    User user = findUserById(userId);

    if (!postAuthorizationService.canDeletePost(existing, user)) {
      throw new AccessDeniedException("You are not allowed to delete this post");
    }

    postRepository.delete(existing);
    return postMapper.toResponse(existing);
  }

  @Transactional(readOnly = true)
  public List<PostResponse> getAllPosts() {
    List<Post> allPosts = postRepository.findAll();
    return postMapper.toListResponse(allPosts);
  }

  @Transactional(readOnly = true)
  public PostResponse getPostById(String id) {
    Post post = findPostById(id);
    return postMapper.toResponse(post);
  }

  @Transactional
  public void grantAccess(String postId, String grantedByUserId, GrantAccessRequest request) {
    User grantedByUser = findUserById(grantedByUserId);
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

    if (!postAuthorizationService.canGrantAccess(post, grantedByUser)) {
      throw new AccessDeniedException("Only the post owner or an admin can grant access");
    }

    User grantee = findUserById(request.getGranteeUserId());

    for (PostPermissionType permission : request.getPermissions()) {
      if (!postPermissionRepository.existsByPost_IdAndGrantee_IdAndPermissionType(postId, grantee.getId(), permission)) {
        PostPermission grant = new PostPermission();
        grant.setPost(post);
        grant.setGrantee(grantee);
        grant.setGrantedBy(grantedByUser);
        grant.setPermissionType(permission);
        postPermissionRepository.save(grant);
      }
    }
  }

  @Transactional
  public void revokeAccess(String postId, String grantedByUserId, GrantAccessRequest request) {
    Post post = findPostById(postId);
    User grantedByUser = findUserById(grantedByUserId);

    if (!postAuthorizationService.canGrantAccess(post, grantedByUser)) {
      throw new AccessDeniedException("Only the post owner or an admin can revoke access");
    }

    User grantee = findUserById(request.getGranteeUserId());

    for (PostPermissionType permission : request.getPermissions()) {
      if (postPermissionRepository.existsByPost_IdAndGrantee_IdAndPermissionType(postId, grantee.getId(), permission)) {
        postPermissionRepository.deleteByPost_IdAndGrantee_IdAndPermissionType(postId, grantee.getId(), permission);
      }
    }
  }
}
