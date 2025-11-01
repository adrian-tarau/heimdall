create table protocol_addresses
(
    id          integer                             not null auto_increment primary key,
    name        varchar(200)                        not null,
    `type`      ENUM ('EMAIL', 'HOSTNAME', 'OTHER') not null,
    `value`     varchar(500)                        not null,
    created_at  datetime                            not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;

create unique index value on protocol_addresses (`value`);