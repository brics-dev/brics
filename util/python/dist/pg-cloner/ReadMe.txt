1. Introduction
---------------------------------------------------------------------------------------------------
This is a utility script used to copy entire Postgresql database schemas,
with or without data, from a source to a target, which can be separate servers.

It is wise to first run this script against a harmless sandbox target, such as a local Postgres server
that no one else uses or at least a dedicated database on a shared server. Once a successful cloning
in such an environment is completed, you can proceed to higher targets.


2. Local Postgres server installation
---------------------------------------------------------------------------------------------------
2.1. Get an install package from www.postgresql.org/download/.

2.2. Install in your default or preferred system location. Depending on your OS, you may need to do
an extra step to register Postgres as a service to run automatically unless your OS-specific installer
does that for you (Windows does). Disable that if you prefer to run your Postgres server manually.

2.3. Make sure your bin directory is in the path so that you can run psql.

2.4. It is advised that you do not supply password to psql interactively but to keep them in .pgpass file.
In order for psql and other Postgres clients (such as psycopg from Python) to see the login credentials
in .pgpass, the path to the file needs to be in PGPASSFILE environment variable.
E.g. if the .pgpass file is in D:\CONF\Postgres\.pgpass, your PGPASSFILE variable needs to be set to that path.

The format of .pgpass is as follows:

fitbir-db-stage.cit.nih.gov:5432:tbi_stage:tbi_reposstg:YOUR_PASSWORD
localhost:5432:postgres:postgres:LOCAL_SA_PASSWORD
localhost:5432:somelocaldb:somelocaluser:YOUR_PASSWORD

Then when you run "psql -h localhost -U postgres", it will log you in as SA without asking for your password.
The same works for any other connection configuration.

3. Create local Postgres configurations
---------------------------------------------------------------------------------------------------

3.1. psql -h localhost -U postgres

3.2. If you are not in postgres db (which the prompt should display), run:

        \connect postgres

3.3. Create a user:

        CREATE USER someuser WITH PASSWORD 'somepass';

3.4. Create a DB:

        CREATE DATABASE somedb OWNER someuser;

3.5. Connect to the newly created database:

        \connect somedb

3.6. Create a schema:

        CREATE SCHEMA someschema AUTHORIZATION someuser;

3.7. Assign the newly created schema to be your user's default search path (to not have to prefix table names):

        ALTER ROLE someuser SET search_path = someschema;

3.8. Add the new configuration to your .pgpass file:

        localhost:5432:somedb:someuser:somepass


4. Script usage and configuration
---------------------------------------------------------------------------------------------------

4.1. Usage is:

        pgCloner.py --props=path/to/config.file

4.2. The config file formet is ini file:

        [SOURCE]
        host=SOURCE.HOST
        database=SOURCE.DB
        schema=SOURCE.SCHEMA
        user=SOURCE.USER

        [TARGET]
        host=TARGET.HOST
        database=TARGET.DB
        schema=TARGET.SCHEMA
        user=TARGET.USER

        [DEFAULT]
        logbase=/path/to/log/file

The authentication for both source and target are expected to be defined in .pgpass