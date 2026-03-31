package com.example.estore.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.estore.model.*;
import com.example.estore.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload) {

        try {
          
            if (!payload.containsKey("userId")) {
                return ResponseEntity.badRequest().body("User ID is required");
            }

            Long userId = Long.valueOf(payload.get("userId").toString());

          
            Object itemsObj = payload.get("items");
            if (!(itemsObj instanceof List)) {
                return ResponseEntity.badRequest().body("Items must be a list");
            }

            List<?> rawItems = (List<?>) itemsObj;
            if (rawItems.isEmpty()) {
                return ResponseEntity.badRequest().body("Order items cannot be empty");
            }

            List<OrderItem> items = new ArrayList<>();

            for (Object obj : rawItems) {

                if (!(obj instanceof Map)) {
                    return ResponseEntity.badRequest().body("Invalid item format");
                }

                Map<?, ?> itemMap = (Map<?, ?>) obj;

           
                Object productIdObj = itemMap.get("productId");
                Object quantityObj = itemMap.get("quantity");
                Object priceObj = itemMap.get("price");

                if (productIdObj == null || quantityObj == null || priceObj == null) {
                    return ResponseEntity.badRequest().body("Missing productId/quantity/price");
                }

                Long productId = Long.valueOf(productIdObj.toString());
                Integer quantity = Integer.valueOf(quantityObj.toString());
                Double price = Double.valueOf(priceObj.toString());

              
                if (quantity <= 0) {
                    return ResponseEntity.badRequest().body("Quantity must be greater than 0");
                }

                if (price <= 0) {
                    return ResponseEntity.badRequest().body("Price must be greater than 0");
                }

                
                OrderItem item = new OrderItem();
                Product product = new Product();
                product.setId(productId);

                item.setProduct(product);
                item.setQuantity(quantity);
                item.setPrice(price);

                items.add(item);
            }

            Order order = orderService.createOrder(userId, items);
            return ResponseEntity.ok(order);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request format");
        }
    }

   
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

   
    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

   
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok("Order deleted successfully");
    }
}