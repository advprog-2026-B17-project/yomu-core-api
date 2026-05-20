package com.yomu.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Email is invalid")
    private String email;

    @Pattern(regexp = "^$|^[0-9+()\\-\\s]{8,20}$", message = "Phone number is invalid")
    private String phone;

    @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
    private String displayName;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private boolean updatePassword = false;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isUpdatePassword() { return updatePassword; }
    public void setUpdatePassword(boolean updatePassword) { this.updatePassword = updatePassword; }
}
