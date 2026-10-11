package com.helpdesk.helpdesk_api.repository;

import com.helpdesk.helpdesk_api.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

//    For example, findByStatusOrderByCreatedAtDesc("OPEN") finds open tickets and orders them by creation time, newest first.
    List<Ticket> findByStatusOrderByCreatedAtDesc(String status);

    List<Ticket> findByCreator_IdOrderByCreatedAtDesc(Long userId);

    List<Ticket> findByAssignee_IdOrderByCreatedAtDesc(Long userId);
}
//You don't need to implement these methods manually. Spring Data JPA generates the queries from the method names.