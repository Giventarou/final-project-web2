package com.example.productcrud.repository;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Method lama (biarkan saja)
    List<Product> findByOwner(User owner);

    // BARIS INI WAJIB ADA: Gunakan Page sebagai return type dan tambah Pageable
    Page<Product> findByOwner(User owner, Pageable pageable);

    Optional<Product> findByIdAndOwner(Long id, User owner);

    long countByCategory(Category category);

}