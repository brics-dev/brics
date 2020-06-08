package gov.nih.tbi.constants;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;

@Component
@Scope("singleton")
public class ApplicationConstants implements Serializable {

	private static final long serialVersionUID = -6283943072431073376L;

	private static final String DEFAULT_PORTAL_ROOT = "portal";
	private static final String DEFAULT_REPORTING_ROOT = "reporting";
	public static final Long DEFAULT_PROVIDER = -1L;
	// return null if server url is not defined
	private static final String DEFAULT_SERVER_URL = "";
	private static final String DEFAULT_RDF_FILE_EXPORT = "C:\\RDFExport\\";
	private static final Integer DEFAULT_RDF_SQL_LIMIT = 20000;
	private static final String DEFAULT_REPORTING_LOCAL_USER = "administrator";
	private static final String DEFAULT_QUERY_TOOL_ROOT = "query";
	private static final String ORG_EMAIL_DELIMITER = ";";

	public static final String NIH_PASSWORD_HINT_URL_PROPERTY = "renewPassword.passwordHints";
	public static final String RENEW_PASSWORD_SUBJECT_PROPERTY = "renewPassword.subject";
	public static final String RENEW_ACCOUNT_ROLE_SUBJECT_PROPERTY = "renewRole.subject";
	public static final String RENEW_ACCOUNT_ROLE_MESSAGE_BODY_PROPERTY = "renewRole.body";
	public static final String RENEW_ACCOUNT_ROLE_CONTENT_FOR_FITBIR_PROPERTY = "renewRole.contentForFitbir";
	public static final String RENEW_PASSWORD_URL_PROPERTY = "renewPassword.url";
	public static final String RENEW_PASSWORD_MESSAGE_BODY_PROPERTY = "renewPassword.body";
	public static final String RDF_UPLOAD_FAIL_EMAIL_SUBJECT_PROPERTY = "rdf.upload.fail.email.subject.property";
	public static final String RDF_UPLOAD_FAIL_EMAIL_MESSAGE_BODY_PROPERTY =
			"rdf.upload.fail.email.message.body.property";
	private static final String DEFAULT_DOWNLOAD_PACKAGE_DIR =
			File.separator + "tmp" + File.separator + "qtDownloadPackage" + File.separator;

	public static final String REPORTING_ROOT = "reporting";

	// Pulls from property file stored on file system (via context.xml)

	// Module Properties
	@Value("#{applicationProperties['modules.portalRoot']}")
	private String modulesPortalRoot;
	@Value("#{applicationProperties['modules.org.name']}")
	private String modulesOrgName;
	@Value("#{applicationProperties['modules.org.email']}")
	private String modulesOrgEmail;
	@Value("#{applicationProperties['modules.org.noreply']}")
	private String modulesOrgNoreply;
	@Value("#{applicationProperties['modules.org.phone']}")
	private String modulesOrgPhone;
	@Value("#{applicationProperties['modules.dev.email']}")
	private String modulesDevEmail;

	@Value("#{applicationProperties['triplanar.domain']}")
	private String triplanarHostDomain;

	// Describes if functionality is on or off
	@Value("#{applicationProperties['modules.pf.enabled']}")
	private String modulesPFEnabled;
	@Value("#{applicationProperties['modules.ddt.enabled']}")
	private String modulesDDTEnabled;
	@Value("#{applicationProperties['modules.vt.enabled']}")
	private String modulesVTEnabled;
	@Value("#{applicationProperties['modules.gt.enabled']}")
	private String modulesGTEnabled;
	@Value("#{applicationProperties['modules.st.enabled']}")
	private String modulesSTEnabled;
	@Value("#{applicationProperties['modules.udt.enabled']}")
	private String modulesUDTEnabled;
	@Value("#{applicationProperties['modules.dt.enabled']}")
	private String modulesDTEnabled;
	@Value("#{applicationProperties['modules.qt.enabled']}")
	private String modulesQTEnabled;
	@Value("#{applicationProperties['modules.reporting.enabled']}")
	private String modulesReportingEnabled;
	@Value("#{applicationProperties['modules.om.enabled']}")
	private String modulesOMEnabled;
	@Value("#{applicationProperties['modules.ms.enabled']}")
	private String modulesMSEnabled;
	@Value("#{applicationProperties['modules.account.enabled']}")
	private String modulesAccountEnabled;
	@Value("#{applicationProperties['modules.dashboard.enabled']}")
	private String modulesDashboardEnabled;
	@Value("#{applicationProperties['modules.admindashboard.enabled']}")
	private String modulesAdminDashboardEnabled;
	@Value("#{applicationProperties['modules.account.audit.enabled']}")
	private String modulesAccountAuditEnabled;

	// Describer what URL to use for the functionality
	@Value("#{applicationProperties['modules.public.url.server']}")
	private String modulesPublicURL;
	@Value("#{applicationProperties['modules.pf.url.server']}")
	private String modulesPFURL;
	@Value("#{applicationProperties['modules.ddt.url.server']}")
	private String modulesDDTURL;
	@Value("#{applicationProperties['modules.vt.url.server']}")
	private String modulesVTURL;
	@Value("#{applicationProperties['modules.gt.url.server']}")
	private String modulesGTURL;
	@Value("#{applicationProperties['modules.st.url.server']}")
	private String modulesSTURL;
	@Value("#{applicationProperties['modules.udt.url.server']}")
	private String modulesUDTURL;
	@Value("#{applicationProperties['modules.dt.url.server']}")
	private String modulesDTURL;
	@Value("#{applicationProperties['modules.qt.url.server']}")
	private String modulesQTURL;
	@Value("#{applicationProperties['modules.reporting.url.server']}")
	private String modulesReportingURL;
	@Value("#{applicationProperties['modules.om.url.server']}")
	private String modulesOMURL;
	@Value("#{applicationProperties['modules.ms.url.server']}")
	private String modulesMSURL;
	@Value("#{applicationProperties['modules.account.url.server']}")
	private String modulesAccountURL;
	@Value("#{applicationProperties['modules.rprocess.url.server']}")
	private String modulesRProcessURL;
	@Value("#{applicationProperties['modules.mipav.url.client']}")
	private String modulesMIPAVURL;
	@Value("#{applicationProperties['modules.scheduler.rdf.load.failure.notification.email']}")
	private String rdfLoadFailureNotificationEmail;
	@Value("#{applicationProperties['modules.infra.email']}")
	private String modulesInfraEmail;
	@Value("#{applicationProperties['modules.rdf.doUpload']}")
	private Boolean modulesRdfDoUpload;
	@Value("#{applicationProperties['modules.reporting.local.user']}")
	private String reportingLocalUser;

