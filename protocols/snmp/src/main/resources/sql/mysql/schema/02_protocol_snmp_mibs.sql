create table protocol_snmp_mibs
(
    id             integer                         not null auto_increment primary key,
    name           varchar(100)                    not null,
    `type`         enum ('SYSTEM','USER','IMPORT') not null,
    module_id      varchar(200)                    not null,
    enterprise_oid varchar(200),
    message_oids   varchar(5000),
    create_at_oids varchar(1000),
    sent_at_oids   varchar(1000),
    severity_oids  varchar(1000),
    created_at     datetime                        not null,
    modified_at    datetime                        not null,
    file_name      varchar(100)                    not null,
    `content`      mediumtext,
    description    varchar(1000)
) ENGINE = InnoDB;

create index protocol_snmp_mibs$module_id on protocol_snmp_mibs (module_id);