create table rest_scenario
(
    id            integer      not null auto_increment primary key,
    natural_id    varchar(100) not null,
    simulation_id integer      not null,
    name          varchar(100) not null,
    start_time   integer,
    graceful_stop integer,
    `function`   varchar(100),
    tolerating_threshold  int default 500  not null,
    frustrating_threshold int default 2000 not null,
    created_at    datetime     not null,
    modified_at   datetime,
    tags          varchar(100),
    description   varchar(1000),
    constraint nk$rest_scenario$natural_id unique key (natural_id),
    constraint fk$rest_scenario$simulation foreign key (simulation_id) references rest_simulation (id)
) ENGINE = InnoDB;