package org.example.service;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.utils.PasswordUtil;

public class AdminService {
    private final UserRepository userRepo;

    public AdminService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public void onboardUser(User user, String initialPassword) {
        // Validation: Email uniqueness check
        if (userRepo.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }
        user.setPasswordHash(PasswordUtil.hash(initialPassword));
        userRepo.save(user);
    }
}