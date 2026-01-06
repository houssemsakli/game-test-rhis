package com.rhis.solutions.controllers;

import com.rhis.solutions.entities.User;
import com.rhis.solutions.repositories.UserRepository;
import com.rhis.solutions.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private AuthService authService;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public Map<String,Object> register(@RequestBody Map<String,String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String display = body.getOrDefault("displayName", username);
        if (userRepo.findByUsername(username).isPresent()) {
            return Map.of("error","username_exists");
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(encoder.encode(password));
        u.setDisplayName(display);
        userRepo.save(u);
        return Map.of("ok", true);
    }

    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody Map<String,String> body) {
        String username = body.get("username"), password = body.get("password");
        var uOpt = userRepo.findByUsername(username);
        if (uOpt.isEmpty()) return Map.of("error","bad_credentials");
        var u = uOpt.get();
        if (!encoder.matches(password, u.getPassword())) {
            return Map.of("error","bad_credentials");
        }
        String token = authService.createToken(username);
        return Map.of("token", token, "userId", u.getId(), "displayName", u.getDisplayName());
    }
}