package org.example.service;

import org.example.entity.User;
import org.example.entity.enums.UserStatus;
import org.example.repository.UserRepository;
import org.example.utils.JwtUtil;
import org.example.utils.PasswordUtil;

public class AuthService {

    private final UserRepository userRepo;
    private final String secret;
    private final long expiryMs;

    public AuthService(UserRepository userRepo, String secret, long expiryMs) {
        this.userRepo = userRepo;
        this.secret = secret;
        this.expiryMs = expiryMs;
    }

    public String login(String email, String password) {
        User user = userRepo.findByEmail(email);

        if (user == null) throw new RuntimeException("Invalid credentials");
        if (user.getStatus() != UserStatus.ACTIVE) throw new RuntimeException("User inactive");

        if (!PasswordUtil.verify(password, user.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");

        return JwtUtil.generate(user, secret, expiryMs);
    }
}
