package com.example.lab12dub2.service;

import com.example.lab12dub2.model.*;
import com.example.lab12dub2.repository.BookRepository;
import com.example.lab12dub2.repository.MessageRepository;
import com.example.lab12dub2.repository.RatingRepository;
import com.example.lab12dub2.repository.CommentRepository;
import com.example.lab12dub2.repository.TransactionRepository;
import com.example.lab12dub2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class BookExchangeService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // User Operations
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username);
    }

    public void createUser(String username, String email, String password, String firstName, String lastName, String phone, LocalDate dateOfBirth, User.Role role) {
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists.");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setDateOfBirth(dateOfBirth);
        user.setRole(role);
        userRepository.save(user);
    }

    public void updateUser(String oldUsername, String newUsername, String email, String firstName, String lastName, String phone, LocalDate dateOfBirth, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can update users.");
        }
        User user = userRepository.findByUsername(oldUsername);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (!oldUsername.equals(newUsername) && userRepository.findByUsername(newUsername) != null) {
            throw new IllegalArgumentException("New username already exists.");
        }
        user.setUsername(newUsername);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setDateOfBirth(dateOfBirth);
        userRepository.save(user);
    }

    public void deleteUser(String username, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can delete users.");
        }
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (user.getRole() == User.Role.ADMIN && currentUser.getUsername().equals(username)) {
            throw new IllegalArgumentException("Admins cannot delete themselves.");
        }
        userRepository.delete(user);
    }

    public User getUserWithBooks(String username) {
        return userRepository.findByUsernameWithBooks(username);
    }

    public List<User> getAllUsers(User currentUser) {
        return userRepository.findAll();
    }

    public List<User> searchUsersByUsername(String username, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            return userRepository.findAll().stream()
                    .filter(user -> user.getFirstName().toLowerCase().contains(username.toLowerCase()) ||
                            user.getLastName().toLowerCase().contains(username.toLowerCase()))
                    .toList();
        }
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    public long getTotalUsers(User currentUser) {
        return userRepository.count();
    }

    // Book Operations
    public void createBook(String title, String author, String status, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (bookRepository.findByTitleAndAuthor(title, author) != null) {
            throw new IllegalArgumentException("Book already exists.");
        }
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setStatus(status);
        book.setUser(user);
        bookRepository.save(book);
    }

    public void updateBook(Boolean Borrow, String oldTitle, String oldAuthor, String newTitle, String newAuthor, String status, String ownerUsername, User currentUser) {
        Book book = bookRepository.findByTitleAndAuthor(oldTitle, oldAuthor);
        if (book == null) {
            throw new IllegalArgumentException("Book not found.");
        }
        if ((currentUser.getRole() != User.Role.ADMIN && (book.getUser() == null || !book.getUser().equals(currentUser))) && !Borrow) {
            throw new IllegalArgumentException("You can only update your own books.");
        }
        User newOwner = ownerUsername != null ? userRepository.findByUsername(ownerUsername) : null;
        if (ownerUsername != null && newOwner == null) {
            throw new IllegalArgumentException("New owner not found.");
        }
        book.setTitle(newTitle);
        book.setAuthor(newAuthor);
        book.setStatus(status);
        book.setUser(newOwner);
        bookRepository.save(book);
    }

    public void deleteBook(String title, String author, User currentUser) {
        Book book = bookRepository.findByTitleAndAuthor(title, author);
        if (book == null) {
            throw new IllegalArgumentException("Book not found.");
        }
        if (currentUser.getRole() != User.Role.ADMIN && (book.getUser() == null || !book.getUser().equals(currentUser))) {
            throw new IllegalArgumentException("You can only delete your own books.");
        }
        List<Transaction> transactions = transactionRepository.findByBook(book);
        for (Transaction transaction : transactions) {
            deleteTransaction(transaction.getId(), currentUser);
        }
        bookRepository.delete(book);
    }

    public void deleteTransaction(long id, User currentUser)
    {
        if (currentUser.getRole() != User.Role.ADMIN)
            throw new IllegalArgumentException("Not allowed to delete this transaction.");
        transactionRepository.deleteById(id);
    }

    public List<Book> getAllBooks(User currentUser) {
        return bookRepository.findAll();
    }

    public List<Book> getBooksByStatus(String status, User currentUser) {
        return bookRepository.findAll().stream()
                .filter(book -> book.getStatus().equals(status))
                .toList();
    }

    public List<Book> searchBooksByTitle(String title, User currentUser) {
        return bookRepository.findAll().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .toList();
    }

    public List<Book> searchBooksByTitleAndStatus(String title, String status, User currentUser) {
        return bookRepository.findAll().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()) && book.getStatus().equals(status))
                .toList();
    }

    public long getTotalBooks(User currentUser) {
        return bookRepository.count();
    }

    // Rating Operations
    public void addRating(User reviewer, User reviewedUser, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        Rating ratingEntity = new Rating();
        ratingEntity.setReviewer(reviewer);
        ratingEntity.setReviewedUser(reviewedUser);
        ratingEntity.setRating(rating);
        ratingEntity.setComment(comment);
        ratingRepository.save(ratingEntity);
    }

    public List<Rating> getRatingsForUser(User user) {
        return ratingRepository.findByReviewedUser(user);
    }

    // Message Operations
    public void sendMessage(User sender, User receiver, String content) {
        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("You cannot send a message to yourself.");
        }
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false); // Set as unread by default
        messageRepository.save(message);
    }

    public List<Message> getMessagesBetweenUsers(User user1, User user2) {
        return messageRepository.findMessagesBetweenUsers(user1, user2);
    }

    public List<User> getConversationPartners(User user) {
        return messageRepository.findConversationPartners(user);
    }

    @Transactional
    public void markMessagesAsRead(User sender, User receiver) {
        messageRepository.markMessagesAsRead(sender, receiver);
    }

    // Transaction Operations
    public void createTransaction(Book book, User borrower, String type) {
        Transaction transaction = new Transaction();
        transaction.setBook(book);
        transaction.setBorrower(borrower);
        transaction.setOwner(book.getUser());
        transaction.setType(type);
        transaction.setDate(LocalDate.now());
        transactionRepository.save(transaction);
    }

    public List<Transaction> getUserTransactions(User user) {
        return transactionRepository.findByBorrowerOrOwner(user, user);
    }

    // Comment Operations
    public List<Comment> getCommentsForBook(Book book) {
        List<Comment> topLevelComments = commentRepository.findByBookAndParentCommentIsNull(book);
        topLevelComments.forEach(this::loadReplies);
        return topLevelComments;
    }

    private void loadReplies(Comment comment) {
        List<Comment> replies = commentRepository.findByParentComment(comment);
        comment.setReplies(replies);
        replies.forEach(this::loadReplies);
    }

    public Comment createComment(Book book, User user, String content, Comment parentComment) {
        if (book == null || user == null || content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Book, user, and content are required to create a comment.");
        }
        Comment comment = new Comment(book, user, content, parentComment);
        return commentRepository.save(comment);
    }

    public long countBooksByStatus(String status) {
        return bookRepository.countByStatus(status);
    }

    public void returnBook(Book book, User authenticatedUser) {
        if (!"Borrowed".equals(book.getStatus())) {
            throw new IllegalStateException("Only borrowed books can be returned.");
        }

        if (authenticatedUser.getRole() != User.Role.ADMIN && !book.getUser().equals(authenticatedUser)) {
            throw new IllegalStateException("You did not borrow this book!");
        }
        book.setStatus("Available");
        bookRepository.save(book);
    }
}