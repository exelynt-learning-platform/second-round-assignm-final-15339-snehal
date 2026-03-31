package com.example.estore.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.estore.dto.AuthResponse;
import com.example.estore.dto.LoginRequest;
import com.example.estore.dto.SignupRequest;
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
   
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(new AuthResponse("Email already exists!", false));
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getEmail().equalsIgnoreCase("tikolesnehal@gmail.com")) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER);
        }

        userRepo.save(user);
        String token = jwtUtil.generateToken(
        	    user.getEmail(),
        	    user.getName(),
        	    user.getRole().name()
        	);
return ResponseEntity.ok(
            new AuthResponse("User registered successfully!", true, token, user.getRole().name(), user.getName(),user.getId())
        );
    }


 
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new AuthResponse("Invalid credentials!", false, null, null, null, null));
        }

        String token = jwtUtil.generateToken(
        	    user.getEmail(),
        	    user.getName(),
        	    user.getRole().name()
        	);
 return ResponseEntity.ok(
                new AuthResponse(
                        "Login successful!",
                        true,
                        token,
                        user.getRole().name(),
                        user.getName(),   
                        user.getId()        
                )
        );
    }



    // Get current user using token (no service/filter)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String email;

        try {
            email = jwtUtil.extractEmail(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }

        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        return ResponseEntity.ok(userOpt.get());
    }
    

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepo.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found!");
        }

        userRepo.deleteById(id);
        return ResponseEntity.ok("User deleted successfully!");
    }
    
//    Update User
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<User> userOpt = userRepo.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found!");
        }

        User user = userOpt.get();

        if (updates.containsKey("name")) user.setName((String) updates.get("name"));
        if (updates.containsKey("email")) user.setEmail((String) updates.get("email"));
        if (updates.containsKey("phone")) user.setPhone((String) updates.get("phone"));
        if (updates.containsKey("address")) user.setAddress((String) updates.get("address"));

        if (updates.containsKey("password")) {
            String newPassword = (String) updates.get("password");
            if (newPassword != null && !newPassword.isEmpty()) {
                user.setPassword(passwordEncoder.encode(newPassword));
            }
        }

        userRepo.save(user);
        return ResponseEntity.ok("User updated successfully!");
    }


    

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> userOpt = userRepo.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found!");
        }

        User user = userOpt.get();

     
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setResetOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10)); // expires in 10 min
        userRepo.save(user);

      
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("eStore Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp + "\nIt will expire in 10 minutes.");
        mailSender.send(message);

        return ResponseEntity.ok("OTP sent successfully to your email!");
    }
    

    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found!");
        }

        User user = userOpt.get();

        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp)) {
            return ResponseEntity.badRequest().body("Invalid OTP!");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("OTP has expired!");
        }

 
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setOtpExpiry(null);
        userRepo.save(user);

        return ResponseEntity.ok("Password reset successfully!");
    }



}
