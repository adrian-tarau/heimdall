create table database_schemas
(
    id          integer                                          not null auto_increment primary key,
    name        varchar(100)                                     not null,
    `type`      ENUM ('MYSQL', 'MARIADB', 'POSTGRES', 'VERTICA') not null,
    url         varchar(2000)                                    not null,
    username    varchar(100)                                     not null,
    password    varchar(100)                                     not null,
    mappings    varchar(5000),
    time_zone   varchar(100) default 'America/New_York'          not null,
    created_at  datetime                                         not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;