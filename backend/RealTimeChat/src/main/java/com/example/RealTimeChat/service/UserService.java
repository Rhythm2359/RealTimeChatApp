package com.example.RealTimeChat.service;

import com.example.RealTimeChat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Check if username exists
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    // Update online/offline status
    public void setUserOnlineStatus(String username, boolean isOnline) {
        userRepository.updateUserOnlineStatus(username, isOnline);
    }
}
