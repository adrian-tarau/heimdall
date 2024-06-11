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
    hoops              int,
    connection_timeout int,
    read_timeout       int,
    write_timeout      int,
    created_at         datetime          not null,
    modified_at        datetime,
    description        varchar(1000),
    constraint fk$infrastructure_ping$service foreign key (service_id) references infrastructure_service (id),
    constraint fk$infrastructure_ping$server foreign key (server_id) references infrastructure_server (id)

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
    status                 ENUM('SUCCESS','FAILURE','TIMEOUT','CANCEL')  not null,
    error_code             int,
    error_message          varchar(1000),
    constraint fk$infrastructure_ping_result$ping foreign key (ping_id) references infrastructure_ping (id),
    constraint fk$infrastructure_ping_result$service foreign key (service_id) references infrastructure_service (id),
    constraint fk$infrastructure_ping_result$server foreign key (server_id) references infrastructure_server (id)

) ENGINE = InnoDB;