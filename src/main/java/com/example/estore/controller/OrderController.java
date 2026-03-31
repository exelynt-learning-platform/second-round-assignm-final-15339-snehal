package com.example.estore.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
    public Order createOrder(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) payload.get("items");

        List<OrderItem> items = itemList.stream().map(itemMap -> {
            OrderItem item = new OrderItem();
            Product product = new Product();
            product.setId(Long.valueOf(itemMap.get("productId").toString()));
            item.setProduct(product);
            item.setQuantity((Integer) itemMap.get("quantity"));
            item.setPrice(Double.valueOf(itemMap.get("price").toString()));
            return item;
        }).collect(Collectors.toList());

        return orderService.createOrder(userId, items);
    }

    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable Long userId) {
        return orderService.getOrdersByUser(userId);
    }

    @GetMapping("/all")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }
    
    @DeleteMapping("/{orderId}")
    public String deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return "Order deleted successfully.";
    }

}
