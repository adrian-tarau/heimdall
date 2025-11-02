create table database_statements
(
    id              integer                                          not null auto_increment primary key,
    schema_id       integer                                          not null,
    database_type   ENUM ('MYSQL', 'MARIADB', 'POSTGRES', 'VERTICA') not null,
    user_id         integer                                          not null,
    statement_id    varchar(100)                                     not null,
    `type`          ENUM ('OTHER', 'CREATE','ALTER','DROP','TRUNCATE',
        'RENAME','SELECT','INSERT','DELETE','UPDATE','MERGE','CALL',
        'LOAD','SET','OPTIMIZE','LOCK')                              not null,
    execution_count bigint                                           not null,
    total_duration  float                                            not null,
    avg_duration    float                                            not null,
    min_duration    float                                            not null,
    max_duration    float                                            not null,
    created_at      datetime                                         not null,
    modified_at     datetime,
    `length`        integer                                          not null,
    resource        varchar(500)                                     not null,
    constraint fk$database_statements$schema foreign key (schema_id) references database_schemas (id),
    constraint fk$database_statements$user foreign key (user_id) references database_users (id)
) ENGINE = InnoDB;

create unique index database_statements$statement_id on database_statements (statement_id);
create index database_statements$created_at on database_statements (created_at);