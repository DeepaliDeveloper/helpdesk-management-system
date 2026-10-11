package com.helpdesk.helpdesk_api.dto;

public record UserResponse(
        Long id,
        String username,
        String email,
        String role
) {
}