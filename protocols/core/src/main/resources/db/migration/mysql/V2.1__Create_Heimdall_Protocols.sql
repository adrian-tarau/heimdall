create table protocol_addresses
(
    id          integer                    not null auto_increment primary key,
    name        varchar(100)               not null,
    `type`        ENUM ('EMAIL', 'HOSTNAME') not null,
    `value`       varchar(500)               not null,
    created_at  datetime                   not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;

create unique index value on protocol_addresses (`value`);

create table protocol_parts
(
    id         integer                     not null auto_increment primary key,
    name       varchar(100)                not null,
    type       ENUM ('BODY', 'ATTACHMENT') not null,
    resource   varchar(500)                not null,
    created_at datetime                    not null
) ENGINE = InnoDB;