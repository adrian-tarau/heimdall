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