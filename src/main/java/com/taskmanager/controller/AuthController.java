package com.taskmanager.controller;

import com.taskmanager.config.JwtUtil;
import com.taskmanager.model.User;
import com.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> request) {
        try {
            String role = request.getOrDefault("role", "MEMBER").toUpperCase();
            User user = userService.registerUser(
                    request.get("name"),
                    request.get("email"),
                    request.get("password"),
                    User.Role.valueOf(role)
            );
            String token = jwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString(),
                    "id", user.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.get("email"), request.get("password")));
            User user = userService.findByEmail(request.get("email"));
            String token = jwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString(),
                    "id", user.getId()
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
        }
    }
}