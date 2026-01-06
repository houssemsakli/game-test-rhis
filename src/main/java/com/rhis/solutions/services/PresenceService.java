package com.rhis.solutions.services;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public void userConnected(String username) {
        if (username != null) {
            onlineUsers.add(username);
        }
    }

    public void userDisconnected(String username) {
        if (username != null) {
            onlineUsers.remove(username);
        }
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers;
    }
}