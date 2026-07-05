package com.teoryman.blogmanager.auth.roleaccess;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostPermissionRepository extends JpaRepository<PostPermission, String> {
  boolean existsByPost_IdAndGrantee_IdAndPermissionType(String postId, String granteeId, PostPermissionType permission);

  List<PostPermission> findByPost_Id(String postId);

  void deleteByPost_IdAndGrantee_IdAndPermissionType(String postId, String granteeId, PostPermissionType permission);
}
