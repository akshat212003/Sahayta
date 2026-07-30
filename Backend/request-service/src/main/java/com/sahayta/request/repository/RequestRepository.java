package com.sahayta.request.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sahayta.request.entity.Request;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByStatus(String status);

    List<Request> findByRequesterId(Long requesterId);

    List<Request> findByDonationId(Long donationId);

}