	// SFTP Properties
	@Value("#{applicationProperties['datadrop.sftp.url']}")
	private String sftpUrl;
	@Value("#{applicationProperties['datadrop.sftp.port']}")
	private Integer sftpPort;
	@Value("#{applicationProperties['datadrop.sftp.basedir']}")
	private String sftpBaseDir;
	@Value("#{applicationProperties['datadrop.sftp.user']}")
	private String sftpUser;
	@Value("#{applicationProperties['datadrop.sftp.password']}")
	private String sftpPassword;


	@Value("#{applicationProperties['modules.reporting.file.dir']}")
	private String reportingFileDir;
	@Value("#{applicationProperties['modules.reporting.savedQueries.dir']}")

	private String triplanarBaseDir;

	@Value("#{applicationProperties['reporting.downloadPackageDir']}")
	private String downloadPackageDirectory;

	// This is a list longs. The numbers represent instances that are duplicates of other instances (important when
	// writing across instances)
	@Value("#{applicationProperties['modules.duplicates']}")
	private String modulesDuplicates;

	// Mappings of disease types to URLS. Only entering one URL is possible
	private Map<Long, String> modulesOrgNameMap;
	private Map<Long, String> modulesOrgEmailMap;
	private Map<Long, String> modulesOrgNoreplyMap;
	private Map<Long, String> modulesOrgPhoneMap;

	private Map<Long, String> modulesDDTMap;
	private Map<Long, String> modulesVTMap;
	private Map<Long, String> modulesGTMap;
	private Map<Long, String> modulesSTMap;
	private Map<Long, String> modulesUDTMap;
	private Map<Long, String> modulesDTMap;
	private Map<Long, String> modulesQTMap;
	private Map<Long, String> modulesReportingMap;
	private Map<Long, String> modulesOMMap;
	private Map<Long, String> modulesMSMap;
	private Map<Long, String> modulesAccountMap;
	private Map<Long, String> modulesRProcessMap;
	private Map<Long, String> modulesPublicMap;
	private Map<Long, String> modulesPFMap;

	private List<String> modulesOrgEmailList;
	private List<Long> duplicateList;

	// Credentials for administrator account
	@Value("#{applicationProperties['modules.administrator.username']}")
	private String administratorName;
	@Value("#{applicationProperties['modules.administrator.password']}")
	private String administratorPassword;

	/**
	 * The GUID Prefixes for the Validation Tool. Developer's Note: This is a temporary fix for the validation process
	 * and should be eventually be removed. MG
	 */
	@Value("#{applicationProperties['modules.guid.prefix']}")
	private String guidPrefix;

	@Value("#{applicationProperties['modules.guid.invalidPrefix']}")
	private String guidInvPrefix;

	// Secure web services true or false (false on local only)
	// There is a hack here to support autowiring of a static field. (The set property is defined in context.xml)
	@Value("#{applicationProperties['modules.webservices.secured']}")
	private boolean wsSecuredNonStatic;
	private static boolean wsSecured;

	// RDF Generation Properties
	@Value("#{applicationProperties['modules.rdf.file.path']}")
	private String rdfFileExportPath;

	@Value("#{applicationProperties['modules.rdf.sql.limit']}")
	private Integer rdfSQLLimit;

	@Value("#{applicationProperties['modules.rdf.generate.all']}")
	public Boolean isRdfGenerateAll;

	@Value("#{applicationProperties['modules.rdfgen.log.dir']}")
	public String rdfGenLogPath;

	@Value("#{applicationProperties['modules.account.removeUnused']}")
	private Boolean eraseUnsusedAccounts;

	private static DatafileEndpointInfo dataDropEndpointInfo;

	@PostConstruct
	public void init() {

		dataDropEndpointInfo = new DatafileEndpointInfo();
		dataDropEndpointInfo.setUrl(getSftpUrl());
		dataDropEndpointInfo.setPort(getSftpPort());
		dataDropEndpointInfo.setUserName(getSftpUser());
		dataDropEndpointInfo.setPassword(getSftpPassword());
	}

	public Boolean getEraseUnsusedAccounts() {
		return eraseUnsusedAccounts;
	}

	public void setEraseUnsusedAccounts(Boolean eraseUnsusedAccounts) {
		this.eraseUnsusedAccounts = eraseUnsusedAccounts;
	}

	private final String DEFAULT_RDFGEN_LOG_PATH = "/opt/apache-tomcat/logs/rdfgen.log";

	public void setWsSet(boolean wsSecuredArg) {

		ApplicationConstants.wsSecured = wsSecuredNonStatic;
	}

	/***************
	 * 
	 * Getters for Org Properties
	 * 
	 **************/
	public String getPortalRoot() {

		if (modulesPortalRoot == null || modulesPortalRoot.isEmpty()) {
			return DEFAULT_PORTAL_ROOT;
		} else {
			return modulesPortalRoot;

		}
	}

