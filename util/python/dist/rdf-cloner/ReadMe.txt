1. Introduction
---------------------------------------------------------------------------------------------------
This is a utility script used to copy entire RDF Graphs,
with or without data, from a source to a target, which can be separate servers.

It is wise to first run this script against a harmless sandbox target, such as a local Virtuoso server
that no one else uses or at least a dedicated database on a shared server. Once a successful cloning
in such an environment is completed, you can proceed to higher targets.


2. Local Virtuoso server installation
---------------------------------------------------------------------------------------------------
2.1. Download an install package or pre-built binary if on Windows from https://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VOSDownload.

2.2. Extract the ZIP file in your default or preferred system location. Determine the root location for your Virtuoso installation. For best results, we recommend putting the ZIP (and/or the directory created upon its extraction) into the C:/Program Files/ (or C:/Program Files (x86)/, for a 32-bit Virtuoso on 64-bit Windows) directory.

2.3. Create a new system environment variable called VIRTUOSO_HOME, with this path (e.g., C:/Program Files/OpenLink Software/VOS7/virtuoso-opensource/) for its value. 

2.4 Add the following string to the end of the existing PATH value: ;%VIRTUOSO_HOME%/bin;%VIRTUOSO_HOME%/lib

2.5.. You may need to restart your machine in order for these environment variable changes to take place.

2.6. You should now be able to start your local virtuoso instance by opening a command line and executing: virtuoso-t -f

2.7. By default, the Virtuoso server will listen for HTTP connections at TCP port 8890, and for SQL data access at TCP port 1111. These ports may be changed by editing the virtuoso.ini file.

2.8. Once started, you should be able to access the virtuoso Conductor interface at http://localhost:8890/conductor. Using the default username and password dba:dba, you should be able to access all of the Conductor features and services for your local instance.

2.9. For this and additional instructions on installation of virtuoso on a local windows platform, visit https://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VOSUsageWindows.


3. Configuring Local virtuoso.ini file
---------------------------------------------------------------------------------------------------
3.1. You will need to make a couple minor adjustments to your local virtuoso.ini configuration file, now found in %VIRTUOSO_HOME%/database.

3.2. If you want examples of other .ini files used for configuring virtuoso environments, you can checkout the 'build' project from svn at svn+ssh://{username}@fitbir-dev-repo.cit.nih.gov/tbi_dev/source/branches/{branch of choice}/build. Within the build project, navigate to env/build/uni/stack/virtuoso. There you will find several .ini files. You may use that to compare with your local default .ini file.

3.3. The primary property you are concerned with modifying in the local virtuoso.ini file is 'DirsAllowed'. This is where the imported data and sparql files will be placed as you copy data. For example, I created an 'import' directory under my virtuoso parent directory like so: C:\virtuoso\import. Therefore, my .ini file contained: "DirsAllowed	= ., ../vad, C:\virtuoso\import"

3.4. You will also need to ensure that the ports specified in the local .ini configuration file are what you expect to use. E.g. ServerPort	= 1111 & ServerPort	= 8890 should be the defaults.

3.5. If you see errors concerning the local configuration file when starting virtuoso, you can copy the virtuoso.ini file into the /bin directory of your virtuoso installation to have it run by default, or you can specify you configuration file by running: virtuoso-t +configfile {path to your .ini file}


4. Checkout util Project and Python Configuration
---------------------------------------------------------------------------------------------------
4.1. In order to get the rdf cloner python script, you will need to check out the 'util' project from svn at svn+ssh://{username}@fitbir-dev-repo.cit.nih.gov/tbi_dev/source/branches/{branch of choice}/util.

4.2. Within the util project, you can find the rdf cloner script at /python/dist/rdf-cloner/pgCloner.py. Configuration files are in /python/dist/rdf-cloner/conf.

4.3. If on Windows, a minor change to the rdfCloner.py script is needed. On line 1 of the .py script you will see the following code: #!/bin/python3.  This must be changed to #!/usr/bin/python3.

