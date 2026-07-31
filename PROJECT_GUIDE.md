# 📚 Sahayta: Master Project Guide & Interview Defense

This document provides a complete technical reference for the **Sahayta Platform** project defense and technical interviews.

---

## 🎯 Project Overview
Sahayta is a Geo-Based Community Resource Sharing Platform designed to connect Donors, Receivers, and NGOs with zero middleman loss.

---

## 🏗️ Architecture
- **Microservices**: Spring Boot (Java 17)
- **Service Discovery**: Netflix Eureka (`8761`)
- **API Gateway**: Spring Cloud Gateway (`8080`)
- **Security**: JWT + BCrypt + Role-Based Access Control (RBAC)
- **Database**: MySQL (Database-per-service pattern: `auth_db`, `donation_db`, `request_db`, `admin_db`)
- **Frontend**: React 18 + Vite + Tailwind CSS + Lucide Icons + Leaflet Maps

---

## 🔑 Top Viva Questions & Quick Answers

1. **Q: Why Microservices?**
   - A: Independent scaling, fault isolation, and decoupled database management.

2. **Q: How does 10 KM location matching work?**
   - A: Using the Haversine formula based on spherical trigonometry to calculate distance between GPS coordinates.

3. **Q: How are passwords stored?**
   - A: Encrypted using `BCryptPasswordEncoder` with salt factor 10.

4. **Q: How does Service Discovery work?**
   - A: Eureka Server acts as a registry where microservices register on startup; API Gateway dynamically discovers routes.
