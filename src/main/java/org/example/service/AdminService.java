package org.example.service;

import io.reactivex.rxjava3.core.Completable;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.utils.PasswordUtil;

public class AdminService {
    private final UserRepository userRepo;

    public AdminService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // This method resolves the "Cannot resolve method onboardUserRx" error
    public Completable onboardUserRx(User user, String initialPassword) {
        return Completable.fromAction(() -> {
            if (userRepo.findByEmail(user.getEmail()) != null) {
                throw new RuntimeException("Email already exists"); // Required validation [cite: 59]
            }
            user.setPasswordHash(PasswordUtil.hash(initialPassword)); // Required hashing [cite: 221]
            userRepo.save(user);
        });
    }
}