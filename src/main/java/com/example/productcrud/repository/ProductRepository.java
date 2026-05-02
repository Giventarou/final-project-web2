package com.example.productcrud.repository;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Search by name only
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Filter by category only
    Page<Product> findByCategory(Category category, Pageable pageable);

    // Search by name AND filter by category
    Page<Product> findByNameContainingIgnoreCaseAndCategory(String name, Category category, Pageable pageable);
}