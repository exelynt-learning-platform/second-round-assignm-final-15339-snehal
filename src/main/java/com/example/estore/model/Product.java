package com.example.estore.model;

import com.example.estore.enums.Category;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   
    @NotBlank(message = "Title is required")
    private String title;

   
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    
    @Column(length = 1000)
    private String description;

   
    private String image;

   
    @Embedded
    private Rating rating;

   
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private int stock;

   
    @Enumerated(EnumType.STRING)
    private Category category;
}