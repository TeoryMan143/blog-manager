package com.teoryman.blogmanager.auth.roleaccess;

import com.teoryman.blogmanager.comment.Comment;
import com.teoryman.blogmanager.post.Post;
import com.teoryman.blogmanager.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostAuthorizationService {

  @Autowired
  private PostPermissionRepository postPermissionRepository;

  @Transactional(readOnly = true)
  public boolean canModifyPost(Post post, User user) {
    if (user.getRole() == Role.ADMIN) return true;
    if (isOwner(post, user)) return true;
    return hasGrant(post, user, PostPermissionType.MODIFY);
  }

  @Transactional(readOnly = true)
  public boolean canDeletePost(Post post, User user) {
    if (user.getRole() == Role.ADMIN) return true;
    if (isOwner(post, user)) return true;
    return hasGrant(post, user, PostPermissionType.DELETE);
  }

  @Transactional(readOnly = true)
  public boolean canDeleteComment(Comment comment, User user) {
    if (user.getRole() == Role.ADMIN || user.getRole() == Role.MODERATOR) return true;
    if (comment.getAuthor().getId().equals(user.getId())) return true;
    Post post = comment.getPost();
    if (isOwner(post, user)) return true;
    return hasGrant(post, user, PostPermissionType.DELETE_COMMENTS);
  }

  @Transactional(readOnly = true)
  public boolean canGrantAccess(Post post, User user) {
    return user.getRole() == Role.ADMIN || isOwner(post, user);
  }

  private boolean isOwner(Post post, User user) {
    return post.getAuthor().getId().equals(user.getId());
  }

  private boolean hasGrant(Post post, User user, PostPermissionType type) {
    return postPermissionRepository.existsByPost_IdAndGrantee_IdAndPermissionType(
            post.getId(), user.getId(), type
    );
  }
}