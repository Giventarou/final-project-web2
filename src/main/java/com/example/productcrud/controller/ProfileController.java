package com.example.productcrud.controller;

import com.example.productcrud.dto.UpdateProfileRequest;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String showEditProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName(user.getFullName());
        request.setEmail(user.getEmail());
        request.setPhoneNumber(user.getPhoneNumber());
        request.setAddress(user.getAddress());
        request.setBio(user.getBio());
        request.setProfileImageUrl(user.getProfileImageUrl());

        model.addAttribute("updateProfileRequest", request);
        return "edit-profile";
    }

    @PostMapping("/profile/edit")
    public String processEditProfile(@ModelAttribute UpdateProfileRequest request,
                                     @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setBio(request.getBio());

        // Proses upload file jika ada
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            try {
                // Buat folder jika belum ada
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generate nama file unik
                String originalFilename = profileImageFile.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFilename = UUID.randomUUID().toString() + extension;

                // Simpan file
                Path filePath = uploadPath.resolve(newFilename);
                Files.copy(profileImageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Simpan URL ke database
                user.setProfileImageUrl("/uploads/profile-images/" + newFilename);

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Gagal upload foto: " + e.getMessage());
                return "redirect:/profile/edit";
            }
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui!");
        return "redirect:/profile";
    }
}