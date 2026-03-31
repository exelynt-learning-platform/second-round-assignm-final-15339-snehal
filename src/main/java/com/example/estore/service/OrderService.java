package com.example.estore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.estore.model.Order;
import com.example.estore.model.OrderItem;
import com.example.estore.model.User;
import com.example.estore.repository.OrderItemRepository;
import com.example.estore.repository.OrderRepository;
import com.example.estore.repository.ProductRepository;
import com.example.estore.repository.UserRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

  
    public Order createOrder(Long userId, List<OrderItem> items) {
    
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

      
        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(total);
        order = orderRepository.save(order);

        
        for (OrderItem item : items) {
            item.setOrder(order);
            orderItemRepository.save(item);
        }

        return order; 
    }


    public List<Order> getOrdersByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUser(user);
    }

   
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

}
