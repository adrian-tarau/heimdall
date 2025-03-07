create table infrastructure_dns
(
    id          integer               not null auto_increment primary key,
    natural_id  varchar(100)          not null,
    name        varchar(500)          not null,
    hostname    varchar(100)          not null,
    domain      varchar(500),
    ip          varchar(100)          not null,
    valid       boolean default false not null,
    tags        varchar(500),
    created_at  datetime              not null,
    modified_at datetime,
    description varchar(1000),
    constraint nk$infrastructure_dns$natural_id unique key (natural_id)
) ENGINE = InnoDB;

create table infrastructure_cluster
(
    id          integer                                 not null auto_increment primary key,
    natural_id  varchar(100)                            not null,
    name        varchar(500)                            not null,
    `type`      ENUM ('PHYSICAL', 'VIRTUAL')            not null,
    time_zone   varchar(100) default 'America/New_York' not null,
    tags        varchar(500),
    created_at  datetime                                not null,
    modified_at datetime,
    description varchar(1000),
    constraint nk$infrastructure_cluster$natural_id unique key (natural_id)
) ENGINE = InnoDB;

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

create table infrastructure_service
(
    id                 integer                                   not null auto_increment primary key,
    natural_id         varchar(500)                              not null,
    name               varchar(100)                              not null,
    `type`             ENUM ('ICMP', 'HTTP','SSH', 'TCP','UDP')  not null,
    port               integer                                   not null,
    path               varchar(500)                              null,
    liveness_path      varchar(500)                              null,
    readiness_path     varchar(500)                              null,
    metrics_path       varchar(500)                              null,
    auth_type          ENUM ('NONE', 'BASIC','BEARER','API_KEY') not null,
    username           varchar(100),
    password           varchar(100),
    token              varchar(5000),
    tags               varchar(500),
    tls                boolean default false                     not null,
    discoverable       boolean default false                     not null,
    connection_timeout int     default 5000                      not null,
    read_timeout       int     default 5000                      not null,
    write_timeout      int     default 5000                      not null,
    created_at         datetime                                  not null,
    modified_at        datetime,
    description        varchar(1000),
    constraint nk$infrastructure_service$natural_id unique key (natural_id)
) ENGINE = InnoDB;

create table infrastructure_server_to_service
(
    server_id  integer not null,
    service_id integer not null,
    constraint pk$infrastructure_server_to_service primary key (server_id, service_id),
    constraint fk$infrastructure_server_to_service$server foreign key (server_id) references infrastructure_server (id),
    constraint fk$infrastructure_server_to_service$service foreign key (service_id) references infrastructure_service (id)
) ENGINE = InnoDB;

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