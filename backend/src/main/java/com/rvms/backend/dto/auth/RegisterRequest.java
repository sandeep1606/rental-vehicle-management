package com.rvms.backend.dto.auth;

import com.rvms.backend.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotBlank String fullName,
        String phone,
        @NotBlank(message = "Role is required") String role,
        Long branchId
) {
    public Role roleEnum() {
        return Role.valueOf(role.toUpperCase());
    }
}
