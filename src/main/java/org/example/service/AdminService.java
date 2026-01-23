package org.example.service;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single; // ✅ Import Single
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.json.JsonObject;
import org.example.dto.OnboardRequest;
import org.example.entity.BulkUpload;
import org.example.entity.StudentProfile;
import org.example.entity.TeacherProfile;
import org.example.entity.User;
import org.example.entity.enums.BulkUploadStatus;
import org.example.entity.enums.Role;
import org.example.entity.enums.UserStatus;
import org.example.repository.BulkUploadRepository;
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
    private final BulkUploadRepository bulkUploadRepo; // ✅ NEW

    public AdminService(UserRepository userRepo, StudentProfileRepository studentRepo, TeacherProfileRepository teacherRepo) {
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
        this.bulkUploadRepo = new BulkUploadRepository(); // Initialize here
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

    // --- PHASE 1: ASYNC BULK IMPORT ---

    // 1. Initiate: Creates record, returns ID, starts background thread
    public Single<JsonObject> initiateBulkImport(Long adminId, String filePath) {
        return Single.fromCallable(() -> {
            User admin = userRepo.findById(adminId);

            BulkUpload upload = new BulkUpload();
            upload.setUploadedBy(admin);
            upload.setUploadType("MIXED");
            upload.setStatus(BulkUploadStatus.IN_PROGRESS);
            bulkUploadRepo.save(upload);

            // Trigger Async Processing (Fire & Forget)
            Completable.fromAction(() -> processBulkFile(upload.getId(), filePath))
                    .subscribeOn(Schedulers.io()) // Run on IO thread
                    .subscribe(
                            () -> System.out.println("✅ Bulk upload " + upload.getId() + " completed."),
                            err -> {
                                System.err.println("❌ Bulk upload " + upload.getId() + " crashed: " + err.getMessage());
                                markUploadFailed(upload.getId(), err.getMessage());
                            }
                    );

            return new JsonObject()
                    .put("uploadId", upload.getId())
                    .put("message", "Upload started. Track status via /admin/uploads/" + upload.getId() + "/status");
        });
    }

    // 2. Process: The heavy lifting (Background)
    private void processBulkFile(Long uploadId, String filePath) {
        BulkUpload upload = bulkUploadRepo.findById(uploadId);
        List<String> errors = new ArrayList<>();
        int success = 0;
        int failed = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int rowNum = 0;
            while ((line = br.readLine()) != null) {
                rowNum++;
                // Skip Header
                if (rowNum == 1 && line.toLowerCase().contains("email")) continue;
                if (line.trim().isEmpty()) continue;

                try {
                    // -1 limit ensures empty trailing strings are included (though split works for middle empty too)
                    String[] parts = line.split(",", -1);

                    if (parts.length < 5) throw new RuntimeException("Insufficient columns");

                    String name = parts[0].trim();
                    String email = parts[1].trim();
                    String mobile = parts[2].trim();
                    String roleStr = parts[3].trim().toUpperCase();
                    String password = parts[4].trim();

                    // ✅ FIX: Explicitly check for empty strings
                    if (name.isEmpty()) throw new RuntimeException("Name is required");
                    if (email.isEmpty()) throw new RuntimeException("Email is required");
                    if (mobile.isEmpty()) throw new RuntimeException("Mobile is required");
                    if (password.isEmpty()) throw new RuntimeException("Password is required");

                    // Validate Role Enum
                    Role role;
                    try {
                        role = Role.valueOf(roleStr);
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Invalid Role: " + roleStr);
                    }

                    // Optional: Simple Regex for Email (Good to have)
                    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        throw new RuntimeException("Invalid Email Format");
                    }

                    // Profile Params (Handle potential IndexOutOfBounds if they are missing in CSV)
                    String p1 = parts.length > 5 ? parts[5].trim() : null; // Enrollment / Specialization
                    String p2 = parts.length > 6 ? parts[6].trim() : null; // Grade / Qualification
                    String p3 = parts.length > 7 ? parts[7].trim() : null; // Parent / Salary
                    String p4 = parts.length > 8 ? parts[8].trim() : null; // Exp Years

                    // Parse Numbers safely
                    Double salary = (p3 != null && !p3.isEmpty() && role == Role.TEACHER) ? Double.parseDouble(p3) : null;
                    Integer expYears = (p4 != null && !p4.isEmpty() && role == Role.TEACHER) ? Integer.parseInt(p4) : null;

                    createUserAndProfile(name, email, mobile, role, password,
                            p1, p2, p3, p1, p2, salary, expYears, null);

                    success++;
                } catch (Exception e) {
                    failed++;
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                }
            }

            // ... (Rest of the method remains the same: saving upload status) ...
            upload.setSuccessCount(success);
            upload.setFailureCount(failed);
            upload.setTotalRecords(success + failed);
            upload.setErrorReport(errors);
            upload.setStatus(BulkUploadStatus.COMPLETED);
            bulkUploadRepo.save(upload);

        } catch (Exception e) {
            markUploadFailed(uploadId, "File read error: " + e.getMessage());
        }
    }

    private void markUploadFailed(Long uploadId, String reason) {
        BulkUpload upload = bulkUploadRepo.findById(uploadId);
        if (upload != null) {
            upload.setStatus(BulkUploadStatus.FAILED);
            List<String> errs = new ArrayList<>();
            errs.add(reason);
            upload.setErrorReport(errs);
            bulkUploadRepo.save(upload);
        }
    }

    // Helper to avoid duplication
    private void createUserAndProfile(String name, String email, String mobile, Role role, String password,
                                      String p1, String p2, String p3, String p4, String p5, Double p6, Integer p7, String p8) {
        if (userRepo.findByEmail(email) != null) throw new RuntimeException("Email " + email + " already exists");

        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setMobileNumber(mobile);
        user.setRole(role);
        user.setPasswordHash(PasswordUtil.hash(password));
        userRepo.save(user);

        if (role == Role.STUDENT) {
            StudentProfile sp = new StudentProfile();
            sp.setUser(user);
            sp.setEnrollmentNumber(p1);
            sp.setGrade(p2);
            sp.setParentName(p3);
            sp.setCourseEnrolled(p8);
            studentRepo.save(sp);
        } else if (role == Role.TEACHER) {
            TeacherProfile tp = new TeacherProfile();
            tp.setUser(user);
            tp.setSubjectSpecialization(p4);
            tp.setQualification(p5);
            tp.setSalary(p6);
            tp.setExperienceYears(p7);
            teacherRepo.save(tp);
        }
    }

    // --- PHASE 1: ENABLE/DISABLE USER ---
    public Single<JsonObject> updateUserStatusRx(Long userId, UserStatus status) {
        return Single.fromCallable(() -> {
            User user = userRepo.findById(userId);
            if (user == null) throw new RuntimeException("User not found");

            // Prevent Admin from disabling themselves
            if (user.getRole() == Role.ADMIN) throw new RuntimeException("Cannot disable Admin users");

            user.setStatus(status);
            userRepo.save(user);

            return new JsonObject().put("message", "User status updated to " + status);
        });
    }
    public BulkUploadRepository getBulkUploadRepo() {
        return bulkUploadRepo;
    }
    // Soft Delete User (Mark as deleted)
    public Single<JsonObject> deleteUserRx(Long userId) {
        return Single.fromCallable(() -> {
            User user = userRepo.findById(userId);
            if (user == null) throw new RuntimeException("User not found");

            // Prevent deleting yourself (Admin)
            if (user.getRole() == Role.ADMIN) throw new RuntimeException("Cannot delete Admin");

            user.setIsDeleted(true); // Matches BaseEntity.isDeleted
            user.setStatus(UserStatus.INACTIVE); // Also deactivate them
            userRepo.save(user);

            return new JsonObject().put("message", "User deleted successfully");
        });
    }
}