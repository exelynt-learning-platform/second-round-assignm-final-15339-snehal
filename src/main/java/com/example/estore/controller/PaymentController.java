package com.example.estore.controller;



import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") 
public class PaymentController {

    private RazorpayClient razorpayClient;

 
    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;



    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {

        try {
            JSONObject options = new JSONObject();
            options.put("amount", data.get("amount"));

            Order order = razorpayClient.orders.create(options);

            return ResponseEntity.ok(order.toString());

        } catch (Exception e) {

         
            return ResponseEntity.ok("Payment failed");  
            
        }
    }

   
    public static class OrderRequest {
        private int amount;

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }
}

