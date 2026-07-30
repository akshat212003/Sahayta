package com.sahayta.admin.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import com.sahayta.admin.payload.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sahayta.admin.dto.RequestDto;
import com.sahayta.admin.entity.Admin;
import com.sahayta.admin.service.AdminService;
import com.sahayta.admin.service.client.AdminRequestService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminRequestService adminRequestService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Admin Service is running"));
    }

    // Create Admin
    @PostMapping
    public Admin saveAdmin(@Valid @RequestBody Admin admin) {
        return adminService.saveAdmin(admin);
    }

    // Get All Admins
    @GetMapping("/all")
    public List<Admin> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    // Get Admin By ID
    @GetMapping("/{id}")
    public ApiResponse<Admin> getAdminById(@PathVariable Long id) {
        Admin admin = adminService.getAdminById(id);
        return new ApiResponse<>(true, "Admin fetched successfully", admin);
    }

    // Get Admin By Email
    @GetMapping("/email/{email}")
    public Admin getAdminByEmail(@PathVariable String email) {
        return adminService.getAdminByEmail(email);
    }

    // Update Admin
    @PutMapping("/{id}")
    public Admin updateAdmin(@PathVariable Long id, @Valid @RequestBody Admin admin) {
        return adminService.updateAdmin(id, admin);
    }

    // Delete Admin
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(Map.of("message", "Admin deleted successfully"));
    }

    // Pagination + Sorting
    @GetMapping
    public Page<Admin> getAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return adminService.getAdmins(page, size, sortBy, direction);
    }

    @GetMapping("/sort")
    public List<Admin> getAdminsSorted(
            @RequestParam String field,
            @RequestParam(defaultValue = "asc") String direction) {
        return adminService.getAdminsSorted(field, direction);
    }

    @GetMapping("/search")
    public List<Admin> searchAdmins(@RequestParam String name) {
        return adminService.searchAdmins(name);
    }

    // ==================== REQUEST & DONATION OVERRIDE MANAGEMENT ====================

    @GetMapping("/requests/pending")
    public List<RequestDto> getPendingRequests() {
        return adminRequestService.getPendingRequests();
    }

    @GetMapping("/requests/all")
    public List<RequestDto> getAllRequests() {
        return adminRequestService.getAllRequests();
    }

    @PutMapping("/requests/{id}/approve")
    public RequestDto approveRequest(@PathVariable Long id) {
        return adminRequestService.approveRequest(id);
    }

    @PutMapping("/requests/{id}/reject")
    public RequestDto rejectRequest(@PathVariable Long id) {
        return adminRequestService.rejectRequest(id);
    }

    @PutMapping("/requests/{id}/override-status")
    public RequestDto overrideRequestStatus(@PathVariable Long id, @RequestParam String status) {
        return adminRequestService.overrideRequestStatus(id, status);
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Map<String, String>> deleteRequestOverride(@PathVariable Long id) {
        adminRequestService.overrideDeleteRequest(id);
        return ResponseEntity.ok(Map.of("message", "Request force deleted by Admin"));
    }

    @PutMapping("/donations/{id}/override-status")
    public ResponseEntity<Map<String, String>> overrideDonationStatus(@PathVariable Long id, @RequestParam String status) {
        adminRequestService.overrideDonationStatus(id, status);
        return ResponseEntity.ok(Map.of("message", "Donation status force updated to " + status));
    }

    @DeleteMapping("/donations/{id}")
    public ResponseEntity<Map<String, String>> deleteDonationOverride(@PathVariable Long id) {
        adminRequestService.overrideDeleteDonation(id);
        return ResponseEntity.ok(Map.of("message", "Donation force deleted by Admin"));
    }
}