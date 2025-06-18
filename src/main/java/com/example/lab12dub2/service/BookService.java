package com.example.lab12dub2.service;
import com.example.lab12dub2.model.*;
import com.example.lab12dub2.repository.BookRepository;
import com.example.lab12dub2.repository.TransactionRepository;
import com.example.lab12dub2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private TransactionRepository transactionRepository;
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

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> getBooksByStatus(String status) {
        return bookRepository.findAll().stream()
                .filter(book -> book.getStatus().equals(status))
                .toList();
    }

    public List<Book> searchBooksByTitle(String title) {
        return bookRepository.findAll().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .toList();
    }

    public List<Book> searchBooksByTitleAndStatus(String title, String status) {
        return bookRepository.findAll().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()) && book.getStatus().equals(status))
                .toList();
    }

    public long getTotalBooks() {
        return bookRepository.count();
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
