package com.example.productcrud.controller;

import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import com.example.productcrud.service.ProductService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    // ✅ Kategori hardcoded — tampil di dropdown, nilai String disimpan ke DB
    private static final List<String> KATEGORI_LIST = List.of(
            "Elektronik",
            "Buku",
            "Makanan",
            "Pakaian",
            "Kesehatan",
            "Olahraga",
            "Rumah Tangga",
            "Otomotif",
            "Kecantikan",
            "Lainnya"
    );

    public ProductController(ProductService productService,
                             UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    @GetMapping("/")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = getCurrentUser(userDetails);
        List<Product> allProducts = productService.findAllByOwner(currentUser);

        long totalValue = allProducts.stream().mapToLong(p -> p.getPrice() * p.getStock()).sum();

        model.addAttribute("totalProducts", allProducts.size());
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("activeCount", productService.countActiveByOwner(currentUser));
        model.addAttribute("inactiveCount", productService.countInactiveByOwner(currentUser));
        model.addAttribute("categoryCounts", productService.countByCategory(currentUser));
        model.addAttribute("lowStockProducts", productService.findLowStockByOwner(currentUser));

        return "index";
    }

    // ✅ FIXED: sekarang support search keyword + filter kategori + pagination
    @GetMapping("/products")
    public String listProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        User currentUser = getCurrentUser(userDetails);
        int pageSize = 10;

        Page<Product> productPage = productService.findWithPaginationAndFilter(
                currentUser, keyword, category, page, pageSize);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("keyword", keyword);       // ✅ untuk retain nilai search
        model.addAttribute("category", category);     // ✅ untuk retain nilai filter
        model.addAttribute("categories", KATEGORI_LIST); // ✅ untuk dropdown filter

        return "product/list";
    }

    @GetMapping("/products/{id}")
    public String detailProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails);
        return productService.findByIdAndOwner(id, currentUser)
                .map(product -> {
                    model.addAttribute("product", product);
                    return "product/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan.");
                    return "redirect:/products";
                });
    }

    @GetMapping("/products/new")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = getCurrentUser(userDetails);
        Product product = new Product();
        product.setCreatedAt(LocalDate.now());
        model.addAttribute("product", product);
        model.addAttribute("categories", KATEGORI_LIST);
        return "product/form";
    }

    @GetMapping("/products/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails);
        return productService.findByIdAndOwner(id, currentUser)
                .map(product -> {
                    model.addAttribute("product", product);
                    model.addAttribute("categories", KATEGORI_LIST);
                    return "product/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan.");
                    return "redirect:/products";
                });
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails);

        if (product.getCreatedAt() == null) {
            product.setCreatedAt(LocalDate.now());
        }

        if (product.getId() != null) {
            boolean isOwner = productService.findByIdAndOwner(product.getId(), currentUser).isPresent();
            if (!isOwner) {
                redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan.");
                return "redirect:/products";
            }
        }

        product.setOwner(currentUser);
        productService.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil disimpan!");
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(userDetails);
        boolean isOwner = productService.findByIdAndOwner(id, currentUser).isPresent();

        if (isOwner) {
            productService.deleteByIdAndOwner(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil dihapus!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan.");
        }

        return "redirect:/products";
    }
}