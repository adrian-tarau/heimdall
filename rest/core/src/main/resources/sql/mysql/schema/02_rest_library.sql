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
    `global` boolean default false not null,
    hash     varchar(64),
    version int default 1 not null,
    created_by         varchar(100),
    modified_by        varchar(100),
    created_at  datetime            not null,
    modified_at datetime,
    tags        varchar(100),
    description varchar(1000),
    constraint fk$rest_library$project foreign key (project_id) references rest_project (id)
) ENGINE = InnoDB;

create table rest_library_history
(
    id                 integer             auto_increment primary key,
    rest_library_id    int                      not null,
    resource           varchar(1000)            not null,
    version            int,
    modified_by        varchar(100),
    modified_at        datetime,
    constraint fk$rest_library$id foreign key (rest_library_id) references rest_library (id)
) ENGINE = InnoDB;