create table database_users
(
    id          integer      not null auto_increment primary key,
    name        varchar(100) not null,
    created_at  datetime     not null,
    modified_at datetime,
    description varchar(500)
) ENGINE = InnoDB;