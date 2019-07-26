package gov.nih.tbi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import gov.nih.tbi.commons.model.BricsInstanceType;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.dictionary.model.MissingPropertyException;

public class ModulesConstants {

	private static final String DEFAULT_PORTAL_ROOT = "portal";
	// return null if server url is not defined
	private static final String DEFAULT_SERVER_URL = "";
	private static final String ORG_EMAIL_DELIMITER = ";";

	public static final String NIH_PASSWORD_HINT_URL_PROPERTY = "renewPassword.passwordHints";
	public static final String RENEW_PASSWORD_SUBJECT_PROPERTY = "renewPassword.subject";
	public static final String RENEW_ACCOUNT_ROLE_SUBJECT_PROPERTY = "renewRole.subject";
	public static final String RENEW_ACCOUNT_ROLE_MESSAGE_BODY_PROPERTY = "renewRole.body";
	public static final String RENEW_ACCOUNT_ROLE_CONTENT_FOR_FITBIR_PROPERTY = "renewRole.contentForFitbir";
	public static final String RENEW_PASSWORD_URL_PROPERTY = "renewPassword.url";
	public static final String RENEW_PASSWORD_MESSAGE_BODY_PROPERTY = "renewPassword.body";

	public static final String QUERY_ROOT = "query";
	public static final String REPORTING = "reporting";
	
	public static final String SESSION_STYLE_KEY = "style.key";
	public static final String NON_NTRR = "NON-NTRR";
	public static final String NTRR = "NTRR";
	public static final String CISTAR = "CISTAR";

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
	@Value("#{applicationProperties['modules.ws.enabled']}")
	private String modulesWSEnabled;
	@Value("#{applicationProperties['modules.gtservice.enabled']}")
	private String modulesGTServiceEnabled;
	@Value("#{applicationProperties['modules.account.enabled']}")
	private String modulesAccountEnabled;
	@Value("#{applicationProperties['modules.dashboard.enabled']}")
	private String modulesDashboardEnabled;
	@Value("#{applicationProperties['modules.admindashboard.enabled']}")
	private String modulesAdminDashboardEnabled;
	@Value("#{applicationProperties['modules.account.audit.enabled']}")
	private String modulesAccountAuditEnabled;
	@Value("#{applicationProperties['modules.env.isLocal']}")
	private String modulesEnvIsLocal;

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
	@Value("#{applicationProperties['modules.ws.url.server']}")
	private String modulesWSURL;
	@Value("#{applicationProperties['modules.gtservice.url.server']}")
	private String modulesGTServiceURL;
	@Value("#{applicationProperties['modules.account.url.server']}")
	private String modulesAccountURL;
	@Value("#{applicationProperties['modules.mipav.url.client']}")
	private String modulesMIPAVURL;
	@Value("#{applicationProperties['modules.infra.email']}")
	private String modulesInfraEmail;

	@Value("#{applicationProperties['modules.guid.service.username']}")
	private String modulesGuidServiceUsername;

	@Value("#{applicationProperties['modules.guid.service.password']}")
	private String modulesGuidServicePassword;

	@Value("#{applicationProperties['modules.guid.service.token']}")
	private String modulesGuidServiceToken;

	@Value("#{applicationProperties['modules.guid.service.serverLoginUrl']}")
	private String modulesGuidServerLoginUrl;

	@Value("#{applicationProperties['modules.guid.service.userJwtUrl']}")
	private String modulesGuidUserJwtUrl;

	@Value("#{applicationProperties['modules.guid.service.renewUrl']}")
	private String modulesGuidRenewUrl;

	@Value("#{applicationProperties['modules.style.default']}")
	private String modulesStyleDefault;
	private static String modulesStyleDefaultStat;

	@Value("#{applicationProperties['webstart.sftp.basedir']}")
	private String webstartSftpBasedir;

	@Value("#{applicationProperties['webstart.sftp.name']}")
	private String webstartSftpName;

	@Value("#{applicationProperties['webstart.sftp.port']}")
	private String webstartSftpPort;

	@Value("#{applicationProperties['webstart.sftp.url']}")
	private String webstartSftpUrl;

	@Value("#{applicationProperties['webstart.sftp.user']}")
	private String webstartSftpUser;

	@Value("#{applicationProperties['webstart.sftp.passwd']}")
	private String webstartSftpPasswd;

