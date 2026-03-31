package com.example.estore.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.estore.enums.Role;
import com.example.estore.model.Product;
import com.example.estore.model.User;
import com.example.estore.repository.ProductRepository;
import com.example.estore.repository.UserRepository;
import com.example.estore.util.JwtUtil;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private JwtUtil jwtUtil;

   
    private boolean isAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring(7);
        try {
            String role = jwtUtil.extractRole(token);
            return "ADMIN".equalsIgnoreCase(role);
        } catch (Exception e) {
            return false;
        }
    }

 
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Access denied! Admins only.");
        }
        List<User> users = userRepo.findAll();
        return ResponseEntity.ok(users);
    }


    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts(@RequestHeader("Authorization") String authHeader) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Access denied! Admins only.");
        }
        List<Product> products = productRepo.findAll();
        return ResponseEntity.ok(products);
    }

  
    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Access denied! Admins only.");
        }

        Optional<User> userOpt = userRepo.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found!");
        }

        userRepo.deleteById(id);
        return ResponseEntity.ok("User deleted successfully by admin!");
    }

    
    @PutMapping("/update-role/{id}")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {

        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Access denied! Admins only.");
        }

        Optional<User> userOpt = userRepo.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found!");
        }

        User user = userOpt.get();
        String newRoleStr = request.get("role");
        try {
            Role newRole = Role.valueOf(newRoleStr.toUpperCase());
            user.setRole(newRole);
            userRepo.save(user);
            return ResponseEntity.ok("User role updated successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role provided!");
        }
    }
}