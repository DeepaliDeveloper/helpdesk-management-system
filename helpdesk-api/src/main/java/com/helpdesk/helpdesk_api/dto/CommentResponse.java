package com.helpdesk.helpdesk_api.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long ticketId,
        String username,
        String commentText,
        LocalDateTime createdAt
) {
}