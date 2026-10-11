package com.helpdesk.helpdesk_api.controller;

import com.helpdesk.helpdesk_api.dto.*;
import com.helpdesk.helpdesk_api.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(
            @Valid @RequestBody CreateTicketRequest request) {
        return ticketService.createTicket(request);
    }

    @GetMapping
    public List<TicketResponse> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/{id}")
    public TicketResponse getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }


    @PatchMapping("/{id}/status")
    public TicketResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            @RequestParam Long changedByUserId) {
        return ticketService.updateStatus(
                id, request.status(), changedByUserId);
    }

    @PatchMapping("/{id}/assign")
    public TicketResponse assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request) {
        return ticketService.assignTicket(
                id, request.assigneeUserId());
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request) {
        return ticketService.addComment(
                id, request.userId(), request.commentText());
    }

    @GetMapping("/{id}/comments")
    public List<CommentResponse> getComments(@PathVariable Long id) {
        return ticketService.getComments(id);
    }

    @GetMapping("/{id}/history")
    public List<TicketHistoryResponse> getHistory(@PathVariable Long id) {
        return ticketService.getHistory(id);
    }

}

//@RestController: exposes methods as HTTP endpoints and serializes responses as JSON.
//@RequestMapping("/api/tickets"): defines the common URL prefix.
//@PostMapping: handles ticket creation.
//@GetMapping: retrieves tickets.
//@RequestBody: converts incoming JSON into a Java DTO.
//@Valid: runs the DTO's validation rules.
//@PathVariable: reads the ticket ID from the URL.