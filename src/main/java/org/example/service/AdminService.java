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

    // ✅ Add this getter so AdminHandler can access findPaged()
    public UserRepository getUserRepo() {
        return userRepo;
    }

    public Completable onboardUserRx(User user, String initialPassword) {
        return Completable.fromAction(() -> {
            if (userRepo.findByEmail(user.getEmail()) != null) {
                throw new RuntimeException("Email already exists");
            }
            user.setPasswordHash(PasswordUtil.hash(initialPassword));
            userRepo.save(user);
        });
    }
}