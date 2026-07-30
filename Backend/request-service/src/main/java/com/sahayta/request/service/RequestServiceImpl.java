package com.sahayta.request.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.sahayta.request.dto.DonationDto;
import com.sahayta.request.dto.NearbyDonationResponseDTO;
import com.sahayta.request.entity.Request;
import com.sahayta.request.exception.ResourceNotFoundException;
import com.sahayta.request.maps.LocationService;
import com.sahayta.request.repository.RequestRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class RequestServiceImpl implements RequestService {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private LocationService locationService;

    @Autowired
    private RestClient restClient;

    @Autowired
    private RequestRepository requestRepository;

    @Override
    public Request saveRequest(Request request) {
        if (request.getDonationId() == null) {
            try {
                if (request.getAddress() != null) {
                    double[] coordinates = locationService.getCoordinates(request.getAddress());
                    NearbyDonationResponseDTO nearestDonation = getNearestDonation(
                            coordinates[0], coordinates[1], request.getCategory());
                    if (nearestDonation != null) {
                        request.setDonationId(nearestDonation.getId());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to resolve nearest donation: " + e.getMessage());
            }
        }

        if (request.getStatus() == null) {
            request.setStatus("PENDING");
        }
        if (request.getRequestDate() == null) {
            request.setRequestDate(java.time.LocalDateTime.now());
        }

        return requestRepository.save(request);
    }

    @Override
    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    @Override
    public Request getRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + id));
    }

    @Override
    public Request updateRequest(Long id, Request request) {
        Request existingRequest = getRequestById(id);

        existingRequest.setDonationId(request.getDonationId());
        existingRequest.setRequesterId(request.getRequesterId());
        existingRequest.setRequesterName(request.getRequesterName());
        existingRequest.setContactNumber(request.getContactNumber());
        existingRequest.setAddress(request.getAddress());
        existingRequest.setQuantityRequested(request.getQuantityRequested());
        existingRequest.setStatus(request.getStatus());

        return requestRepository.save(existingRequest);
    }

    @Transactional
    @Override
    public void deleteRequest(Long id) {
        Request request = getRequestById(id);
        requestRepository.delete(request);
    }

    @Override
    public List<Request> getRequestsByStatus(String status) {
        return requestRepository.findByStatus(status);
    }

    @Override
    public List<Request> getRequestsByRequesterId(Long requesterId) {
        return requestRepository.findByRequesterId(requesterId);
    }

    @Override
    public List<Request> getRequestsByDonationId(Long donationId) {
        return requestRepository.findByDonationId(donationId);
    }

    private DonationDto getDonation(Long donationId) {
        if (donationId == null) return null;
        try {
            return restClient.get()
                    .uri("http://localhost:8082/internal/donations/{id}", donationId)
                    .retrieve()
                    .body(DonationDto.class);
        } catch (Exception e) {
            System.err.println("Donation " + donationId + " not found: " + e.getMessage());
            return null;
        }
    }

    private DonationDto updateDonation(DonationDto donation) {
        return restClient.put()
                .uri("http://localhost:8082/internal/donations/{id}", donation.getId())
                .body(donation)
                .retrieve()
                .body(DonationDto.class);
    }

    private NearbyDonationResponseDTO getNearestDonation(
            Double latitude,
            Double longitude,
            String category) {
        String token = httpServletRequest.getHeader("Authorization");

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("localhost")
                            .port(8082)
                            .path("/donations/nearest")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("category", category)
                            .build())
                    .header("Authorization", token)
                    .retrieve()
                    .body(NearbyDonationResponseDTO.class);
        } catch (Exception e) {
            System.err.println("Failed to fetch nearest donation: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Request> getRequestsForDonor(Long donorId) {
        List<Request> allRequests = requestRepository.findAll();
        List<Request> donorRequests = new ArrayList<>();

        for (Request request : allRequests) {
            if (request.getDonationId() == null) continue;
            DonationDto donation = getDonation(request.getDonationId());
            if (donation == null) continue;
            if (donation.getCreatedBy() != null && donation.getCreatedBy().equals(donorId)) {
                donorRequests.add(request);
            }
        }

        return donorRequests;
    }

    @Override
    public Request approveRequest(Long id, Long donorId) {
        Request request = getRequestById(id);

        if (!request.getStatus().equalsIgnoreCase("PENDING")) {
            throw new RuntimeException("This request has already been processed.");
        }

        DonationDto donation = getDonation(request.getDonationId());

        if (donation == null || donation.getCreatedBy() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donation owner not found.");
        }

        if (!donation.getCreatedBy().equals(donorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to approve this request.");
        }

        if (request.getQuantityRequested() > donation.getQuantity()) {
            throw new RuntimeException("Requested quantity exceeds available donation quantity.");
        }

        int remaining = donation.getQuantity() - request.getQuantityRequested();
        donation.setQuantity(remaining);

        if (remaining == 0) {
            donation.setStatus("COMPLETED");
        }

        updateDonation(donation);
        request.setStatus("APPROVED");

        return requestRepository.save(request);
    }

    @Override
    public Request rejectRequest(Long id, Long donorId) {
        Request request = getRequestById(id);

        if (!request.getStatus().equalsIgnoreCase("PENDING")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This request has already been processed.");
        }

        DonationDto donation = getDonation(request.getDonationId());

        if (donation == null || donation.getCreatedBy() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donation owner not found.");
        }

        if (!donation.getCreatedBy().equals(donorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to reject this request.");
        }

        request.setStatus("REJECTED");
        return requestRepository.save(request);
    }

    @Override
    public Request overrideRequestStatus(Long id, String status) {
        Request request = getRequestById(id);
        request.setStatus(status.toUpperCase());
        return requestRepository.save(request);
    }

    @Override
    public Request cancelRequest(Long id, Long requesterId) {
        Request request = getRequestById(id);
        if (!request.getRequesterId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot cancel this request.");
        }
        if (!request.getStatus().equalsIgnoreCase("PENDING")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending requests can be cancelled.");
        }
        request.setStatus("CANCELLED");
        request.setUpdatedAt(java.time.LocalDateTime.now());
        return requestRepository.save(request);
    }
}