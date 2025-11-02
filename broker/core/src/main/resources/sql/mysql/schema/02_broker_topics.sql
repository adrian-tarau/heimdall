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