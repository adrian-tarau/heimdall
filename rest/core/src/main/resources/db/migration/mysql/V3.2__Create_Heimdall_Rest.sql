create table rest_environment
(
    id          integer                                 not null auto_increment primary key,
    name        varchar(100)                            not null,
    url         varchar(2000)                           not null,
    username    varchar(100)                            not null,
    password    varchar(100)                            not null,
    bearer      varchar(500)                            not null,
    api_key     varchar(500)                            not null,
    time_zone   varchar(100) default 'America/New_York' not null,
    parameters  mediumtext,
    created_at  datetime                                not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;

create table rest_test_suite
(
    id          integer               not null auto_increment primary key,
    name        varchar(100)          not null,
    type        ENUM ('JMETER', 'K6') not null,
    model       mediumblob            not null,
    parameters  mediumtext,
    created_at  datetime              not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;

create table rest_test
(
    id            integer               not null auto_increment primary key,
    test_suite_id integer               not null,
    name          varchar(100)          not null,
    type          ENUM ('JMETER', 'K6') not null,
    model         mediumblob            not null,
    parameters    mediumtext,
    created_at    datetime              not null,
    modified_at   datetime,
    description   varchar(1000),
    constraint fk$rest_test$test_suite foreign key (test_suite_id) references rest_test_suite (id)
) ENGINE = InnoDB;

create table rest_session
(
    id            bigint                        not null auto_increment primary key,
    test_suite_id integer,
    test_id       integer,
    status        ENUM ('SUCCESSFUL', 'FAILED') not null,
    type          ENUM ('JMETER', 'K6')         not null,
    started_at    datetime                      not null,
    ended_at      datetime                      not null,
    duration      int                           not null,
    resource      varchar(1000),
    constraint fk$rest_session$test foreign key (test_id) references rest_test (id),
    constraint fk$rest_session$test_suite foreign key (test_suite_id) references rest_test_suite (id)
) ENGINE = InnoDB;

create index rest_session$started_at on rest_session (started_at);