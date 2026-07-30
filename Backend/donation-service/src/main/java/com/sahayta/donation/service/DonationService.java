package com.sahayta.donation.service;

import java.util.List;

import com.sahayta.donation.dto.NearbyDonationResponseDTO;
import com.sahayta.donation.entity.Donation;

public interface DonationService {
    Donation saveDonation(Donation donation);
    List<Donation> getAllDonations();
    Donation getDonationById(Long id);
    Donation updateDonation(Long id, Donation donation);
    Donation updateDonation(Long id, Donation donation, Long userId);
    void deleteDonation(Long id);
    List<Donation> getDonationsByCategory(String category);
    List<Donation> getDonationsByStatus(String status);
    List<Donation> getDonationsByAddress(String address);
    List<Donation> getDonationsByCreatedBy(Long createdBy);
    List<NearbyDonationResponseDTO> getNearbyDonations(Double latitude, Double longitude, Double radius, String category);
    NearbyDonationResponseDTO getNearestDonation(Double latitude, Double longitude, String category);
}