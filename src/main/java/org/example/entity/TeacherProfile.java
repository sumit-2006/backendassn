package org.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher_profiles")
public class TeacherProfile extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String subjectSpecialization;
    private String qualification;
    private Double salary;

    // Getters and Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSubjectSpecialization() { return subjectSpecialization; }
    public void setSubjectSpecialization(String subject) { this.subjectSpecialization = subject; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
}