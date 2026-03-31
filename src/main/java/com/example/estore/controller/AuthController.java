package com.example.estore.controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.estore.dto.*;
import com.example.estore.enums.Role;
import com.example.estore.model.User;
import com.example.estore.repository.UserRepository;
import com.example.estore.util.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    // ✅ Admin email from env
    @Value("${app.admin.email}")
    private String adminEmail;

    // ✅ Reusable token method
    private String generateToken(User user) {
        String role = (user.getRole() != null) ? user.getRole().name() : "USER";
        return jwtUtil.generateToken(user.getEmail(), user.getName(), role);
    }

    // ---------------- SIGNUP ----------------
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {

        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Email and password are required!");
        }

        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(new AuthResponse("Email already exists!", false));
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // ✅ No hardcoded email
        if (request.getEmail().equalsIgnoreCase(adminEmail)) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER);
        }

        userRepo.save(user);

        String token = generateToken(user);

        return ResponseEntity.ok(
                new AuthResponse("User registered successfully!", true, token,
                        user.getRole().name(), user.getName(), user.getId())
        );
    }

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Optional<User> userOpt = userRepo.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse("User not found!", false));
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse("Invalid credentials!", false));
        }

        String token = generateToken(user);

        return ResponseEntity.ok(
                new AuthResponse("Login successful!", true, token,
                        user.getRole().name(), user.getName(), user.getId())
        );
    }

    // ---------------- GET CURRENT USER ----------------
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Invalid token");
        }

        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }
    }

    // ---------------- UPDATE USER ----------------
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody UpdateUserRequest updates) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updates.getName() != null) user.setName(updates.getName());
        if (updates.getEmail() != null) user.setEmail(updates.getEmail());
        if (updates.getPhone() != null) user.setPhone(updates.getPhone());
        if (updates.getAddress() != null) user.setAddress(updates.getAddress());

        if (updates.getPassword() != null && !updates.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updates.getPassword()));
        }

        userRepo.save(user);
        return ResponseEntity.ok("User updated successfully!");
    }

    // ---------------- DELETE USER ----------------
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepo.delete(user);
        return ResponseEntity.ok("User deleted successfully!");
    }

    // ---------------- FORGOT PASSWORD ----------------
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {

        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required!");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = String.format("%06d", new Random().nextInt(999999));

        user.setResetOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepo.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP is: " + otp);

        mailSender.send(message);

        return ResponseEntity.ok("OTP sent successfully!");
    }

    // ---------------- RESET PASSWORD ----------------
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp)) {
            return ResponseEntity.badRequest().body("Invalid OTP!");
        }

        // ✅ FIXED NPE
        if (user.getOtpExpiry() == null ||
                user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("OTP expired!");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password cannot be empty!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setOtpExpiry(null);

        userRepo.save(user);

        return ResponseEntity.ok("Password reset successfully!");
    }
}