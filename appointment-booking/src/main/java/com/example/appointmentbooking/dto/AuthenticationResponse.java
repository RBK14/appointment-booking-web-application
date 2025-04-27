package com.example.appointmentbooking.dto;

import com.example.appointmentbooking.model.user.UserRole;
import lombok.Data;

@Data
public class AuthenticationResponse {
    Long userId;
    UserRole userRole;
    private String token;
    private long expiration;
}
