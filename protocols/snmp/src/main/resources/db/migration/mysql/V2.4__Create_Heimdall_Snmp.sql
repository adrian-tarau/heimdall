create table protocol_snmp_events
(
    id               integer      not null auto_increment primary key,
    agent_address_id integer      not null,
    bindings_id      integer      not null,
    version          varchar(50)  not null,
    community_string varchar(200) not null,
    enterprise       varchar(200) not null,
    trap_type        integer,
    created_at       datetime     not null,
    sent_at          datetime     not null,
    received_at      datetime     not null,
    constraint fk$snmps$agent foreign key (agent_address_id) references protocol_addresses (id),
    constraint fk$snmps$bindings foreign key (bindings_id) references protocol_parts (id)
) ENGINE = InnoDB;

create index protocol_snmp_events$received_at on protocol_snmp_events (received_at);

create table protocol_snmp_mibs
(
    id             integer                not null auto_increment primary key,
    name           varchar(100)           not null,
    `type`         enum ('SYSTEM','USER','IMPORT') not null,
    module_id      varchar(200)           not null,
    enterprise_oid varchar(200),
    message_oids   varchar(5000),
    create_at_oids varchar(5000),
    sent_at_oids   varchar(5000),
    severity_oids  varchar(5000),
    created_at     datetime               not null,
    modified_at    datetime               not null,
    file_name      varchar(100)           not null,
    `content`      mediumtext,
    description    varchar(1000)
) ENGINE = InnoDB;

create index protocol_snmp_mibs$module_id on protocol_snmp_mibs (module_id);