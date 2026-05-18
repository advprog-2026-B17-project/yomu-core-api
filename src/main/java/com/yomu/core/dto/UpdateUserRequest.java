package com.yomu.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
    private String displayName;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private boolean updatePassword = false;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isUpdatePassword() { return updatePassword; }
    public void setUpdatePassword(boolean updatePassword) { this.updatePassword = updatePassword; }
}