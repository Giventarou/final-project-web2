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
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // By owner
    List<Product> findByOwner(User owner);
    Page<Product> findByOwner(User owner, Pageable pageable);
    Optional<Product> findByIdAndOwner(Long id, User owner);
    void deleteByIdAndOwner(Long id, User owner);

    // Search + filter + owner
    Page<Product> findByOwnerAndNameContainingIgnoreCase(User owner, String name, Pageable pageable);
    Page<Product> findByOwnerAndCategory(User owner, String category, Pageable pageable);
    Page<Product> findByOwnerAndNameContainingIgnoreCaseAndCategory(User owner, String name, String category, Pageable pageable);

    // Dashboard stats
    long countByOwnerAndActive(User owner, boolean active);
    List<Product> findByOwnerAndStockLessThan(User owner, int stock);

    @Query("SELECT p.category, COUNT(p) FROM Product p WHERE p.owner = :owner GROUP BY p.category")
    List<Object[]> countByCategoryAndOwner(@Param("owner") User owner);

    // Count by category name and owner (untuk CategoryController)
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :categoryName AND p.owner = :owner")
    long countByCategoryNameAndOwner(@Param("categoryName") String categoryName, @Param("owner") User owner);
}