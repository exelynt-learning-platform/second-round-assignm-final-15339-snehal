package com.example.estore.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.estore.enums.Category;
import com.example.estore.model.Product;
import com.example.estore.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product add(Product product) {
        return productRepository.save(product);
    }

    public Product update(Long id, Product updatedProduct) {
        Product product = getById(id);
        product.setTitle(updatedProduct.getTitle());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setImage(updatedProduct.getImage());
        product.setCategory(updatedProduct.getCategory());
        product.setRating(updatedProduct.getRating());
        product.setStock(updatedProduct.getStock());
        return productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }
    
   
    public List<Product> getByCategory(String category) {
        try {
            Category cat = Category.valueOf(category.toUpperCase());
            return productRepository.findByCategory(cat);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
    }
    
    public List<Product> searchProducts(String query) {
        return productRepository.findByTitleContainingIgnoreCase(query);
    }

}

