# Query Tool API

Query Tool API is a micro-service for accessing features available in the query tool, through well-documented RESTful API endpoints.


## Getting Started

These instructions will help start running this project locally in Eclipse.


### Prerequisites

```
JDK EE 1.8
Eclipse
Testng Plug-in
Spring Boot Eclipse Plug-in (optional)
```


### Importing The Project

In Eclipse, File -> Import -> Existing Maven Project, point the wizard to the root directory of the project (i.e. the folder that has the parent pom.xml).  Make sure query-api is checked and import.  After that, Eclipse should automatically set it up as a Maven project and pull in all the required dependencies.


### Configuring the Project

This project requires that conf.dir be fined in the VM arguments, so the application is able to find the properties files.  In Eclipse, right-click on the query-api project -> Run -> Run Configuration -> Spring Boot App -> Arguments tab.  
Add the following to the end of the <b>VM arguments</b> text area:

```
-Dconf.dir=<path to properties directory> 
```

### Properties files

This project uses the same properties folder structure as the other microservices.  This project only requires <b>meta.properties</b> and <b>qt-rdf.properties</b> for each tenant.  In the properties directory, the folder structure should be the following:

```
.
├── datasources
	└── pdbp
		├── meta.properties
		└── qt-rdf.properties
	└── fitbir
		├── meta.properties
		└── qt-rdf.properties
	
```