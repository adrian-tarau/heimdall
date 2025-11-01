create table infrastructure_dns
(
    id          integer               not null auto_increment primary key,
    natural_id  varchar(100)          not null,
    name        varchar(500)          not null,
    hostname    varchar(100)          not null,
    domain      varchar(500),
    ip          varchar(100)          not null,
    valid       boolean default false not null,
    tags        varchar(500),
    created_at  datetime              not null,
    modified_at datetime,
    description varchar(1000),
    constraint nk$infrastructure_dns$natural_id unique key (natural_id)
) ENGINE = InnoDB;