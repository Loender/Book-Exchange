package com.example.lab12dub2.service;
import com.example.lab12dub2.model.*;
import com.example.lab12dub2.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
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
}
