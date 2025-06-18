package com.example.lab12dub2.service;

import com.example.lab12dub2.model.Rating;
import com.example.lab12dub2.model.User;
import com.example.lab12dub2.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingService {
    @Autowired
    private RatingRepository ratingRepository;

    public void addRating(User reviewer, User reviewedUser, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        Rating ratingEntity = new Rating();
        ratingEntity.setReviewer(reviewer);
        ratingEntity.setReviewedUser(reviewedUser);
        ratingEntity.setRating(rating);
        ratingEntity.setComment(comment);
        ratingRepository.save(ratingEntity);
    }

    public List<Rating> getRatingsForUser(User user) {
        return ratingRepository.findByReviewedUser(user);
    }
}
