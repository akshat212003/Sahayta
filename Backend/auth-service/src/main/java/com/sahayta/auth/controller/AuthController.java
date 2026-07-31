package com.sahayta.auth.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sahayta.auth.dto.LoginRequest;
import com.sahayta.auth.dto.LoginResponse;
import com.sahayta.auth.dto.RefreshRequest;
import com.sahayta.auth.dto.RefreshResponse;
import com.sahayta.auth.dto.RegisterRequest;
import com.sahayta.auth.dto.RegisterResponse;
import com.sahayta.auth.dto.UpdateProfileRequest;
import com.sahayta.auth.dto.UserProfileDTO;
import com.sahayta.auth.entity.User;
import com.sahayta.auth.enums.Role;
import com.sahayta.auth.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@SecurityRequirement(name = "BearerAuth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public RegisterResponse registerUser(@Valid @RequestBody RegisterRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public LoginResponse loginUser(@Valid @RequestBody LoginRequest request) {
        return userService.loginUser(request);
    }

    @PostMapping("/google-login")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String name = body.get("name");
        String roleStr = body.get("role");
        Role role = null;
        if (roleStr != null) {
            try { role = Role.valueOf(roleStr.toUpperCase()); } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(userService.googleLogin(email, name, role));
    }

    @GetMapping("/public-stats")
    public ResponseEntity<Map<String, Object>> getPublicStats() {
        return ResponseEntity.ok(userService.getPublicStats());
    }

    @GetMapping("/public-ngos")
    public ResponseEntity<List<UserProfileDTO>> getPublicNgos() {
        return ResponseEntity.ok(userService.getPublicNgos());
    }

    @GetMapping("/profile")
    public UserProfileDTO getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return userService.getUserProfileDTO(user.getEmail());
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = (User) authentication.getPrincipal();
        userService.updateProfile(user.getEmail(), request);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    @PostMapping("/upload-picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) throws IOException {
        User user = (User) authentication.getPrincipal();
        String pictureUrl = userService.uploadProfilePicture(user.getEmail(), file);
        return ResponseEntity.ok(Map.of("profilePicture", pictureUrl));
    }

    @PostMapping("/upload-qr")
    public ResponseEntity<Map<String, String>> uploadPaymentQrCode(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) throws IOException {
        User user = (User) authentication.getPrincipal();
        String qrUrl = userService.uploadPaymentQrCode(user.getEmail(), file);
        return ResponseEntity.ok(Map.of("paymentQrCode", qrUrl));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String resetToken = userService.forgotPassword(email);
        return ResponseEntity.ok(Map.of(
                "message", "Password reset instructions generated successfully.",
                "token", resetToken,
                "resetUrl", "/reset-password?token=" + resetToken
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            token = body.get("email");
        }
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            newPassword = body.get("password");
        }
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully in database."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> body) {
        User user = (User) authentication.getPrincipal();
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        userService.changePassword(user.getEmail(), currentPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @GetMapping("/users")
    public List<UserProfileDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/pending")
    public List<UserProfileDTO> getPendingUsers() {
        return userService.getPendingUsers();
    }

    @GetMapping("/users/{id}")
    public UserProfileDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/users/{id}/approve")
    public ResponseEntity<Map<String, String>> approveUser(@PathVariable Long id) {
        userService.approveUser(id);
        return ResponseEntity.ok(Map.of("message", "User registration application approved successfully!"));
    }

    @PutMapping("/users/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectUser(@PathVariable Long id) {
        userService.rejectUser(id);
        return ResponseEntity.ok(Map.of("message", "User registration application rejected."));
    }

    @PutMapping("/users/{id}/toggle-active")
    public ResponseEntity<Map<String, String>> toggleUserActive(@PathVariable Long id) {
        userService.toggleUserActive(id);
        return ResponseEntity.ok(Map.of("message", "User status updated"));
    }

    @PostMapping("/refresh")
    public RefreshResponse refreshToken(@RequestBody RefreshRequest request) {
        return userService.refreshToken(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        userService.logoutUser(user.getEmail());
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}