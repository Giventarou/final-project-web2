package com.example.productcrud.controller;

import com.example.productcrud.dto.ChangePasswordRequest;
import com.example.productcrud.dto.RegisterRequest;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute RegisterRequest registerRequest,
                                  RedirectAttributes redirectAttributes) {
        // Validasi: username tidak boleh kosong
        if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Username tidak boleh kosong!");
            return "redirect:/register";
        }

        // Validasi: password tidak boleh kosong
        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Password tidak boleh kosong!");
            return "redirect:/register";
        }

        // Validasi: password harus cocok
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password dan konfirmasi password tidak cocok!");
            return "redirect:/register";
        }

        // Validasi: username belum terdaftar
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username sudah digunakan!");
            return "redirect:/register";
        }

        // simpan full name dan email saat register
        User user = new User();
        user.setUsername(registerRequest.getUsername().trim());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Registrasi berhasil! Silakan login.");
        return "redirect:/login";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        return "change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@ModelAttribute ChangePasswordRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        RedirectAttributes redirectAttributes) {

        if (request.getOldPassword().isEmpty() || request.getNewPassword().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Semua kolom harus diisi!");
            return "redirect:/change-password";
        }

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password lama salah!");
            return "redirect:/change-password";
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password baru dan konfirmasi tidak cocok!");
            return "redirect:/change-password";
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        redirectAttributes.addFlashAttribute("success", "Password berhasil diubah!");
        return "redirect:/products";
    }
}