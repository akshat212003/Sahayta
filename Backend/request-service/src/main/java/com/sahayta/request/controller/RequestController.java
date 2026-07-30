package com.sahayta.request.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.sahayta.request.entity.Request;
import com.sahayta.request.security.UserPrincipal;
import com.sahayta.request.service.RequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/requests")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @PostMapping
    public ResponseEntity<Request> saveRequest(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody Request request) {
        request.setRequesterId(user.getUserId());
        request.setRequesterName(user.getName());
        request.setStatus("PENDING");
        return ResponseEntity.ok(requestService.saveRequest(request));
    }

    @GetMapping
    public ResponseEntity<List<Request>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Request> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Request> updateRequest(
            @PathVariable Long id,
            @RequestBody Request request) {
        return ResponseEntity.ok(requestService.updateRequest(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRequest(
            @PathVariable Long id) {
        requestService.deleteRequest(id);
        return ResponseEntity.ok(Map.of("message", "Request deleted successfully"));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Request>> getRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(requestService.getRequestsByStatus(status));
    }

    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<Request>> getRequestsByRequesterId(@PathVariable Long requesterId) {
        return ResponseEntity.ok(requestService.getRequestsByRequesterId(requesterId));
    }

    @GetMapping("/donation/{donationId}")
    public ResponseEntity<List<Request>> getRequestsByDonationId(@PathVariable Long donationId) {
        return ResponseEntity.ok(requestService.getRequestsByDonationId(donationId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Request>> getMyRequests(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(requestService.getRequestsByRequesterId(user.getUserId()));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Request> approveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(requestService.approveRequest(id, user.getUserId()));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Request> rejectRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(requestService.rejectRequest(id, user.getUserId()));
    }

    @PutMapping("/{id}/override-status")
    public ResponseEntity<Request> overrideRequestStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(requestService.overrideRequestStatus(id, status));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Request> cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(requestService.cancelRequest(id, user.getUserId()));
    }

    @GetMapping("/my-donation-requests")
    public ResponseEntity<List<Request>> getRequestsForMyDonations(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(requestService.getRequestsForDonor(user.getUserId()));
    }

    @GetMapping("/stats/my")
    public ResponseEntity<Map<String, Long>> getMyStats(
            @AuthenticationPrincipal UserPrincipal user) {
        List<Request> myRequests = requestService.getRequestsByRequesterId(user.getUserId());
        long pending = myRequests.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long approved = myRequests.stream().filter(r -> "APPROVED".equals(r.getStatus())).count();
        long rejected = myRequests.stream().filter(r -> "REJECTED".equals(r.getStatus())).count();
        long cancelled = myRequests.stream().filter(r -> "CANCELLED".equals(r.getStatus())).count();
        return ResponseEntity.ok(Map.of(
                "total", (long) myRequests.size(),
                "pending", pending,
                "approved", approved,
                "rejected", rejected,
                "cancelled", cancelled
        ));
    }
}