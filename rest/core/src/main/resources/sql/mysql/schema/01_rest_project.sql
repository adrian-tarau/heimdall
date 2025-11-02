create table rest_project
(
    id              integer             not null auto_increment primary key,
    natural_id      varchar(100)        not null,
    name            varchar(100)        not null,
    type ENUM ('NONE', 'GIT', 'SVN') not null,
    uri  varchar(2000),
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