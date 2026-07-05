package com.teoryman.blogmanager.auth.roleaccess.dto;

import com.teoryman.blogmanager.auth.roleaccess.PostPermission;
import com.teoryman.blogmanager.auth.roleaccess.PostPermissionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostPermissionResponse {
  private String id;
  private String granteeId;
  private String granteeUsername;
  private PostPermissionType permissionType;

  public PostPermissionResponse(PostPermission permission) {
    this.id = permission.getId();
    this.granteeId = permission.getGrantee().getId();
    this.granteeUsername = permission.getGrantee().getUsername();
    this.permissionType = permission.getPermissionType();
  }
}