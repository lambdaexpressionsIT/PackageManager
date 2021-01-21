/*
 * from SYS user, create an user that can create a table
 */

CREATE USER package_manager IDENTIFIED BY "pAcMaN_1337_@$!";
GRANT CREATE SESSION to package_manager;
grant create table to package_manager;
grant resource to package_manager;
alter user package_manager quota unlimited on users;