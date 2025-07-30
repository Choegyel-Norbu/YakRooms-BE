package com.yakrooms.be.service;

import com.yakrooms.be.model.enums.Role;
import java.util.List;

public interface UserRoleService {

    /**
     * Add a role to a user
     * 
     * @param userId The user ID
     * @param role The role to add
     */
    void addRoleToUser(Long userId, Role role);

    /**
     * Remove a role from a user
     * 
     * @param userId The user ID
     * @param role The role to remove
     */
    void removeRoleFromUser(Long userId, Role role);

    /**
     * Get all roles for a user
     * 
     * @param userId The user ID
     * @return List of roles
     */
    List<Role> getUserRoles(Long userId);

    /**
     * Check if a user has a specific role
     * 
     * @param userId The user ID
     * @param role The role to check
     * @return true if user has the role, false otherwise
     */
    boolean userHasRole(Long userId, Role role);

    /**
     * Set roles for a user (replaces existing roles)
     * 
     * @param userId The user ID
     * @param roles List of roles to set
     */
    void setUserRoles(Long userId, List<Role> roles);
} 