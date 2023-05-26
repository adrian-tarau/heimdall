create table syslogs
(
    id             integer       not null auto_increment primary key,
    address_id     integer       not null,
    message_id  integer       not null,
    severity       tinyint       not null,
    facility       varchar(50)   not null,
    sent_at        datetime      not null,
    received_at    datetime      not null,
    constraint fk$syslogs$address foreign key (address_id) references addresses (id),
    constraint fk$syslogs$attachment foreign key (message_id) references parts (id)
) ENGINE = InnoDB;