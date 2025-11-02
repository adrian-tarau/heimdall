create table rest_schedule
(
    id             integer auto_increment primary key,
    environment_id integer                         not null,
    simulation_id  integer                         not null,
    type           ENUM ('EXPRESSION', 'INTERVAL') not null,
    expression     varchar(100),
    `interval`     varchar(100),
    active         boolean default true,
    vus int,
    duration varchar(50),
    iterations int,
    attributes varchar(4000),
    created_at     datetime                        not null,
    modified_at    datetime,
    description    varchar(1000),
    constraint fk$rest_schedule$environment foreign key (environment_id) references infrastructure_environment (id),
    constraint fk$rest_schedule$simulation foreign key (simulation_id) references rest_simulation (id)
) ENGINE = InnoDB;