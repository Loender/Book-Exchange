package com.example.lab12dub2.controller;

import com.example.lab12dub2.model.Book;
import com.example.lab12dub2.model.Comment;
import com.example.lab12dub2.model.User;
import com.example.lab12dub2.service.CommentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CommentController {

    @Autowired
    private CommentService service;

    private Book book;
    private User authenticatedUser;

    @FXML
    private Label headerLabel;

    @FXML
    private TreeView<Comment> commentsTreeView;

    @FXML
    private TextArea commentArea;

    @FXML
    private Button replyButton;

    public void setBookAndUser(Book book, User authenticatedUser) {
        this.book = book;
        this.authenticatedUser = authenticatedUser;
        initialize();
    }

    @FXML
    public void initialize() {
        if (book == null || authenticatedUser == null) {
            return;
        }

        headerLabel.setText("Comments for: " + book.getTitle());

        commentsTreeView.setCellFactory(treeView -> new TreeCell<Comment>() {
            @Override
            protected void updateItem(Comment comment, boolean empty) {
                super.updateItem(comment, empty);
                if (empty || comment == null) {
                    setText(null);
                } else {
                    String displayName = authenticatedUser.getRole() == User.Role.ADMIN ?
                            comment.getUser().getUsername() :
                            comment.getUser().getFirstName() + " " + comment.getUser().getLastName();
                    setText(displayName + ": " + comment.getContent() + " (" + comment.getCreatedAt() + ")");
                }
            }
        });

        commentsTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            replyButton.setDisable(newSelection == null);
        });

        refreshComments();
    }

    private void refreshComments() {
        List<Comment> topLevelComments = service.getCommentsForBook(book);
        TreeItem<Comment> root = new TreeItem<>(); // Invisible root
        commentsTreeView.setRoot(root);
        commentsTreeView.setShowRoot(false);


        topLevelComments.forEach(comment -> {
            TreeItem<Comment> commentItem = new TreeItem<>(comment);
            buildCommentTree(comment, commentItem);
            root.getChildren().add(commentItem);
        });
    }

    private void buildCommentTree(Comment comment, TreeItem<Comment> parentItem) {
        List<Comment> replies = comment.getReplies();
        if (replies != null && !replies.isEmpty()) {
            replies.forEach(reply -> {
                TreeItem<Comment> replyItem = new TreeItem<>(reply);
                parentItem.getChildren().add(replyItem);
                buildCommentTree(reply, replyItem);
            });
        }
    }

    @FXML
    public void postComment() {
        String content = commentArea.getText().trim();
        if (content.isEmpty()) {
            showErrorAlert("Comment cannot be empty.");
            return;
        }

        try {
            service.createComment(book, authenticatedUser, content, null);
            commentArea.clear();
            refreshComments();
            showSuccessAlert("Comment posted successfully.");
        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }

    @FXML
    public void postReply() {
        String content = commentArea.getText().trim();
        if (content.isEmpty()) {
            showErrorAlert("Reply cannot be empty.");
            return;
        }

        TreeItem<Comment> selectedItem = commentsTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showErrorAlert("Please select a comment to reply to.");
            return;
        }

        Comment parentComment = selectedItem.getValue();
        try {
            service.createComment(book, authenticatedUser, content, parentComment);
            commentArea.clear();
            refreshComments();
            showSuccessAlert("Reply posted successfully.");
        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }

    @FXML
    public void clearCommentArea() {
        commentArea.clear();
        commentsTreeView.getSelectionModel().clearSelection();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}