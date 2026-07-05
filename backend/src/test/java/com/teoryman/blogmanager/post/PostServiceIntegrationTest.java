package com.teoryman.blogmanager.post;

import com.teoryman.blogmanager.BaseTestContainerIntegrationTest;
import com.teoryman.blogmanager.auth.roleaccess.PostPermissionRepository;
import com.teoryman.blogmanager.auth.roleaccess.PostPermissionType;
import com.teoryman.blogmanager.auth.roleaccess.Role;
import com.teoryman.blogmanager.auth.roleaccess.dto.GrantAccessRequest;
import com.teoryman.blogmanager.common.exception.ResourceNotFoundException;
import com.teoryman.blogmanager.post.dto.PostRequest;
import com.teoryman.blogmanager.user.User;
import com.teoryman.blogmanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Post Service Integration Tests")
class PostServiceIntegrationTest extends BaseTestContainerIntegrationTest {

  @Autowired
  private PostService postService;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PostPermissionRepository postPermissionRepository;

  private User author;

  @BeforeEach
  void setUp() {
    author = new User();
    author.setUsername("author");
    author.setEmail("author@example.com");
    author.setPassword("password");
    author = userRepository.save(author);
  }

  // ---------- helpers ----------

  private User createUser(String username, Role role) {
    User user = new User();
    user.setUsername(username);
    user.setEmail(username + "@example.com");
    user.setPassword("password");
    user.setRole(role);
    return userRepository.save(user);
  }

  private Post createPost(User postAuthor, String title) {
    Post post = new Post();
    post.setTitle(title);
    post.setContent("Some content");
    post.setAuthor(postAuthor);
    return postRepository.save(post);
  }

  private PostRequest requestWith(String title, String content) {
    PostRequest request = new PostRequest();
    request.setTitle(title);
    request.setContent(content);
    return request;
  }

