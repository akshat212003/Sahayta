package com.sahayta.donation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.sahayta.donation.entity.Donation;
import com.sahayta.donation.service.DonationService;


@RestController
@RequestMapping("/internal/donations")
public class InternalDonationController {

    @Autowired
    private DonationService donationService;

    @GetMapping("/{id}")
    public Donation getDonation(@PathVariable Long id) {
        return donationService.getDonationById(id);
    }

    @PutMapping("/{id}")
    public Donation updateDonation(@PathVariable Long id,
                                   @RequestBody Donation donation) {
        return donationService.updateDonation(id, donation);
    }
}
