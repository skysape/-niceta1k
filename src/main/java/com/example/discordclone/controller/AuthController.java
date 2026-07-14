package com.example.discordclone.controller;

import com.example.discordclone.model.User;
import com.example.discordclone.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String login = request.get("login");
            String password = request.get("password");
            String email = request.get("email");
            String nickname = request.get("nickname");

            User user = userService.registerUser(login, password, email, nickname);
            return ResponseEntity.ok(Map.of(
                "message", "Registration successful. Verify code sent to email.",
                "email", user.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        boolean verified = userService.verifyEmail(email, code);
        if (verified) {
            return ResponseEntity.ok(Map.of("message", "Email verified successfully!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification code."));
        }
    }
}