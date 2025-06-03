package com.example.lab12dub2.repository;

import com.example.lab12dub2.model.Book;
import com.example.lab12dub2.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBookAndParentCommentIsNull(Book book);

    List<Comment> findByParentComment(Comment parentComment);
}