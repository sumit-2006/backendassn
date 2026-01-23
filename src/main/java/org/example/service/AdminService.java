package org.example.service;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single; // ✅ Import Single
import io.vertx.core.json.JsonObject;
import org.example.dto.OnboardRequest;
import org.example.entity.StudentProfile;
import org.example.entity.TeacherProfile;
import org.example.entity.User;
import org.example.entity.enums.Role;
import org.example.repository.StudentProfileRepository;
import org.example.repository.TeacherProfileRepository;
import org.example.repository.UserRepository;
import org.example.utils.PasswordUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class AdminService {
    private final UserRepository userRepo;
    private final StudentProfileRepository studentRepo;
    private final TeacherProfileRepository teacherRepo;

    public AdminService(UserRepository userRepo, StudentProfileRepository studentRepo, TeacherProfileRepository teacherRepo) {
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
    }

    public UserRepository getUserRepo() {
        return userRepo;
    }

    // Single User Onboarding
    public Completable onboardUserRx(OnboardRequest request) {
        return Completable.fromAction(() -> {
            if (userRepo.findByEmail(request.getEmail()) != null) {
                throw new RuntimeException("Email already exists");
            }

            User user = new User();
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setMobileNumber(request.getMobileNumber());
            user.setRole(request.getRole());
            user.setPasswordHash(PasswordUtil.hash(request.getInitialPassword()));

            userRepo.save(user);

            if (request.getRole() == Role.STUDENT) {
                StudentProfile sp = new StudentProfile();
                sp.setUser(user);
                sp.setEnrollmentNumber(request.getEnrollmentNumber());
                sp.setGrade(request.getGrade());
                sp.setParentName(request.getParentName());
                sp.setCourseEnrolled(request.getCourseEnrolled());
                studentRepo.save(sp);
            } else if (request.getRole() == Role.TEACHER) {
                TeacherProfile tp = new TeacherProfile();
                tp.setUser(user);
                tp.setSubjectSpecialization(request.getSubjectSpecialization());
                tp.setQualification(request.getQualification());
                tp.setSalary(request.getSalary());
                tp.setExperienceYears(request.getExperienceYears());
                teacherRepo.save(tp);
            }
        });
    }

    // ✅ NEW: Bulk Import Method (Fixes "no method" error)
    public Single<JsonObject> bulkImportRx(String filePath) {
        return Single.fromCallable(() -> {
            List<String> errors = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;

            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                int rowNum = 0;

                while ((line = br.readLine()) != null) {
                    rowNum++;
                    // Skip Header or Empty Lines
                    if (rowNum == 1 && line.toLowerCase().contains("email")) continue;
                    if (line.trim().isEmpty()) continue;

                    String[] parts = line.split(",");
                    // Expecting 7 columns: Name, Email, Mobile, Role, Param1, Param2, Param3
                    if (parts.length < 7) {
                        errors.add("Row " + rowNum + ": Invalid format. Expected 7 columns.");
                        failureCount++;
                        continue;
                    }

                    try {
                        String name = parts[0].trim();
                        String email = parts[1].trim();
                        String mobile = parts[2].trim();
                        Role role = Role.valueOf(parts[3].trim().toUpperCase());

                        // Profile Params
                        String p1 = parts[4].trim();
                        String p2 = parts[5].trim();
                        String p3 = parts[6].trim();

                        if (userRepo.findByEmail(email) != null) {
                            throw new RuntimeException("Email already exists");
                        }

                        // Create User
                        User user = new User();
                        user.setFullName(name);
                        user.setEmail(email);
                        user.setMobileNumber(mobile);
                        user.setRole(role);
                        user.setPasswordHash(PasswordUtil.hash("Temp@123")); // Default password
                        userRepo.save(user);

                        // Save Profile
                        if (role == Role.STUDENT) {
                            StudentProfile sp = new StudentProfile();
                            sp.setUser(user);
                            sp.setEnrollmentNumber(p1);
                            sp.setGrade(p2);
                            sp.setParentName(p3);
                            studentRepo.save(sp);
                        }
                        else if (role == Role.TEACHER) {
                            TeacherProfile tp = new TeacherProfile();
                            tp.setUser(user);
                            tp.setSubjectSpecialization(p1);
                            tp.setQualification(p2);
                            tp.setSalary(Double.parseDouble(p3));
                            teacherRepo.save(tp);
                        }

                        successCount++;

                    } catch (Exception e) {
                        errors.add("Row " + rowNum + ": " + e.getMessage());
                        failureCount++;
                    }
                }
            }

            return new JsonObject()
                    .put("totalProcessed", successCount + failureCount)
                    .put("success", successCount)
                    .put("failed", failureCount)
                    .put("errors", errors);
        });
    }
}