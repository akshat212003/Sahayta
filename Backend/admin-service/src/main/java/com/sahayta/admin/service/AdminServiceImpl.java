package com.sahayta.admin.service;
import com.sahayta.admin.exception.DuplicateResourceException;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sahayta.admin.entity.Admin;
import com.sahayta.admin.exception.ResourceNotFoundException;
import com.sahayta.admin.repository.AdminRepository;
import org.springframework.data.domain.Sort;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;



    @Override
    public Admin saveAdmin(Admin admin) {

        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {
            throw new DuplicateResourceException(
                    "Admin with email '" + admin.getEmail() + "' already exists.");
        }

        return adminRepository.save(admin);
    }
    
    @Override
    public List<Admin> getAdminsSorted(String field, String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();

        return adminRepository.findAll(sort);
    }
    @Override
    public Page<Admin> getAdmins(int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return adminRepository.findAll(pageable);
    }
    
    @Override
    public Admin getAdminByEmail(String email) {

        return adminRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Admin not found with email : " + email));
    }

    @Override
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    public Admin getAdminById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Admin not found with id : " + id));
    }

    @Override
    public Admin updateAdmin(Long id, Admin admin) {

        Admin existingAdmin = adminRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Admin not found with id : " + id));

        existingAdmin.setName(admin.getName());
        existingAdmin.setEmail(admin.getEmail());
        existingAdmin.setRole(admin.getRole());

        return adminRepository.save(existingAdmin);
    }

    @Override
    public void deleteAdmin(Long id) {

        Admin admin = adminRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Admin not found with id : " + id));

        adminRepository.delete(admin);
    }
    @Override
    public List<Admin> searchAdmins(String name) {

        return adminRepository.findByNameContainingIgnoreCase(name);

    }
}