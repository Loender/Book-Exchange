package com.example.lab12dub2.service;
import com.example.lab12dub2.model.*;
import com.example.lab12dub2.repository.MessageRepository;
import com.example.lab12dub2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageRepository messageRepository;
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
}
