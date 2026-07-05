package com.teoryman.blogmanager.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
  List<Comment> findByPostIdOrderByCreatedAtDesc(String postId);

  List<Comment> findByAuthorIdOrderByCreatedAtDesc(String authorId);
}
