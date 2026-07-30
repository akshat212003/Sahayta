package com.sahayta.admin.service.client;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.sahayta.admin.dto.RequestDto;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AdminRequestService {

    @Autowired
    private RestClient restClient;
    
    @Autowired
    private HttpServletRequest httpServletRequest;

    private static final String REQUEST_SERVICE = "http://localhost:8083";
    private static final String DONATION_SERVICE = "http://localhost:8082";

    public List<RequestDto> getPendingRequests() {
    	String token = httpServletRequest.getHeader("Authorization");

    	RequestDto[] requests = restClient.get()
    	        .uri(REQUEST_SERVICE + "/requests/status/PENDING")
    	        .header("Authorization", token)
    	        .retrieve()
    	        .body(RequestDto[].class);

        if (requests == null) return List.of();
        return Arrays.asList(requests);
    }

    public List<RequestDto> getAllRequests() {
    	String token = httpServletRequest.getHeader("Authorization");

        RequestDto[] requests = restClient.get()
                .uri(REQUEST_SERVICE + "/requests")
                .header("Authorization", token)
                .retrieve()
                .body(RequestDto[].class);

        if (requests == null) return List.of();
        return Arrays.asList(requests);
    }

    public RequestDto approveRequest(Long id) {
        return overrideRequestStatus(id, "APPROVED");
    }

    public RequestDto rejectRequest(Long id) {
        return overrideRequestStatus(id, "REJECTED");
    }

    public RequestDto overrideRequestStatus(Long id, String status) {
    	String token = httpServletRequest.getHeader("Authorization");

    	return restClient.put()
    	        .uri(REQUEST_SERVICE + "/requests/{id}/override-status?status=" + status, id)
    	        .header("Authorization", token)
    	        .retrieve()
    	        .body(RequestDto.class);
    }

    public void overrideDeleteRequest(Long id) {
    	String token = httpServletRequest.getHeader("Authorization");

    	restClient.delete()
    	        .uri(REQUEST_SERVICE + "/requests/{id}", id)
    	        .header("Authorization", token)
    	        .retrieve()
    	        .toBodilessEntity();
    }

    public void overrideDonationStatus(Long id, String status) {
    	String token = httpServletRequest.getHeader("Authorization");

    	restClient.put()
    	        .uri(DONATION_SERVICE + "/donations/{id}/status?status=" + status, id)
    	        .header("Authorization", token)
    	        .retrieve()
    	        .toBodilessEntity();
    }

    public void overrideDeleteDonation(Long id) {
    	String token = httpServletRequest.getHeader("Authorization");

    	restClient.delete()
    	        .uri(DONATION_SERVICE + "/donations/{id}", id)
    	        .header("Authorization", token)
    	        .retrieve()
    	        .toBodilessEntity();
    }
}