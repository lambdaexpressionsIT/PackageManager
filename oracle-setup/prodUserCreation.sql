/*
 * from SYS user, create an user that can create a table
 */

CREATE USER package_manager_user IDENTIFIED BY "pAcMaN_1337_@$!";
GRANT CREATE SESSION to package_manager_user;
grant create table to package_manager_user;
grant resource to package_manager_user;
alter user package_manager_user quota unlimited on users;