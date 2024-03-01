create table database_schemas
(
    id          integer                                          not null auto_increment primary key,
    name        varchar(100)                                     not null,
    `type`      ENUM ('MYSQL', 'MARIADB', 'POSTGRES', 'VERTICA') not null,
    url         varchar(2000)                                    not null,
    username    varchar(100)                                     not null,
    password    varchar(100)                                     not null,
    mappings    varchar(5000),
    created_at  datetime                                         not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;

create table database_statements
(
    id             varchar(100)                                                    not null primary key,
    schema_id      integer                                                         not null,
    `type`         ENUM ('UNKNOWN', 'CREATE','ALTER','DROP','TRUNCATE','RENAME','SELECT'
        ,'INSERT','DELETE','UPDATE','MERGE','CALL','LOAD','SET','OPTIMIZE','LOCK') not null,
    resource       varchar(500)                                                    not null,
    `length`       integer                                                         not null,
    executions     integer                                                         not null,
    total_duration bigint                                                          not null,
    created_at     datetime                                                        not null,
    modified_at    datetime,
    constraint fk$database_statements$schema foreign key (schema_id) references database_schemas (id)
) ENGINE = InnoDB;

create index database_statements$modified_at on database_statements (modified_at);

create table database_snapshots
(
    id                  varchar(100) not null primary key,
    resource            varchar(500) not null,
    statements_active   integer      not null,
    statements_waiting  integer      not null,
    statements_blocked  integer      not null,
    statements_inactive integer      not null,
    statements_killed   integer      not null,
    created_at          datetime     not null
) ENGINE = InnoDB;

create index database_snapshots$created_at on database_snapshots (created_at);
