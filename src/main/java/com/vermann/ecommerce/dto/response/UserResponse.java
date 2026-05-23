package com.vermann.ecommerce.dto.response;

import com.vermann.ecommerce.model.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
}
