package com.example.productcrud.service;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAllByUser(User user) {
        return categoryRepository.findByOwner(user);
    }

    public Optional<Category> findByIdAndUser(Long id, User user) {
        return categoryRepository.findByIdAndOwner(id, user);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public void delete(Category category) {
        categoryRepository.delete(category);
    }

    public boolean existsByNameAndUser(String name, User user) {
        return categoryRepository.existsByNameAndOwner(name, user);
    }

    public boolean existsByNameAndUserAndIdNot(String name, User user, Long id) {
        return categoryRepository.findByNameAndOwner(name, user)
                .map(c -> !c.getId().equals(id))
                .orElse(false);
    }
}