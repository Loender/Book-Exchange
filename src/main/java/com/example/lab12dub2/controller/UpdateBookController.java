package com.example.lab12dub2.controller;

import com.example.lab12dub2.model.Book;
import com.example.lab12dub2.model.User;
import com.example.lab12dub2.service.BookService;
import com.example.lab12dub2.service.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateBookController {
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<User> userComboBox;
    @FXML private Button updateButton;
    @Autowired
    private UserService serviceUser;
    @Autowired
    private BookService serviceBook;
    private Book book;
    private MainController mainController;
    boolean borrow = true;

    public void setBook(Book book, MainController mainController) {
        this.book = book;
        this.mainController = mainController;
        // Populate fields with existing book data
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        statusComboBox.getSelectionModel().select(book.getStatus());
        userComboBox.getSelectionModel().select(book.getUser());

        // Initialize ComboBoxes
        statusComboBox.setItems(FXCollections.observableArrayList("Available", "Borrowed"));
        userComboBox.setItems(FXCollections.observableArrayList(serviceUser.getAllUsers()));
        userComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user != null ? user.getUsername() : "None";
            }

            @Override
            public User fromString(String string) {
                return null; // Not needed for display-only
            }
        });
    }
    @FXML private void initialize() {
        updateButton.disableProperty().bind(
                titleField.textProperty().isEmpty()
                        .or(authorField.textProperty().isEmpty())
                        .or(statusComboBox.getSelectionModel().selectedItemProperty().isNull())
        );
    }
    @FXML private void update() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String status = statusComboBox.getSelectionModel().getSelectedItem();
        User selectedUser = userComboBox.getSelectionModel().getSelectedItem();
        String ownerUsername = selectedUser != null ? selectedUser.getUsername() : null;

        try {
            serviceBook.updateBook(borrow, book.getTitle(), book.getAuthor(), title, author, status, ownerUsername, mainController.getAuthenticatedUser());
            mainController.refreshAll();
            showSuccessAlert("Book updated successfully.");
            close();
        } catch (IllegalArgumentException e) {
            showErrorAlert("Update failed: " + e.getMessage());
        }
    }
    @FXML private void cancel() {
        close();
    }
    private void close() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Update Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Update Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}