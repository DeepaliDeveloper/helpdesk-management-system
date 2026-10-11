package com.helpdesk.helpdesk_api.repository;

import com.helpdesk.helpdesk_api.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketHistoryRepository
        extends JpaRepository<TicketHistory, Long> {

    List<TicketHistory> findByTicket_IdOrderByChangedAtAsc(Long ticketId);
}