package com.sahayta.auth.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sahayta.auth.dto.LoginRequest;
import com.sahayta.auth.dto.LoginResponse;
import com.sahayta.auth.dto.RefreshRequest;
import com.sahayta.auth.dto.RefreshResponse;
import com.sahayta.auth.dto.RegisterRequest;
import com.sahayta.auth.dto.RegisterResponse;
import com.sahayta.auth.dto.UpdateProfileRequest;
import com.sahayta.auth.dto.UserProfileDTO;
import com.sahayta.auth.entity.RefreshToken;
import com.sahayta.auth.entity.User;
import com.sahayta.auth.enums.Role;
import com.sahayta.auth.exception.ResourceNotFoundException;
import com.sahayta.auth.repository.UserRepository;
import com.sahayta.auth.util.JwtUtil;

@Service
public class UserService {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String UPLOAD_DIR = "uploads/profile-pictures/";

    public RegisterResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setCity(request.getCity());
        user.setAddress(request.getAddress());
        user.setActive(true);
        user.setApprovalStatus(request.getRole() == Role.NGO ? "PENDING" : "APPROVED");
        user.setCreatedAt(LocalDateTime.now());

        // Assign Lat/Lng based on city or default coordinates
        if (request.getLatitude() != null && request.getLongitude() != null) {
            user.setLatitude(request.getLatitude());
            user.setLongitude(request.getLongitude());
        } else {
            double offsetLat = (Math.random() - 0.5) * 0.04;
            double offsetLng = (Math.random() - 0.5) * 0.04;
            String cityLower = request.getCity() != null ? request.getCity().toLowerCase() : "";
            if (cityLower.contains("mumbai")) {
                user.setLatitude(19.0760 + offsetLat);
                user.setLongitude(72.8777 + offsetLng);
            } else if (cityLower.contains("delhi")) {
                user.setLatitude(28.6139 + offsetLat);
                user.setLongitude(77.2090 + offsetLng);
            } else if (cityLower.contains("bangalore") || cityLower.contains("bengaluru")) {
                user.setLatitude(12.9716 + offsetLat);
                user.setLongitude(77.5946 + offsetLng);
            } else {
                // Default Pune
                user.setLatitude(18.5204 + offsetLat);
                user.setLongitude(73.8567 + offsetLng);
            }
        }

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.ADMIN) {
            try {
                jdbcTemplate.update(
                    "INSERT IGNORE INTO admin_db.admins (id, name, email, role) VALUES (?, ?, ?, ?)",
                    savedUser.getId(), savedUser.getName(), savedUser.getEmail(), "ADMIN"
                );
            } catch (Exception e) {
                System.err.println("Notice: Could not sync admin: " + e.getMessage());
            }
        }

        return new RegisterResponse("User Registered Successfully", savedUser.getId());
    }

    public LoginResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + request.getEmail() + ". Please register first."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }
        if (request.getRole() != null && user.getRole() != request.getRole()) {
            throw new RuntimeException("Role mismatch: Your account is registered as " + user.getRole().name() + ". Please select '" + user.getRole().name() + "' to log in or register an account.");
        }
        if ("REJECTED".equalsIgnoreCase(user.getApprovalStatus())) {
            throw new RuntimeException("Your registration application was rejected by Admin.");
        }
        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated. Please contact admin.");
        }
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        return new LoginResponse("Login Successful", accessToken, refreshToken);
    }

    public LoginResponse googleLogin(String email, String name, Role role) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setName(name != null && !name.isBlank() ? name : email.split("@")[0]);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setPhone("G-" + UUID.randomUUID().toString().substring(0, 8));
            user.setRole(role != null ? role : Role.DONOR);
            user.setActive(true);
            user.setApprovalStatus("APPROVED");
            user.setCreatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        }
        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated. Contact admin.");
        }
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        return new LoginResponse("Google Login Successful", accessToken, refreshToken);
    }

    public void approveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setApprovalStatus("APPROVED");
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void rejectUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setApprovalStatus("REJECTED");
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<UserProfileDTO> getPendingUsers() {
        return userRepository.findAll().stream()
                .filter(u -> "PENDING".equalsIgnoreCase(u.getApprovalStatus()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserProfileDTO> getPublicNgos() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.NGO)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserProfileDTO getUserProfileDTO(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toDTO(user);
    }

    public UserProfileDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toDTO(user);
    }

    public List<UserProfileDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setBio(request.getBio());
        if (request.getLatitude() != null) user.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) user.setLongitude(request.getLongitude());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public String uploadProfilePicture(String email, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        String pictureUrl = "/uploads/profile-pictures/" + filename;
        user.setProfilePicture(pictureUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return pictureUrl;
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));
        return Base64.getEncoder().encodeToString(email.getBytes());
    }

    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("Email or Token and new password are required");
        }

        User user = userRepository.findByEmail(token.trim()).orElse(null);

        if (user == null) {
            try {
                String decodedEmail = new String(Base64.getDecoder().decode(token.trim()));
                user = userRepository.findByEmail(decodedEmail.trim()).orElse(null);
            } catch (Exception ignored) {}
        }

        if (user == null) {
            throw new ResourceNotFoundException("User account not found for email: " + token);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public Map<String, Object> getPublicStats() {
        long totalUsers = userRepository.count();
        long ngoCount = userRepository.findAll().stream().filter(u -> u.getRole() == Role.NGO).count();
        long donationsCount = 0;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM donation_db.donations", Integer.class);
            donationsCount = count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("Could not query donation_db for public stats: " + e.getMessage());
        }

        long activeCities = userRepository.findAll().stream()
                .map(User::getCity)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .count();

        return Map.of(
                "totalUsers", totalUsers,
                "ngoPartners", ngoCount,
                "totalDonations", donationsCount,
                "activeCities", activeCities > 0 ? activeCities : 1
        );
    }

    public void toggleUserActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(!user.isActive());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public RefreshResponse refreshToken(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        if (refreshToken == null) throw new RuntimeException("Invalid Refresh Token");
        if (refreshTokenService.isExpired(refreshToken)) throw new RuntimeException("Refresh Token Expired");
        String accessToken = jwtUtil.generateToken(refreshToken.getUser());
        return new RefreshResponse(accessToken);
    }

    public void logoutUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenService.deleteToken(user);
    }

    private UserProfileDTO toDTO(User user) {
        return new UserProfileDTO(
                user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getRole(), user.getAddress(), user.getCity(), user.getBio(),
                user.getProfilePicture(), user.getLatitude(), user.getLongitude(),
                user.isActive(), user.getApprovalStatus(), user.getCreatedAt()
        );
    }
}