	public String getOrgName() {

		if (modulesOrgName == null || modulesOrgName.isEmpty()) {
			return "FITBIR";
		} else {
			if (modulesOrgNameMap == null || modulesOrgNameMap.isEmpty()) {
				modulesOrgNameMap = getURLMap(modulesOrgName);
			}
			return modulesOrgNameMap.get(DEFAULT_PROVIDER);
		}
	}

	public String getModulesOrgName(Long diseaseId) {

		if (modulesOrgNameMap == null || modulesOrgNameMap.isEmpty()) {
			modulesOrgNameMap = getURLMap(modulesOrgName);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesOrgNameMap.size() == 1) {
			return modulesOrgNameMap.get(DEFAULT_PROVIDER);
		}
		return modulesOrgNameMap.get(diseaseId);
	}

	/**
	 * This function is for use of the brics-scheduler project only!
	 * 
	 * @param orgEmailString
	 * @return
	 */
	public List<String> loadOrgEmailList(String orgEmailString) {

		List<String> emailList = new ArrayList<String>();

		if (orgEmailString != null && !orgEmailString.isEmpty()) {

			String[] orgEmails = orgEmailString.split(ORG_EMAIL_DELIMITER);

			if (orgEmails.length == 0) {
				emailList.add(orgEmailString);
			}

			for (String orgEmail : orgEmailString.split(ORG_EMAIL_DELIMITER)) {
				emailList.add(orgEmail);
			}
		}

		return Collections.unmodifiableList(emailList);
	}

	/**
	 * This function is for use of the brics-scheduler project only!
	 * 
	 * @return
	 */
	public List<String> getOrgEmail() {

		if (modulesOrgEmailList == null) {
			modulesOrgEmailList = loadOrgEmailList(modulesOrgEmail);
		}

		return new ArrayList<String>(modulesOrgEmailList);
	}

	public String getModulesOrgEmail() {

		if (modulesOrgEmail == null || modulesOrgEmail.isEmpty()) {
			return "fitbir-ops@cit.nih.gov";
		} else {
			if (modulesOrgEmailMap == null || modulesOrgEmailMap.isEmpty()) {
				modulesOrgEmailMap = getURLMap(modulesOrgEmail);
			}
			return modulesOrgEmailMap.get(DEFAULT_PROVIDER);
		}
	}

	public String getModulesOrgEmail(Long diseaseId) {

		if (modulesOrgEmailMap == null || modulesOrgEmailMap.isEmpty()) {
			modulesOrgEmailMap = getURLMap(modulesOrgEmail);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesOrgEmailMap.size() == 1) {
			return modulesOrgEmailMap.get(DEFAULT_PROVIDER);
		}
		return modulesOrgEmailMap.get(diseaseId);
	}

	public String getOrgNoreply() {

		if (modulesOrgNoreply == null || modulesOrgNoreply.isEmpty()) {
			return "noreply@cit.nih.gov";
		} else {
			if (modulesOrgNoreplyMap == null || modulesOrgNoreplyMap.isEmpty()) {
				modulesOrgNoreplyMap = getURLMap(modulesOrgNoreply);
			}
			return modulesOrgNoreplyMap.get(DEFAULT_PROVIDER);
		}
	}

	public String getModulesOrgNoreply(Long diseaseId) {

		if (modulesOrgNoreplyMap == null || modulesOrgNoreplyMap.isEmpty()) {
			modulesOrgNoreplyMap = getURLMap(modulesOrgNoreply);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesOrgNoreplyMap.size() == 1) {
			return modulesOrgNoreplyMap.get(DEFAULT_PROVIDER);
		}
		return modulesOrgNoreplyMap.get(diseaseId);
	}

	public String getOrgPhone() {

		if (modulesOrgPhone == null || modulesOrgPhone.isEmpty()) {
			return "FITBIR";
		} else {
			if (modulesOrgPhoneMap == null || modulesOrgPhoneMap.isEmpty()) {
				modulesOrgPhoneMap = getURLMap(modulesOrgPhone);
			}
			return modulesOrgPhoneMap.get(DEFAULT_PROVIDER);
		}
	}

	public String getModulesOrgPhone(Long diseaseId) {

		if (modulesOrgPhoneMap == null || modulesOrgPhoneMap.isEmpty()) {
			modulesOrgPhoneMap = getURLMap(modulesOrgPhone);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesOrgPhoneMap.size() == 1) {
			return modulesOrgPhoneMap.get(DEFAULT_PROVIDER);
		}
		return modulesOrgPhoneMap.get(diseaseId);
	}

	public String getDevEmail() {

		if (modulesDevEmail == null || modulesDevEmail.isEmpty()) {
			return "FITBIR-DEV@mail.nih.gov";
		} else {
			return modulesDevEmail;
		}
	}

	/***************
	 * 
	 * Getters for Enabled
	 * 
	 **************/

