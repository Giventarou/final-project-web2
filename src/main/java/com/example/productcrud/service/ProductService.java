package com.example.productcrud.service;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ===== Method lama =====
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

    // ===== Method by Owner =====
    public List<Product> findAllByOwner(User owner) {
        return productRepository.findByOwner(owner);
    }

    public Page<Product> findAllByOwner(User owner, Pageable pageable) {
        return productRepository.findByOwner(owner, pageable);
    }

    public Optional<Product> findByIdAndOwner(Long id, User owner) {
        return productRepository.findByIdAndOwner(id, owner);
    }

    @Transactional
    public void deleteByIdAndOwner(Long id, User owner) {
        productRepository.deleteByIdAndOwner(id, owner);
    }

    // ===== Dashboard Stats =====
    public long countActiveByOwner(User owner) {
        return productRepository.countByOwnerAndActive(owner, true);
    }

    public long countInactiveByOwner(User owner) {
        return productRepository.countByOwnerAndActive(owner, false);
    }

    public List<Product> findLowStockByOwner(User owner) {
        return productRepository.findByOwnerAndStockLessThan(owner, 5);
    }

    public Map<String, Long> countByCategory(User owner) {
        List<Object[]> results = productRepository.countByCategoryAndOwner(owner);
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : results) {
            String category = row[0] != null ? row[0].toString() : "Tanpa Kategori";
            Long count = (Long) row[1];
            map.put(category, count);
        }
        return map;
    }

    // ===== Count by Category Name (untuk CategoryController) =====
    public long countByCategoryAndOwner(String categoryName, User owner) {
        return productRepository.countByCategoryNameAndOwner(categoryName, owner);
    }

    // ===== Pagination + Search + Filter =====
    public Page<Product> findWithPaginationAndFilter(User owner, String keyword, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = category != null && !category.trim().isEmpty();

        if (hasKeyword && hasCategory) {
            return productRepository.findByOwnerAndNameContainingIgnoreCaseAndCategory(owner, keyword, category, pageable);
        } else if (hasKeyword) {
            return productRepository.findByOwnerAndNameContainingIgnoreCase(owner, keyword, pageable);
        } else if (hasCategory) {
            return productRepository.findByOwnerAndCategory(owner, category, pageable);
        } else {
            return productRepository.findByOwner(owner, pageable);
        }
    }
}