create table rest_simulation
(
    id          integer                         not null auto_increment primary key,
    natural_id  varchar(100)                    not null,
    name        varchar(100)                    not null,
    type        ENUM ('JMETER', 'K6','GATLING') not null,
    resource    varchar(1000)                   not null,
    created_at  datetime                        not null,
    modified_at datetime,
    tags        varchar(100),
    description varchar(1000),
    constraint nk$rest_simulation$natural_id unique key (natural_id)
) ENGINE = InnoDB;

create table rest_scenario
(
    id            integer      not null auto_increment primary key,
    natural_id    varchar(100) not null,
    simulation_id integer      not null,
    name          varchar(100) not null,
    start_time    integer      not null,
    gracefulStop  integer      not null,
    `function`      varchar(100) not null,
    created_at    datetime     not null,
    modified_at   datetime,
    tags          varchar(100),
    description   varchar(1000),
    constraint nk$rest_scenario$natural_id unique key (natural_id),
    constraint fk$rest_scenario$simulation foreign key (simulation_id) references rest_simulation (id)
) ENGINE = InnoDB;

create table rest_step
(
    id          integer                                 not null auto_increment primary key,
    natural_id  varchar(100)                            not null,
    scenario_id integer                                 not null,
    name        varchar(100)                            not null,
    created_at  datetime                                not null,
    modified_at datetime,
    tags        varchar(100),
    description varchar(1000),
    constraint nk$rest_step$natural_id unique key (natural_id),
    constraint fk$rest_step$scenario foreign key (scenario_id) references rest_scenario (id)
) ENGINE = InnoDB;

create table rest_schedule
(
    id              integer                       auto_increment primary key,
    environment_id  integer                       not null,
    simulation_id   integer                       not null,
    expression      varchar(100),
    `interval`      integer,
    created_at      datetime                      not null,
    modified_at     datetime,
    description     varchar(1000),
    constraint fk$rest_schedule$environment foreign key (environment_id) references infrastructure_environment (id),
    constraint fk$rest_schedule$simulation foreign key (simulation_id) references rest_simulation (id)
) ENGINE = InnoDB;

create table rest_output
(
    id                           bigint                        not null auto_increment primary key,
    environment_id               integer                       not null,
    simulation_id                integer                       not null,
    started_at                   datetime                      not null,
    ended_at                     datetime                      not null,
    duration                     integer                       not null,
    data_received                float                         not null,
    data_sent                    float                         not null,
    iterations                   float                         not null,
    iteration_duration           float                         not null,
    vus                          float                         not null,
    vus_max                      float                         not null,
    http_request_blocked         float                         not null,
    http_request_connecting      float                         not null,
    http_Request_duration        float                         not null,
    http_request_failed          float                         not null,
    http_request_receiving       float                         not null,
    http_request_sending         float                         not null,
    http_request_tls_handshaking float                         not null,
    http_request_waiting         float                         not null,
    http_requests                float                         not null,
    version                      varchar(50),
    description                  varchar(1000),
    constraint fk$rest_output$environment foreign key (environment_id) references infrastructure_environment (id),
    constraint fk$rest_output$simulation foreign key (simulation_id) references rest_simulation (id)
) ENGINE = InnoDB;

create index ix$rest_output$started on rest_output (started_at);