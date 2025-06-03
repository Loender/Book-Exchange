package com.example.lab12dub2.controller;

import com.example.lab12dub2.model.User;
import com.example.lab12dub2.service.BookExchangeService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private DatePicker dateOfBirthPicker;

    @FXML
    private Button registerButton;

    @Autowired
    private BookExchangeService service;

    @FXML
    private void initialize() {
        registerButton.disableProperty().bind(
                usernameField.textProperty().isEmpty()
                        .or(emailField.textProperty().isEmpty())
                        .or(passwordField.textProperty().isEmpty())
                        .or(firstNameField.textProperty().isEmpty())
                        .or(lastNameField.textProperty().isEmpty())
        );
    }

    @FXML
    private void register() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        LocalDate dateOfBirth = dateOfBirthPicker.getValue();

        // Optional field handling
        if (phone.isEmpty()) {
            phone = null;
        }

        try {
            service.createUser(username, email, password, firstName, lastName, phone, dateOfBirth, User.Role.USER);
            showSuccessAlert("Registration successful! You can now log in.");
            close();
        } catch (IllegalArgumentException e) {
            showErrorAlert("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Registration Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}