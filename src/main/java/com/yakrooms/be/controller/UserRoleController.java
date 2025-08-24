package com.yakrooms.be.controller;

import com.yakrooms.be.model.enums.Role;
import com.yakrooms.be.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    // Add role to user - Only SUPER_ADMIN can manage user roles
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{userId}/roles/{role}")
    public ResponseEntity<String> addRoleToUser(
            @PathVariable Long userId,
            @PathVariable Role role) {
        userRoleService.addRoleToUser(userId, role);
        return ResponseEntity.ok("Role " + role + " added to user " + userId);
    }

    // Remove role from user - Only SUPER_ADMIN can manage user roles
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{userId}/roles/{role}")
    public ResponseEntity<String> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Role role) {
        userRoleService.removeRoleFromUser(userId, role);
        return ResponseEntity.ok("Role " + role + " removed from user " + userId);
    }

    // Get user roles - Only SUPER_ADMIN can view user roles
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{userId}/roles")
    public ResponseEntity<List<Role>> getUserRoles(@PathVariable Long userId) {
        List<Role> roles = userRoleService.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }

    // Check if user has specific role - Only SUPER_ADMIN can check user roles
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{userId}/roles/{role}")
    public ResponseEntity<Boolean> userHasRole(
            @PathVariable Long userId,
            @PathVariable Role role) {
        boolean hasRole = userRoleService.userHasRole(userId, role);
        return ResponseEntity.ok(hasRole);
    }

    // Set user roles - Only SUPER_ADMIN can set user roles
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{userId}/roles")
    public ResponseEntity<String> setUserRoles(
            @PathVariable Long userId,
            @RequestBody List<Role> roles) {
        userRoleService.setUserRoles(userId, roles);
        return ResponseEntity.ok("Roles updated for user " + userId);
    }
} 