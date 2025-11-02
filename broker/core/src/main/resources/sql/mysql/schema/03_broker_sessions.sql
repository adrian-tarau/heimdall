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