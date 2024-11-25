create table rest_project
(
    id              integer             not null auto_increment primary key,
    natural_id      varchar(100)        not null,
    name            varchar(100)        not null,
    type            ENUM ('GIT', 'SVN') not null,
    uri             varchar(2000)       not null,
    user_name       varchar(100),
    password        varchar(100),
    token           varchar(2000),
    library_path    varchar(2000),
    simulation_path varchar(2000),
    created_at      datetime            not null,
    modified_at     datetime,
    tags            varchar(100),
    description     varchar(1000)
) ENGINE = InnoDB;

create table rest_library
(
    id          integer             auto_increment primary key,
    natural_id  varchar(100)        not null,
    project_id  integer,
    name        varchar(100)        not null,
    type        ENUM ('JMETER', 'K6','GATLING') not null,
    path     varchar(2000)         not null,
    resource    varchar(2000)       not null,
    override boolean default false not null,
    hash     varchar(64),
    created_at  datetime            not null,
    modified_at datetime,
    tags        varchar(100),
    description varchar(1000),
    constraint fk$rest_library$project foreign key (project_id) references rest_project (id)
) ENGINE = InnoDB;

create table rest_simulation
(
    id          integer                         not null auto_increment primary key,
    natural_id  varchar(100)                    not null,
    project_id  integer,
    name        varchar(100)                    not null,
    type        ENUM ('JMETER', 'K6','GATLING') not null,
    path     varchar(2000)         not null,
    resource    varchar(1000)                   not null,
    timeout     integer                         not null,
    override boolean default false not null,
    hash varchar(64),
    created_at  datetime                        not null,
    modified_at datetime,
    tags        varchar(100),
    description varchar(1000),
    constraint nk$rest_simulation$natural_id unique key (natural_id),
    constraint fk$rest_simulation$project foreign key (project_id) references rest_project (id)
) ENGINE = InnoDB;

create table rest_scenario
(
    id            integer      not null auto_increment primary key,
    natural_id    varchar(100) not null,
    simulation_id integer      not null,
    name          varchar(100) not null,
    start_time   integer,
    graceful_stop integer,
    `function`   varchar(100),
    created_at    datetime     not null,
    modified_at   datetime,
    tags          varchar(100),
    description   varchar(1000),
    constraint nk$rest_scenario$natural_id unique key (natural_id),
    constraint fk$rest_scenario$simulation foreign key (simulation_id) references rest_simulation (id)
) ENGINE = InnoDB;

create table rest_step
(
    id          integer      not null auto_increment primary key,
    natural_id  varchar(100) not null,
    scenario_id integer      not null,
    name        varchar(100) not null,
    created_at  datetime     not null,
    modified_at datetime,
    tags        varchar(100),
    description varchar(1000),
    constraint nk$rest_step$natural_id unique key (natural_id),
    constraint fk$rest_step$scenario foreign key (scenario_id) references rest_scenario (id)
) ENGINE = InnoDB;

create table rest_schedule
(
    id             integer auto_increment primary key,
    environment_id integer                         not null,
    simulation_id  integer                         not null,
    type           ENUM ('EXPRESSION', 'INTERVAL') not null,
    expression     varchar(100),
    `interval`     varchar(100),
    active         boolean default true,
    created_at     datetime                        not null,
    modified_at    datetime,
    description    varchar(1000),
    constraint fk$rest_schedule$environment foreign key (environment_id) references infrastructure_environment (id),
    constraint fk$rest_schedule$simulation foreign key (simulation_id) references rest_simulation (id)
) ENGINE = InnoDB;

create table rest_result
(
    id                           bigint   not null auto_increment primary key,
    environment_id               integer  not null,
    simulation_id                integer  not null,
    status        enum ('SUCCESSFUL', 'FAILED','CANCELED') not null,
    error_message                varchar(500),
    started_at                   datetime not null,
    ended_at                     datetime not null,
    duration                     integer  not null,
    data_received                float,
    data_sent                    float,
    iterations                   float,
    iteration_duration           float,
    vus                          float,
    vus_max                      float,
    http_request_blocked         float,
    http_request_connecting      float,
    http_Request_duration        float,
    http_request_failed          float,
    http_request_receiving       float,
    http_request_sending         float,
    http_request_tls_handshaking float,
    http_request_waiting         float,
    http_requests                float,
    version                      varchar(50),
    logs_uri                     varchar(500),
    report_uri                   varchar(500),
    data_uri                   varchar(500),
    description                  varchar(1000),
    constraint fk$rest_result$environment foreign key (environment_id) references infrastructure_environment (id),
    constraint fk$rest_result$simulation foreign key (simulation_id) references rest_simulation (id)
) ENGINE = InnoDB;

create index ix$rest_result$started on rest_result (started_at);

create table rest_output
(
    id                           bigint   not null auto_increment primary key,
    environment_id               integer  not null,
    simulation_id                integer  not null,
    result_id                    bigint  not null,
    scenario_id integer not null,
    status        enum ('SUCCESSFUL', 'FAILED','CANCELED') not null,
    started_at                   datetime not null,
    ended_at                     datetime not null,
    duration                     integer  not null,
    data_received                float    not null,
    data_sent                    float    not null,
    iterations                   float    not null,
    iteration_duration           float    not null,
    vus                          float    not null,
    vus_max                      float    not null,
    http_request_blocked         float    not null,
    http_request_connecting      float    not null,
    http_Request_duration        float    not null,
    http_request_failed          float    not null,
    http_request_receiving       float    not null,
    http_request_sending         float    not null,
    http_request_tls_handshaking float    not null,
    http_request_waiting         float    not null,
    http_requests                float    not null,
    constraint fk$rest_output$environment foreign key (environment_id) references infrastructure_environment (id),
    constraint fk$rest_output$simulation foreign key (simulation_id) references rest_simulation (id),
    constraint fk$rest_output$result foreign key (result_id) references rest_result (id),
    constraint fk$rest_output$scenario foreign key (scenario_id) references rest_scenario (id)
) ENGINE = InnoDB;

create index ix$rest_output$started on rest_output (started_at);