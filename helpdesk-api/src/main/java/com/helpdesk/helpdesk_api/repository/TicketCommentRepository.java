package com.helpdesk.helpdesk_api.repository;

import com.helpdesk.helpdesk_api.entity.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketCommentRepository
        extends JpaRepository<TicketComment, Long> {

    List<TicketComment> findByTicket_IdOrderByCreatedAtAsc(Long ticketId);
}