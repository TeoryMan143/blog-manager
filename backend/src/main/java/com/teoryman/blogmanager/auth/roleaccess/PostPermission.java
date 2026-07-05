package com.teoryman.blogmanager.auth.roleaccess;

import com.teoryman.blogmanager.post.Post;
import com.teoryman.blogmanager.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "post_permissions",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"post_id", "grantee_id", "permission_type"}
        )
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostPermission {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(name = "permission_type", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private PostPermissionType permissionType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "granted_by_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User grantedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "grantee_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User grantee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Post post;
}