package com.example.estore.controller;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PaymentController {

    private RazorpayClient razorpayClient;

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

 
    @PostConstruct
    public void init() throws Exception {
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {

        try {
            JSONObject options = new JSONObject();

            int amount = (int) data.get("amount");

            options.put("amount", amount * 100); // ₹ → paise
            options.put("currency", "INR");

            Order order = razorpayClient.orders.create(options);

            return ResponseEntity.ok(order.toString());

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Payment failed: " + e.getMessage());
        }
    }
}