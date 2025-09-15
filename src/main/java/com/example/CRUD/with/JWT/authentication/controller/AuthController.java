package com.example.CRUD.with.JWT.authentication.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.CRUD.with.JWT.authentication.model.User;
import com.example.CRUD.with.JWT.authentication.repository.UserRepository;
import com.example.CRUD.with.JWT.authentication.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // -------------------- LOGIN --------------------
    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String username, @RequestParam String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserDetails user = (UserDetails) auth.getPrincipal();
        String role = user.getAuthorities().iterator().next().getAuthority();
        String token = jwtUtil.generateToken(user.getUsername(), role);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", role);
        return response;
    }

    // -------------------- USER REGISTER --------------------
    @PostMapping("/register")
    public Map<String, String> register(@RequestParam String username,
                                        @RequestParam String password,
                                        @RequestParam(defaultValue = "ROLE_USER") String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(role);

        userRepository.save(newUser);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("role", newUser.getRole());
        return response;
    }

    // -------------------- ADMIN REGISTER --------------------
    @PostMapping("/register/admin")
    public Map<String, String> registerAdmin(@RequestParam String username,
                                             @RequestParam String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Admin username already exists!");
        }

        User newAdmin = new User();
        newAdmin.setUsername(username);
        newAdmin.setPassword(passwordEncoder.encode(password));
        newAdmin.setRole("ROLE_ADMIN");

        userRepository.save(newAdmin);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Admin registered successfully");
        response.put("role", newAdmin.getRole());
        return response;
    }
}
