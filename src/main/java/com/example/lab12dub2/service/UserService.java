package com.example.lab12dub2.service;

import com.example.lab12dub2.model.*;
import com.example.lab12dub2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

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

    public List<User> getAllUsers() {
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
        public long getTotalUsers() {
        return userRepository.count();
    }
}
