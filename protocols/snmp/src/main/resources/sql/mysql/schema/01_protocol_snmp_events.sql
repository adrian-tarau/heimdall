create table protocol_snmp_events
(
    id               integer  not null auto_increment primary key,
    agent_address_id integer  not null,
    message_id       integer  not null,
    bindings_id      integer  not null,
    version          integer  not null,
    community_string varchar(200),
    enterprise       varchar(200),
    trap_type        integer,
    created_at       datetime not null,
    sent_at          datetime not null,
    received_at      datetime not null,
    constraint fk$snmps$agent foreign key (agent_address_id) references protocol_addresses (id),
    constraint fk$snmps$bindings foreign key (bindings_id) references protocol_parts (id)
) ENGINE = InnoDB;

create index protocol_snmp_events$received_at on protocol_snmp_events (received_at);