package com.example.lab12dub2.controller;

import com.example.lab12dub2.model.Rating;
import com.example.lab12dub2.model.User;
import com.example.lab12dub2.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class UserDetailsController {

    @Autowired
    private RatingService service;

    private User selectedUser;
    private User authenticatedUser;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label firstNameLabel;

    @FXML
    private Label lastNameLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label dateOfBirthLabel;

    @FXML
    private ListView<String> booksListView;

    @FXML
    private ListView<Rating> ratingsListView;

    private ObservableList<String> booksList = FXCollections.observableArrayList(); // Fixed declaration
    private ObservableList<Rating> ratingsList = FXCollections.observableArrayList();

    public void setUser(User selectedUser, User authenticatedUser) {
        this.selectedUser = selectedUser;
        this.authenticatedUser = authenticatedUser;
        initialize();
    }

    @FXML
    public void initialize() {
        if (selectedUser == null || authenticatedUser == null) {
            return;
        }

        // Populate user details
        usernameLabel.setText("Username: " + selectedUser.getUsername());
        emailLabel.setText("Email: " + selectedUser.getEmail());
        firstNameLabel.setText("First Name: " + selectedUser.getFirstName());
        lastNameLabel.setText("Last Name: " + selectedUser.getLastName());
        phoneLabel.setText("Phone: " + (selectedUser.getPhone() != null ? selectedUser.getPhone() : "Not provided"));
        dateOfBirthLabel.setText("Date of Birth: " + (selectedUser.getDateOfBirth() != null ? selectedUser.getDateOfBirth().toString() : "Not provided"));

        // Customize the ratingsListView display
        ratingsListView.setCellFactory(listView -> new ListCell<Rating>() {
            @Override
            protected void updateItem(Rating rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText(null);
                } else {
                    String reviewerName = authenticatedUser.getRole() == User.Role.ADMIN ?
                            rating.getReviewer().getUsername() :
                            rating.getReviewer().getFirstName() + " " + rating.getReviewer().getLastName();
                    setText(reviewerName + ": " + rating.getRating() + " stars - " + rating.getComment());
                }
            }
        });

        // Refresh books and ratings
        refreshBooks();
        refreshRatings();
    }

    private void refreshBooks() {
        booksList.clear();
        if (selectedUser.getBooks() != null) {
            selectedUser.getBooks().forEach(book -> booksList.add(book.getTitle() + " by " + book.getAuthor()));
        }
        booksListView.setItems(booksList);
    }

    private void refreshRatings() {
        ratingsList.clear();
        ratingsList.addAll(service.getRatingsForUser(selectedUser)); // Fixed: Pass User object instead of String
        ratingsListView.setItems(ratingsList);
    }
}