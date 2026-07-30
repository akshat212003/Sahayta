package com.sahayta.donation.dto;

import java.time.LocalDate;

public class NearbyDonationResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String category;
    private Integer quantity;
    private String imageUrl;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDate expiryDate;
    private Double distance;

    public NearbyDonationResponseDTO() {
    }

    public NearbyDonationResponseDTO(Long id, String title, String description, String category,
                                   Integer quantity, String imageUrl, String address,
                                   Double latitude, Double longitude, LocalDate expiryDate, Double distance) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.expiryDate = expiryDate;
        this.distance = distance;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
}