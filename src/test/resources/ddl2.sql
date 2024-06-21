create table tbb
(
    id          bigint auto_increment comment 'auto increment id'
        primary key,
    host_name   varchar(64) default ''                not null comment 'host name',
    port        varchar(64) default ''                not null comment 'port',
    type        int         default 0                 not null comment 'node type: ACTUAL or CONTAINER',
    launch_date timestamp   default CURRENT_TIMESTAMP not null comment 'launch date',
    modify_time timestamp   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'modified time',
    create_time timestamp   default CURRENT_TIMESTAMP not null comment 'created time'
)
    comment 'DB WorkerID Assigner for UID Generator' engine = InnoDB
                                                     charset = utf8;

create table if not exists `tb`
(
    id   bigint,
    name varchar(20) null default 'abc' comment '',
    primary key (id)
);