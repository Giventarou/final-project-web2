package com.example.productcrud.service;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import com.example.productcrud.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Method lama — tetap ada agar tidak breaking
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    // ✅ Method baru untuk Pagination + Search + Filter
    public Page<Product> findWithPaginationAndFilter(String keyword, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = category != null && !category.trim().isEmpty();

        if (hasKeyword && hasCategory) {
            try {
                Category cat = Category.valueOf(category);
                return productRepository.findByNameContainingIgnoreCaseAndCategory(keyword, cat, pageable);
            } catch (IllegalArgumentException e) {
                return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
            }
        } else if (hasKeyword) {
            return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (hasCategory) {
            try {
                Category cat = Category.valueOf(category);
                return productRepository.findByCategory(cat, pageable);
            } catch (IllegalArgumentException e) {
                return productRepository.findAll(pageable);
            }
        } else {
            return productRepository.findAll(pageable);
        }
    }
}