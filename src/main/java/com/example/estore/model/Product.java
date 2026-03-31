package com.example.estore.model;

import com.example.estore.enums.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private String title;
    private Double price;

    @Column(length = 1000)
    private String description;

   
    private String image;

    @Embedded
    private Rating rating;
    
    @Column(nullable = false)
    private int stock;
    
    @Enumerated(EnumType.STRING)
    private Category category;

}
