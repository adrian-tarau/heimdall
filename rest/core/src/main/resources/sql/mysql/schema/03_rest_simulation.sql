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
    version int default 1 not null,
    created_by         varchar(100),
    modified_by        varchar(100),
    created_at  datetime                        not null,
    modified_at datetime,
    tags        varchar(100),
    description varchar(1000),
    constraint nk$rest_simulation$natural_id unique key (natural_id),
    constraint fk$rest_simulation$project foreign key (project_id) references rest_project (id)
) ENGINE = InnoDB;

create table rest_simulation_history
(
    id                 integer             auto_increment primary key,
    rest_simulation_id int                      not null,
    resource           varchar(1000)            not null,
    version            int,
    modified_by        varchar(100),
    modified_at        datetime,
    constraint fk$rest_simulation$id foreign key (rest_simulation_id) references rest_simulation (id)
) ENGINE = InnoDB;