package org.example.utils;

import io.vertx.core.json.JsonObject;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final int MIN_PASSWORD_LENGTH = 6;
    public static void validateLogin(String email, String password) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password is required");
    }
    public static void validateOnboardRequest(JsonObject body) {
        if (body == null) throw new IllegalArgumentException("Request body is required");

        String name = body.getString("fullName", "").trim();
        String email = body.getString("email", "").trim();
        String mobile = body.getString("mobileNumber", "").trim();
        String password = body.getString("initialPassword", "").trim();
        String role = body.getString("role", "").trim();

        validateCommonFields(name, email, mobile, password, role);
    }

    // Validate a Bulk Upload Row (CSV Parts)
    public static void validateBulkRow(String name, String email, String mobile, String password, String role) {
        validateCommonFields(name, email, mobile, password, role);
    }

    // Shared Logic
    private static void validateCommonFields(String name, String email, String mobile, String password, String role) {
        if (name.isEmpty()) throw new IllegalArgumentException("Full Name is required");

        if (email.isEmpty()) throw new IllegalArgumentException("Email is required");
        if (!EMAIL_PATTERN.matcher(email).matches()) throw new IllegalArgumentException("Invalid email format: " + email);

        if (mobile.isEmpty()) throw new IllegalArgumentException("Mobile Number is required");
        if (mobile.length() != 10 || !mobile.matches("^\\d{10}$")) throw new IllegalArgumentException("Invalid Mobile Number: " + mobile);

        if (password.isEmpty()) throw new IllegalArgumentException("Password is required");
        if (password.length() < MIN_PASSWORD_LENGTH) throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");

        if (role.isEmpty()) throw new IllegalArgumentException("Role is required");
        try {
            org.example.entity.enums.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Role: " + role);
        }
    }
}