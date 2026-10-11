package com.helpdesk.helpdesk_api.service;

import com.helpdesk.helpdesk_api.dto.CreateTicketRequest;
import com.helpdesk.helpdesk_api.dto.TicketResponse;
import com.helpdesk.helpdesk_api.entity.Ticket;
import com.helpdesk.helpdesk_api.entity.User;
import com.helpdesk.helpdesk_api.repository.TicketCommentRepository;
import com.helpdesk.helpdesk_api.repository.TicketHistoryRepository;
import com.helpdesk.helpdesk_api.repository.TicketRepository;
import com.helpdesk.helpdesk_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketCommentRepository commentRepository;

    @Mock
    private TicketHistoryRepository historyRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void createTicket_shouldCreateTicketSuccessfully() {
        // Arrange
        User creator = new User();
        ReflectionTestUtils.setField(creator, "id", 10L);
        creator.setUsername("deepali.dev");
        creator.setEmail("deepali.dev@example.com");
        creator.setRole("EMPLOYEE");

        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Payment issue");
        request.setDescription("Payment API is failing");
        request.setPriority("high");
        request.setCreatedByUserId(10L);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(creator));

        AtomicReference<Ticket> savedTicket = new AtomicReference<>();

        when(ticketRepository.saveAndFlush(any(Ticket.class)))
                .thenAnswer(invocation -> {
                    Ticket ticket = invocation.getArgument(0);
                    ReflectionTestUtils.setField(ticket, "id", 100L);
                    savedTicket.set(ticket);
                    return ticket;
                });

        when(ticketRepository.findById(100L))
                .thenAnswer(invocation ->
                        Optional.of(savedTicket.get()));

        // Act
        TicketResponse response = ticketService.createTicket(request);

        // Assert
        assertEquals(100L, response.id());
        assertEquals("Payment issue", response.title());
        assertEquals("HIGH", response.priority());
        assertEquals("OPEN", response.status());
        assertEquals(10L, response.createdByUserId());
        assertEquals("deepali.dev", response.createdByUsername());

        verify(ticketRepository).saveAndFlush(any(Ticket.class));
    }

    @Test
    void createTicket_shouldRejectInvalidPriority() {
        // Arrange
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Payment issue");
        request.setDescription("Payment API is failing");
        request.setPriority("URGENT");
        request.setCreatedByUserId(10L);

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.createTicket(request));

        // Assert
        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode());

        verifyNoInteractions(userRepository);
        verifyNoInteractions(ticketRepository);
    }

    @Test
    void createTicket_shouldFailWhenCreatorDoesNotExist() {
        // Arrange
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Payment issue");
        request.setDescription("Payment API is failing");
        request.setPriority("HIGH");
        request.setCreatedByUserId(999L);

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.createTicket(request));

        // Assert
        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode());

        verify(ticketRepository, never())
                .saveAndFlush(any(Ticket.class));
    }
}