package com.example.productcrud.repository;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByOwner(User owner);
    Optional<Category> findByIdAndOwner(Long id, User owner);
    Optional<Category> findByNameAndOwner(String name, User owner);
    boolean existsByNameAndOwner(String name, User owner);
}