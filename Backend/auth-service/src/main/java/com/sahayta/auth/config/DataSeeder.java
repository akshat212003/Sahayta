package com.sahayta.auth.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sahayta.auth.entity.User;
import com.sahayta.auth.enums.Role;
import com.sahayta.auth.repository.UserRepository;

/**
 * DataSeeder — runs once on every application startup.
 * Creates the single hardcoded admin account if it does not already exist.
 *
 * Admin credentials:
 *   Email    : sahaytaadmin@gmail.com
 *   Password : Sahayta@2026
 *   Role     : ADMIN
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final String ADMIN_EMAIL    = "sahaytaadmin@gmail.com";
    private static final String ADMIN_PASSWORD = "Sahayta@2026";
    private static final String ADMIN_NAME     = "Sahayta Admin";
    private static final String ADMIN_PHONE    = "0000000000";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
                User admin = new User();
                admin.setName(ADMIN_NAME);
                admin.setEmail(ADMIN_EMAIL);
                admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                admin.setPhone(ADMIN_PHONE);
                admin.setRole(Role.ADMIN);
                admin.setCity("Pune");
                admin.setAddress("Sahayta Platform HQ");
                admin.setActive(true);
                admin.setApprovalStatus("APPROVED");
                admin.setCreatedAt(LocalDateTime.now());

                userRepository.save(admin);
                System.out.println("✅ [DataSeeder] Admin account created: " + ADMIN_EMAIL);
            } else {
                System.out.println("ℹ️  [DataSeeder] Admin account already exists: " + ADMIN_EMAIL);
            }
        } catch (Exception e) {
            System.err.println("⚠️  [DataSeeder] Could not seed admin account: " + e.getMessage());
        }
    }
}
