
CREATE TABLE users (
   id BIGSERIAL PRIMARY KEY,
   username VARCHAR(100) NOT NULL UNIQUE,
   email VARCHAR(150) NOT NULL UNIQUE,
   password_hash VARCHAR(255) NOT NULL,
   role VARCHAR(30) NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT chk_user_role
       CHECK (role IN ('EMPLOYEE', 'AGENT', 'ADMIN'))
);

CREATE TABLE tickets (
     id BIGSERIAL PRIMARY KEY,
     title VARCHAR(150) NOT NULL,
     description TEXT NOT NULL,
     priority VARCHAR(20) NOT NULL,
     status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
     created_by BIGINT NOT NULL,
     assigned_to BIGINT,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

     CONSTRAINT fk_ticket_creator
         FOREIGN KEY (created_by) REFERENCES users(id),

     CONSTRAINT fk_ticket_assignee
         FOREIGN KEY (assigned_to) REFERENCES users(id),

     CONSTRAINT chk_ticket_priority
         CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

     CONSTRAINT chk_ticket_status
         CHECK (status IN
                ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))
);

CREATE TABLE ticket_comments (
     id BIGSERIAL PRIMARY KEY,
     ticket_id BIGINT NOT NULL,
     user_id BIGINT NOT NULL,
     comment_text TEXT NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

     CONSTRAINT fk_comment_ticket
         FOREIGN KEY (ticket_id) REFERENCES tickets(id),

     CONSTRAINT fk_comment_user
         FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE ticket_history (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    changed_by BIGINT NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets(id),

    CONSTRAINT fk_history_user
        FOREIGN KEY (changed_by) REFERENCES users(id)
);

CREATE INDEX idx_tickets_status
    ON tickets(status);

CREATE INDEX idx_tickets_created_by
    ON tickets(created_by);

CREATE INDEX idx_tickets_assigned_to
    ON tickets(assigned_to);

CREATE INDEX idx_comments_ticket_id
    ON ticket_comments(ticket_id);

CREATE INDEX idx_history_ticket_id
    ON ticket_history(ticket_id);
