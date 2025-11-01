create table protocol_snmp_agent_simulator_rule
(
    id              integer                             not null auto_increment primary key,
    natural_id      varchar(100)                        not null,
    name            varchar(100)                        not null,
    content         longtext                            not null,
    enabled         boolean     default true            not null,
    created_at      datetime                            not null,
    modified_at     datetime,
    tags            varchar(500),
    description     varchar(1000)
) ENGINE = InnoDB;