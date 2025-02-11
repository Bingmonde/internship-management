package com.prose.service.dto;

public class UpdatePasswordDTO {
    private String currentPassword;
    private String newPassword;
    public UpdatePasswordDTO() { }

    public UpdatePasswordDTO(String oldPassword, String newPassword) {
    }

    public String getCurrentPassword() {
        return currentPassword;
    }
    public String getNewPassword() {
        return newPassword;
    }
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
