package org.example.service;

import io.vertx.core.json.JsonObject;
import org.example.entity.User;
import org.example.entity.enums.Role;
import org.example.entity.enums.UserStatus;
import org.example.repository.StudentProfileRepository;
import org.example.repository.TeacherProfileRepository;
import org.example.repository.UserRepository;
import org.example.utils.JwtUtil;
import org.example.utils.PasswordUtil;

// src/main/java/org/example/service/AuthService.java
import io.reactivex.rxjava3.core.Single;

public class AuthService {

    private final UserRepository userRepo;
    private final String secret;
    private final long expiryMs;
    private final StudentProfileRepository studentRepo; // ✅ NEW
    private final TeacherProfileRepository teacherRepo;

    public AuthService(UserRepository userRepo, String secret, long expiryMs, StudentProfileRepository studentRepo, TeacherProfileRepository teacherRepo) {
        this.userRepo = userRepo;
        this.secret = secret;
        this.expiryMs = expiryMs;
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
    }


    public String login(String email, String password) {
        User user = userRepo.findByEmail(email);

        if (user == null) throw new RuntimeException("Invalid credentials");
        if (user.getStatus() != UserStatus.ACTIVE) throw new RuntimeException("User inactive");

        if (!PasswordUtil.verify(password, user.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");

        return JwtUtil.generate(user, secret, expiryMs);
    }


    public Single<String> loginRx(String email, String password) {
        return Single.fromCallable(() -> {
            User user = userRepo.findByEmail(email);
            if (user == null || user.getStatus() != UserStatus.ACTIVE ||
                    !PasswordUtil.verify(password, user.getPasswordHash())) {
                throw new RuntimeException("Invalid credentials");
            }
            return JwtUtil.generate(user, secret, expiryMs);
        });
    }

    // ✅ THIS IS THE METHOD YOU WERE MISSING
    // 3. The New Logic for Profile
    public Single<JsonObject> getProfileRx(Long userId) {
        return Single.fromCallable(() -> {
            // A. Get Common Info
            User user = userRepo.findById(userId);
            if (user == null) throw new RuntimeException("User not found");

            JsonObject response = new JsonObject()
                    .put("id", user.getId())
                    .put("fullName", user.getFullName())
                    .put("email", user.getEmail())
                    .put("role", user.getRole())
                    .put("mobile", user.getMobileNumber());

            // B. Check Role & Get Specific Info
            if (user.getRole() == Role.STUDENT) {
                var profile = studentRepo.findByUserId(userId);
                if (profile != null) {
                    response.put("details", new JsonObject()
                            .put("grade", profile.getGrade())
                            .put("enrollmentNumber", profile.getEnrollmentNumber())
                            .put("parentName", profile.getParentName()));
                }
            }
            else if (user.getRole() == Role.TEACHER) {
                var profile = teacherRepo.findByUserId(userId);
                if (profile != null) {
                    response.put("details", new JsonObject()
                            .put("specialization", profile.getSubjectSpecialization())
                            .put("qualification", profile.getQualification())
                            .put("salary", profile.getSalary()));
                }
            }

            return response;
        });
    }

}
