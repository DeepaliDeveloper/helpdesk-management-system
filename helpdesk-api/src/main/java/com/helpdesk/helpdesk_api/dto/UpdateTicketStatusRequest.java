package com.helpdesk.helpdesk_api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTicketStatusRequest(
        @NotBlank String status
) {
}