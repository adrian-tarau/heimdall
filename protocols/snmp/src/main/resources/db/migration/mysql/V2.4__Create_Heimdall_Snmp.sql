create table protocol_snmp_events
(
    id               integer      not null auto_increment primary key,
    agent_address_id integer      not null,
    bindings_id      integer      not null,
    version          varchar(50)  not null,
    community_string varchar(200) not null,
    enterprise       varchar(200) not null,
    trap_type        integer,
    sent_at          datetime     not null,
    received_at      datetime     not null,
    constraint fk$snmps$agent foreign key (agent_address_id) references protocol_addresses (id),
    constraint fk$snmps$bindings foreign key (bindings_id) references protocol_parts (id)
) ENGINE = InnoDB;