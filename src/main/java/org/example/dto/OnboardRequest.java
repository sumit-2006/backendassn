package org.example.dto;
import org.example.entity.enums.Role;

public class OnboardRequest {
    public String fullName;
    public String email;
    public String mobileNumber;
    public Role role;
    public String initialPassword; // Field from doc [cite: 54]
}