	@Value("#{applicationProperties['webstart.portal.root']}")
	private String webstartPortalRoot;

	@Value("#{applicationProperties['modules.style.key']}")
	private String modulesStyleKey;

	@Value("#{applicationProperties['biorepository.iu.username']}")
	private String biorepositoryIuUsername;

	@Value("#{applicationProperties['biorepository.iu.password']}")
	private String biorepositoryIuPassword;

	@Value("#{applicationProperties['biorepository.iu.wsBase']}")
	private String biorepositoryIuWsBase;

	@Value("#{applicationProperties['biorepository.iu.bricsmanager.email']}")
	private String biorepositoryIuBricsManagerEmail;

	// comma delimited list of catalogs
	@Value("#{applicationProperties['biorepository.iu.catalogs']}")
	private String biorepositoryIuCatalogs;

	// This is a list longs. The numbers represent instances that are duplicates of other instances (important when
	// writing across instances)
	@Value("#{applicationProperties['modules.duplicates']}")
	private String modulesDuplicates;

	@Value("#{applicationProperties['MIRTH.connect.channel.url']}")
	private String mirthConnectChannelUrl;

	@Value("#{applicationProperties['ImportService.datadrop.location']}")
	private String importServiceDatadropLocation;
    
	@Value("#{applicationProperties['brics.data.processor.email.sender']}")
	private String dataProcessorEmailSender;
	
	@Value("#{applicationProperties['Last-Deployed']}")
	private String lastDeployedTimeStamp;

	// Mappings of disease types to URLS. Only entering one URL is possible
	private Map<Long, String> modulesOrgNameMap;
	private Map<Long, String> modulesOrgEmailMap;
	private Map<Long, String> modulesOrgNoreplyMap;
	private Map<Long, String> modulesOrgPhoneMap;

	private Map<Long, String> modulesDDTMap;
	private Map<Long, String> modulesVTMap;
	private Map<Long, String> modulesGTMap;
	private Map<Long, String> modulesGTServiceMap;
	private Map<Long, String> modulesSTMap;
	private Map<Long, String> modulesUDTMap;
	private Map<Long, String> modulesDTMap;
	private Map<Long, String> modulesQTMap;
	private Map<Long, String> modulesReportingMap;
	private Map<Long, String> modulesOMMap;
	private Map<Long, String> modulesMSMap;
	private Map<Long, String> modulesWSMap;
	private Map<Long, String> modulesAccountMap;
	private Map<Long, String> modulesPublicMap;
	private Map<Long, String> modulesPFMap;
	private Map<Long, String> guidPrefixMap;
	private Map<Long, String> guidInvPrefixMap;
	private Map<Long, String> modulesStylingMap;
	private Map<Long, String> saltedPasswordMap;

	public static Map<String, String> GLOBAL_XSL_PARAMETER_MAP = new HashMap<String, String>();

	public Map<String, String> getGlobalXslParameterMap() {
		GLOBAL_XSL_PARAMETER_MAP.put("webroot", getModulesDDTURL());
		GLOBAL_XSL_PARAMETER_MAP.put("imageroot", getModulesDDTURL() + "images");
		GLOBAL_XSL_PARAMETER_MAP.put("imageroot", getModulesDDTURL() + "images");
		GLOBAL_XSL_PARAMETER_MAP.put("cssstylesheet", getModulesDDTURL() + "images");
		GLOBAL_XSL_PARAMETER_MAP.put("title", "Eform");

		return GLOBAL_XSL_PARAMETER_MAP;
	}


	private List<String> modulesOrgEmailList;
	private List<Long> duplicateList;

	// Credentials for administrator account
	@Value("#{applicationProperties['modules.administrator.username']}")
	private String administratorName;
	@Value("#{applicationProperties['modules.administrator.password']}")
	private String administratorPassword;
	@Value("#{applicationProperties['modules.administrator.saltedpassword']}")
	private String saltedAdministratorPassword;

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

	@Value("#{applicationProperties['modules.account.removeUnused']}")
	private Boolean eraseUnsusedAccounts;

	// Credentials for the OSTI IAD web service, which is used for DOI create/update actions.
	@Value("#{applicationProperties['doi.iad.ws.url']}")
	private String iadWsUrl;
	@Value("#{applicationProperties['doi.iad.username']}")
	private String iadUsername;
	@Value("#{applicationProperties['doi.iad.password']}")
	private String iadPassword;