  private GrantAccessRequest grantRequest(String granteeId, PostPermissionType... permissions) {
    GrantAccessRequest request = new GrantAccessRequest();
    request.setGranteeUserId(granteeId);
    request.setPermissions(Set.of(permissions));
    return request;
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
    PostRequest request = new PostRequest();
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

    PostRequest request = new PostRequest();
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

  @Test
  @DisplayName("Should throw ResourceNotFoundException when creating post with unknown author")
  void testCreatePost_withUnknownUser_shouldThrow() {
    PostRequest request = requestWith("Orphan post", "content");

    assertThrows(ResourceNotFoundException.class,
            () -> postService.addPost(request, "non-existent-id"));
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when updating a non-existent post")
  void testUpdatePost_nonExistentPost_shouldThrow() {
    PostRequest request = requestWith("x", "y");

    assertThrows(ResourceNotFoundException.class,
            () -> postService.updatePost("non-existent-id", request, author.getId()));
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when fetching a non-existent post")
  void testGetPostById_nonExistent_shouldThrow() {
    assertThrows(ResourceNotFoundException.class,
            () -> postService.getPostById("non-existent-id"));
  }

  // ---------- new: role-based update/delete authorization ----------

  @Test
  @DisplayName("Should allow admin to update a post they do not own")
  void testUpdatePost_asAdmin_shouldSucceedEvenIfNotOwner() {
    User admin = createUser("admin", Role.ADMIN);
    Post post = createPost(author, "Original title");

    var updated = postService.updatePost(post.getId(), requestWith("Admin edit", "content"), admin.getId());

    assertEquals("Admin edit", updated.getTitle());
  }

  @Test
  @DisplayName("Should deny update from an unrelated common user")
  void testUpdatePost_asUnrelatedCommonUser_shouldThrowAccessDenied() {
    User intruder = createUser("intruder", Role.COMMON);
    Post post = createPost(author, "Original title");

    assertThrows(AccessDeniedException.class,
            () -> postService.updatePost(post.getId(), requestWith("Hacked", "content"), intruder.getId()));
  }

  @Test
  @DisplayName("Should deny update from a moderator without an explicit grant")
  void testUpdatePost_asModerator_withoutGrant_shouldThrowAccessDenied() {
    User moderator = createUser("moderator", Role.MODERATOR);
    Post post = createPost(author, "Original title");

    assertThrows(AccessDeniedException.class,
            () -> postService.updatePost(post.getId(), requestWith("Hacked", "content"), moderator.getId()));
  }

  @Test
  @DisplayName("Should allow update when the user has an explicit MODIFY grant")
  void testUpdatePost_withExplicitModifyGrant_shouldSucceed() {
    User grantee = createUser("grantee", Role.COMMON);
    Post post = createPost(author, "Original title");

    postService.grantAccess(post.getId(), author.getId(),
            grantRequest(grantee.getId(), PostPermissionType.MODIFY));

    var updated = postService.updatePost(post.getId(), requestWith("Edited by grantee", "content"), grantee.getId());

    assertEquals("Edited by grantee", updated.getTitle());
  }

  @Test
  @DisplayName("Should allow admin to delete a post they do not own")
  void testDeletePost_asAdmin_shouldSucceed() {
    User admin = createUser("admin", Role.ADMIN);
    Post post = createPost(author, "To delete");

    postService.deletePost(post.getId(), admin.getId());

    assertFalse(postRepository.existsById(post.getId()));
  }

  @Test
  @DisplayName("Should deny delete from an unrelated common user, leaving the post intact")
  void testDeletePost_asUnrelatedCommonUser_shouldThrowAccessDenied() {
    User intruder = createUser("intruder", Role.COMMON);
    Post post = createPost(author, "Protected post");

    assertThrows(AccessDeniedException.class,
            () -> postService.deletePost(post.getId(), intruder.getId()));

    assertTrue(postRepository.existsById(post.getId()));
  }

  @Test
  @DisplayName("Should allow delete when the user has an explicit DELETE grant")
  void testDeletePost_withExplicitDeleteGrant_shouldSucceed() {
    User grantee = createUser("grantee", Role.COMMON);
    Post post = createPost(author, "Grant-deletable post");

    postService.grantAccess(post.getId(), author.getId(),
            grantRequest(grantee.getId(), PostPermissionType.DELETE));

    postService.deletePost(post.getId(), grantee.getId());

    assertFalse(postRepository.existsById(post.getId()));
  }

  @Test
  @DisplayName("Should create a permission row when the owner grants access")
  void testGrantAccess_asOwner_shouldCreatePermissionRow() {
    User grantee = createUser("grantee", Role.COMMON);
    Post post = createPost(author, "Shared post");

    postService.grantAccess(post.getId(), author.getId(),
            grantRequest(grantee.getId(), PostPermissionType.MODIFY));

    assertTrue(postPermissionRepository.existsByPost_IdAndGrantee_IdAndPermissionType(
            post.getId(), grantee.getId(), PostPermissionType.MODIFY));
  }

  @Test
  @DisplayName("Should allow an admin to grant access on a post they do not own")
  void testGrantAccess_asAdmin_shouldSucceedEvenIfNotOwner() {
    User admin = createUser("admin", Role.ADMIN);
    User grantee = createUser("grantee", Role.COMMON);
    Post post = createPost(author, "Shared post");

    postService.grantAccess(post.getId(), admin.getId(),
            grantRequest(grantee.getId(), PostPermissionType.DELETE_COMMENTS));

    assertTrue(postPermissionRepository.existsByPost_IdAndGrantee_IdAndPermissionType(
            post.getId(), grantee.getId(), PostPermissionType.DELETE_COMMENTS));
  }

  @Test
  @DisplayName("Should deny granting access from an unrelated common user")
  void testGrantAccess_asUnrelatedCommonUser_shouldThrowAccessDenied() {
    User outsider = createUser("outsider", Role.COMMON);
    User grantee = createUser("grantee", Role.COMMON);
    Post post = createPost(author, "Protected post");

    assertThrows(AccessDeniedException.class,
            () -> postService.grantAccess(post.getId(), outsider.getId(),
                    grantRequest(grantee.getId(), PostPermissionType.MODIFY)));
  }

  @Test
  @DisplayName("Should not create a duplicate permission row when granting the same permission twice")
  void testGrantAccess_calledTwiceWithSamePermission_shouldNotDuplicate() {
    User grantee = createUser("grantee", Role.COMMON);
    Post post = createPost(author, "Shared post");

    postService.grantAccess(post.getId(), author.getId(), grantRequest(grantee.getId(), PostPermissionType.MODIFY));
    postService.grantAccess(post.getId(), author.getId(), grantRequest(grantee.getId(), PostPermissionType.MODIFY));

    assertTrue(postPermissionRepository.existsByPost_IdAndGrantee_IdAndPermissionType(
            post.getId(), grantee.getId(), PostPermissionType.MODIFY));
  }

  @Test
  @DisplayName("Should remove the permission row on revoke")
  void testRevokeAccess_shouldRemovePermissionRow() {
    User grantee = createUser("grantee", Role.COMMON);
    Post post = createPost(author, "Shared post");

    postService.grantAccess(post.getId(), author.getId(), grantRequest(grantee.getId(), PostPermissionType.MODIFY));
    postService.revokeAccess(post.getId(), author.getId(), grantRequest(grantee.getId(), PostPermissionType.MODIFY));

    assertFalse(postPermissionRepository.existsByPost_IdAndGrantee_IdAndPermissionType(
            post.getId(), grantee.getId(), PostPermissionType.MODIFY));
  }

  @Test
  @DisplayName("Should deny revoke from an unrelated user and leave the grant untouched")
  void testRevokeAccess_asUnrelatedUser_shouldThrowAccessDeniedAndKeepGrant() {
    User outsider = createUser("outsider", Role.COMMON);
    User grantee = createUser("grantee", Role.COMMON);
    Post post = createPost(author, "Shared post");

    postService.grantAccess(post.getId(), author.getId(), grantRequest(grantee.getId(), PostPermissionType.DELETE));

    assertThrows(AccessDeniedException.class,
            () -> postService.revokeAccess(post.getId(), outsider.getId(),
                    grantRequest(grantee.getId(), PostPermissionType.DELETE)));

    assertTrue(postPermissionRepository.existsByPost_IdAndGrantee_IdAndPermissionType(
            post.getId(), grantee.getId(), PostPermissionType.DELETE));
  }
}