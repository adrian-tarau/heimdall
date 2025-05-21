create table provider
(
    id                              int                 not null primary key auto_increment,
    natural_id                      varchar(100)        not null,
    name                            varchar(100)        not null,
    uri                             varchar(1000),
    api_key                         varchar(500),
    author                          varchar(100)        default '',
    tags                            varchar(500),
    license                         varchar(1000)       default 'Proprietary',
    version                         varchar(50)         default '',
    description                     varchar(1000)
) ENGINE = InnoDB;

create table model
(
    id                              int                not null primary key auto_increment,
    natural_id                      varchar(100)       not null,
    provider_id                     int                not null,
    name                            varchar(100)       not null,
    uri                             varchar(1000),
    api_key                         varchar(500),
    model_name                      varchar(100),
    temperature                     decimal,
    top_p                           decimal,
    top_k                           int,
    frequency_penalty               decimal,
    presence_penalty                decimal,
    maximum_output_tokens           int,
    stop_sequences                  varchar(1000)       not null,
    response_format                 enum('TEXT','JSON') default 'TEXT',
    tags                            varchar(500),
    description                     varchar(1000),
    constraint nk$model$natural_id unique key (natural_id),
    constraint fk$model$provider_id foreign key (provider_id) references provider (id)
) ENGINE = InnoDB;