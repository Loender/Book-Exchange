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
public class UpdateUserController {

    @Autowired
    private BookExchangeService service;

    private User user;
    private MainController mainController;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private Button updateButton;

    public void setUser(User user, MainController mainController) {
        this.user = user;
        this.mainController = mainController;
        usernameField.setText(user.getUsername() != null ? user.getUsername() : "");
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        firstNameField.setText(user.getFirstName() != null ? user.getFirstName() : "");
        lastNameField.setText(user.getLastName() != null ? user.getLastName() : "");
        phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
        dateOfBirthPicker.setValue(user.getDateOfBirth());
    }

    @FXML private void initialize() {
        updateButton.disableProperty().bind(
                usernameField.textProperty().isEmpty()
                        .or(emailField.textProperty().isEmpty())
                        .or(firstNameField.textProperty().isEmpty())
                        .or(lastNameField.textProperty().isEmpty())
        );
    }
    @FXML private void update() {
        String newUsername = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String firstName = firstNameField.getText() != null ? firstNameField.getText().trim() : "";
        String lastName = lastNameField.getText() != null ? lastNameField.getText().trim() : "";
        String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";
        LocalDate dateOfBirth = dateOfBirthPicker.getValue();

        if (phone.isEmpty()) {
            phone = null;
        }

        // Additional validation to ensure required fields are not empty
        if (newUsername.isEmpty() || email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Username, email, first name, and last name are required.");
            alert.showAndWait();
            return;
        }

        try {
            service.updateUser(user.getUsername(), newUsername, email, firstName, lastName, phone, dateOfBirth, mainController.getAuthenticatedUser());
            mainController.refreshAll();
            close();
        } catch (IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
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
}