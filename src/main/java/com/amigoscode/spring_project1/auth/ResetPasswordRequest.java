package com.amigoscode.spring_project1.auth;

public record ResetPasswordRequest(String token, String newPassword) {
}