	// Flag for enabling or disabling the DOI feature in portal.
	@Value("#{applicationProperties['modules.doi.enabled']}")
	private String enableDoi;
	
	// GUID System User Information, will be used to create JWT for web service calls
	@Value("#{applicationProperties['guid.systemuser.username']}")
	private String guidSysUserUsername;
	@Value("#{applicationProperties['guid.systemuser.firstname']}")
	private String guidSysUserFirstname;
	@Value("#{applicationProperties['guid.systemuser.lastname']}")
	private String guidSysUserLastname;
	@Value("#{applicationProperties['guid.systemuser.organization']}")
	private String guidSysUserOrg;
	
	
	//Account Reporting Module
	@Value("#{applicationProperties['modules.accountReporting.rootFilePath']}")
	private String accountReportingRootFilePath;
	
	// PF protocol closingout
	@Value("#{applicationProperties['proforms.protocol.closingout.enable']}")
	private String pfProtocolClosingoutEnable;


	public Boolean getEraseUnsusedAccounts() {
		if (eraseUnsusedAccounts == null) {
			return false;
		}

		return eraseUnsusedAccounts;
	}

	//this allows the public site to access some data in the repository.
	@Value("#{applicationProperties['modules.repository.public.service']}")
	public Boolean isRepositoryPublic;
	
	public Boolean getIsRepositoryPublic(){
		if(isRepositoryPublic == null){
			return Boolean.FALSE;
		}
		return isRepositoryPublic;
	}

	//this allows the public site to access some data in the meta study module.
	@Value("#{applicationProperties['modules.metastudy.public.service']}")
	public Boolean isMetaStudyPublic;
		
	public Boolean getIsMetaStudyPublic(){
		if(isMetaStudyPublic == null){
			return Boolean.FALSE;
		}
		return isMetaStudyPublic;
	}

	public void setWsSet(boolean wsSecuredArg) {

		ModulesConstants.wsSecured = wsSecuredNonStatic;
	}

	public void setDefaultStylingKey(boolean stylingKeyArg) {
	    System.out.println("*** Setting Default Styling Key ***");
		ModulesConstants.modulesStyleDefaultStat = this.modulesStyleDefault;
	}

	/***************
	 * 
	 * Getters for Org Properties
	 * 
	 **************/
	public String getModulesPortalRoot() {

		if (modulesPortalRoot == null || modulesPortalRoot.isEmpty()) {
			return DEFAULT_PORTAL_ROOT;
		} else {
			return modulesPortalRoot;

		}
	}

