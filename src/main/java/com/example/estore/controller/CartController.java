package com.example.estore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.estore.model.CartItem;
import com.example.estore.service.CartService;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class CartController {

    @Autowired
    private CartService cartService;

    
    @PostMapping("/add")
    public CartItem addToCart(@RequestBody CartItem item) {
        return cartService.addToCart(item);
    }

   
    @GetMapping
    public List<CartItem> getCart(@RequestParam Long userId) {
        return cartService.getCart(userId);
    }

 
    @DeleteMapping("/clear/{userId}")
    public void clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
    }
    
    @PutMapping("/{itemId}")
    public CartItem updateQuantity(@PathVariable Long itemId, @RequestBody CartItem updatedItem) {
        return cartService.updateQuantity(itemId, updatedItem.getQuantity());
    }
    
    @DeleteMapping("/{itemId}")
    public void removeItem(@PathVariable Long itemId) {
        cartService.removeItem(itemId);
    }
    
    @GetMapping("/count/{userId}")
    public ResponseEntity<Integer> getCartCount(@PathVariable Long userId) {
        int count = cartService.getCartCountByUserId(userId);
        return ResponseEntity.ok(count);
    }

}
