package com.rhis.solutions.services;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final Map<String, String> tokenToUsername = new ConcurrentHashMap<>();

    public String createToken(String username) {
        String token = UUID.randomUUID().toString();
        tokenToUsername.put(token, username);
        return token;
    }
    public String getUsernameForToken(String token) {
        return tokenToUsername.get(token);
    }
}

