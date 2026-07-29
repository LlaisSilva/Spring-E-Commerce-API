package com.amigoscode.spring_project1.auth;

import lombok.Builder;

@Builder
public record AuthenticationResponse(String token) {
}
