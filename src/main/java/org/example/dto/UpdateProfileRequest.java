package org.example.dto;

public class UpdateProfileRequest {
    private String fullName;
    private String mobileNumber;
    private String courseEnrolled;
    // Student specific
    private String parentName;
    private Integer experienceYears;
    // Teacher specific
    private String qualification;

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getCourseEnrolled() { return courseEnrolled; }
    public void setCourseEnrolled(String c) { this.courseEnrolled = c; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer e) { this.experienceYears = e; }
}