package com.helpdesk.helpdesk_api.dto;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String title,
        String description,
        String priority,
        String status,
        Long createdByUserId,
        String createdByUsername,
        Long assignedToUserId,
        LocalDateTime createdAt
) {
}
//Java records are useful for DTOs because Java generates the constructor, accessors and other standard methods for you.