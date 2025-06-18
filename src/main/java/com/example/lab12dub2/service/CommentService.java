package com.example.lab12dub2.service;
import com.example.lab12dub2.model.*;
import com.example.lab12dub2.repository.BookRepository;
import com.example.lab12dub2.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private BookRepository bookRepository;
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
