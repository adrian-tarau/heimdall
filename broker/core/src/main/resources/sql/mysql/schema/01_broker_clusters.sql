create table broker_clusters
(
    id          integer                                 not null auto_increment primary key,
    name        varchar(100)                            not null,
    type        ENUM ('KAFKA', 'PULSAR', 'RABBITMQ')    not null,
    time_zone   varchar(100) default 'America/New_York' not null,
    parameters  mediumtext,
    created_at  datetime                                not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;