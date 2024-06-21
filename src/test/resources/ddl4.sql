create table if not exists `tb`
(
    id   bigint comment 'pk',
    name varchar(30) null default 'abc' comment '',
    primary key (id)
);