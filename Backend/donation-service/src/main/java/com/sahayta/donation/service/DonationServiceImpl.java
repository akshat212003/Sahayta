package com.sahayta.donation.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sahayta.donation.dto.NearbyDonationResponseDTO;
import com.sahayta.donation.entity.Donation;
import com.sahayta.donation.maps.LocationService;
import com.sahayta.donation.repository.DonationRepository;

import jakarta.transaction.Transactional;

@Service
public class DonationServiceImpl implements DonationService {

    @Autowired
    private LocationService locationService;

    @Autowired
    private DonationRepository donationRepository;

    @Override
    public Donation saveDonation(Donation donation) {
        if ((donation.getLatitude() == null || donation.getLongitude() == null) &&
            (donation.getAddress() != null && !donation.getAddress().isBlank())) {
            double[] coordinates = locationService.getCoordinates(donation.getAddress());
            if (coordinates != null) {
                donation.setLatitude(coordinates[0]);
                donation.setLongitude(coordinates[1]);
            }
        }
        if (donation.getStatus() == null || donation.getStatus().isBlank()) {
            donation.setStatus("AVAILABLE");
        }
        return donationRepository.save(donation);
    }

    @Override
    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    @Override
    public Donation getDonationById(Long id) {
        return donationRepository.findById(id).orElse(null);
    }

    @Override
    public Donation updateDonation(Long id, Donation donation) {
        Donation existing = donationRepository.findById(id).orElse(null);
        if (existing == null) return null;

        if (donation.getTitle() != null) existing.setTitle(donation.getTitle());
        if (donation.getDescription() != null) existing.setDescription(donation.getDescription());
        if (donation.getCategory() != null) existing.setCategory(donation.getCategory());
        if (donation.getQuantity() != null) existing.setQuantity(donation.getQuantity());
        if (donation.getAddress() != null) existing.setAddress(donation.getAddress());
        if (donation.getLatitude() != null) existing.setLatitude(donation.getLatitude());
        if (donation.getLongitude() != null) existing.setLongitude(donation.getLongitude());
        if (donation.getExpiryDate() != null) existing.setExpiryDate(donation.getExpiryDate());
        if (donation.getStatus() != null) existing.setStatus(donation.getStatus());
        if (donation.getImageUrl() != null) existing.setImageUrl(donation.getImageUrl());

        return donationRepository.save(existing);
    }

    @Override
    public Donation updateDonation(Long id, Donation donation, Long userId) {
        Donation existing = donationRepository.findById(id).orElse(null);
        if (existing == null) return null;
        if (!existing.getCreatedBy().equals(userId)) {
            throw new RuntimeException("You are not authorized to update this donation");
        }
        return updateDonation(id, donation);
    }

    @Transactional
    @Override
    public void deleteDonation(Long id) {
        donationRepository.deleteById(id);
    }

    @Override
    public List<Donation> getDonationsByCategory(String category) {
        return donationRepository.findByCategory(category);
    }

    @Override
    public List<Donation> getDonationsByStatus(String status) {
        return donationRepository.findByStatus(status);
    }

    @Override
    public List<Donation> getDonationsByAddress(String address) {
        return donationRepository.findByAddressContainingIgnoreCase(address);
    }

    @Override
    public List<Donation> getDonationsByCreatedBy(Long createdBy) {
        return donationRepository.findByCreatedBy(createdBy);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Earth's radius in KM
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    @Override
    public List<NearbyDonationResponseDTO> getNearbyDonations(
            Double latitude, Double longitude, Double radius, String category) {
        List<Donation> donations = donationRepository.findAll();
        double effectiveRadius = (radius != null && radius > 0) ? radius : 10.0;

        return donations.stream()
                .filter(d -> category == null || category.isBlank() || d.getCategory().equalsIgnoreCase(category))
                .filter(d -> "AVAILABLE".equalsIgnoreCase(d.getStatus()))
                .map(d -> {
                    Double dLat = d.getLatitude();
                    Double dLng = d.getLongitude();
                    if (dLat == null || dLng == null) {
                        if (d.getAddress() != null && !d.getAddress().isBlank()) {
                            double[] coords = locationService.getCoordinates(d.getAddress());
                            if (coords != null) {
                                dLat = coords[0];
                                dLng = coords[1];
                            }
                        }
                    }
                    if (dLat == null || dLng == null) {
                        // Fallback match within radius if coordinates unpopulated
                        dLat = latitude;
                        dLng = longitude;
                    }
                    double distance = calculateDistance(latitude, longitude, dLat, dLng);
                    return new NearbyDonationResponseDTO(
                            d.getId(), d.getTitle(), d.getDescription(), d.getCategory(),
                            d.getQuantity(), d.getImageUrl(), d.getAddress(),
                            dLat, dLng, d.getExpiryDate(),
                            Math.round(distance * 100.0) / 100.0);
                })
                .filter(dto -> dto.getDistance() <= effectiveRadius)
                .sorted(Comparator.comparing(NearbyDonationResponseDTO::getDistance))
                .collect(Collectors.toList());
    }

    @Override
    public NearbyDonationResponseDTO getNearestDonation(
            Double latitude, Double longitude, String category) {
        List<NearbyDonationResponseDTO> nearbyDonations =
                getNearbyDonations(latitude, longitude, Double.MAX_VALUE, category);
        if (nearbyDonations.isEmpty()) return null;
        return nearbyDonations.get(0);
    }
}