	public String getModulesOrgName() {

		if (modulesOrgName == null || modulesOrgName.isEmpty()) {
			return "FITBIR";
		} else {
			if (modulesOrgNameMap == null || modulesOrgNameMap.isEmpty()) {
				modulesOrgNameMap = getURLMap(modulesOrgName);
			}
			return modulesOrgNameMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesOrgName(Long diseaseId) {

		if (modulesOrgNameMap == null || modulesOrgNameMap.isEmpty()) {
			modulesOrgNameMap = getURLMap(modulesOrgName);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesOrgNameMap.size() == 1) {
			return modulesOrgNameMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return modulesOrgNameMap.get(diseaseId);
	}

	/**
	 * This function is used to populate facets
	 * 
	 * @return
	 */
	public Map<Long, String> getModulesOrgNameMap() {
		if (modulesOrgNameMap == null || modulesOrgNameMap.isEmpty()) {
			modulesOrgNameMap = getURLMap(modulesOrgName);
		}
		return modulesOrgNameMap;
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
	public List<String> getModulesOrgEmailList() {

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
			return modulesOrgEmailMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesOrgEmail(Long diseaseId) {

		if (modulesOrgEmailMap == null || modulesOrgEmailMap.isEmpty()) {
			modulesOrgEmailMap = getURLMap(modulesOrgEmail);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesOrgEmailMap.size() == 1) {
			return modulesOrgEmailMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return modulesOrgEmailMap.get(diseaseId);
	}

	public String getModulesOrgNoreply() {

		if (modulesOrgNoreply == null || modulesOrgNoreply.isEmpty()) {
			return "noreply@cit.nih.gov";
		} else {
			if (modulesOrgNoreplyMap == null || modulesOrgNoreplyMap.isEmpty()) {
				modulesOrgNoreplyMap = getURLMap(modulesOrgNoreply);
			}
			return modulesOrgNoreplyMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesOrgNoreply(Long diseaseId) {

		if (modulesOrgNoreplyMap == null || modulesOrgNoreplyMap.isEmpty()) {
			modulesOrgNoreplyMap = getURLMap(modulesOrgNoreply);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesOrgNoreplyMap.size() == 1) {
			return modulesOrgNoreplyMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return modulesOrgNoreplyMap.get(diseaseId);
	}

	public String getModulesOrgPhone() {

		if (modulesOrgPhone == null || modulesOrgPhone.isEmpty()) {
			return "FITBIR";
		} else {
			if (modulesOrgPhoneMap == null || modulesOrgPhoneMap.isEmpty()) {
				modulesOrgPhoneMap = getURLMap(modulesOrgPhone);
			}
			return modulesOrgPhoneMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesOrgPhone(Long diseaseId) {

		if (modulesOrgPhoneMap == null || modulesOrgPhoneMap.isEmpty()) {
			modulesOrgPhoneMap = getURLMap(modulesOrgPhone);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesOrgPhoneMap.size() == 1) {
			return modulesOrgPhoneMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return modulesOrgPhoneMap.get(diseaseId);
	}

	public String getModulesDevEmail() {

		if (modulesDevEmail == null || modulesDevEmail.isEmpty()) {
			return "REPLACED@mail.nih.gov";
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
			return Boolean.FALSE;
		} else {
			return Boolean.valueOf(modulesPFEnabled);
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
	
	public Boolean getModulesWSEnabled() {

		if (modulesWSEnabled == null || modulesWSEnabled.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(modulesWSEnabled);
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

	public Boolean getEnvIsLocal() {

		if (this.modulesEnvIsLocal == null || this.modulesEnvIsLocal.isEmpty()) {
			return false;
		} else {
			return Boolean.parseBoolean(this.modulesEnvIsLocal);
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
			return modulesPublicMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesPublicURL(Long diseaseId) {

		if (modulesPublicMap == null || modulesPublicMap.isEmpty()) {
			modulesPublicMap = getURLMap(modulesPublicURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesPublicMap.size() == 1) {
			return modulesPublicMap.get(ServiceConstants.DEFAULT_PROVIDER);
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
			return modulesPFMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesPFURL(Long diseaseId) {

		if (modulesPFMap == null || modulesPFMap.isEmpty()) {
			modulesPFMap = getURLMap(modulesPFURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesPFMap.size() == 1) {
			return modulesPFMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return modulesPFMap.get(diseaseId);
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
			return modulesDDTMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesDDTURL(Long diseaseId) {

		if (modulesDDTMap == null || modulesDDTMap.isEmpty()) {
			modulesDDTMap = getURLMap(modulesDDTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesDDTMap.size() == 1) {
			return modulesDDTMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return modulesDDTMap.get(diseaseId);
	}

	public Map<Long, String> getModulesDDTMap() {

		if (modulesDDTMap == null || modulesDDTMap.isEmpty()) {
			modulesDDTMap = getURLMap(modulesDDTURL);
		}
		return modulesDDTMap;
	}

	public String getModulesQTURL() {

		if (modulesQTURL == null || modulesQTURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesQTMap == null || modulesQTMap.isEmpty()) {
				modulesQTMap = getURLMap(modulesQTURL);
			}
			return modulesQTMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesReportingURL() {

		if (modulesReportingURL == null || modulesReportingURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesReportingMap == null || modulesReportingMap.isEmpty()) {
				modulesReportingMap = getURLMap(modulesReportingURL);
			}
			return modulesReportingMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}
	
	public String getModulesReportingURL(Long diseaseId) {

		if (modulesReportingMap == null || modulesReportingMap.isEmpty()) {
			modulesReportingMap = getURLMap(modulesReportingURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesReportingMap.size() == 1) {
			return modulesReportingMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return modulesReportingMap.get(diseaseId);
	}
	
	public String getModulesQTURL(Long diseaseId) {

		if (modulesQTMap == null || modulesQTMap.isEmpty()) {
			modulesQTMap = getURLMap(modulesQTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesQTMap.size() == 1) {
			return modulesQTMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return modulesQTMap.get(diseaseId);
	}
	

	public Map<Long, String> getModulesQTMap() {

		if (modulesQTMap == null || modulesQTMap.isEmpty()) {
			modulesQTMap = getURLMap(modulesQTURL);
		}
		return modulesQTMap;
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
			return modulesOMMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesOMURL(Long diseaseId) {

		if (modulesOMMap == null || modulesOMMap.isEmpty()) {
			modulesOMMap = getURLMap(modulesOMURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesOMMap.size() == 1) {
			return modulesOMMap.get(ServiceConstants.DEFAULT_PROVIDER);
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
			return modulesMSMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesMSURL(Long diseaseId) {

		if (modulesMSMap == null || modulesMSMap.isEmpty()) {
			modulesMSMap = getURLMap(modulesMSURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesMSMap.size() == 1) {
			return modulesMSMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return modulesMSMap.get(diseaseId);
	}

	public Map<Long, String> getModulesMSMap() {

		if (modulesMSMap == null || modulesMSMap.isEmpty()) {
			modulesMSMap = getURLMap(modulesMSURL);
		}
		return modulesMSMap;
	}

	public String getModulesWSURL() {
		if (modulesWSURL == null || modulesWSURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesWSMap == null || modulesWSMap.isEmpty()) {
				modulesWSMap = getURLMap(modulesWSURL);
			}
			return modulesWSMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesWSURL(Long diseaseId) {
		if (modulesWSMap == null || modulesWSMap.isEmpty()) {
			modulesWSMap = getURLMap(modulesWSURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesWSMap.size() == 1) {
			return modulesWSMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return modulesWSMap.get(diseaseId);
	}

	public Map<Long, String> getModulesWSMap() {

		if (modulesWSMap == null || modulesWSMap.isEmpty()) {
			modulesWSMap = getURLMap(modulesWSURL);
		}
		return modulesWSMap;
	}

	public String getModulesVTURL() {

		if (modulesVTURL == null || modulesVTURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesVTMap == null || modulesVTMap.isEmpty()) {
				modulesVTMap = getURLMap(modulesVTURL);
			}
			return modulesVTMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesVTURL(Long diseaseId) {

		if (modulesVTMap == null || modulesVTMap.isEmpty()) {
			modulesVTMap = getURLMap(modulesVTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesVTMap.size() == 1) {
			return modulesVTMap.get(ServiceConstants.DEFAULT_PROVIDER);
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
			return modulesGTMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesGuidServiceToken() {
		return modulesGuidServiceToken;
	}

	public void setModulesGuidServiceToken(String modulesGuidServiceToken) {
		this.modulesGuidServiceToken = modulesGuidServiceToken;
	}

	public String getModulesGTURL(Long diseaseId) {

		if (modulesGTMap == null || modulesGTMap.isEmpty()) {
			modulesGTMap = getURLMap(modulesGTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesGTMap.size() == 1) {
			return modulesGTMap.get(ServiceConstants.DEFAULT_PROVIDER);
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
			return modulesSTMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesSTURL(Long diseaseId) {

		if (modulesSTMap == null || modulesSTMap.isEmpty()) {
			modulesSTMap = getURLMap(modulesSTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesSTMap.size() == 1) {
			return modulesSTMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return modulesSTMap.get(diseaseId);
	}
	
	public Map<Long, String> getSaltedAdministratorPasswordMap() {
		if (saltedPasswordMap == null || saltedPasswordMap.isEmpty()) {
			saltedPasswordMap = getURLMap(saltedAdministratorPassword);
		}
		return saltedPasswordMap; 
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
			return modulesUDTMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesUDTURL(Long diseaseId) {

		if (modulesUDTMap == null || modulesUDTMap.isEmpty()) {
			modulesUDTMap = getURLMap(modulesUDTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesUDTMap.size() == 1) {
			return modulesUDTMap.get(ServiceConstants.DEFAULT_PROVIDER);
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
			return modulesDTMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesDTURL(Long diseaseId) {

		if (modulesDTMap == null || modulesDTMap.isEmpty()) {
			modulesDTMap = getURLMap(modulesDTURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesDTMap.size() == 1) {
			return modulesDTMap.get(ServiceConstants.DEFAULT_PROVIDER);
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
			return modulesAccountMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}
	
	public String getSaltedAdministratorPassword(Long diseaseId) {

		if (saltedPasswordMap == null || saltedPasswordMap.isEmpty()) {
			saltedPasswordMap = getURLMap(saltedAdministratorPassword);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (saltedPasswordMap.size() == 1) {
			return saltedPasswordMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return saltedPasswordMap.get(diseaseId);
	}


	public String getModulesAccountURL(Long diseaseId) {

		if (modulesAccountMap == null || modulesAccountMap.isEmpty()) {
			modulesAccountMap = getURLMap(modulesAccountURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesAccountMap.size() == 1) {
			return modulesAccountMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return modulesAccountMap.get(diseaseId);
	}

	public String getStylingKey(Long diseaseId) {

		if (this.modulesStylingMap == null || this.modulesStylingMap.isEmpty()) {

			if(this.modulesStyleKey != null) {
				this.modulesStylingMap = getURLMap(this.modulesStyleKey);
			} else {
				return null;
			}
		}

		String result = null;

		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (this.modulesStylingMap.size() == 1) {
			result = this.modulesStylingMap.get(ServiceConstants.DEFAULT_PROVIDER);

		} else {
			result = this.modulesStylingMap.get(diseaseId);
		}


		return result;
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
		if (modulesDuplicates == null || modulesDuplicates.equals(ServiceConstants.EMPTY_STRING)) {
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
	
	public String getSaltedAdministratorPassword() {

		return saltedAdministratorPassword;
	}

	// public boolean getWsSecured()
	// {
	//
	// return wsSecured;
	// }

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
			map.put(ServiceConstants.DEFAULT_PROVIDER, inputString);
		} else {
			for (String keyValue : inputString.split(" *; *")) {
				String[] pairs = keyValue.split(" *, *", 2);
				map.put(Long.valueOf(pairs[0]), pairs.length == 1 ? "" : pairs[1]);
				// The first item in the map also gets put with key: -1
				if (map.get(ServiceConstants.DEFAULT_PROVIDER) == null) {
					map.put(ServiceConstants.DEFAULT_PROVIDER, pairs.length == 1 ? "" : pairs[1]);
				}
			}
		}
		return map;
	}

	public static boolean getWsSecured() {

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

	public String getGuidPrefix(Long diseaseId) {

		if (guidPrefixMap == null || guidPrefixMap.isEmpty()) {
			guidPrefixMap = getURLMap(guidPrefix);
		}
		if (modulesDTMap.size() == 1) {
			return guidPrefixMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return guidPrefixMap.get(diseaseId);
	}

	public String getGuidPrefix() {

		if (guidPrefix == null || guidPrefix.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (guidPrefixMap == null || guidPrefixMap.isEmpty()) {
				guidPrefixMap = getURLMap(guidPrefix);
			}
			return guidPrefixMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getGuidInvPrefix(Long diseaseId) {

		if (guidInvPrefixMap == null || guidInvPrefixMap.isEmpty()) {
			guidInvPrefixMap = getURLMap(guidInvPrefix);
		}
		if (guidInvPrefixMap.size() == 1) {
			return guidInvPrefixMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
		return guidInvPrefixMap.get(diseaseId);
	}

	public String getGuidInvPrefix() {

		if (guidInvPrefix == null || guidInvPrefix.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (guidInvPrefixMap == null || guidInvPrefixMap.isEmpty()) {
				guidInvPrefixMap = getURLMap(guidInvPrefix);
			}
			return guidInvPrefixMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesInfraEmail() {
		return modulesInfraEmail;
	}

	public String getBiorepositoryIuUsername() {
		return biorepositoryIuUsername;
	}

	public void setBiorepositoryIuUsername(String biorepositoryIuUsername) {
		this.biorepositoryIuUsername = biorepositoryIuUsername;
	}

	public String getBiorepositoryIuPassword() {
		return biorepositoryIuPassword;
	}

	public void setBiorepositoryIuPassword(String biorepositoryIuPassword) {
		this.biorepositoryIuPassword = biorepositoryIuPassword;
	}

	public String getBiorepositoryIuWsBase() {
		return biorepositoryIuWsBase;
	}

	public void setBiorepositoryIuWsBase(String biorepositoryIuWsBase) {
		this.biorepositoryIuWsBase = biorepositoryIuWsBase;
	}

	public String getBiorepositoryIuBricsManagerEmail() {
		return biorepositoryIuBricsManagerEmail;
	}

	public void setBiorepositoryIuBricsManagerEmail(String biorepositoryIuBricsManagerEmail) {
		this.biorepositoryIuBricsManagerEmail = biorepositoryIuBricsManagerEmail;
	}

	public List<String> getBiorepositoryIuCatalogs() {
		return BRICSStringUtils.delimitedStringToList(this.biorepositoryIuCatalogs, ServiceConstants.COMMA);
	}

	public String getModulesGTServiceEnabled() {
		return modulesGTServiceEnabled;
	}

	public void setModulesGTServiceEnabled(String modulesGTServiceEnabled) {
		this.modulesGTServiceEnabled = modulesGTServiceEnabled;
	}

	public String getModulesGTServiceURL() {
		if (modulesGTServiceURL == null || modulesGTServiceURL.isEmpty()) {
			return DEFAULT_SERVER_URL;
		} else {
			if (modulesGTServiceMap == null || modulesGTServiceMap.isEmpty()) {
				modulesGTServiceMap = getURLMap(modulesGTServiceURL);
			}
			return modulesGTServiceMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}
	}

	public String getModulesGTServiceURL(Long diseaseId) {
		if (modulesGTServiceMap == null || modulesGTServiceMap.isEmpty()) {
			modulesGTServiceMap = getURLMap(modulesGTServiceURL);
		}
		// Case: A disease was given but there is only 1 option (The default option at
		// ServiceConstants.DEFAULT_PROVIDER)
		if (modulesGTServiceMap.size() == 1) {
			return modulesGTServiceMap.get(ServiceConstants.DEFAULT_PROVIDER);
		}

		return modulesGTServiceMap.get(diseaseId);
	}

	public String getModulesGuidServiceUsername() {
		return modulesGuidServiceUsername;
	}

	public void setModulesGuidServiceUsername(String modulesGuidServiceUsername) {
		this.modulesGuidServiceUsername = modulesGuidServiceUsername;
	}

	public String getModulesGuidServicePassword() {
		return modulesGuidServicePassword;
	}

	public void setModulesGuidServicePassword(String modulesGuidServicePassword) {
		this.modulesGuidServicePassword = modulesGuidServicePassword;
	}

	public String getModulesGuidServerLoginUrl() {
		return modulesGuidServerLoginUrl;
	}

	public void setModulesGuidServerLoginUrl(String modulesGuidServerLoginUrl) {
		this.modulesGuidServerLoginUrl = modulesGuidServerLoginUrl;
	}

	public String getModulesGuidUserJwtUrl() {
		return modulesGuidUserJwtUrl;
	}

	public void setModulesGuidUserJwtUrl(String modulesGuidUserJwtUrl) {
		this.modulesGuidUserJwtUrl = modulesGuidUserJwtUrl;
	}

	public String getModulesGuidRenewUrl() {
		return modulesGuidRenewUrl;
	}

	public void setModulesGuidRenewUrl(String modulesGuidRenewUrl) {
		this.modulesGuidRenewUrl = modulesGuidRenewUrl;
	}

	public String getWebstartSftpBasedir() {

		if(!StringUtils.isBlank(this.webstartSftpBasedir)) {
			return this.webstartSftpBasedir;
		} else {
			throw new MissingPropertyException("Modules property webstart.sftp.basedir missing");
		}
	}

	public String getWebstartSftpName() {

		if(!StringUtils.isBlank(this.webstartSftpName)) {
			return this.webstartSftpName;
		} else {
			throw new MissingPropertyException("Modules property webstart.sftp.name missing");
		}
	}

	public String getWebstartSftpPort() {

		if(!StringUtils.isBlank(this.webstartSftpPort)) {
			return this.webstartSftpPort;
		} else {
			throw new MissingPropertyException("Modules property webstart.sftp.port missing");
		}
	}

	public String getWebstartSftpUrl() {

		if(!StringUtils.isBlank(this.webstartSftpUrl)) {
			return this.webstartSftpUrl;
		} else {
			throw new MissingPropertyException("Modules property webstart.sftp.url missing");
		}
	}

	public String getWebstartSftpUser() {

		if(!StringUtils.isBlank(this.webstartSftpUser)) {
			return this.webstartSftpUser;
		} else {
			throw new MissingPropertyException("Modules property webstart.sftp.user missing");
		}
	}

	public String getWebstartSftpPassword() {

		if(!StringUtils.isBlank(this.webstartSftpPasswd)) {
			return this.webstartSftpPasswd;
		} else {
			throw new MissingPropertyException("Modules property webstart.sftp.passwd missing");
		}
	}

	public String getWebstartPortalRoot() {

		if(!StringUtils.isBlank(this.webstartPortalRoot)) {
			return this.webstartPortalRoot;
		} else {
			throw new MissingPropertyException("Modules property webstart.portal.root missing");
		}
	}

	public String getMirthConnectChannelUrl() {

		if(!StringUtils.isBlank(this.mirthConnectChannelUrl)) {
			return this.mirthConnectChannelUrl;
		} else {
			throw new MissingPropertyException("Modules property MIRTH.connect.channel.url missing");
		}
	}

	public String getImportServiceDatadropLocation() {

		if(!StringUtils.isBlank(this.importServiceDatadropLocation)) {
			return this.importServiceDatadropLocation;
		} else {
			throw new MissingPropertyException("Modules property ImportService.datadrop.location missing");
		}
	}
	
	public String getDataProcessorEmailSender() {
		if(!StringUtils.isBlank(this.dataProcessorEmailSender)) {
		return dataProcessorEmailSender;
		} else {
			throw new MissingPropertyException("Modules property brics.data.processor.email.sender missing");
		}
	}


	public static String getDefaultStylingKey() {
		return ModulesConstants.modulesStyleDefaultStat;
	}

	public String getIadWsUrl() {
		return iadWsUrl != null ? iadWsUrl.trim() : null;
	}

	public String getIadUsername() {
		return iadUsername != null ? iadUsername.trim() : null;
	}

	public String getIadPassword() {
		return iadPassword != null ? iadPassword.trim() : null;
	}

	public Boolean isDoiEnabled() {
		return enableDoi != null ? Boolean.valueOf(enableDoi.trim()) : Boolean.FALSE;
	}
	
	public boolean isNTRRInstance() {
		
		String orgName = getModulesOrgName();
		if (!StringUtils.isEmpty(orgName)) {
			return (BricsInstanceType.NTRR == BricsInstanceType.getByInstanceName(orgName));
		}
		
		return false;
	}

	public String getGuidSysUserUsername() {
		return guidSysUserUsername;
	}

	public String getGuidSysUserFirstname() {
		return guidSysUserFirstname;
	}

	public String getGuidSysUserLastname() {
		return guidSysUserLastname;
	}

	public String getGuidSysUserOrg() {
		return guidSysUserOrg;
	}

	public String getModulesStyleKey() {
		return modulesStyleKey;
	}

	public void setModulesStyleKey(String modulesStyleKey) {
		this.modulesStyleKey = modulesStyleKey;
	}

	public String getAccountReportingRootFilePath() {
		return accountReportingRootFilePath;
	}

	public void setAccountReportingRootFilePath(String accountReportingRootFilePath) {
		this.accountReportingRootFilePath = accountReportingRootFilePath;
	}

	public String getEnvironmentFlag() {
		String envFlag = "";
		if(modulesAccountURL.contains("stage")) {
			envFlag = " - STAGE";
		} else if (modulesAccountURL.contains("uat")) {
			envFlag = " - UAT";
		} else if (modulesAccountURL.contains("dev")) {
			envFlag = " - DEV";
		} else if (modulesAccountURL.contains("demo")) {
			envFlag = " - DEMO";
		} else if (modulesAccountURL.contains("test")) {
			envFlag = " - TEST";
		} else if (modulesAccountURL.contains("local")) {
			envFlag = " - LOCAL";
		}
		
		return envFlag;
	}

	public String getLastDeployedTimeStamp() {
		return lastDeployedTimeStamp;
	}

	public void setLastDeployedTimeStamp(String lastDeployedTimeStamp) {
		this.lastDeployedTimeStamp = lastDeployedTimeStamp;
	}
	
	public Boolean getPfProtocolClosingoutEnable() {
		if (pfProtocolClosingoutEnable == null || pfProtocolClosingoutEnable.isEmpty()) {
			return Boolean.FALSE;
		} else {
			return Boolean.valueOf(pfProtocolClosingoutEnable);
		}
	}

}
