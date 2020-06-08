--obfuscete user emails
update tbi_user
set email = 'dcb@dcbdev.com'
where id != 2;

--delete useless data
DELETE FROM download_file_dataset;
DELETE FROM download_file;
DELETE FROM download_package;


delete from datafile_endpoint_info;
--SFTP info for host server
insert into datafile_endpoint_info
values ('eyegene_sftp_server', 'eyegene-host-stage.cit.nih.gov', 'eyegene_data_drop', '<CHANGEME>', null, 22, 1);
--SFTP info for documents server
insert into datafile_endpoint_info
values ('documents_sftp_server', 'eyegene-host-stage.cit.nih.gov', 'eyegene_data_drop', '<CHANGEME>', null, 22, 2);


--run this to get the salted password and update
--corresponding modules.properties
select encode(password, 'hex') from account where user_name = 'administrator';