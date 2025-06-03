package com.example.lab12dub2.repository;

import com.example.lab12dub2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.books WHERE u.username = :username")
    User findByUsernameWithBooks(@Param("username") String username);

    List<User> findAll();

    List<User> findByUsernameContainingIgnoreCase(String username);

    long count();
}