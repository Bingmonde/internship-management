package com.prose.service.dto;

import com.prose.entity.users.Teacher;
import com.prose.entity.users.auth.Role;
import com.prose.service.Exceptions.RoleAndUserTypeNotCompatibleException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ProfileDTO<T> {
    private Role role;
    private T user;

    public ProfileDTO(Role role, T user) throws RoleAndUserTypeNotCompatibleException {
        // if role and user type are not compatible
        if (
                (role == Role.TEACHER && !(user instanceof TeacherDTO)) ||
                (role == Role.EMPLOYEUR && !(user instanceof EmployeurDTO)) ||
                (role == Role.STUDENT && !(user instanceof StudentDTO)) ||
                (role == Role.PROGRAM_MANAGER && !(user instanceof ProgramManagerDTO))
        ) {
            throw new RoleAndUserTypeNotCompatibleException("Role and user type are not compatible");
        }
        this.role = role;
        this.user = user;
    }
}