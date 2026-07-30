package com.sahayta.request.service;

import java.util.List;

import com.sahayta.request.entity.Request;

public interface RequestService {
    Request saveRequest(Request request);
    List<Request> getAllRequests();
    Request getRequestById(Long id);
    Request updateRequest(Long id, Request request);
    void deleteRequest(Long id);
    List<Request> getRequestsByStatus(String status);
    List<Request> getRequestsByRequesterId(Long requesterId);
    List<Request> getRequestsByDonationId(Long donationId);
    List<Request> getRequestsForDonor(Long donorId);
    Request approveRequest(Long id, Long donorId);
    Request rejectRequest(Long id, Long donorId);
    Request cancelRequest(Long id, Long requesterId);
    Request overrideRequestStatus(Long id, String status);
}