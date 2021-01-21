/*
 * from the created user, create the package table
 */
create table package (id number(19,0) generated as identity, appname varchar2(255 char), path varchar2(255 char), valid number(1,0) not null, version number(10,0) not null, primary key (id)) tablespace USERS;