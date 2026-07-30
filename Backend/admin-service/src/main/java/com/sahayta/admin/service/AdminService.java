package com.sahayta.admin.service;

import java.util.List;
import org.springframework.data.domain.Page;
import com.sahayta.admin.entity.Admin;

public interface AdminService {

    Admin saveAdmin(Admin admin);

    List<Admin> getAllAdmins();

    Page<Admin> getAdmins(int page, int size, String sortBy, String direction);
    
    List<Admin> getAdminsSorted(String field, String direction);
    List<Admin> searchAdmins(String name);
    Admin getAdminById(Long id);

    
    Admin updateAdmin(Long id, Admin admin);

    void deleteAdmin(Long id);

    Admin getAdminByEmail(String email);
}