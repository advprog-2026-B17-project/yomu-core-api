package com.yomu.core.dto;

public class AuthResponse {
    private String token;
    private String userId;
    private String username;
    private String displayName;
    private String role;

    public AuthResponse(String token, String userId, String username, String displayName, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getRole() { return role; }
}
