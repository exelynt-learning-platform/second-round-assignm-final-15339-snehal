package com.example.estore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.estore.model.CartItem;
import com.example.estore.model.Product;
import com.example.estore.model.User;
import com.example.estore.repository.CartRepository;
import com.example.estore.repository.ProductRepository;
import com.example.estore.repository.UserRepository;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public CartItem addToCart(CartItem item) {
        if (item.getUser() == null || item.getUser().getId() == null) {
            throw new RuntimeException("User is required when adding to cart");
        }

        if (item.getProduct() == null || item.getProduct().getId() == null) {
            throw new RuntimeException("Product is required when adding to cart");
        }

        User user = userRepository.findById(item.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(item.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

       
        List<CartItem> existingItems = cartRepository.findByUserId(user.getId());
        for (CartItem existing : existingItems) {
            if (existing.getProduct().getId().equals(product.getId())) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                return cartRepository.save(existing);
            }
        }

        item.setUser(user);
        item.setProduct(product);
        return cartRepository.save(item);
    }

    public List<CartItem> getCart(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }
    
    
    public CartItem updateQuantity(Long itemId, int quantity) {
        CartItem item = cartRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setQuantity(quantity);
        return cartRepository.save(item);
    }

    
    public void removeItem(Long itemId) {
        cartRepository.deleteById(itemId);
    }
    
    public int getCartCountByUserId(Long userId) {
        return cartRepository.countByUserId(userId);
    }

}
