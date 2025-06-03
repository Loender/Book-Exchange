package com.example.lab12dub2.repository;

import com.example.lab12dub2.model.Rating;
import com.example.lab12dub2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByReviewedUser(User reviewedUser);
}