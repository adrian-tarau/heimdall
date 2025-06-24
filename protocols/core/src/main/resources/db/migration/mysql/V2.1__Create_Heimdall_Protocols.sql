create table protocol_addresses
(
    id          integer                             not null auto_increment primary key,
    name        varchar(200)                        not null,
    `type`      ENUM ('EMAIL', 'HOSTNAME', 'OTHER') not null,
    `value`     varchar(500)                        not null,
    created_at  datetime                            not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;

create unique index value on protocol_addresses (`value`);

create table protocol_parts
(
    id         integer                                    not null auto_increment primary key,
    type       ENUM ('BODY', 'ATTACHMENT')                not null,
    name       varchar(500),
    file_name  varchar(200),
    length     integer                                    not null,
    mime_type  ENUM ('TEXT_PLAIN','TEXT_CSS','TEXT_JAVASCRIPT','TEXT_HTML','TEXT_XML','TEXT'
        ,'IMAGE_PNG','IMAGE_JPEG','IMAGE_GIF','IMAGE_TIFF','IMAGE_BMP','IMAGE','FONT'
        ,'APPLICATION_JSON','APPLICATION_OCTET_STREAM')
                       default 'APPLICATION_OCTET_STREAM' not null,
    resource   varchar(500)                               not null,
    merged     boolean default false                      not null,
    created_at datetime                                   not null
) ENGINE = InnoDB;
