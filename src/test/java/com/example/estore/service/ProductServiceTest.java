package com.example.estore.service;

import com.example.estore.model.Product;
import com.example.estore.repository.ProductRepository;
import com.example.estore.enums.Category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void testGetAll() {
        Product p = new Product();
        p.setTitle("Laptop");

        Mockito.when(productRepository.findAll()).thenReturn(List.of(p));

        List<Product> result = productService.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void testGetById() {
        Product p = new Product();
        p.setId(1L);

        Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        Product result = productService.getById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void testGetById_NotFound() {
        Mockito.when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getById(1L));
    }

    @Test
    void testAddProduct() {
        Product p = new Product();
        p.setTitle("Phone");

        Mockito.when(productRepository.save(p)).thenReturn(p);

        Product result = productService.add(p);

        assertEquals("Phone", result.getTitle());
    }

    @Test
    void testUpdateProduct() {
        Product existing = new Product();
        existing.setId(1L);

        Product updated = new Product();
        updated.setTitle("Updated");

        Mockito.when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(productRepository.save(existing)).thenReturn(existing);

        Product result = productService.update(1L, updated);

        assertEquals("Updated", result.getTitle());
    }

    @Test
    void testDelete() {
        productService.delete(1L);
        Mockito.verify(productRepository).deleteById(1L);
    }

    @Test
    void testGetByCategory() {
        Product p = new Product();
        p.setCategory(Category.ELECTRONICS);

        Mockito.when(productRepository.findByCategory(Category.ELECTRONICS))
                .thenReturn(List.of(p));

        List<Product> result = productService.getByCategory("electronics");

        assertEquals(1, result.size());
    }

    @Test
    void testInvalidCategory() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.getByCategory("wrong"));
    }

    @Test
    void testSearchProducts() {
        Product p = new Product();
        p.setTitle("Laptop");

        Mockito.when(productRepository.findByTitleContainingIgnoreCase("lap"))
                .thenReturn(List.of(p));

        List<Product> result = productService.searchProducts("lap");

        assertEquals(1, result.size());
    }
}