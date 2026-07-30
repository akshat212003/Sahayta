package com.sahayta.donation.security;

public class UserPrincipal {

    private Long userId;
    private String name;
    private String email;

    public UserPrincipal(Long userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}