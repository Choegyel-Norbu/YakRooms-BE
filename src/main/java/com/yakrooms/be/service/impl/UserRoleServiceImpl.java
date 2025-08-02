package com.yakrooms.be.service.impl;

import com.yakrooms.be.model.entity.User;
import com.yakrooms.be.model.enums.Role;
import com.yakrooms.be.repository.UserRepository;
import com.yakrooms.be.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void addRoleToUser(Long userId, Role role) {
        User user = userRepository.findByIdWithCollections(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.addRole(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeRoleFromUser(Long userId, Role role) {
        User user = userRepository.findByIdWithCollections(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.removeRole(role);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getUserRoles(Long userId) {
        User user = userRepository.findByIdWithCollections(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return user.getRoles();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userHasRole(Long userId, Role role) {
        User user = userRepository.findByIdWithCollections(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return user.hasRole(role);
    }

    @Override
    @Transactional
    public void setUserRoles(Long userId, List<Role> roles) {
        User user = userRepository.findByIdWithCollections(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setRoles(roles);
        userRepository.save(user);
    }
} 