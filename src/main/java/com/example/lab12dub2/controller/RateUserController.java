package com.example.lab12dub2.controller;

import com.example.lab12dub2.model.User;
import com.example.lab12dub2.service.RatingService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RateUserController {

    @Autowired
    private RatingService service;

    private User reviewer;
    private User reviewedUser;

    @FXML
    private Slider ratingSlider;
    @FXML
    private TextArea commentField;
    @FXML
    private Button submitButton;

    public void setUsers(User reviewer, User reviewedUser) {
        this.reviewer = reviewer;
        this.reviewedUser = reviewedUser;
        ratingSlider.setMin(1);
        ratingSlider.setMax(5);
        ratingSlider.setValue(3);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setSnapToTicks(true);
    }

    @FXML
    private void initialize() {
        submitButton.disableProperty().bind(commentField.textProperty().isEmpty());
    }

    @FXML
    private void submitRating() {
        int rating = (int) ratingSlider.getValue();
        String comment = commentField.getText().trim();
        try {
            service.addRating(reviewer, reviewedUser, rating, comment);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Rating submitted successfully.");
            alert.showAndWait();
            close();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to submit rating: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) ratingSlider.getScene().getWindow();
        stage.close();
    }
}