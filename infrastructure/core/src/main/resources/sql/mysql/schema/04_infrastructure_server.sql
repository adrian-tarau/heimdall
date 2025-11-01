create table infrastructure_server
(
    id          integer                                 not null auto_increment primary key,
    cluster_id  integer,
    natural_id  varchar(100)                            not null,
    name        varchar(500)                            not null,
    hostname    varchar(200)                            not null,
    `type`      ENUM ('PHYSICAL', 'VIRTUAL')            not null,
    time_zone   varchar(100) default 'America/New_York' not null,
    icmp        boolean      default true               not null,
    tags        varchar(500),
    created_at  datetime                                not null,
    modified_at datetime,
    description varchar(1000),
    attributes  mediumtext,
    constraint fk$infrastructure_server$cluster foreign key (cluster_id) references infrastructure_cluster (id),
    constraint nk$infrastructure_server$natural_id unique key (natural_id)
) ENGINE = InnoDB;

create table infrastructure_server_to_service
(
    server_id  integer not null,
    service_id integer not null,
    constraint pk$infrastructure_server_to_service primary key (server_id, service_id),
    constraint fk$infrastructure_server_to_service$server foreign key (server_id) references infrastructure_server (id),
    constraint fk$infrastructure_server_to_service$service foreign key (service_id) references infrastructure_service (id)
) ENGINE = InnoDB;