4.4. You will also need to create a local property configuration file for the rdfCloner. You can do this by copying one of the existing configuration files (e.g. stage-build.dict or stage-local.dict) in the /python/dist/rdf-cloner/conf folder in the util project path. Rename the .dict file (e.g. myconf.dict) and configure the properties to fit your local platform. 'source' is the server you are copying data from, 'datadir' is the local path you are importing to, 'isqlPort' is the default port that the script will execute sparql queries against to import data into your local virtuoso platform.
Ex.)
[DEFAULT]
source=http://ibis-rdfstore-stage.cit.nih.gov:8890/sparql/
graph=http://ninds.nih.gov:8080/allTriples.ttl
datadir=C:\\virtuoso\\import
isqlPort=1111
batchSize=7500
target=http://localhost:8890/sparql/
logBase=C:\\virtuoso\\log\\rdfCloner
procName=myconf


5. Python Installation
---------------------------------------------------------------------------------------------------
5.1. Download and install Python 3.6.1 (or later) from https://www.python.org/downloads/

5.2. Execute the installer & setup application for Python.

5.3. Create the following system environment variable named PYTHONPATH with the value: %BRICS_HOME%\util\python\common. In this case, %BRICS_HOME% is the path to your local parent directory for your brics project. E.g. C:\Development\WORKSPACES\3.2-CRIT\util\python\common. Reminder, environment variable changes may require a restart of your machine.

5.4. Before running the rdfCloner.py script, you will need to install the following python modules: 'requests', 'psycopg2' and 'CommonUtil'. 

5.5 The requests and psycopg2 modules may be installed by executing the following commands: python -m pip install requests. And python -m pip install psycopg2.

5.5 The CommonUtil module will require the specification of your PYTHONPATH, by executing the following command: python -m pip install CommonUtil -t %PYTHONPATH%


6. Running the rdfCloner
---------------------------------------------------------------------------------------------------
6.1. You should now be ready to run the rdCloner.py script.

6.2. First, make sure your local virtuoso instance is started. You can check this by opening http://localhost:8890/conductor/ in your browser.

6.3. Then, run the following in a command line: ${BRICS_HOME}/util/python/dist/rdf-cloner/rdfCloner.py --props=.\conf\myconf.dict *NOTE: again, here ${BICS_HOME} represents your local parent directory for brics projects.

6.4. Wait. Typically if on-site this process will take roughly 1.5 hours. If remote, it can take up to 2.5 hrs. You will see output as each batch is executed and imported. Keep track of any error messages. For example, if the import process is failing, stop the process and troubleshoot the issue. Don't wait the full copy time only to have to execute the entire script again.


7. Quick Notes and Troubleshooting
---------------------------------------------------------------------------------------------------
7.1. Start virtuoso w/ config file : "virtuoso-t +configfile ${CONFIGFILE}"

7.2. Install python module : "python -m pip install ${PLUGIN}"

7.3. Execute rdfCloner : "${BRICS_HOME}/util/python/dist/rdf-cloner/rdfCloner.py --props=${CONFIGFILE}"

7.4. "The rdfCloner.py script does not seem to be retrieving properties from the local configuration file. Symptom is KeyErrors thrown from the script." - When running the command with "--props=.\conf\myconf.dict", ensure you have navigated to the same directory as rdfCloner.py first. Otherwise the ".\" will look in the wrong directory for the configuration file. Alternatively, you can specify the full path to you configuration file.

7.5. "Receiving the error message 'isql is not recognized...' during the import phase of the rdfCloner script." - Ensure that you have added "%VIRTUOSO_HOME%\bin to the PATH system environment variable. This error is caused by the script not being able to find the isql.exe executable file. Make sure to restart your machine after modifying environment variables. To double-check your a variable, you can execute "echo %VARIABLENAME%", and the path assigned to that variable name should be printed.