package org.example.dto;

import org.example.entity.enums.Role;

public class OnboardRequest {
    // Changed to private to follow Java standards
    private String fullName;
    private String email;
    private String mobileNumber;
    private Role role;
    private String initialPassword; // Field from doc

    // ✅ NEW: Student Specific Fields
    private String enrollmentNumber;
    private String grade;
    private String parentName;

    // ✅ NEW: Teacher Specific Fields
    private String subjectSpecialization;
    private String qualification;
    private Double salary;

    // --- Getters and Setters (REQUIRED for the Service code to work) ---

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getInitialPassword() { return initialPassword; }
    public void setInitialPassword(String initialPassword) { this.initialPassword = initialPassword; }

    // Student Data Accessors
    public String getEnrollmentNumber() { return enrollmentNumber; }
    public void setEnrollmentNumber(String enrollmentNumber) { this.enrollmentNumber = enrollmentNumber; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    // Teacher Data Accessors
    public String getSubjectSpecialization() { return subjectSpecialization; }
    public void setSubjectSpecialization(String subjectSpecialization) { this.subjectSpecialization = subjectSpecialization; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
}