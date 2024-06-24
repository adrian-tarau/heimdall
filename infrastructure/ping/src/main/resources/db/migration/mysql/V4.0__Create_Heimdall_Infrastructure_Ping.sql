drop table if exists infrastructure_ping_result;
drop table if exists infrastructure_ping;

create table infrastructure_ping
(
    id                 int               not null auto_increment primary key,
    service_id         int               not null,
    server_id          int               not null,
    name               varchar(100)      not null,
    active             bool default true not null,
    `interval`         int               not null,
    tags               varchar(500),
    hoops              int,
    connection_timeout int,
    read_timeout       int,
    write_timeout      int,
    created_at         datetime          not null,
    modified_at        datetime,
    description        varchar(1000),
    constraint fk$infrastructure_ping$service foreign key (service_id) references infrastructure_service (id),
    constraint fk$infrastructure_ping$server foreign key (server_id) references infrastructure_server (id),
    constraint nk$infrastructure_ping$service_server unique key (service_id, server_id)

) ENGINE = InnoDB;

create table infrastructure_ping_result
(
    id                     int                                  not null auto_increment primary key,
    ping_id                int                                  not null,
    service_id             int                                  not null,
    server_id              int                                  not null,
    started_at             datetime                             not null,
    ended_at               datetime                             not null,
    duration               int                                  not null,
    status                 ENUM('L3CON','L4CON','L4TOUT','L7TOUT','L7RSP','L7STS','L3OK','L4OK','L7OK','NA')  not null,
    error_code             int,
    error_message          varchar(1000),
    constraint fk$infrastructure_ping_result$ping foreign key (ping_id) references infrastructure_ping (id),
    constraint fk$infrastructure_ping_result$service foreign key (service_id) references infrastructure_service (id),
    constraint fk$infrastructure_ping_result$server foreign key (server_id) references infrastructure_server (id)

) ENGINE = InnoDB;

create index ix$infrastructure_ping_result$started_at on infrastructure_ping_result (started_at);