package com.example.estore.model;

import com.example.estore.enums.Category;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ DB constraints instead of validation
    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private Double price;

    @Column(length = 1000)
    private String description;

    private String image;

    @Embedded
    private Rating rating;

    @Column(nullable = false)
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
}