create table broker_cluster
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

create table broker_topic
(
    id          integer                              not null auto_increment primary key,
    broker_id   integer                              not null,
    name        varchar(100)                         not null,
    type        ENUM ('KAFKA', 'PULSAR', 'RABBITMQ') not null,
    parameters  mediumtext,
    created_at  datetime                             not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;

create table broker_session
(
    id          bigint                               not null auto_increment primary key,
    broker_id   integer                              not null,
    topic_id    integer                              not null,
    status      ENUM ('SUCCESSFUL', 'FAILED')        not null,
    type        ENUM ('KAFKA', 'PULSAR', 'RABBITMQ') not null,
    started_at  datetime                             not null,
    ended_at    datetime                             not null,
    duration    int                                  not null,
    event_count int                                  not null,
    event_size  bigint                               not null,
    resource    varchar(1000),
    constraint fk$broker_session$broker foreign key (broker_id) references broker_cluster (id),
    constraint fk$broker_session$topic foreign key (topic_id) references broker_topic (id)
) ENGINE = InnoDB;

create index broker_session$started_at on broker_session (started_at);