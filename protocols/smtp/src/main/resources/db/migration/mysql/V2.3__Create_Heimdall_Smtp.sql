create table protocol_smtp_events
(
    id               integer           not null auto_increment primary key,
    subject          varchar(500),
    from_id          integer           not null,
    to_id            integer           not null,
    message_id       integer           not null,
    attachment_count integer default 0 not null,
    created_at       datetime          not null,
    sent_at          datetime          not null,
    received_at      datetime          not null,
    constraint fk$smtp$message foreign key (message_id) references protocol_parts (id),
    constraint fk$smtps$from foreign key (from_id) references protocol_addresses (id),
    constraint fk$smtps$to foreign key (to_id) references protocol_addresses (id)
) ENGINE = InnoDB;

create index protocol_smtp_events$received_at on protocol_smtp_events (received_at);

create table protocol_smtp_attachments
(
    id            integer not null auto_increment primary key,
    smtp_event_id integer not null,
    part_id       integer not null,
    constraint fk$smtp_attachments$smtp foreign key (smtp_event_id) references protocol_smtp_events (id),
    constraint fk$smtp_attachments$attachment foreign key (part_id) references protocol_parts (id)
) ENGINE = InnoDB;