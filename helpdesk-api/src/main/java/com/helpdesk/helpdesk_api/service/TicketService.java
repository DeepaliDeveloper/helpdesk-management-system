package com.helpdesk.helpdesk_api.service;

import com.helpdesk.helpdesk_api.dto.CommentResponse;
import com.helpdesk.helpdesk_api.dto.CreateTicketRequest;
import com.helpdesk.helpdesk_api.dto.TicketHistoryResponse;
import com.helpdesk.helpdesk_api.dto.TicketResponse;
import com.helpdesk.helpdesk_api.entity.Ticket;
import com.helpdesk.helpdesk_api.entity.TicketComment;
import com.helpdesk.helpdesk_api.entity.TicketHistory;
import com.helpdesk.helpdesk_api.entity.User;
import com.helpdesk.helpdesk_api.repository.TicketCommentRepository;
import com.helpdesk.helpdesk_api.repository.TicketHistoryRepository;
import com.helpdesk.helpdesk_api.repository.TicketRepository;
import com.helpdesk.helpdesk_api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Set;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final TicketCommentRepository commentRepository;
    private final TicketHistoryRepository historyRepository;

    private static final Set<String> ALLOWED_PRIORITIES =
            Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");
    private static final Set<String> ALLOWED_STATUSES =
            Set.of("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");

    public TicketService(
            TicketRepository ticketRepository,
            UserRepository userRepository, EntityManager entityManager, TicketCommentRepository commentRepository, TicketHistoryRepository historyRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {

        String priority = request.getPriority()
                .trim()
                .toUpperCase();

        if (!ALLOWED_PRIORITIES.contains(priority)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Priority must be LOW, MEDIUM, HIGH or CRITICAL"
            );
        }

        User creator = userRepository.findById(
                request.getCreatedByUserId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Creator user not found"
        ));

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle().trim());
        ticket.setDescription(request.getDescription().trim());
        ticket.setPriority(priority);
        ticket.setStatus("OPEN");
        ticket.setCreator(creator);

        Ticket saved = ticketRepository.saveAndFlush(ticket);

        // Reload to retrieve database-generated values such as created_at.
//        Ticket reloaded = ticketRepository.findById(saved.getId())
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.INTERNAL_SERVER_ERROR,
//                        "Could not retrieve saved ticket"
//                ));
//        entityManager.refresh(saved);
//        return toResponse(saved);
        Ticket persistedTicket = ticketRepository.findById(saved.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        return toResponse(persistedTicket);

    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ticket not found: " + id
                ));

        return toResponse(ticket);
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreator().getId(),
                ticket.getCreator().getUsername(),
                ticket.getAssignee() == null
                        ? null : ticket.getAssignee().getId(),
                ticket.getCreatedAt()
        );
    }

    @Transactional
    public TicketResponse updateStatus(
            Long ticketId, String requestedStatus, Long changedByUserId) {

        String newStatus = requestedStatus.trim().toUpperCase();

        if (!ALLOWED_STATUSES.contains(newStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid ticket status");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        User changedBy = userRepository.findById(changedByUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        String oldStatus = ticket.getStatus();

        if (!oldStatus.equals(newStatus)) {
            ticket.setStatus(newStatus);
            ticketRepository.save(ticket);

            TicketHistory history = new TicketHistory();
            history.setTicket(ticket);
            history.setChangedBy(changedBy);
            history.setOldStatus(oldStatus);
            history.setNewStatus(newStatus);
            historyRepository.save(history);
        }

        return toResponse(ticket);
    }

    @Transactional
    public TicketResponse assignTicket(Long ticketId, Long assigneeUserId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        User assignee = userRepository.findById(assigneeUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignee not found"));

        if (!Set.of("AGENT", "ADMIN").contains(assignee.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tickets can only be assigned to agents or admins");
        }

        ticket.setAssignee(assignee);
        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public CommentResponse addComment(
            Long ticketId, Long userId, String commentText) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setUser(user);
        comment.setCommentText(commentText.trim());

        TicketComment saved = commentRepository.saveAndFlush(comment);

        return new CommentResponse(
                saved.getId(),
                ticket.getId(),
                user.getUsername(),
                saved.getCommentText(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ticket not found");
        }

        return commentRepository
                .findByTicket_IdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(c -> new CommentResponse(
                        c.getId(),
                        c.getTicket().getId(),
                        c.getUser().getUsername(),
                        c.getCommentText(),
                        c.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketHistoryResponse> getHistory(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ticket not found");
        }

        return historyRepository
                .findByTicket_IdOrderByChangedAtAsc(ticketId)
                .stream()
                .map(h -> new TicketHistoryResponse(
                        h.getId(),
                        h.getChangedBy().getUsername(),
                        h.getOldStatus(),
                        h.getNewStatus(),
                        h.getChangedAt()))
                .toList();
    }
}