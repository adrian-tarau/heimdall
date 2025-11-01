create table protocol_smtp_attachments
(
    id            integer not null auto_increment primary key,
    smtp_event_id integer not null,
    part_id       integer not null,
    constraint fk$smtp_attachments$smtp foreign key (smtp_event_id) references protocol_smtp_events (id),
    constraint fk$smtp_attachments$attachment foreign key (part_id) references protocol_parts (id)
) ENGINE = InnoDB;