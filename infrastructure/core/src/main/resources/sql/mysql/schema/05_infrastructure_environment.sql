create table infrastructure_environment
(
    id          integer      not null auto_increment primary key,
    natural_id  varchar(500) not null,
    name        varchar(100) not null,
    base_uri    varchar(500) not null,
    app_path    varchar(100),
    api_path    varchar(100),
    tags        varchar(500),
    version varchar(50),
    created_at  datetime     not null,
    modified_at datetime,
    description varchar(1000),
    attributes  mediumtext,
    constraint nk$infrastructure_environment$natural_id unique key (natural_id)
) ENGINE = InnoDB;

create table infrastructure_environment_to_cluster
(
    environment_id integer not null,
    cluster_id     integer not null,
    constraint pk$infrastructure_environment_to_cluster primary key (environment_id, cluster_id),
    constraint fk$infrastructure_environment_to_cluster$environment foreign key (environment_id) references infrastructure_environment (id),
    constraint fk$infrastructure_environment_to_cluster$cluster foreign key (cluster_id) references infrastructure_cluster (id)
) ENGINE = InnoDB;

create table infrastructure_environment_to_server
(
    environment_id integer not null,
    server_id      integer not null,
    constraint pk$infrastructure_environment_to_server primary key (environment_id, server_id),
    constraint fk$infrastructure_environment_to_server$environment foreign key (environment_id) references infrastructure_environment (id),
    constraint fk$infrastructure_environment_to_server$server foreign key (server_id) references infrastructure_server (id)
) ENGINE = InnoDB;