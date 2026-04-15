CREATE TABLE history_entries (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_entries_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,

    CONSTRAINT fk_history_entries_actor
        FOREIGN KEY (actor_id) REFERENCES users(id)
);

CREATE INDEX idx_history_entries_ticket_created_at
    ON history_entries(ticket_id, created_at, id);
