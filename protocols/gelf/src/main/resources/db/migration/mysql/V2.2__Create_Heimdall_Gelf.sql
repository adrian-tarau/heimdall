create table protocol_gelf_events
(
    id               integer     not null auto_increment primary key,
    address_id       integer     not null,
    short_message_id integer     not null,
    long_message_id  integer,
    fields_id        integer,
    version          varchar(50) not null,
    level            tinyint     not null,
    facility         tinyint     not null,
    created_at       datetime    not null,
    sent_at          datetime    not null,
    received_at      datetime    not null,
    constraint fk$gelf$address foreign key (address_id) references protocol_addresses (id),
    constraint fk$gelf$short_message foreign key (short_message_id) references protocol_parts (id),
    constraint fk$gelf$long_message foreign key (long_message_id) references protocol_parts (id),
    constraint fk$gelf$fields foreign key (fields_id) references protocol_parts (id)
) ENGINE = InnoDB;