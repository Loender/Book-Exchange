package com.example.lab12dub2.controller;

import com.example.lab12dub2.model.Transaction;
import com.example.lab12dub2.model.User;
import com.example.lab12dub2.service.BookExchangeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionHistoryController {

    @Autowired
    private BookExchangeService service;

    private User user;

    @FXML
    private TableView<Transaction> transactionTable;
    @FXML
    private TableColumn<Transaction, String> bookTitleColumn;
    @FXML
    private TableColumn<Transaction, String> typeColumn;
    @FXML
    private TableColumn<Transaction, String> otherUserColumn;
    @FXML
    private TableColumn<Transaction, String> dateColumn;

    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.user = user;
        bookTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBook().getTitle()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        otherUserColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getType().equals("BORROW") ?
                        (user.getRole() == User.Role.ADMIN ? cellData.getValue().getOwner().getUsername() :
                                cellData.getValue().getOwner().getFirstName() + " " + cellData.getValue().getOwner().getLastName()) :
                        (user.getRole() == User.Role.ADMIN ? cellData.getValue().getBorrower().getUsername() :
                                cellData.getValue().getBorrower().getFirstName() + " " + cellData.getValue().getBorrower().getLastName())
        ));
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate().toString()));
        transactionTable.setItems(transactions);
        refreshTransactions();
    }

    @FXML
    private void initialize() {
    }

    @FXML
    private void close() {
        Stage stage = (Stage) transactionTable.getScene().getWindow();
        stage.close();
    }

    private void refreshTransactions() {
        transactions.setAll(service.getUserTransactions(user));
    }
}