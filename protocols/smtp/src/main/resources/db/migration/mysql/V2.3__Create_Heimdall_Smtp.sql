create table smtps
(
    id          integer  not null auto_increment primary key,
    subject     varchar(500),
    from_id     integer  not null,
    to_id       integer  not null,
    sent_at     datetime not null,
    received_at datetime not null,
    constraint fk$smtps$from foreign key (from_id) references addresses (id),
    constraint fk$smtps$to foreign key (to_id) references addresses (id)
) ENGINE = InnoDB;

create table smtp_attachments
(
    id            integer not null auto_increment primary key,
    smtp_id       integer not null,
    attachment_id integer not null,
    constraint fk$smtp_attachments$smtp foreign key (smtp_id) references smtps (id),
    constraint fk$smtp_attachments$attachment foreign key (attachment_id) references parts (id)
) ENGINE = InnoDB;