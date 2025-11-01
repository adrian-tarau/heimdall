create table infrastructure_cluster
(
    id          integer                                 not null auto_increment primary key,
    natural_id  varchar(100)                            not null,
    name        varchar(500)                            not null,
    `type`      ENUM ('PHYSICAL', 'VIRTUAL')            not null,
    time_zone   varchar(100) default 'America/New_York' not null,
    tags        varchar(500),
    created_at  datetime                                not null,
    modified_at datetime,
    description varchar(1000),
    constraint nk$infrastructure_cluster$natural_id unique key (natural_id)
) ENGINE = InnoDB;