	public Boolean getModulesPFEnabled() {

		if (modulesPFEnabled == null || modulesPFEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesPFEnabled);
		}
	}

	public Boolean getModulesDDTEnabled() {

		if (modulesDDTEnabled == null || modulesDDTEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesDDTEnabled);
		}
	}

	public Boolean getModulesVTEnabled() {

		if (modulesVTEnabled == null || modulesVTEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesVTEnabled);
		}
	}

	public Boolean getModulesGTEnabled() {

		if (modulesGTEnabled == null || modulesGTEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesGTEnabled);
		}
	}

	public Boolean getModulesSTEnabled() {

		if (modulesSTEnabled == null || modulesSTEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesSTEnabled);
		}
	}

	public Boolean getModulesUDTEnabled() {

		if (modulesUDTEnabled == null || modulesUDTEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesUDTEnabled);
		}
	}

	public Boolean getModulesDTEnabled() {

		if (modulesDTEnabled == null || modulesDTEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesDTEnabled);
		}
	}

	public Boolean getModulesQTEnabled() {

		if (modulesQTEnabled == null || modulesQTEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesQTEnabled);
		}
	}

	public Boolean getModulesReportingEnabled() {

		if (modulesReportingEnabled == null || modulesReportingEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesReportingEnabled);
		}
	}
	
	public Boolean getModulesOMEnabled() {

		if (modulesOMEnabled == null || modulesOMEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesOMEnabled);
		}
	}

	public Boolean getModulesMSEnabled() {

		if (modulesMSEnabled == null || modulesMSEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesMSEnabled);
		}
	}

	public Boolean getModulesAccountEnabled() {

		if (modulesAccountEnabled == null || modulesAccountEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesAccountEnabled);
		}
	}

	public Boolean getModulesAccountAuditEnabled() {

		if (modulesAccountAuditEnabled == null || modulesAccountAuditEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesAccountAuditEnabled);
		}
	}

	public Boolean getModulesDashboardEnabled() {

		if (modulesDashboardEnabled == null || modulesDashboardEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesDashboardEnabled);
		}
	}

	public Boolean getModulesAdminDashboardEnabled() {

		if (modulesAdminDashboardEnabled == null || modulesAdminDashboardEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesAdminDashboardEnabled);
		}
	}

	/***************
	 * 
	 * Getters for URL
	 * 
	 **************/

	public String getModulesPublicURL() {

		if (modulesPublicURL == null || modulesPublicURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesPublicMap == null || modulesPublicMap.isEmpty()) {
				modulesPublicMap = getURLMap(modulesPublicURL);
			}
			return modulesPublicMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
	}

	public String getModulesPublicURL(Long diseaseId) {

		if (modulesPublicMap == null || modulesPublicMap.isEmpty()) {
			modulesPublicMap = getURLMap(modulesPublicURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesPublicMap.size() == 1) {
			return modulesPublicMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
		return modulesPublicMap.get(diseaseId);
	}

	public Map<Long, String> getModulesPublicMap() {

		if (modulesPublicMap == null || modulesPublicMap.isEmpty()) {
			modulesPublicMap = getURLMap(modulesPublicURL);
		}
		return modulesPublicMap;
	}

	public String getModulesPFURL() {

		if (modulesPFURL == null || modulesPFURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesPFMap == null || modulesPFMap.isEmpty()) {
				modulesPFMap = getURLMap(modulesPFURL);
			}
			return modulesPFMap.get(DEFAULT_PROVIDER);
		}
	}

	public String getModulesPFURL(Long diseaseId) {

		if (modulesPFMap == null || modulesPFMap.isEmpty()) {
			modulesPFMap = getURLMap(modulesPFURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesPFMap.size() == 1) {
			return modulesPFMap.get(DEFAULT_PROVIDER);
		}
		return modulesPFMap.get(diseaseId) + DEFAULT_PORTAL_ROOT;
	}

	public Map<Long, String> getModulesPFMap() {

		if (modulesPFMap == null || modulesPFMap.isEmpty()) {
			modulesPFMap = getURLMap(modulesPFURL);
		}
		return modulesPFMap;
	}

	public String getModulesDDTURL() {

		if (modulesDDTURL == null || modulesDDTURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesDDTMap == null || modulesDDTMap.isEmpty()) {
				modulesDDTMap = getURLMap(modulesDDTURL);
			}
			return modulesDDTMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
	}

	public String getModulesDDTURL(Long diseaseId) {

		if (modulesDDTMap == null || modulesDDTMap.isEmpty()) {
			modulesDDTMap = getURLMap(modulesDDTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesDDTMap.size() == 1) {
			return modulesDDTMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
		return modulesDDTMap.get(diseaseId);
	}

	public Map<Long, String> getModulesDDTMap() {

		if (modulesDDTMap == null || modulesDDTMap.isEmpty()) {
			modulesDDTMap = getURLMap(modulesDDTURL);
		}
		return modulesDDTMap;
	}





	public Map<Long, String> getModulesQTMap() {

		if (modulesQTMap == null || modulesQTMap.isEmpty()) {
			modulesQTMap = getURLMap(modulesQTURL);
		}
		return modulesQTMap;
	}

	public String getModulesQTURL() {

		if (modulesQTURL == null || modulesQTURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesQTMap == null || modulesQTMap.isEmpty()) {
				modulesQTMap = getURLMap(modulesQTURL);
			}
			return modulesQTMap.get(DEFAULT_PROVIDER) + DEFAULT_QUERY_TOOL_ROOT;
		}
	}

	public String getModulesQTURL(Long diseaseId) {

		if (modulesQTMap == null || modulesQTMap.isEmpty()) {
			modulesQTMap = getURLMap(modulesQTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesQTMap.size() == 1) {
			return modulesQTMap.get(DEFAULT_PROVIDER) + DEFAULT_QUERY_TOOL_ROOT;
		}
		return modulesQTMap.get(diseaseId);
	}


	public String getModulesReportingURL() {

		if (modulesReportingURL == null || modulesReportingURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesReportingMap == null || modulesReportingMap.isEmpty()) {
				modulesReportingMap = getURLMap(modulesReportingURL);
			}
			return modulesReportingMap.get(DEFAULT_PROVIDER) + DEFAULT_REPORTING_ROOT;
		}
	}

	public String getModulesReportingURL(Long diseaseId) {

		if (modulesReportingMap == null || modulesReportingMap.isEmpty()) {
			modulesReportingMap = getURLMap(modulesReportingURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesReportingMap.size() == 1) {
			return modulesReportingMap.get(DEFAULT_PROVIDER) + DEFAULT_REPORTING_ROOT;
		}
		return modulesReportingMap.get(diseaseId);
	}

	public Map<Long, String> getModulesReportingMap() {

		if (modulesReportingMap == null || modulesReportingMap.isEmpty()) {
			modulesReportingMap = getURLMap(modulesReportingURL);
		}
		return modulesReportingMap;
	}
	
	public String getModulesOMURL() {

		if (modulesOMURL == null || modulesOMURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesOMMap == null || modulesOMMap.isEmpty()) {
				modulesOMMap = getURLMap(modulesOMURL);
			}
			return modulesOMMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
	}

	public String getModulesOMURL(Long diseaseId) {

		if (modulesOMMap == null || modulesOMMap.isEmpty()) {
			modulesOMMap = getURLMap(modulesOMURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesOMMap.size() == 1) {
			return modulesOMMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
		return modulesOMMap.get(diseaseId);
	}

	public Map<Long, String> getModulesOMMap() {

		if (modulesOMMap == null || modulesOMMap.isEmpty()) {
			modulesOMMap = getURLMap(modulesOMURL);
		}
		return modulesOMMap;
	}

	public String getModulesMSURL() {

		if (modulesMSURL == null || modulesMSURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesMSMap == null || modulesMSMap.isEmpty()) {
				modulesMSMap = getURLMap(modulesMSURL);
			}
			return modulesMSMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
	}

	public String getModulesMSURL(Long diseaseId) {

		if (modulesMSMap == null || modulesMSMap.isEmpty()) {
			modulesMSMap = getURLMap(modulesMSURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesMSMap.size() == 1) {
			return modulesMSMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
		return modulesMSMap.get(diseaseId);
	}

	public Map<Long, String> getModulesMSMap() {

		if (modulesMSMap == null || modulesMSMap.isEmpty()) {
			modulesMSMap = getURLMap(modulesMSURL);
		}
		return modulesMSMap;
	}

	public String getModulesVTURL() {

		if (modulesVTURL == null || modulesVTURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesVTMap == null || modulesVTMap.isEmpty()) {
				modulesVTMap = getURLMap(modulesVTURL);
			}
			return modulesVTMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
	}

	public String getModulesVTURL(Long diseaseId) {

		if (modulesVTMap == null || modulesVTMap.isEmpty()) {
			modulesVTMap = getURLMap(modulesVTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesVTMap.size() == 1) {
			return modulesVTMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
		return modulesVTMap.get(diseaseId);
	}

	public Map<Long, String> getModulesVTMap() {

		if (modulesVTMap == null || modulesVTMap.isEmpty()) {
			modulesVTMap = getURLMap(modulesVTURL);
		}
		return modulesVTMap;
	}

	public String getModulesGTURL() {

		if (modulesGTURL == null || modulesGTURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesGTMap == null || modulesGTMap.isEmpty()) {
				modulesGTMap = getURLMap(modulesGTURL);
			}
			return modulesGTMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
	}

	public String getModulesGTURL(Long diseaseId) {

		if (modulesGTMap == null || modulesGTMap.isEmpty()) {
			modulesGTMap = getURLMap(modulesGTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesGTMap.size() == 1) {
			return modulesGTMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
		return modulesGTMap.get(diseaseId);
	}

	public Map<Long, String> getModuleGTMap() {

		if (modulesGTMap == null || modulesGTMap.isEmpty()) {
			modulesGTMap = getURLMap(modulesGTURL);
		}
		return modulesGTMap;
	}

	public String getModulesSTURL() {

		if (modulesSTURL == null || modulesSTURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesSTMap == null || modulesSTMap.isEmpty()) {
				modulesSTMap = getURLMap(modulesSTURL);
			}
			return modulesSTMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
	}

	public String getModulesSTURL(Long diseaseId) {

		if (modulesSTMap == null || modulesSTMap.isEmpty()) {
			modulesSTMap = getURLMap(modulesSTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesSTMap.size() == 1) {
			return modulesSTMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
		return modulesSTMap.get(diseaseId);
	}

	public Map<Long, String> getModulesSTMap() {

		if (modulesSTMap == null || modulesSTMap.isEmpty()) {
			modulesSTMap = getURLMap(modulesSTURL);
		}
		return modulesSTMap;
	}

	public String getModulesUDTURL() {

		if (modulesUDTURL == null || modulesUDTURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesUDTMap == null || modulesUDTMap.isEmpty()) {
				modulesUDTMap = getURLMap(modulesUDTURL);
			}
			return modulesUDTMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
	}

	public String getModulesUDTURL(Long diseaseId) {

		if (modulesUDTMap == null || modulesUDTMap.isEmpty()) {
			modulesUDTMap = getURLMap(modulesUDTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesUDTMap.size() == 1) {
			return modulesUDTMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
		return modulesUDTMap.get(diseaseId);
	}

	public Map<Long, String> getModuleUDTMap() {

		if (modulesUDTMap == null || modulesUDTMap.isEmpty()) {
			modulesUDTMap = getURLMap(modulesUDTURL);
		}
		return modulesUDTMap;
	}

	public String getModulesDTURL() {

		if (modulesDTURL == null || modulesDTURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesDTMap == null || modulesDTMap.isEmpty()) {
				modulesDTMap = getURLMap(modulesDTURL);
			}
			return modulesDTMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
	}

	public String getModulesDTURL(Long diseaseId) {

		if (modulesDTMap == null || modulesDTMap.isEmpty()) {
			modulesDTMap = getURLMap(modulesDTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesDTMap.size() == 1) {
			return modulesDTMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
		return modulesDTMap.get(diseaseId);
	}

	public Map<Long, String> getModulesDTMap() {

		if (modulesDTMap == null || modulesDTMap.isEmpty()) {
			modulesDTMap = getURLMap(modulesDTURL);
		}
		return modulesDTMap;
	}

	public String getModulesAccountURL() {

		if (modulesAccountURL == null || modulesAccountURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesAccountMap == null || modulesAccountMap.isEmpty()) {
				modulesAccountMap = getURLMap(modulesAccountURL);
			}
			return modulesAccountMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
	}

	public String getModulesAccountURL(Long diseaseId) {

		if (modulesAccountMap == null || modulesAccountMap.isEmpty()) {
			modulesAccountMap = getURLMap(modulesAccountURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesAccountMap.size() == 1) {
			return modulesAccountMap.get(DEFAULT_PROVIDER) + DEFAULT_PORTAL_ROOT;
		}
		return modulesAccountMap.get(diseaseId);
	}
	
	public String getRProcessURL() {

		if (modulesRProcessURL == null || modulesRProcessURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesRProcessMap == null || modulesRProcessMap.isEmpty()) {
				modulesRProcessMap = getURLMap(modulesRProcessURL);
			}
			return modulesRProcessMap.get(DEFAULT_PROVIDER);
		}
	}

	public String getRProcessURL(Long diseaseId) {

		if (modulesRProcessMap == null || modulesRProcessMap.isEmpty()) {
			modulesRProcessMap = getURLMap(modulesRProcessURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesRProcessMap.size() == 1) {
			return modulesRProcessMap.get(DEFAULT_PROVIDER);
		}
		return modulesRProcessMap.get(diseaseId);
	}

	public String getMIPAVURL() throws RuntimeException {

		if (modulesMIPAVURL == null || modulesMIPAVURL.isEmpty()) {
			throw new RuntimeException("No MIPAV client specified in properties!");
		}
		return modulesMIPAVURL;
	}

	public Map<Long, String> getModulesAccountMap() {

		if (modulesAccountMap == null || modulesAccountMap.isEmpty()) {
			modulesAccountMap = getURLMap(modulesAccountURL);
		}
		return modulesAccountMap;
	}

	public List<Long> getDuplicateModules() {

		if (duplicateList != null) {
			return duplicateList;
		}

		// Parse the duplicateList and store in memory
		duplicateList = new ArrayList<Long>();
		if (modulesDuplicates == null || modulesDuplicates.equals(ReportingConstants.EMPTY_STRING)) {
			return duplicateList;
		}
		String[] dupArray = modulesDuplicates.split(",");
		for (String s : dupArray) {
			duplicateList.add(Long.valueOf(s));
		}

		return duplicateList;
	}

	public boolean isDuplicateModule(Long diseaseId) {

		return getDuplicateModules().contains(diseaseId);
	}

	public String getAdministratorUsername() {

		return administratorName;
	}

	public String getAdministratorPassword() {

		return administratorPassword;
	}

	/**
	 * Takes a string A,X;B,Y;C,Z... and creates a map where A,B,C are Long and XYZ are String. A string that is not a
	 * mapping (module only contains 1 URL) will create a map with the element in ServiceConstants.DEFAULT_PROVIDER.
	 * 
	 * 
	 * @param inputString
	 * @return
	 */
	private Map<Long, String> getURLMap(String inputString) {

		Map<Long, String> map = new LinkedHashMap<Long, String>();
		if (!inputString.contains(",") && !inputString.contains(";")) {
			// Case: The string is not in mapping format. It is just a single URL
			map.put(DEFAULT_PROVIDER, inputString);
		} else {
			for (String keyValue : inputString.split(" *; *")) {
				String[] pairs = keyValue.split(" *, *", 2);
				map.put(Long.valueOf(pairs[0]), pairs.length == 1 ? "" : pairs[1]);
				// The first item in the map also gets put with key: -1
				if (map.get(DEFAULT_PROVIDER) == null) {
					map.put(DEFAULT_PROVIDER, pairs.length == 1 ? "" : pairs[1]);
				}
			}
		}
		return map;
	}

	public static boolean isWebservicesSecured() {

		return wsSecured;
	}

	/***********************************************
	 * 
	 * Developer's Note: The getters and setters below are for Validation Tool GUID validation process. This is
	 * temporary.
	 * 
	 * 
	 * 
	 ***********************************************/

	public String getRdfFileExportPath() {

		if (rdfFileExportPath == null || rdfFileExportPath.isEmpty()) {
			rdfFileExportPath = DEFAULT_RDF_FILE_EXPORT;
		}

		return rdfFileExportPath;
	}

	public Integer getRdfSQLLimit() {

		if (rdfSQLLimit == null || rdfSQLLimit <= 0) {
			rdfSQLLimit = DEFAULT_RDF_SQL_LIMIT;
		}

		return rdfSQLLimit;
	}

	public String getRdfLoadFailureNotificationEmail() {

		if (rdfLoadFailureNotificationEmail == null || rdfLoadFailureNotificationEmail.isEmpty()) {
			return "FITBIR-DEV@mail.nih.gov";
		} else {
			return rdfLoadFailureNotificationEmail;
		}

	}

	public Boolean getRdfGenerateAll() {

		if (isRdfGenerateAll == null) {
			return true;
		} else {
			return isRdfGenerateAll;
		}
	}

	public String getModulesInfraEmail() {
		return modulesInfraEmail;
	}

	public Boolean getModulesRdfDoUpload() {
		if (modulesRdfDoUpload == null) {
			return false;
		} else {
			return modulesRdfDoUpload;
		}
	}

	public String getRdfGenLogPath() {
		if (rdfGenLogPath == null) {
			rdfGenLogPath = DEFAULT_RDFGEN_LOG_PATH;
		}

		return rdfGenLogPath;
	}

	public String getReportingToolLocalUser() {
		if (reportingLocalUser == null || reportingLocalUser.equals("")) {
			return DEFAULT_REPORTING_LOCAL_USER;
		}
		return reportingLocalUser;
	}

	public String getSftpUrl() {
		return sftpUrl;
	}

	public Integer getSftpPort() {
		return sftpPort;
	}

	public String getSftpBaseDir() {
		return sftpBaseDir;
	}

	public String getSftpUser() {
		return sftpUser;
	}

	public String getSftpPassword() {
		return sftpPassword;
	}

	/// declare up top of class
	public DatafileEndpointInfo getDataDropEndpointInfo() {
		return dataDropEndpointInfo;
	}


	public String getDownloadPackageDir() {
		return downloadPackageDirectory;
	}


	private static final String CAS_LOGIN_PATH = "/cas/login";
	private static final String LOCAL_LOGIN_PATH = "/portal/jsp/login.jsp";

	/**
	 * BEGIN WEB SERVICE REQUESTS - THESE SHOULD PROBABLY BE MOVED INTO A DIFFERENT CONSTANTS FILE
	 */
	private static final String WEBSERVICE_REPORTING_PERMISSION_PATH = "/ws/account/account/reporting/permissions";
	private static final String WEBSERVICE_REPORTING_IS_ADMIN = "/ws/account/account/reporting/isAdmin";
	private static final String WEBSERVICE_REPORTING_GET_ACCOUNT = "/ws/account/account/user";
	private static final String WEBSERVICE_REPORTING_GET_ACCOUNT_MAP_BY_USERNAMES =
			"/ws/account/account/get/map/byUserName";
	private static final String WEBSERVICE_REPORTING_GET_ACCOUNTS_BY_ROLE = "/ws/account/account/role";

	private static final String WEBSERVICE_REPORTING_SERVICE_GENERATE_DOWNLOAD_PACKAGE_PATH =
			"/ws/reportingService/generateDownloadPackage";

	private static final String WEBSERVICE_REPORTING_SERVICE_UPLOAD_DOWNLOAD_PACKAGE_PATH =
			"/ws/reportingService/uploadDownloadPackage";
	private static final String WEBSERVICE_REPORTING_SERVICE_ACCESS_RECORD_PACKAGE_PATH =
			"/ws/repository/repository/accessRecord/create";

	private static final String WEBSERVICE_REPORTING_SAVED_REPORTING_CREATE_PATH = "/ws/savedReportingService/SavedReporting/save";
	private static final String WEBSERVICE_REPORTING_SAVED_REPORTING_REMOVE_PATH =
			"/ws/savedReportingService/SavedReporting/remove";
	private static final String WEBSERVICE_REPORTING_SAVED_REPORTING_LIST_PATH =
			"/ws/savedReportingService/SavedReporting/get/list";
	private static final String WEBSERVICE_REPORTING_SAVED_REPORTING_GET_PATH = "/ws/savedReportingService/SavedReporting/get";

	private static final String WEBSERVICE_REPORTING_INTERNAL_SAVED_REPORTING_SAVE_PATH = "/service/savedQueries/save";

	private static final String WEBSERVICE_REPORTING_META_STUDY_LIST_PATH = "/ws/metastudy/getMetaStudyList";

	private static final String WEBSERVICE_REPORTING_LINK_SAVED_REPORTING_META_STUDY =
			"/ws/metastudy/linkSavedReportingMetaStudy";

	private static final String WEBSERVICE_META_STUDY_ROOT = "/ws/metastudy/";

	private static final String WEBSERVICE_REPORTING_SAVED_REPORTING_NAME_UNIQUE_PATH =
			"/ws/savedReportingService/SavedReporting/util/unique/name";
	private static final String WEBSERVICE_REPORTING_SAVED_REPORTING_REGISTER_PERMISSION_PATH =
			"/ws/account/account/entityMap/register";
	private static final String WEBSERVICE_REPORTING_SAVED_REPORTING_REGISTER_PERMISSION_LIST_PATH =
			"/ws/account/account/entityMap/register/list";

	private static final String WEBSERVICE_REPORTING_UNREGISTER_ENITY_MAP_IDS_PATH =
			"/ws/account/account/entityMap/unregister/list";
	
	private static final String WEBSERVICE_RBOX_PROCESS = 
			"/rbox/v1/script/process";

	private static final String WEBSERVICE_REPORTING_SAVED_REPORTING_LIST_PERMISSION_PATH =
			"/ws/account/account/entityMap/listEntityAccess";

	private static final String WEBSERVICE_REPORTING_LIST_PERMISSION_AUTHORITIES =
			"/ws/account/account/PermissionAuthorities";

	private static final String WEBSERVICE_REPORTING_SERVICE_GENERATE_ORDER_MANAGER_ITEM_PATH =
			"/ws/orderManagement/addItem";

	private static final String WEBSERVICE_REPORTING_SERVICE_GET_ORDER_QUEUE_PATH =
			"/ws/orderManagement/biosampleQueue";

	private static final String WEBSERVICE_DICTIONARY_GET_DATAELEMENT_VALUE_RANGE_MAP_PATH =
			"/ws/ddt/dictionary/getDEValueRangeMap";

	private static final String WEBSERVICE_DATASET_FILE_PATH_PATH =
			"/ws/repository/repository/study/dataset/dataset_file_path";

	private static final String WEBSERVICE_DICTIONARY_GET_SCHEMA_LIST_PATH = "/ws/ddt/dictionary/schemas";

	private static final String WEBSERVICE_DICTIONARY_GET_SCHEMA_LIST_BY_FORM_PATH = "/ws/ddt/dictionary/schemas/byFormNames";
	
	private static final String WEBSERVICE_REPORTING_SERVICE_THUMBNAIL_ENDPOINT_INFO_PATH =
			"/ws/repository/repository/getThumbnailEndpointInfo";

	private static final String WEBSERVICE_SUMMARY_DATA_SERVICE_RESET_CACHE_PATH = "/ws/summaryData/resetCache";
	
	private static final String WEBSERVICE_REPORTING_SAVED_REPORTING_FILE_NAME_UNIQUE_PATH =
			"/ws/savedReportingService/SavedReporting/util/unique/fileName";
	
	
	private static final String WEBSERVICE_REPORTING_GET_SAVED_REPORTING_BY_META_STUDY_AND_REPORTING_NAME_PATH =
			"/ws/savedReportingService/SavedReporting/util/savedReportingPerMetaStudy";

	public String getLogInURL() {
		if (wsSecured) {
			return getModulesAccountURL() + CAS_LOGIN_PATH;
		} else {
			return getModulesAccountURL() + LOCAL_LOGIN_PATH;
		}
	}

	public String getReportingWebServiceURL() {

		return getModulesAccountURL() + WEBSERVICE_REPORTING_SERVICE_GENERATE_DOWNLOAD_PACKAGE_PATH;
	}

	public String getUploadDownloadPackageWebServiceURL() {

		return getModulesAccountURL() + WEBSERVICE_REPORTING_SERVICE_UPLOAD_DOWNLOAD_PACKAGE_PATH;
	}

	public String getAccessRecordPackageWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_SERVICE_ACCESS_RECORD_PACKAGE_PATH;
	}

	public String getSavedReportingCreateSavedReportingWebServiceURL() {

		return getModulesAccountURL() + WEBSERVICE_REPORTING_SAVED_REPORTING_CREATE_PATH;
	}

	public String getSavedReportingRemoveSavedReportingWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_SAVED_REPORTING_REMOVE_PATH;

	}

	public String getUserSavedReportingListWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_SAVED_REPORTING_LIST_PATH;
	}

	public String getUserMetaStudyListWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_META_STUDY_LIST_PATH;
	}

	public String linkSavedReportingMetaStudyServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_LINK_SAVED_REPORTING_META_STUDY;

	}

	public String getMetaStudyServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_META_STUDY_ROOT;

	}

	public String getSavedReportingGetWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_SAVED_REPORTING_GET_PATH;
	}



	public String getSavedReportingNameUniqueWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_SAVED_REPORTING_NAME_UNIQUE_PATH;
	}

	public String updateSavedReportingPermissionWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_SAVED_REPORTING_REGISTER_PERMISSION_PATH;
	}

	public String updateSavedReportingPermissionListWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_SAVED_REPORTING_REGISTER_PERMISSION_LIST_PATH;
	}

	public String getUnregisterEntityMapByIdsWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_UNREGISTER_ENITY_MAP_IDS_PATH;
	}
	
	public String getRboxProcessURL() {
		return getRProcessURL() + WEBSERVICE_RBOX_PROCESS;
	}

	public String getPermissionAuthoritiesWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_LIST_PERMISSION_AUTHORITIES;
	}

	public String getListSavedReportingPermissionsWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_SAVED_REPORTING_LIST_PERMISSION_PATH;
	}


	public String getReportingOrderManagreURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_SERVICE_GENERATE_ORDER_MANAGER_ITEM_PATH;
	}

	public String getReportingOrderQueueURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_SERVICE_GET_ORDER_QUEUE_PATH;
	}

	public String getDataElementValueRangeMapURL() {
		return getModulesDDTURL() + WEBSERVICE_DICTIONARY_GET_DATAELEMENT_VALUE_RANGE_MAP_PATH;
	}

	public String getSchemaListURL() {
		return getModulesDDTURL() + WEBSERVICE_DICTIONARY_GET_SCHEMA_LIST_PATH;
	}
	
	public String getSchemaListByFormURL() {
		return getModulesDDTURL() + WEBSERVICE_DICTIONARY_GET_SCHEMA_LIST_BY_FORM_PATH;
	}

	public String getPermissionsWebserviceURL() {

		return getModulesAccountURL() + WEBSERVICE_REPORTING_PERMISSION_PATH;
	}

	public String getIsAdminWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_IS_ADMIN;
	}

	public String getAccountWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_GET_ACCOUNT;
	}

	public String getAccountMapByUserNameURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_GET_ACCOUNT_MAP_BY_USERNAMES;
	}

	public String getAccountsByRoleURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_GET_ACCOUNTS_BY_ROLE;
	}

	public String getThumbnailWebserviceURL() {

		return getModulesAccountURL() + WEBSERVICE_REPORTING_SERVICE_THUMBNAIL_ENDPOINT_INFO_PATH;
	}

	public String getSummaryDataResetCacheURL() {
		return getModulesAccountURL() + WEBSERVICE_SUMMARY_DATA_SERVICE_RESET_CACHE_PATH;
	}

	public String getDatasetFilePathURL() {
		return getModulesSTURL() + WEBSERVICE_DATASET_FILE_PATH_PATH;
	}
	
	public String getSavedReportingFileNameUniqueWebServiceURL() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_SAVED_REPORTING_FILE_NAME_UNIQUE_PATH;
	}

	public String getSavedReportingByNameAndMetaStudyPath() {
		return getModulesAccountURL() + WEBSERVICE_REPORTING_GET_SAVED_REPORTING_BY_META_STUDY_AND_REPORTING_NAME_PATH;
	}

}
