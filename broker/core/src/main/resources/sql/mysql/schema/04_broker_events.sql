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