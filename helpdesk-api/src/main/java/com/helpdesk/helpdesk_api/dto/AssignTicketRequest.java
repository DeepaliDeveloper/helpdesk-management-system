package com.helpdesk.helpdesk_api.dto;

import jakarta.validation.constraints.NotNull;

public record AssignTicketRequest(
        @NotNull Long assigneeUserId
) {
}