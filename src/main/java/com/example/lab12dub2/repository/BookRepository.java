package com.example.lab12dub2.repository;

import com.example.lab12dub2.model.Book;
import com.example.lab12dub2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    Book findByTitleAndAuthor(String title, String author);
    @Query("SELECT COUNT(b) FROM Book b WHERE b.status = :status")
    long countByStatus(@Param("status") String status);
}