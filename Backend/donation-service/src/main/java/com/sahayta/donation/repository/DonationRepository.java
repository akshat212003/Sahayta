package com.sahayta.donation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sahayta.donation.entity.Donation;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByCategory(String category);
    
    List<Donation> findByStatus(String status);
    
    List<Donation> findByAddressContainingIgnoreCase(String address);
    
    List<Donation> findByCreatedBy(Long createdBy);
    
    List<Donation> findByLatitudeIsNotNullAndLongitudeIsNotNull();

}