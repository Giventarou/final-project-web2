package com.example.productcrud.repository;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByOwner(User owner);

    Page<Product> findByOwner(User owner, Pageable pageable);

    Optional<Product> findByIdAndOwner(Long id, User owner);

    void deleteByIdAndOwner(Long id, User owner);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.owner = :owner AND p.active = true")
    long countActiveByOwner(@Param("owner") User owner);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.owner = :owner AND p.active = false")
    long countInactiveByOwner(@Param("owner") User owner);

    @Query("SELECT p.category as category, COUNT(p) as count FROM Product p WHERE p.owner = :owner GROUP BY p.category")
    List<Map<String, Object>> countByCategory(@Param("owner") User owner);

    @Query("SELECT p FROM Product p WHERE p.owner = :owner AND p.stock < 5")
    List<Product> findLowStockByOwner(@Param("owner") User owner);

    long countByCategoryAndOwner(String category, User owner);
}
