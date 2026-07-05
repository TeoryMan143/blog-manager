package com.teoryman.blogmanager.auth.roleaccess.dto;

import com.teoryman.blogmanager.auth.roleaccess.PostPermissionType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrantAccessRequest {
  @NotBlank
  String granteeUserId;
  Set<PostPermissionType> permissions;
}
