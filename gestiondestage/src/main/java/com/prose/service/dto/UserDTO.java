package com.prose.service.dto;

import com.prose.entity.users.UserApp;
import com.prose.entity.users.auth.Role;

public record UserDTO (Long id, String email, String password, Role role) {

    public static UserDTO toDTO(UserApp userApp) {
        return new UserDTO(userApp.getId(), userApp.getEmail(), userApp.getPassword(), userApp.getRole());
    }
}
