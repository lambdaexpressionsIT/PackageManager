/*
 * from the created user, create the package table
 */
create table package
(
    id            number(19,0) generated as identity,
    packagename   varchar2(255 char),
    appname       varchar2(255 char),
    version       varchar2(255 char),
    filename      varchar2(255 char),
    path          varchar2(255 char),
    valid         number(1,0) not null,
    versionnumber number(19,0) not null,
    primary key (id)
)