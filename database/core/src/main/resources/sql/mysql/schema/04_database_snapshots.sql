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