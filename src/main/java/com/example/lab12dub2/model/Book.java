package com.example.lab12dub2.model;

import jakarta.persistence.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;

@Entity
public class Book{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;

    // JPA-persisted fields
    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // JavaFX properties for UI binding (not persisted by JPA)
    @Transient
    private final StringProperty statusProperty = new SimpleStringProperty();

    @Transient
    private final ObjectProperty<User> userProperty = new SimpleObjectProperty<>();

    public Book() {
        statusProperty.addListener((obs, oldValue, newValue) -> this.status = newValue);
        userProperty.addListener((obs, oldValue, newValue) -> this.user = newValue);
    }



    // Getters and setters for JPA fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.statusProperty.set(status); // Sync with JavaFX property
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        this.userProperty.set(user); // Sync with JavaFX property
    }

    // JavaFX property access methods
    public StringProperty statusProperty() {
        return statusProperty;
    }

    public ObjectProperty<User> userProperty() {
        return userProperty;
    }

    // Initialize JavaFX properties when the entity is loaded
    @PostLoad
    private void initProperties() {
        this.statusProperty.set(status);
        this.userProperty.set(user);
    }
}