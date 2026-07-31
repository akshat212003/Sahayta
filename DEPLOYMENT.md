# 🚀 Sahayta Microservices Deployment Guide

This guide provides step-by-step instructions for deploying the **Sahayta Platform** (Spring Boot Microservices + MySQL + React Vite Frontend).

---

## 🏗️ Architecture Overview

- **Frontend**: React + Vite + Tailwind CSS
- **API Gateway**: Port `8080`
- **Eureka Discovery**: Port `8761`
- **Auth Service**: Port `8081` (`auth_db`)
- **Donation Service**: Port `8082` (`donation_db`)
- **Request Service**: Port `8083` (`request_db`)
- **Admin Service**: Port `8084` (`admin_db`)

---

## 🌐 Quickest Way: Deploying Frontend to Vercel & Backend to Railway/Render

### 1. Database Setup (Cloud MySQL)
- Create a free MySQL database on **Railway.app** or **Aiven.io**.
- Execute:
  ```sql
  CREATE DATABASE auth_db;
  CREATE DATABASE donation_db;
  CREATE DATABASE request_db;
  CREATE DATABASE admin_db;
  ```

### 2. Backend Deployment Order
Deploy the Spring Boot services in this sequence:
1. `eureka-server` (Note down Eureka URL)
2. `auth-service`
3. `donation-service`
4. `request-service`
5. `admin-service`
6. `api-gateway` (Note down Gateway public URL)

### 3. Frontend Deployment (Vercel)
1. Push `Frontend/` folder to GitHub.
2. Import project into **Vercel.com**.
3. Set environment variable: `VITE_API_URL` = `https://your-api-gateway-url`.
4. Deploy!
