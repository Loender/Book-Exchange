package com.example.lab12dub2.repository;

import com.example.lab12dub2.model.Book;
import com.example.lab12dub2.model.Transaction;
import com.example.lab12dub2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBorrowerOrOwner(User borrower, User owner);
    List<Transaction> findByBook(Book book);
}