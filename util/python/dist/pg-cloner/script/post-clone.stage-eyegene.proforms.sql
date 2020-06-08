
--set email to a general address so users don’t get emails
update usr
set email = 'dcb@dcbstage.com'
where username !='administrator';


--update url for proforms
update db_metadata
set web_server_url_base ='https://cdrns-stage.cit.nih.gov/proforms';


-- perform also the necessary steps below on the target database
-- check proforms public schema for public role permissions
	- right click on public schema and select properties
	- click on privileges tab
	- in the privileges section, confirm public is the role and check "USAGE" and "CREATE"
	- click the Add/Change button
	- click OK
-- modify proforms functions and trigger functions owner
	- right click on each one and select properties, then change the owner and click OK.
