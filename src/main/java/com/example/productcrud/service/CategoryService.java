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
        return categoryRepository.findByUser(user);
    }

    public Optional<Category> findByIdAndUser(Long id, User user) {
        return categoryRepository.findByIdAndUser(id, user);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public void delete(Category category) {
        categoryRepository.delete(category);
    }

    public boolean existsByNameAndUser(String name, User user) {
        return categoryRepository.existsByNameAndUser(name, user);
    }

    public boolean existsByNameAndUserAndIdNot(String name, User user, Long id) {
        return categoryRepository.findByNameAndUser(name, user)
                .map(c -> !c.getId().equals(id))
                .orElse(false);
    }
}
