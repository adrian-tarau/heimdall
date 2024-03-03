create table database_schemas
(
    id          integer                                          not null auto_increment primary key,
    name        varchar(100)                                     not null,
    `type`      ENUM ('MYSQL', 'MARIADB', 'POSTGRES', 'VERTICA') not null,
    url         varchar(2000)                                    not null,
    username    varchar(100)                                     not null,
    password    varchar(100)                                     not null,
    mappings    varchar(5000),
    time_zone   varchar(100) default 'America/New_York'          not null,
    created_at  datetime                                         not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;

create table database_users
(
    id            integer                                          not null auto_increment primary key,
    name          varchar(100)                                     not null,
    created_at    datetime                                         not null,
    modified_at   datetime,
    description   varchar(500)
) ENGINE = InnoDB;

create table database_statements
(
    id                integer                                          not null auto_increment primary key,
    schema_id         integer                                          not null,
    database_type     ENUM ('MYSQL', 'MARIADB', 'POSTGRES', 'VERTICA') not null,
    user_id           integer                                          not null,
    statement_id      varchar(100)                                     not null,
    `type`            ENUM ('OTHER', 'CREATE','ALTER','DROP','TRUNCATE',
        'RENAME','SELECT','INSERT','DELETE','UPDATE','MERGE','CALL',
        'LOAD','SET','OPTIMIZE','LOCK')                                not null,
    execution_count   bigint                                           not null,
    total_duration    float                                            not null,
    avg_duration      float                                            not null,
    min_duration      float                                            not null,
    max_duration      float                                            not null,
    variance_duration float                                            not null,
    std_dev_duration  float                                            not null,
    created_at        datetime                                         not null,
    modified_at       datetime,
    `length`          integer                                          not null,
    resource          varchar(500)                                     not null,
    constraint fk$database_statements$schema foreign key (schema_id) references database_schemas (id),
    constraint fk$database_statements$user foreign key (user_id) references database_users (id)
) ENGINE = InnoDB;

create unique index database_statements$statement_id on database_statements (statement_id);
create index database_statements$created_at on database_statements (created_at);

create table database_snapshots
(
    id                             bigint                                           not null auto_increment primary key,
    schema_id                      integer                                          not null,
    database_type                  ENUM ('MYSQL', 'MARIADB', 'POSTGRES', 'VERTICA') not null,
    session_active_count           integer                                          not null,
    session_waiting_count          integer                                          not null,
    session_blocked_count          integer                                          not null,
    session_inactive_count         integer                                          not null,
    session_killed_count           integer                                          not null,
    transaction_running_count      integer                                          not null,
    transaction_blocked_count      integer                                          not null,
    transaction_committing_count   integer                                          not null,
    transaction_rolling_back_count integer                                          not null,
    incomplete                     boolean default false                            not null,
    created_at                     datetime                                         not null,
    resource                       varchar(500)                                     not null,
    constraint fk$database_snapshots$schema foreign key (schema_id) references database_schemas (id)
) ENGINE = InnoDB;

create index database_snapshots$created_at on database_snapshots (created_at);
