package com.example.lab12dub2.repository;

import com.example.lab12dub2.model.Message;
import com.example.lab12dub2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.timestamp ASC")
    List<Message> findMessagesBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.receiver = :user UNION SELECT DISTINCT m.receiver FROM Message m WHERE m.sender = :user")
    List<User> findConversationPartners(@Param("user") User user);

    @Transactional
    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.sender = :sender AND m.receiver = :receiver AND m.read = false")
    void markMessagesAsRead(@Param("sender") User sender, @Param("receiver") User receiver);
}