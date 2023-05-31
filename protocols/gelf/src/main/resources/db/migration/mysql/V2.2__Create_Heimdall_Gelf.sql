create table protocol_gelf_events
(
    id                  integer       not null auto_increment primary key,
    address_id          integer       not null,
    short_attachment_id integer,
    long_attachment_id  integer,
    version             varchar(50)   not null,
    level               tinyint       not null,
    facility            varchar(50)   not null,
    fields              varchar(4000) not null,
    sent_at             datetime      not null,
    received_at         datetime      not null,
    constraint fk$gelf$address foreign key (address_id) references protocol_addresses (id),
    constraint fk$gelf$short_attachment foreign key (short_attachment_id) references protocol_parts (id),
    constraint fk$gelf$long_attachment foreign key (long_attachment_id) references protocol_parts (id)
) ENGINE = InnoDB;