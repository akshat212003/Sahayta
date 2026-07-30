package com.sahayta.auth.dto;

import com.sahayta.auth.enums.Role;
import java.time.LocalDateTime;

public class UserProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private String address;
    private String city;
    private String bio;
    private String profilePicture;
    private Double latitude;
    private Double longitude;
    private boolean isActive;
    private String approvalStatus;
    private LocalDateTime createdAt;

    public UserProfileDTO() {}

    public UserProfileDTO(Long id, String name, String email, String phone, Role role,
                          String address, String city, String bio, String profilePicture,
                          Double latitude, Double longitude, boolean isActive, String approvalStatus, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.address = address;
        this.city = city;
        this.bio = bio;
        this.profilePicture = profilePicture;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isActive = isActive;
        this.approvalStatus = approvalStatus;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
