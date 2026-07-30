package com.sahayta.donation.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sahayta.donation.dto.DonationRequestDTO;
import com.sahayta.donation.dto.DonationResponseDTO;
import com.sahayta.donation.dto.NearbyDonationResponseDTO;
import com.sahayta.donation.entity.Donation;
import com.sahayta.donation.security.UserPrincipal;
import com.sahayta.donation.service.DonationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/donations")
public class DonationController {

    @Autowired
    private DonationService donationService;

    private static final String UPLOAD_DIR = "uploads/donations/";

    @PostMapping
    public ResponseEntity<Donation> saveDonation(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody Donation donation) {
        if (user != null) {
            donation.setCreatedBy(user.getUserId());
        }
        return ResponseEntity.ok(donationService.saveDonation(donation));
    }

    @GetMapping
    public ResponseEntity<List<Donation>> getAllDonations() {
        return ResponseEntity.ok(donationService.getAllDonations());
    }

    @GetMapping("/my")
    public ResponseEntity<List<Donation>> getMyDonations(
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(donationService.getDonationsByCreatedBy(user.getUserId()));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Donation>> getDonationsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(donationService.getDonationsByCategory(category));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Donation>> getDonationsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(donationService.getDonationsByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Donation> getDonationById(@PathVariable Long id) {
        Donation donation = donationService.getDonationById(id);
        if (donation == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(donation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Donation> updateDonation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Donation donation) {
        Long userId = user != null ? user.getUserId() : null;
        Donation updated = donationService.updateDonation(id, donation, userId);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Donation> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Donation donation = donationService.getDonationById(id);
        if (donation == null) return ResponseEntity.notFound().build();
        donation.setStatus(status);
        return ResponseEntity.ok(donationService.saveDonation(donation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDonation(@PathVariable Long id) {
        donationService.deleteDonation(id);
        return ResponseEntity.ok(Map.of("message", "Donation deleted successfully"));
    }

    @GetMapping("/address/{address}")
    public ResponseEntity<List<Donation>> getDonationsByAddress(@PathVariable String address) {
        return ResponseEntity.ok(donationService.getDonationsByAddress(address));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Donation>> getDonationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(donationService.getDonationsByCreatedBy(userId));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyDonationResponseDTO>> getNearbyDonations(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radius,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(donationService.getNearbyDonations(latitude, longitude, radius, category));
    }

    @GetMapping("/nearest")
    public ResponseEntity<NearbyDonationResponseDTO> getNearestDonation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) String category) {
        NearbyDonationResponseDTO donation = donationService.getNearestDonation(latitude, longitude, category);
        if (donation == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(donation);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        String imageUrl = "/uploads/donations/" + filename;
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @GetMapping("/stats/my")
    public ResponseEntity<Map<String, Long>> getMyStats(
            @AuthenticationPrincipal UserPrincipal user) {
        if (user == null) return ResponseEntity.ok(Map.of("total", 0L, "available", 0L, "completed", 0L));
        List<Donation> myDonations = donationService.getDonationsByCreatedBy(user.getUserId());
        long total = myDonations.size();
        long available = myDonations.stream().filter(d -> "AVAILABLE".equals(d.getStatus())).count();
        long completed = myDonations.stream().filter(d -> "COMPLETED".equals(d.getStatus())).count();
        return ResponseEntity.ok(Map.of(
                "total", total,
                "available", available,
                "completed", completed
        ));
    }
}