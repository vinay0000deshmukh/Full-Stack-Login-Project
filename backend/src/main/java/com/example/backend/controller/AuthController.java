package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Inject UserRepository + PasswordEncoder
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- LOGIN ----------
        @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        return userRepository.findByUsername(username)
                .map(user -> {
                    String stored = user.getPassword();
                    System.out.println(">> Raw password: " + password);
                    System.out.println(">> Stored hash: " + stored);
                    System.out.println(">> Stored length: " + stored.length());
                    System.out.println(">> Matches? " + passwordEncoder.matches(password, stored));

                    if (passwordEncoder.matches(password, stored)) {
                        return ResponseEntity.ok(Map.of(
                                "status", "success",
                                "message", "Welcome " + username
                        ));
                    } else {
                        return ResponseEntity.status(401).body(Map.of(
                                "status", "error",
                                "message", "Invalid credentials"
                        ));
                    }
                })
                .orElseGet(() -> ResponseEntity.status(401).body(
                        Map.of("status", "error", "message", "Invalid credentials")
                ));
    }


    // ---------- REGISTER (create user with hashed password) ----------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", "error", "message", "username and password required")
            );
        }

        // Check if user already exists
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", "error", "message", "Username already taken")
            );
        }

        // Encode the password before saving
        String encoded = passwordEncoder.encode(password);
        User user = new User(username, encoded);
        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("status", "success", "message", "User registered successfully")
        );
    }

    // ---------- Optional: create a test user via endpoint ----------
   @PostMapping("/create-test-user")
        public ResponseEntity<?> createTestUser() {
            String username = "alice";
            String plain = "password123";

            String encoded = passwordEncoder.encode(plain);
            System.out.println("=== Creating test user ===");
            System.out.println("Plain: " + plain);
            System.out.println("Encoded: " + encoded);
            System.out.println("Matches here? " + passwordEncoder.matches(plain, encoded));

            User user = userRepository.findByUsername(username).orElse(new User());
            user.setUsername(username);
            user.setPassword(encoded);

            userRepository.save(user);

            return ResponseEntity.ok(
                    Map.of("status", "ok", "message", "test user created/updated with BCrypt password")
            );
        }

}
