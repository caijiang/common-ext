
create table if not exists `tb`
(
    id   bigint comment 'pk',
    name varchar(20) null default 'abc' comment '',
    primary key (id)
);