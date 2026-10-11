package com.helpdesk.helpdesk_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 2000) String commentText
) {
}