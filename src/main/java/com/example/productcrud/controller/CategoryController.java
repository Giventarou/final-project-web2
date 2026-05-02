package com.example.productcrud.controller;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.CategoryService;
import com.example.productcrud.service.ProductService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final ProductService productService;
    public CategoryController(CategoryService categoryService, UserRepository userRepository, ProductService productService) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
        this.productService = productService;
    }
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }
    @GetMapping
    public String listCategories(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = getCurrentUser(userDetails);
        model.addAttribute("categories", categoryService.findAllByUser(currentUser));
        return "category/list";
    }
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        return "category/form";
    }
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute Category category,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails);
        if (category.getId() != null) {
            categoryService.findByIdAndUser(category.getId(), currentUser)
                    .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
            if (categoryService.existsByNameAndUserAndIdNot(category.getName(), currentUser, category.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nama kategori sudah ada.");
                return "redirect:/categories/new";
            }
        } else {
            if (categoryService.existsByNameAndUser(category.getName(), currentUser)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nama kategori sudah ada.");
                return "redirect:/categories/new";
            }
        }
        category.setUser(currentUser);
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMessage",
                category.getId() != null ? "Kategori berhasil diperbarui!" : "Kategori berhasil ditambahkan!");
        return "redirect:/categories";
    }
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails);
        return categoryService.findByIdAndUser(id, currentUser)
                .map(category -> {
                    model.addAttribute("category", category);
                    return "category/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Kategori tidak ditemukan.");
                    return "redirect:/categories";
                });
    }
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails);
        return categoryService.findByIdAndUser(id, currentUser)
                .map(category -> {
                    long productCount = productService.countByCategory(category);
                    if (productCount > 0) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "Tidak dapat menghapus kategori karena masih digunakan oleh " + productCount + " produk.");
                        return "redirect:/categories";
                    }
                    categoryService.delete(category);
                    redirectAttributes.addFlashAttribute("successMessage", "Kategori berhasil dihapus!");
                    return "redirect:/categories";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Kategori tidak ditemukan.");
                    return "redirect:/categories";
                });
    }
}
