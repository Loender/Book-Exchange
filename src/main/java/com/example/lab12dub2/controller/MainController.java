package com.example.lab12dub2.controller;

import com.example.lab12dub2.model.Book;
import com.example.lab12dub2.model.Comment;
import com.example.lab12dub2.model.Message;
import com.example.lab12dub2.model.User;
import com.example.lab12dub2.service.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private BookService bookService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    // Connection state (useless feature tbh)
    private final SimpleBooleanProperty isConnected = new SimpleBooleanProperty(true);
    private Connection dbConnection;

    // Role-based disabling
    private final SimpleBooleanProperty isRegularUser = new SimpleBooleanProperty(false);

    // Property to control visibility of create user fields
    private final SimpleBooleanProperty showCreateFields = new SimpleBooleanProperty(false);

    // Property to control visibility of create book fields
    private final SimpleBooleanProperty showCreateBookFields = new SimpleBooleanProperty(false);

    // Authenticated user wrapped in a property to track changes
    private final SimpleObjectProperty<User> authenticatedUserProperty = new SimpleObjectProperty<>(null);
    private User authenticatedUser;

    // Connect/Disconnect Buttons
    @FXML private Button connectButton;
    @FXML private Button disconnectButton;
    @FXML private Button logoutButton;

    // User Tab
    @FXML private ComboBox<User> userComboBox;
    @FXML private HBox usernameBox;
    @FXML private TextField userUsernameField;
    @FXML private HBox emailBox;
    @FXML private TextField userEmailField;
    @FXML private HBox passwordBox;
    @FXML private PasswordField userPasswordField;
    @FXML private HBox firstNameBox;
    @FXML private TextField userFirstNameField;
    @FXML private HBox lastNameBox;
    @FXML private TextField userLastNameField;
    @FXML private HBox phoneBox;
    @FXML private TextField userPhoneField;
    @FXML private HBox dateOfBirthBox;
    @FXML private DatePicker userDateOfBirthPicker;
    @FXML private TextField userSearchField;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> userUsernameColumn;
    @FXML private TableColumn<User, String> userFirstNameColumn;
    @FXML private TableColumn<User, String> userLastNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private Button createUserButton;
    @FXML private Button updateUserButton;
    @FXML private Button deleteUserButton;
    @FXML private Button showDetailsButton;

    // Book Tab
    @FXML private HBox titleBox;
    @FXML private TextField bookTitleField;
    @FXML private HBox authorBox;
    @FXML private TextField bookAuthorField;
    @FXML private HBox statusBox;
    @FXML private ComboBox<String> bookStatusComboBox;
    @FXML private HBox ownerBox;
    @FXML private ComboBox<User> bookUserComboBox;
    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> bookTitleColumn;
    @FXML private TableColumn<Book, String> bookAuthorColumn;
    @FXML private TableColumn<Book, String> bookStatusColumn;
    @FXML private TableColumn<Book, String> bookOwnerColumn;
    @FXML private TextField bookSearchField;
    @FXML private ComboBox<String> bookStatusFilter;
    @FXML private Button createBookButton;
    @FXML private Button updateBookButton;
    @FXML private Button deleteBookButton;
    @FXML private Button readBookButton;
    @FXML private Button rateBookOwnerButton;
    @FXML private Button messageBookOwnerButton;
    @FXML private Button borrowBookButton;
    @FXML private Button viewHistoryButton;
    @FXML private Button viewCommentsButton;

    // Messages Tab
    @FXML private Tab messagesTab;
    @FXML private ListView<User> conversationsListView;
    @FXML private Label conversationLabel;
    @FXML private ListView<Message> messagesListView;
    @FXML private TextArea messageInputArea;
    @FXML private Button sendMessageButton;

    // Statistics Tab
    @FXML private Label totalUsersLabel;
    @FXML private Label totalBooksLabel;
    @FXML private Label totalAvailableBooksLabel;
    @FXML private Label totalBorrowedBooksLabel;

    // TabPane
    @FXML private TabPane tabPane;
    @FXML private Tab manageUsersTab;

    // ObservableLists for dynamic updates
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private ObservableList<Book> bookList = FXCollections.observableArrayList();
    private ObservableList<User> bookUserList = FXCollections.observableArrayList();
    private ObservableList<User> conversationsList = FXCollections.observableArrayList();
    private ObservableList<Message> messagesList = FXCollections.observableArrayList();
    boolean borrow;
    public void setAuthenticatedUser(User user) {
        this.authenticatedUser = user;
        this.authenticatedUserProperty.set(user); // Update the property
        initializeRoleBasedAccess();
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public Tab getMessagesTab() {
        return messagesTab;
    }

    public ListView<User> getConversationsListView() {
        return conversationsListView;
    }

    @FXML public void initialize() {
        // Initialize the database connection
        try {
            dbConnection = dataSource.getConnection();
            System.out.println("Database connection initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
            isConnected.set(false);
        }

        // Initialize User Table
        userUsernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        userFirstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        userLastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        userEmailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        userTable.setItems(userList);

        // Initialize Book Table (defer role-based setup to initializeRoleBasedAccess)
        bookTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        bookAuthorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthor()));
        bookStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        bookTable.setItems(bookList);

        // Initialize ComboBoxes with dynamic StringConverter (will be updated in initializeRoleBasedAccess)
        StringConverter<User> userConverter = new StringConverter<>() {
            @Override
            public String toString(User user) {
                if (user == null) return "";
                if (authenticatedUser != null && authenticatedUser.getRole() == User.Role.ADMIN) {
                    return user.getUsername();
                } else {
                    return user.getFirstName() + " " + user.getLastName();
                }
            }

            @Override
            public User fromString(String string) {
                return null; // Not needed for display-only
            }
        };
        userComboBox.setConverter(userConverter);
        bookUserComboBox.setConverter(userConverter);

        userComboBox.setItems(userList);
        bookUserComboBox.setItems(bookUserList);
        bookStatusFilter.setItems(FXCollections.observableArrayList("All", "Available", "Borrowed"));
        bookStatusFilter.getSelectionModel().select("All");
        bookStatusFilter.setOnAction(event -> updateBookTable());

        // Set items for bookStatusComboBox programmatically
        bookStatusComboBox.setItems(FXCollections.observableArrayList("Available", "Borrowed"));
        bookStatusComboBox.getSelectionModel().select("Available");

        // Initialize Messages Tab
        setupMessagesTab();

        // Synchronize Table and ComboBox selection
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                userComboBox.getSelectionModel().select(newSelection);
            }
        });

        userComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                userTable.getSelectionModel().select(newUser);
            } else {
                clearUserFields();
            }
        });

        // Auto-populate book fields on selection
        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldBook, newBook) -> {
            if (newBook != null) {
                bookTitleField.setText(newBook.getTitle());
                bookAuthorField.setText(newBook.getAuthor());
                bookStatusComboBox.getSelectionModel().select(newBook.getStatus());
                bookUserComboBox.getSelectionModel().select(newBook.getUser());
            } else {
                bookTitleField.clear();
                bookAuthorField.clear();
                bookStatusComboBox.getSelectionModel().select("Available");
                bookUserComboBox.getSelectionModel().clearSelection();
            }
        });

        // Bind visibility of create user fields
        usernameBox.visibleProperty().bind(showCreateFields);
        usernameBox.managedProperty().bind(showCreateFields);
        emailBox.visibleProperty().bind(showCreateFields);
        emailBox.managedProperty().bind(showCreateFields);
        passwordBox.visibleProperty().bind(showCreateFields);
        passwordBox.managedProperty().bind(showCreateFields);
        firstNameBox.visibleProperty().bind(showCreateFields);
        firstNameBox.managedProperty().bind(showCreateFields);
        lastNameBox.visibleProperty().bind(showCreateFields);
        lastNameBox.managedProperty().bind(showCreateFields);
        phoneBox.visibleProperty().bind(showCreateFields);
        phoneBox.managedProperty().bind(showCreateFields);
        dateOfBirthBox.visibleProperty().bind(showCreateFields);
        dateOfBirthBox.managedProperty().bind(showCreateFields);

        // Bind visibility of create book fields
        titleBox.visibleProperty().bind(showCreateBookFields);
        titleBox.managedProperty().bind(showCreateBookFields);
        authorBox.visibleProperty().bind(showCreateBookFields);
        authorBox.managedProperty().bind(showCreateBookFields);
        statusBox.visibleProperty().bind(showCreateBookFields);
        statusBox.managedProperty().bind(showCreateBookFields);
        ownerBox.visibleProperty().bind(showCreateBookFields);
        ownerBox.managedProperty().bind(showCreateBookFields);

        // Bind button disable properties
        createUserButton.disableProperty().bind(isConnected.not());
        updateUserButton.disableProperty().bind(
                userComboBox.getSelectionModel().selectedItemProperty().isNull()
                        .or(isConnected.not())
        );
        deleteUserButton.disableProperty().bind(
                userComboBox.getSelectionModel().selectedItemProperty().isNull()
                        .or(isConnected.not())
        );
        showDetailsButton.disableProperty().bind(
                userComboBox.getSelectionModel().selectedItemProperty().isNull()
                        .or(isConnected.not())
        );
        createBookButton.disableProperty().bind(isConnected.not());
        updateBookButton.disableProperty().bind(
                bookTable.getSelectionModel().selectedItemProperty().isNull()
                        .or(isConnected.not())
        );
        deleteBookButton.disableProperty().bind(
                bookTable.getSelectionModel().selectedItemProperty().isNull()
                        .or(isConnected.not())
        );
        readBookButton.disableProperty().bind(
                bookTable.getSelectionModel().selectedItemProperty().isNull()
                        .or(isConnected.not())
        );
        rateBookOwnerButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> {
                            Book selectedBook = bookTable.getSelectionModel().selectedItemProperty().get();
                            return !isConnected.get() ||
                                    selectedBook == null ||
                                    selectedBook.userProperty().get() == null;
                        },
                        bookTable.getSelectionModel().selectedItemProperty(),
                        isConnected
                )
        );
        messageBookOwnerButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> {
                            Book selectedBook = bookTable.getSelectionModel().selectedItemProperty().get();
                            return !isConnected.get() ||
                                    selectedBook == null ||
                                    selectedBook.userProperty().get() == null;
                        },
                        bookTable.getSelectionModel().selectedItemProperty(),
                        isConnected
                )
        );
        borrowBookButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> {
                            Book selectedBook = bookTable.getSelectionModel().selectedItemProperty().get();
                            return !isConnected.get() ||
                                    selectedBook == null ||
                                    selectedBook.userProperty().get() == null ||
                                    "Borrowed".equals(selectedBook.getStatus());
                        },
                        bookTable.getSelectionModel().selectedItemProperty(),
                        isConnected
                )
        );
        viewHistoryButton.disableProperty().bind(isConnected.not());
        viewCommentsButton.disableProperty().bind(
                bookTable.getSelectionModel().selectedItemProperty().isNull()
                        .or(isConnected.not())
        );

        // Disable other UI components when disconnected or based on role
        userComboBox.disableProperty().bind(isConnected.not());
        userSearchField.disableProperty().bind(isConnected.not());
        userTable.disableProperty().bind(isConnected.not());
        bookTable.disableProperty().bind(isConnected.not());
        bookSearchField.disableProperty().bind(isConnected.not());
        bookStatusFilter.disableProperty().bind(isConnected.not());
        bookStatusComboBox.disableProperty().bind(isConnected.not());
        tabPane.disableProperty().bind(isConnected.not());

        // Bind bookUserComboBox disable property to both isConnected and user role
        bookUserComboBox.disableProperty().bind(
                Bindings.or(
                        isConnected.not(),
                        isRegularUser
                )
        );

        // Disable Messages tab if not logged in or disconnected
        messagesTab.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    boolean isDisabled = authenticatedUserProperty.get() == null || !isConnected.get();
                    System.out.println("MessagesTab disable state: " + isDisabled +
                            " (AuthenticatedUser: " + (authenticatedUserProperty.get() != null ? authenticatedUserProperty.get().getUsername() : "null") +
                            ", Connected: " + isConnected.get() + ")");
                    return isDisabled;
                },
                authenticatedUserProperty,
                isConnected
        ));

        // Bind connect/disconnect buttons
        connectButton.disableProperty().bind(isConnected);
        disconnectButton.disableProperty().bind(isConnected.not());

        // Search functionality with listeners
        userSearchField.textProperty().addListener((obs, oldValue, newValue) -> updateUserTable());
        bookSearchField.textProperty().addListener((obs, oldValue, newValue) -> updateBookTable());

        // Set prompt text for fields
        userUsernameField.setPromptText("Enter username...");
        userEmailField.setPromptText("Enter email...");
        userPasswordField.setPromptText("Enter password...");
        userFirstNameField.setPromptText("Enter first name...");
        userLastNameField.setPromptText("Enter last name...");
        userPhoneField.setPromptText("Enter phone (optional)...");
        bookTitleField.setPromptText("Enter book title...");
        bookAuthorField.setPromptText("Enter author...");
    }
    private void setupMessagesTab() {
        // Set placeholders for empty ListViews
        conversationsListView.setPlaceholder(new Label("No conversations available"));
        messagesListView.setPlaceholder(new Label("No messages in this conversation"));

        // Populate conversations list
        conversationsListView.setItems(conversationsList);
        conversationsListView.setCellFactory(listView -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    String displayName = authenticatedUserProperty.get().getRole() == User.Role.ADMIN ?
                            user.getUsername() :
                            user.getFirstName() + " " + user.getLastName();
                    setText(displayName);
                }
            }
        });

        // Display conversation when a user is selected
        conversationsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            System.out.println("ConversationsListView selection changed: " + (newValue != null ? newValue.getUsername() : "null"));
            if (newValue != null) {
                displayConversation(newValue);
                // Request focus on messageInputArea after selection
                Platform.runLater(() -> messageInputArea.requestFocus());
            } else {
                conversationLabel.setText("Select a user to view conversation");
                messagesList.clear();
            }
        });

        // Display messages with unread messages in bold
        messagesListView.setItems(messagesList);
        messagesListView.setCellFactory(listView -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setFont(Font.font("System", FontWeight.NORMAL, 12));
                } else {
                    String displayName = authenticatedUserProperty.get().getRole() == User.Role.ADMIN ?
                            message.getSender().getUsername() :
                            message.getSender().getFirstName() + " " + message.getSender().getLastName();
                    setText(displayName + " (" + message.getTimestamp() + "): " + message.getContent());
                    if (!message.isRead() && message.getReceiver().equals(authenticatedUserProperty.get())) {
                        setFont(Font.font("System", FontWeight.BOLD, 12));
                    } else {
                        setFont(Font.font("System", FontWeight.NORMAL, 12));
                    }
                }
            }
        });

        // Ensure messageInputArea is editable unless disconnected
        messageInputArea.disableProperty().bind(isConnected.not());

        // Enable send button when a user is selected and message input is not empty
        sendMessageButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> {
                            boolean isDisabled = conversationsListView.getSelectionModel().selectedItemProperty().isNull().get() ||
                                    messageInputArea.textProperty().isEmpty().get() ||
                                    !isConnected.get();
                            System.out.println("SendMessageButton disable state: " + isDisabled +
                                    " (Selected: " + (conversationsListView.getSelectionModel().getSelectedItem() != null ? conversationsListView.getSelectionModel().getSelectedItem().getUsername() : "none") +
                                    ", Input empty: " + messageInputArea.textProperty().isEmpty().get() +
                                    ", Connected: " + isConnected.get() + ")");
                            return isDisabled;
                        },
                        conversationsListView.getSelectionModel().selectedItemProperty(),
                        messageInputArea.textProperty(),
                        isConnected
                )
        );

        refreshConversations();
    }
    private void initializeRoleBasedAccess() {
        if (authenticatedUserProperty.get() == null) {
            return;
        }

        // Set up bookOwnerColumn based on role
        bookOwnerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getUser() != null ?
                        (authenticatedUserProperty.get().getRole() == User.Role.ADMIN ?
                                cellData.getValue().getUser().getUsername() :
                                cellData.getValue().getUser().getFirstName() + " " + cellData.getValue().getUser().getLastName()) :
                        "None"
        ));

        // Configure user table columns based on role
        if (authenticatedUserProperty.get().getRole() == User.Role.USER) {
            // Regular users: hide username and email columns
            userTable.getColumns().remove(userUsernameColumn);
            userTable.getColumns().remove(userEmailColumn);
            // Restrict book creation to require the current user as owner
            bookUserComboBox.setItems(FXCollections.observableArrayList(authenticatedUserProperty.get()));
            bookUserComboBox.getSelectionModel().select(authenticatedUserProperty.get());
            isRegularUser.set(true); // Disable bookUserComboBox via binding
            // Disable create user button (only admins can create users)
            createUserButton.setVisible(false);
            createUserButton.setManaged(false);
            // Disable update and delete user buttons
            updateUserButton.setVisible(false);
            updateUserButton.setManaged(false);
            deleteUserButton.setVisible(false);
            deleteUserButton.setManaged(false);
        } else {
            // Admins have full access
            isRegularUser.set(false);
        }
        refreshAll();
    }
    @FXML
    private void connectToDatabase() {
        try {
            dbConnection = dataSource.getConnection();
            if (!dbConnection.isClosed()) {
                isConnected.set(true);
                refreshAll();
                showSuccessAlert("Successfully connected to the database.");
            } else {
                showErrorAlert("Failed to connect to the database.");
            }
        } catch (SQLException e) {
            showErrorAlert("Error connecting to the database: " + e.getMessage());
        }
    }
    @FXML
    private void disconnectFromDatabase() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
                isConnected.set(false);
                userList.clear();
                bookList.clear();
                bookUserList.clear();
                conversationsList.clear();
                messagesList.clear();
                totalUsersLabel.setText("Total Users: 0");
                totalBooksLabel.setText("Total Books: 0");
                showSuccessAlert("Successfully disconnected from the database.");
            } else {
                showErrorAlert("No active connection to disconnect.");
            }
        } catch (SQLException e) {
            showErrorAlert("Error disconnecting from the database: " + e.getMessage());
        }
    }
    @FXML
    private void logout() throws IOException {
        // Clear authenticated user
        authenticatedUser = null;
        authenticatedUserProperty.set(null);

        // Clear any existing alerts or state
        Stage currentStage = (Stage) tabPane.getScene().getWindow();
        currentStage.close(); // Close the current window first

        // Load the login screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        fxmlLoader.setControllerFactory(applicationContext::getBean);
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);
        Stage stage = new Stage();
        stage.setTitle("Book Exchange System - Login");
        stage.setScene(scene);
        stage.show();
    }
    // User CRUD
    @FXML
    private void toggleCreateUserFields() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot create user: Database is disconnected.");
            return;
        }
        if (showCreateFields.get()) {
            // Fields are visible, so create the user
            createUser();
        } else {
            // Show the fields
            showCreateFields.set(true);
            createUserButton.setText("Submit User");
        }
    }
    private void createUser() {
        String username = userUsernameField.getText();
        String email = userEmailField.getText();
        String password = userPasswordField.getText();
        String firstName = userFirstNameField.getText();
        String lastName = userLastNameField.getText();
        String phone = userPhoneField.getText();
        LocalDate dateOfBirth = userDateOfBirthPicker.getValue();

        if (phone.isEmpty()) {
            phone = null;
        }

        try {
            userService.createUser(username, email, password, firstName, lastName, phone, dateOfBirth, User.Role.USER);
            refreshAll();
            clearUserFields();
            showCreateFields.set(false);
            createUserButton.setText("Create User");
            showSuccessAlert("User created successfully.");
        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }
    @FXML
    private void showUpdateUserWindow() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot update user: Database is disconnected.");
            return;
        }
        User selectedUser = userComboBox.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showErrorAlert("Please select a user.");
            return;
        }
        try {
            URL resourceUrl = getClass().getResource("/fxml/update-user.fxml");
            if (resourceUrl == null) {
                throw new IOException("Cannot find resource: /fxml/update-user.fxml");
            }
            FXMLLoader fxmlLoader = new FXMLLoader(resourceUrl);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(fxmlLoader.load(), 400, 400);
            UpdateUserController controller = fxmlLoader.getController();
            controller.setUser(selectedUser, this);
            Stage stage = new Stage();
            stage.setTitle("Update User: " + selectedUser.getUsername());
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error opening update user window: " + e.getMessage());
        }
    }
    @FXML
    private void deleteUser() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot delete user: Database is disconnected.");
            return;
        }
        User selectedUser = userComboBox.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showErrorAlert("Please select a user.");
            return;
        }
        try {
            userService.deleteUser(selectedUser.getUsername(), authenticatedUserProperty.get());
            refreshAll();
            clearUserFields();
            showSuccessAlert("User deleted successfully.");
        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }
    @FXML
    private void showUserDetails() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot show user details: Database is disconnected.");
            return;
        }
        User selectedUser = userComboBox.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showErrorAlert("Please select a user.");
            return;
        }
        User userWithBooks = userService.getUserWithBooks(selectedUser.getUsername());
        if (userWithBooks == null) {
            showErrorAlert("User not found.");
            return;
        }
        try {
            URL resourceUrl = getClass().getResource("/fxml/user-details.fxml");
            if (resourceUrl == null) {
                throw new IOException("Cannot find resource: /fxml/user-details.fxml");
            }
            FXMLLoader fxmlLoader = new FXMLLoader(resourceUrl);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(fxmlLoader.load(), 400, 500);
            UserDetailsController controller = fxmlLoader.getController();
            controller.setUser(userWithBooks, authenticatedUserProperty.get());
            Stage stage = new Stage();
            stage.setTitle("User Details: " + (authenticatedUserProperty.get().getRole() == User.Role.ADMIN ? userWithBooks.getUsername() : userWithBooks.getFirstName() + " " + userWithBooks.getLastName()));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error opening user details: " + e.getMessage());
        }
    }
    @FXML
    private void clearUserFields() {
        userUsernameField.clear();
        userEmailField.clear();
        userPasswordField.clear();
        userFirstNameField.clear();
        userLastNameField.clear();
        userPhoneField.clear();
        userDateOfBirthPicker.setValue(null);
        userComboBox.getSelectionModel().clearSelection();
        userTable.getSelectionModel().clearSelection();
        showCreateFields.set(false);
        createUserButton.setText("Create User");
    }
    // Book CRUD
    @FXML
    private void toggleCreateBookFields() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot create book: Database is disconnected.");
            return;
        }
        if (showCreateBookFields.get()) {
            // Fields are visible, so create the book
            createBook();
        } else {
            // Show the fields
            showCreateBookFields.set(true);
            createBookButton.setText("Submit Book");
        }
    }
    private void createBook() {
        String title = bookTitleField.getText().trim();
        String author = bookAuthorField.getText().trim();
        String status = bookStatusComboBox.getSelectionModel().getSelectedItem();
        User selectedUser = bookUserComboBox.getSelectionModel().getSelectedItem();

        try {
            bookService.createBook(title, author, status, selectedUser.getUsername());
            refreshAll();
            clearBookFields();
            showCreateBookFields.set(false);
            createBookButton.setText("Create Book");
            showSuccessAlert("Book created successfully.");
        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }
    @FXML
    private void showUpdateBookWindow() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot update book: Database is disconnected.");
            return;
        }
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showErrorAlert("Please select a book.");
            return;
        }
        try {
            URL resourceUrl = getClass().getResource("/fxml/update-book.fxml");
            if (resourceUrl == null) {
                throw new IOException("Cannot find resource: /fxml/update-book.fxml");
            }
            FXMLLoader fxmlLoader = new FXMLLoader(resourceUrl);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(fxmlLoader.load(), 400, 300);
            UpdateBookController controller = fxmlLoader.getController();
            controller.setBook(selectedBook, this);
            Stage stage = new Stage();
            stage.setTitle("Update Book: " + selectedBook.getTitle());
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error opening update book window: " + e.getMessage());
        }
    }
    @FXML
    private void readBook() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot read book: Database is disconnected.");
            return;
        }
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showErrorAlert("Please select a book.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Book Info");
        alert.setHeaderText(null);
        alert.setContentText("Title: " + selectedBook.getTitle() + "\nAuthor: " + selectedBook.getAuthor() + "\nStatus: " + selectedBook.getStatus());
        alert.showAndWait();
        // Record the transaction
        transactionService.createTransaction(selectedBook, authenticatedUserProperty.get(), "READ");
    }
    @FXML
    private void deleteBook() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot delete book: Database is disconnected.");
            return;
        }
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showErrorAlert("Please select a book.");
            return;
        }
        try {
            bookService.deleteBook(selectedBook.getTitle(), selectedBook.getAuthor(), authenticatedUserProperty.get());
            refreshAll();
            showSuccessAlert("Book deleted successfully.");
        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }
    @FXML
    private void rateBookOwner() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot rate owner: Database is disconnected.");
            return;
        }
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null || selectedBook.getUser() == null) {
            showErrorAlert("Please select a book with an owner.");
            return;
        }
        if (selectedBook.getUser().equals(authenticatedUserProperty.get())) {
            showErrorAlert("You cannot rate yourself.");
            return;
        }
        try {
            URL resourceUrl = getClass().getResource("/fxml/rate-user.fxml");
            if (resourceUrl == null) {
                throw new IOException("Cannot find resource: /fxml/rate-user.fxml");
            }
            FXMLLoader fxmlLoader = new FXMLLoader(resourceUrl);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(fxmlLoader.load(), 400, 300);
            RateUserController controller = fxmlLoader.getController();
            controller.setUsers(authenticatedUserProperty.get(), selectedBook.getUser());
            Stage stage = new Stage();
            stage.setTitle("Rate User: " + (authenticatedUserProperty.get().getRole() == User.Role.ADMIN ? selectedBook.getUser().getUsername() : selectedBook.getUser().getFirstName() + " " + selectedBook.getUser().getLastName()));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error opening rate user window: " + e.getMessage());
        }
    }
    @FXML
    private void messageBookOwner() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot message owner: Database is disconnected.");
            return;
        }
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null || selectedBook.getUser() == null) {
            showErrorAlert("Please select a book with an owner.");
            return;
        }
        if (selectedBook.getUser().equals(authenticatedUserProperty.get())) {
            showErrorAlert("You cannot message yourself.");
            return;
        }
        try {
            // Send an initial message to ensure the owner is added to conversations
            messageService.sendMessage(authenticatedUserProperty.get(), selectedBook.getUser(), "Hello! I'm interested in your book: " + selectedBook.getTitle());
            refreshConversations();

            // Find the matching user in conversationsList by ID to handle Hibernate object equality
            User owner = selectedBook.getUser();
            User targetUser = conversationsList.stream()
                    .filter(user -> user.getId().equals(owner.getId()))
                    .findFirst()
                    .orElse(null);

            if (targetUser == null) {
                System.out.println("Target user not found in conversationsList after refresh.");
                showErrorAlert("Failed to find the user in your conversations.");
                return;
            }

            // Open the Messages tab and select the owner
            tabPane.getSelectionModel().select(messagesTab);

            // Select the user in the conversationsListView
            conversationsListView.getSelectionModel().select(targetUser);

            // Force UI refresh to ensure the ListViews render correctly
            Platform.runLater(() -> {
                conversationsListView.refresh();
                messagesListView.refresh();
                messageInputArea.requestFocus();
                System.out.println("Selected user in conversationsListView: " +
                        (conversationsListView.getSelectionModel().getSelectedItem() != null ?
                                conversationsListView.getSelectionModel().getSelectedItem().getUsername() : "none"));
            });

        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }
    @FXML
    private void sendMessageInTab() {
        User selectedUser = conversationsListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showErrorAlert("Please select a user to send a message to.");
            return;
        }

        if (authenticatedUserProperty.get() == null) {
            showErrorAlert("You must be logged in to send a message.");
            return;
        }

        String content = messageInputArea.getText().trim();
        if (content.isEmpty()) {
            showErrorAlert("Message cannot be empty.");
            return;
        }

        try {
            messageService.sendMessage(authenticatedUserProperty.get(), selectedUser, content);
            messageInputArea.clear();
            displayConversation(selectedUser);
            refreshConversations();
            showSuccessAlert("Message sent successfully.");
        } catch (Exception e) {
            showErrorAlert("Failed to send message: " + e.getMessage());
        }
    }
    @FXML
    private void borrowBook() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot borrow book: Database is disconnected.");
            return;
        }
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null || selectedBook.getUser() == null) {
            showErrorAlert("Please select a book with an owner.");
            return;
        }
        if (selectedBook.getUser().equals(authenticatedUserProperty.get())) {
            showErrorAlert("You cannot borrow your own book.");
            return;
        }
        try {
            // Update book status to Borrowed
            bookService.updateBook(
                    borrow = true,
                    selectedBook.getTitle(),
                    selectedBook.getAuthor(),
                    selectedBook.getTitle(),
                    selectedBook.getAuthor(),
                    "Borrowed",
                    selectedBook.getUser() != null ? selectedBook.getUser().getUsername() : null,
                    authenticatedUserProperty.get()
            );
            // Record the transaction
            transactionService.createTransaction(selectedBook, authenticatedUserProperty.get(), "BORROW");

            refreshAll();
            clearBookFields(); // Clear selection to avoid repeated actions
            showSuccessAlert("Book borrowed successfully.");
        } catch (IllegalArgumentException e) {
            showErrorAlert(e.getMessage());
        }
    }
    @FXML
    private void viewTransactionHistory() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot view transaction history: Database is disconnected.");
            return;
        }
        try {
            URL resourceUrl = getClass().getResource("/fxml/transaction-history.fxml");
            if (resourceUrl == null) {
                throw new IOException("Cannot find resource: /fxml/transaction-history.fxml");
            }
            FXMLLoader fxmlLoader = new FXMLLoader(resourceUrl);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            TransactionHistoryController controller = fxmlLoader.getController();
            controller.setUser(authenticatedUserProperty.get());
            Stage stage = new Stage();
            stage.setTitle("Transaction History for " + (authenticatedUserProperty.get().getRole() == User.Role.ADMIN ? authenticatedUserProperty.get().getUsername() : authenticatedUserProperty.get().getFirstName() + " " + authenticatedUserProperty.get().getLastName()));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error opening transaction history: " + e.getMessage());
        }
    }
    @FXML
    private void viewComments() {
        if (!isConnected.get()) {
            showErrorAlert("Cannot view comments: Database is disconnected.");
            return;
        }
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showErrorAlert("Please select a book.");
            return;
        }
        try {
            URL resourceUrl = getClass().getResource("/fxml/comments.fxml");
            if (resourceUrl == null) {
                throw new IOException("Cannot find resource: /fxml/comments.fxml");
            }
            FXMLLoader fxmlLoader = new FXMLLoader(resourceUrl);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(fxmlLoader.load(), 450, 450);
            CommentController controller = fxmlLoader.getController();
            controller.setBookAndUser(selectedBook, authenticatedUserProperty.get());
            Stage stage = new Stage();
            stage.setTitle("Comments for: " + selectedBook.getTitle());
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error opening comments window: " + e.getMessage());
        }
    }
    @FXML
    private void clearBookFields() {
        bookTitleField.clear();
        bookAuthorField.clear();
        bookStatusComboBox.getSelectionModel().select("Available");
        bookUserComboBox.getSelectionModel().select(authenticatedUserProperty.get());
        bookTable.getSelectionModel().clearSelection();
        showCreateBookFields.set(false);
        createBookButton.setText("Create Book");
    }
    private void updateUserTable() {
        if (!isConnected.get()) {
            userList.clear();
            return;
        }
        String searchText = userSearchField.getText().trim().toLowerCase();
        List<User> users;
        if (searchText.isEmpty()) {
            users = userService.getAllUsers();
        } else {
            users = userService.searchUsersByUsername(searchText, authenticatedUserProperty.get());
        }
        userList.setAll(users);
    }
    private void updateBookTable() {
        if (!isConnected.get()) {
            bookList.clear();
            return;
        }
        String searchText = bookSearchField.getText().trim().toLowerCase();
        String selectedStatus = bookStatusFilter.getSelectionModel().getSelectedItem();
        List<Book> books;
        if (selectedStatus == null || selectedStatus.equals("All")) {
            books = searchText.isEmpty() ? bookService.getAllBooks() : bookService.searchBooksByTitle(searchText);
        } else {
            books = searchText.isEmpty() ? bookService.getBooksByStatus(selectedStatus) : bookService.searchBooksByTitleAndStatus(searchText, selectedStatus);
        }
        bookList.setAll(books);
    }
    private void updateUserComboBox() {
        if (!isConnected.get()) {
            userList.clear();
            return;
        }
        userList.setAll(userService.getAllUsers());
    }
    private void updateBookUserComboBox() {
        if (!isConnected.get()) {
            bookUserList.clear();
            return;
        }
        bookUserList.setAll(userService.getAllUsers());
    }
    private void updateStatistics() {
        if (!isConnected.get()) {
            totalUsersLabel.setText("Total Users: 0");
            totalBooksLabel.setText("Total Books: 0");
            totalAvailableBooksLabel.setText("Total Available books: 0");
            totalBorrowedBooksLabel.setText("Total borrowed books: 0");
            return;
        }
        long totalUsers = userService.getTotalUsers();
        long totalBooks = bookService.getTotalBooks();
        long totalAvailableBooks = bookService.countBooksByStatus("Available");
        long totalBorrowedBooks = bookService.countBooksByStatus("Borrowed");
        totalUsersLabel.setText("Total Users: " + totalUsers);
        totalBooksLabel.setText("Total Books: " + totalBooks);
        totalAvailableBooksLabel.setText("Total Available books: " + totalAvailableBooks);
        totalBorrowedBooksLabel.setText("Total borrowed books: " + totalBorrowedBooks);

    }
    @FXML
    private void returnBook() {
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showErrorAlert("No book selected.");
            return;
        }
        if (!"Borrowed".equals(selectedBook.getStatus())) {
            showErrorAlert("Only borrowed books can be returned.");
            return;
        }
        try {
            bookService.returnBook(selectedBook, authenticatedUserProperty.get());
            refreshAll();
            showSuccessAlert("Book returned successfully.");
        } catch (IllegalStateException e) {
            showErrorAlert(e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Error returning book: " + e.getMessage());
        }
    }
    private void displayConversation(User otherUser) {
        System.out.println("Displaying conversation with: " + otherUser.getUsername());
        messagesList.clear();
        List<Message> messages = messageService.getMessagesBetweenUsers(authenticatedUserProperty.get(), otherUser);
        messagesList.addAll(messages);

        // Mark messages as read
        messageService.markMessagesAsRead(otherUser, authenticatedUserProperty.get());

        String displayName = authenticatedUserProperty.get().getRole() == User.Role.ADMIN ?
                otherUser.getUsername() :
                otherUser.getFirstName() + " " + otherUser.getLastName();
        conversationLabel.setText("Conversation with: " + displayName);

        // Scroll to the latest message only if there are messages
        if (!messages.isEmpty()) {
            messagesListView.scrollTo(messagesList.size() - 1);
            messagesListView.getSelectionModel().select(messagesList.size() - 1);
        }

        // Force refresh of messagesListView
        messagesListView.refresh();
    }
    public void refreshConversations() {
        // Store the currently selected user
        User selectedUser = conversationsListView.getSelectionModel().getSelectedItem();

        conversationsList.clear();
        List<User> partners = messageService.getConversationPartners(authenticatedUserProperty.get());
        conversationsList.addAll(partners);

        if (!partners.isEmpty()) {
            // If the previously selected user is still in the list, re-select them
            if (selectedUser != null && partners.contains(selectedUser)) {
                conversationsListView.getSelectionModel().select(selectedUser);
            } else {
                // Otherwise, select the first user
                conversationsListView.getSelectionModel().selectFirst();
            }
        } else {
            messagesList.clear();
            conversationLabel.setText("Select a user to view conversation");
        }

        // Force refresh of conversationsListView
        conversationsListView.refresh();
    }
    public void refreshAll() {
        updateUserTable();
        updateBookTable();
        updateUserComboBox();
        updateBookUserComboBox();
        updateStatistics();
        refreshConversations();
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
