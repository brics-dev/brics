package gov.nih.tbi;

import org.springframework.beans.factory.annotation.Value;

public class RdfGenConstants {

	private static final String DEFAULT_RDF_FILE_EXPORT = "/opt/apache-tomcat/rdf-exports/";
	private static final Integer DEFAULT_RDF_SQL_LIMIT = 20000;
	private final String DEFAULT_RDFGEN_LOG_PATH = "/opt/apache-tomcat/logs/rdfgen.log";
	private final String DEFAULT_TEMP_RDF_DIRECTORY = "/tmp/rdfgen/";

	@Value("#{applicationProperties['rdfgen.file.path']}")
	private String rdfFileExportPath;

	@Value("#{applicationProperties['rdfgen.sql.limit']}")
	private Integer rdfSQLLimit;

	@Value("#{applicationProperties['rdfgen.generate.all']}")
	public Boolean isRdfGenerateAll;

	@Value("#{applicationProperties['rdfgen.log.path']}")
	public String rdfGenLogPath;

	@Value("#{applicationProperties['rdfgen.temp.dir']}")
	private String rdfGenTempDirectory;

	@Value("#{applicationProperties['rdfgen.doUpload']}")
	private Boolean modulesRdfDoUpload;

	@Value("#{applicationProperties['rdfgen.thread.limit']}")
	private Integer threadLimit;

	@Value("#{applicationProperties['rdfgen.business.start.hour']}")
	private Integer businessStartHour;

	@Value("#{applicationProperties['rdfgen.business.end.hour']}")
	private Integer businessEndHour;

	@Value("#{applicationProperties['rdfgen.mdsupdrsx.form.name']}")
	private String mdsUpdrsXName;

	public String getMdsUpdrsXName() {
		
		if(mdsUpdrsXName == null) {
			return "MDS_UPDRS_X";
		}
		
		return mdsUpdrsXName;
	}

	public Integer getThreadLimit() {
		return threadLimit;
	}

	public String getExportPath() {

		if (rdfFileExportPath == null || rdfFileExportPath.isEmpty()) {
			rdfFileExportPath = DEFAULT_RDF_FILE_EXPORT;
		}

		return rdfFileExportPath;
	}

	public Integer getSqlLimit() {

		if (rdfSQLLimit == null || rdfSQLLimit <= 0) {
			rdfSQLLimit = DEFAULT_RDF_SQL_LIMIT;
		}

		return rdfSQLLimit;
	}

	public Boolean getGenerateAll() {

		if (isRdfGenerateAll == null) {
			return true;
		} else {
			return isRdfGenerateAll;
		}
	}

	public Boolean getDoUpload() {
		if (modulesRdfDoUpload == null) {
			return false;
		} else {
			return modulesRdfDoUpload;
		}
	}

	public String getLogPath() {
		if (rdfGenLogPath == null) {
			rdfGenLogPath = DEFAULT_RDFGEN_LOG_PATH;
		}

		return rdfGenLogPath;
	}

	public String getTempExportDirectory() {
		if (rdfGenTempDirectory == null) {
			rdfGenTempDirectory = DEFAULT_TEMP_RDF_DIRECTORY;
		}

		return rdfGenTempDirectory;
	}

	public Integer getBusinessStartHour() {
		return businessStartHour;
	}

	public Integer getBusinessEndHour() {
		return businessEndHour;
	}
}
