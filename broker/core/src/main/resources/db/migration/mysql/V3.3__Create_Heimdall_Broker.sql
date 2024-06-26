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

create table broker_topics
(
    id                     integer                                         not null auto_increment primary key,
    cluster_id             integer                                         not null,
    name                   varchar(100)                                    not null,
    type                   enum ('KAFKA', 'PULSAR', 'RABBITMQ')            not null,
    active                 boolean      default true                       not null,
    sample_size            int,
    parameters             mediumtext,
    mime_type              varchar(100) default 'application/octet-stream' not null,
    name_expression        varchar(2000),
    description_expression varchar(2000),
    attribute_inclusions   varchar(1000),
    attribute_exclusions   varchar(1000),
    attribute_prefixes     varchar(1000),
    created_at             datetime                                        not null,
    modified_at            datetime,
    description            varchar(1000),
    constraint fk$broker_topics$cluster foreign key (cluster_id) references broker_clusters (id)
) ENGINE = InnoDB;

create table broker_sessions
(
    id                  bigint                                    not null auto_increment primary key,
    cluster_id          integer                                   not null,
    topic_id            integer                                   not null,
    status              ENUM ('SUCCESSFUL', 'FAILED', 'CANCELED') not null,
    type                ENUM ('KAFKA', 'PULSAR', 'RABBITMQ')      not null,
    started_at          datetime                                  not null,
    ended_at            datetime                                  not null,
    duration            int                                       not null,
    has_events          boolean default false                     not null,
    total_event_count   int                                       not null,
    total_event_size    bigint                                    not null,
    sampled_event_count int                                       not null,
    sampled_event_size  bigint                                    not null,
    failure_message     varchar(4000),
    resource            varchar(1000),
    constraint fk$broker_sessions$cluster foreign key (cluster_id) references broker_clusters (id),
    constraint fk$broker_sessions$topic foreign key (topic_id) references broker_topics (id)
) ENGINE = InnoDB;

create index ix$broker_sessions$started_at on broker_sessions (started_at);

create table broker_events
(
    id          bigint                               not null auto_increment primary key,
    cluster_id  integer                              not null,
    topic_id    integer                              not null,
    session_id  bigint                               not null,
    type        ENUM ('KAFKA', 'PULSAR', 'RABBITMQ') not null,
    event_id    varchar(100)                         not null,
    event_name  varchar(200),
    created_at  datetime                             not null,
    received_at datetime                             not null,
    constraint fk$broker_events$cluster foreign key (cluster_id) references broker_clusters (id),
    constraint fk$broker_events$topic foreign key (topic_id) references broker_topics (id),
    constraint fk$broker_events$session foreign key (session_id) references broker_sessions (id)
) ENGINE = InnoDB;

create index ix$broker_events$received_at on broker_events (received_at);