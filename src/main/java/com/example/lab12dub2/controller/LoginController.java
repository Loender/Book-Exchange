package com.example.lab12dub2.controller;

import com.example.lab12dub2.model.User;
import com.example.lab12dub2.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;

@Controller
public class LoginController {

    @Autowired private AuthService service;
    @Autowired private ApplicationContext applicationContext;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML public void initialize() {
        usernameField.setPromptText("Enter username...");
        passwordField.setPromptText("Enter password...");
    }
    @FXML public void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        User user = service.authenticate(username, password);
        if (user == null) {
            showErrorAlert("Invalid username or password.");
            return;
        }

        try {
            URL resourceUrl = getClass().getResource("/fxml/main.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(resourceUrl);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
            MainController controller = fxmlLoader.getController();
            controller.setAuthenticatedUser(user);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Book Exchange System");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error loading main interface: " + e.getMessage());
        }
    }
    @FXML public void cancel() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
    @FXML public void showRegistration() {
        try {
            URL resourceUrl = getClass().getResource("/fxml/register.fxml");
            if (resourceUrl == null) {
                throw new IOException("Cannot find resource: /fxml/register.fxml");
            }
            FXMLLoader fxmlLoader = new FXMLLoader(resourceUrl);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
            Stage stage = new Stage();
            stage.setTitle("Book Exchange System - Register");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error opening registration window: " + e.getMessage());
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}