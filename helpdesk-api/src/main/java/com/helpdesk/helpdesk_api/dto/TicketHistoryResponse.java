package com.helpdesk.helpdesk_api.dto;

import java.time.LocalDateTime;

public record TicketHistoryResponse(
        Long id,
        String changedByUsername,
        String oldStatus,
        String newStatus,
        LocalDateTime changedAt
) {
}