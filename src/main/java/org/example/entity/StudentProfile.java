package org.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_profiles")
public class StudentProfile extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(name = "course_enrolled")
    private String courseEnrolled;
    private String enrollmentNumber;
    private String grade;
    private String parentName;


    public String getCourseEnrolled() { return courseEnrolled; }
    public void setCourseEnrolled(String courseEnrolled) { this.courseEnrolled = courseEnrolled; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEnrollmentNumber() { return enrollmentNumber; }
    public void setEnrollmentNumber(String enrollmentNumber) { this.enrollmentNumber = enrollmentNumber; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
}