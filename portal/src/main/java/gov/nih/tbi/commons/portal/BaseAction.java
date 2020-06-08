package gov.nih.tbi.commons.portal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.gson.JsonSyntaxException;
import com.opensymphony.xwork2.ActionSupport;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.AccountUserDetails;
import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.service.hibernate.AccountHistoryManager;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.model.BricsInstanceType;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.guid.exception.InvalidJwtException;
import gov.nih.tbi.guid.model.GuidJwt;
import gov.nih.tbi.guid.ws.GuidServerAuthenticationUtil;
import gov.nih.tbi.guid.ws.exception.AuthenticationException;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class BaseAction extends ActionSupport {

	private static final long serialVersionUID = -3339107809449648314L;

	private static final String ANONYMOUS_USER_NAME = "anonymous";

	public static final String JVM_ARGS = "-Xmx{0}m";
	
	private static final String GET_MICROSERVICE_JWT_URL_TAIL = "/authentication/user/bricslogin";
	private static final String GET_MICROSERVICE_JWT_PARAMS_FORMAT = "username=%s&password=%s";

	static Logger logger = Logger.getLogger(BaseAction.class);

	private String deploymentVersion;
	private String buildID;
	private String deploymentID;

	protected String username;

	/******************************************************************************************************/

	// These should not be injected here and should be moved to a higher level
	// "base" action
	// Because there is no guarantee that the module will have the database tables
	// for this manager
	// These are currently being used for dashboards and need to be moved
	@Autowired
	protected AccountManager accountManager;

	@Autowired
	protected AccountHistoryManager accountHistoryManager;

	@Autowired
	protected DictionaryToolManager dictionaryManager;

	@Autowired
	protected RepositoryManager repositoryManager;

	@Autowired
	protected SessionAccount sessionAccount;

	@Autowired
	protected ModulesConstants modulesConstants;

	@Autowired
	protected GuidServerAuthenticationUtil guidServerAuthUtil;

	public User getUser() {
		return getAccount().getUser();
	}

	/**
	 * Returns the account of the current session (the logged in user)
	 * 
	 * @return
	 */
	public Account getAccount() {
		// The first call to account of the session, the account needs to be taken from
		// spring security and added to the
		// session
		if (sessionAccount == null || sessionAccount.getAccount() == null
				|| sessionAccount.getAccount().getUserName().equals(ANONYMOUS_USER_NAME)) {
			Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
			Account account = null;
			// Case where no user is logged in (published data element and fs search)
			if (auth.getPrincipal() instanceof String && ((String) auth.getPrincipal()).equals("guest")) {
				account = new Account();
				account.setUserName(ANONYMOUS_USER_NAME);
				account.setId(1L);
				account.setDiseaseKey("-1");
			} else {
				account = ((AccountUserDetails) auth.getPrincipal()).getAccount();
			}
			sessionAccount.setAccount(account);
		}

		return sessionAccount.getAccount();
	}

	public Long getDiseaseId() {
		if (sessionAccount == null || sessionAccount.getDiseaseId() == null) {
			Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
			Long diseaseId = null;

			if (auth.getPrincipal() instanceof String && ((String) auth.getPrincipal()).equals("guest")) {
				logger.info("Either no CAS or user is not logged in, returning -1 as disease ID...");
				//return -1 as the default disease ID.  Don't cache in the session, because it has been known to cause issues.
				return -1L;
			} else {
				logger.info("Getting disease ID from authentication principal...");
				diseaseId = ((AccountUserDetails) auth.getPrincipal()).getDiseaseId();
			}
			
			logger.info("Got new disease ID: " + diseaseId);
			sessionAccount.setDiseaseId(diseaseId);
		}

		return sessionAccount.getDiseaseId();
	}

	/**
	 * Checks if the current user is a guest
	 * 
	 * @return - true if user is a guest, false otherwise
	 */
	public boolean getIsGuest() {
		return ANONYMOUS_USER_NAME.equals(getAccount().getUserName());
	}

	/**
	 * this method will check if the user is an Admin user
	 */
	public boolean getIsAdmin() {
		return accountManager.hasRole(getAccount(), RoleType.ROLE_ADMIN);
	}

	public String getUsername() {
		return username;
	}

	public String getUsernameFromRequest() {
		HttpServletRequest request = this.getRequest();
		String result = request.getUserPrincipal().getName();
		return result;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getHash1() {
		String userName = "anonymous";

		if (getAccount() != null && getAccount().getUserName() != null) {
			userName = getAccount().getUserName();
		}

		return HashMethods.getServerHash(userName);
	}

	public String getHash2() {
		String userName = "anonymous";
		String password = "";

		if (getAccount() != null && getAccount().getUserName() != null) {
			userName = getAccount().getUserName();
			password = HashMethods.convertFromByte(getAccount().getPassword());
		}

		return HashMethods.getServerHash(userName, password);
	}

	/******************************************************************************************************/

	/**
	 * Convenience method to get the request
	 * 
	 * @return current request
	 */
	protected HttpServletRequest getRequest() {
		return ServletActionContext.getRequest();
	}

	/**
	 *
	 * @param paramName
	 * @return
	 */
	protected String getRequestParameter(String paramName) {
		HttpServletRequest request = ServletActionContext.getRequest();
		String result = request.getParameter(paramName);

		return result;
	}

	/**
	 * Convenience method to get the namespace
	 * 
	 * @return current request
	 */
	public String getNameSpace() {
		return ServletActionContext.getActionMapping().getNamespace().substring(1);
	}

	/********************** Count Methods *****************************/
	public Long getNewAccountCount() {
		return accountManager.getNumAccountsWithStatus(3L);
	}

	public Long getPendingAccountCount() {
		return accountManager.getNumAccountsWithStatus(4L);
	}

	/**
	 * @return The number of withdrawn requests for the admin dashboard
	 */
	public Long getWithdrawnRequests() {
		return accountManager.getNumAccountsWithStatus(5L);
	}

	public Long getPendingDSCount() {
		return dictionaryManager.getNumDSWithStatus(1L);
	}

	public Long getPendingDECount() {
		return dictionaryManager.getNumDEWithStatus(1L);
	}

	public Long getNewStudyCount() {
		return repositoryManager.getNumStudiesWithStatus(2L);
	}

	public Long getDeleteDatasetCount() {
		return repositoryManager.getNumDatasetsWithStatus(DatasetStatus.DELETED.getId(), true);
	}

	public Long getShareDatasetCount() {
		return repositoryManager.getNumDatasetsWithStatus(DatasetStatus.SHARED.getId(), true);
	}

	public Long getArchiveDatasetCount() {
		return repositoryManager.getNumDatasetsWithStatus(DatasetStatus.ARCHIVED.getId(), true);
	}

	public Long getErrorDatasetCount() {
		return repositoryManager.getNumDatasetsWithStatus(DatasetStatus.ERROR.getId(), false);
	}

	/*****************************************************************/

	/**
	 * Convenience method to get the response
	 * 
	 * @return current response
	 */
	protected HttpServletResponse getResponse() {
		return ServletActionContext.getResponse();
	}

	/**
	 * Convenince method to get the action name
	 * 
	 * @return
	 */
	protected String getActionName() {
		return ServletActionContext.ACTION_NAME;
	}

	/**
	 * Convenience method to get the method
	 */
	protected String getCurrentMethod() {
		String method = ServletActionContext.getActionMapping().getMethod();
		return method;
	}

	public String getCurrentActionName() {
		return ServletActionContext.getContext().getName();
	}

	/**
	 * Convenience method to get the current URL
	 * 
	 * @return
	 */
	protected String getCurrentUrl() {
		return getRequest().getRequestURL().toString();
	}

	/**
	 * Convenience method to get the session. This will create a session if one
	 * doesn't exist.
	 * 
	 * @return the session from the request (request.getSession()).
	 */
	protected HttpSession getSession() {
		return getRequest().getSession();
	}

	public String getDeploymentVersion() throws IOException {
		if (deploymentVersion == null) {
			ServletContext aContext = getSession().getServletContext();
			InputStream fis = aContext.getResourceAsStream("/META-INF/MANIFEST.MF");
			try {
				Properties p = new Properties();
				p.load(fis);
				deploymentVersion = p.getProperty("Implementation-Build");

			} finally {
				fis.close();
			}
		}

		return deploymentVersion;
	}

	public String getBuildID() throws IOException {
		if (this.buildID == null) {
			ServletContext aContext = getSession().getServletContext();
			InputStream fis = aContext.getResourceAsStream("/META-INF/MANIFEST.MF");
			try {
				Properties p = new Properties();
				p.load(fis);
				this.buildID = p.getProperty("Implementation-Build");
			} finally {
				fis.close();
			}
		}
		return this.buildID;
	}

	public String getDeploymentID() throws IOException {
		if (this.deploymentID == null) {
			ServletContext aContext = getSession().getServletContext();
			InputStream fis = aContext.getResourceAsStream("/META-INF/MANIFEST.MF");
			try {
				Properties p = new Properties();
				p.load(fis);
				this.deploymentID = p.getProperty("Repo-Id");
			} finally {
				fis.close();
			}
		}
		return this.deploymentID.substring(0, 11);
	}

	public void getManifest() {
		try {
			ServletContext aContext = getSession().getServletContext();
			InputStream fis = aContext.getResourceAsStream("/META-INF/MANIFEST.MF");
			HttpServletResponse response = ServletActionContext.getResponse();
			IOUtils.copy(fis, response.getOutputStream());
		} catch (Exception ex) {
			// do nothing
		}
	}

	/**
	 * @inheritDoc
	 */
	public String checkUrlPrefix(String url) {
		if (url != null && !(url.toLowerCase().startsWith(ServiceConstants.URL_PREFIX_HTTP)
				|| url.toLowerCase().startsWith(ServiceConstants.URL_PREFIX_HTTPS)
				|| url.toLowerCase().startsWith(ServiceConstants.URL_PREFIX_FTP))) {
			return ServiceConstants.URL_PREFIX_HTTP + url;
		} else {
			return url;
		}
	}

	/******************************************************************************************************/

	public String footer() {
		return "footer";
	}

	public String navigation() {
		return "navigation";
	}

	public String launch() {
		String returnValue = getRequest().getParameter("webstart");

		return returnValue;
	}

	public String loginCheck() {
		if (getRequest().getRemoteUser() == null) {
			return PortalConstants.ACTION_IN;
		}
		return PortalConstants.ACTION_OUT;
	}

	public String landing() {
		return "landing";
	}
	
	public String dashboard() {
		return "dashboard";
	}

	public String adminDashboard() {
		return "adminDashboard";
	}

	public String getPortalRoot() {
		return modulesConstants.getModulesPortalRoot();
	}

	public String permissionError() throws UserPermissionException {
		throw new UserPermissionException("");
	}

	public String uploadButton() {
		return "uploadButton";
	}

	public boolean userFileExists(int index) {
		UserFile file = repositoryManager.getFileById(Long.valueOf(index));

		if (file != null) {
			return true;
		}

		return false;
	}

	/*********************************************************/
	/* Getters to Pull Variables from constants file */
	/*********************************************************/
	public String getOrgEmail() {
		return modulesConstants.getModulesOrgEmail(getDiseaseId());
	}

	public String getOrgPhone() {
		return modulesConstants.getModulesOrgPhone(getDiseaseId());
	}

	public String getOrgName() {
		return modulesConstants.getModulesOrgName(getDiseaseId());
	}

	public Map<Long, String> getOrgNameMap() {
		return modulesConstants.getModulesOrgNameMap();
	}

	public Boolean getModulesDDTEnabled() {
		return modulesConstants.getModulesDDTEnabled();
	}

	public Boolean getModulesVTEnabled() {
		return modulesConstants.getModulesVTEnabled();
	}

	public Boolean getModulesGTEnabled() {
		return modulesConstants.getModulesGTEnabled();
	}

	public Boolean getModulesSTEnabled() {
		return modulesConstants.getModulesSTEnabled();
	}

	public Boolean getModulesUDTEnabled() {
		return modulesConstants.getModulesUDTEnabled();
	}

	public Boolean getModulesDTEnabled() {
		return modulesConstants.getModulesDTEnabled();
	}

	public Boolean getModulesQTEnabled() {
		return modulesConstants.getModulesQTEnabled();
	}

	public Boolean getModulesAccountEnabled() {
		return modulesConstants.getModulesAccountEnabled();
	}

	public Boolean getModulesOMEnabled() {
		return modulesConstants.getModulesOMEnabled();
	}

	public Boolean getModulesMSEnabled() {
		return modulesConstants.getModulesMSEnabled();
	}

	public Boolean getModulesWSEnabled() {
		return modulesConstants.getModulesWSEnabled();
	}

	public Boolean getModulesDashboardEnabled() {
		return modulesConstants.getModulesDashboardEnabled();
	}

	public Boolean getModulesAdminDashboardEnabled() {
		return modulesConstants.getModulesAdminDashboardEnabled();
	}

	// Used for webstart JNLPs
	public String getUploadToolServerName() {
		return modulesConstants.getModulesUDTURL();
	}

	public String getDownloadToolServerName() {
		return modulesConstants.getModulesDTURL();
	}

	// Used for webstart JNLPs
	public String getValidationToolServerName() {
		return modulesConstants.getModulesVTURL();
	}

	// Used for webstart JNLPs
	public String getGuidToolServerName() {
		return modulesConstants.getModulesGTServiceURL();
	}

	// Used for webstart JNLPs
	public String getHashedSftpBasedir() {
		return modulesConstants.getHashedSftpBasedir();
	}

	// Used for webstart JNLPs
	public String getHashedSftpName() {
		return modulesConstants.getHashedSftpName();
	}

	// Used for webstart JNLPs
	public String getHashedSftpPort() {

		String result = modulesConstants.getHashedSftpPort();
		return result;
	}

	// Used for webstart JNLPs
	public String getHashedSftpUrl() {
		return modulesConstants.getHashedSftpUrl();
	}

	// Used for webstart JNLPs
	public String getHashedSftpUser() {
		return modulesConstants.getHashedSftpUser();
	}

	// Used for webstart JNLPs
	public String getHashedSftpPassword() {
		return modulesConstants.getHashedSftpPassword();
	}

	// Used for webstart JNLPs
	public String getWebstartPortalRoot() {
		return modulesConstants.getWebstartPortalRoot();
	}

	// Used for webstart JNLPs
	public String getWebstartSftpBasedir() {
		return modulesConstants.getWebstartSftpBasedir();
	}

	// Used for webstart JNLPs
	public String getWebstartSftpName() {
		return modulesConstants.getWebstartSftpName();
	}

	// Used for webstart JNLPs
	public String getWebstartSftpPort() {

		String result = modulesConstants.getWebstartSftpPort();
		return result;
	}

	// Used for webstart JNLPs
	public String getWebstartSftpUrl() {
		return modulesConstants.getWebstartSftpUrl();
	}

	// Used for webstart JNLPs
	public String getWebstartSftpUser() {
		return modulesConstants.getWebstartSftpUser();
	}

	// Used for webstart JNLPs
	public String getWebstartSftpPassword() {
		return modulesConstants.getWebstartSftpPassword();
	}

	// Used for Submission Tool webstart JNLPs
	public String getWebstartPerformExtraValidation() {
		return modulesConstants.getPerformExtraValidationForSubmissionTool();
	}

	// Used to return the MIPAV client link for the given enviornment
	public String getmIPAVClientURL() {
		return modulesConstants.getMIPAVURL();
	}

	public String getModulesPublicURL() {
		if (modulesConstants.getModulesPublicURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesPublicURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesPublicURL();
			}
		}

		return modulesConstants.getModulesPublicURL(getDiseaseId());
	}

	public String getModulesPFURL() {
		if (modulesConstants.getModulesPFURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesPFURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesPFURL();
			}
		}

		return modulesConstants.getModulesPFURL(getDiseaseId());
	}

	public String getModulesDDTURL() {
		if (modulesConstants.getModulesDDTURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesDDTURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesDDTURL() + modulesConstants.getModulesPortalRoot() + "/";
			}
		}

		return modulesConstants.getModulesDDTURL(getDiseaseId()) + modulesConstants.getModulesPortalRoot() + "/";
	}

	public String getModulesQTURL() {
		logger.debug("QT URL - " + modulesConstants.getModulesQTURL());
		if (modulesConstants.getModulesQTURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesQTURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesQTURL() + ModulesConstants.QUERY_ROOT + "/";
			}
		}

		return modulesConstants.getModulesQTURL(getDiseaseId()) + ModulesConstants.QUERY_ROOT + "/";
	}

	public String getModulesReportingURL() {
		logger.debug("Reporting URL - " + modulesConstants.getModulesReportingURL());
		if (modulesConstants.getModulesReportingURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesReportingURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesReportingURL() + ModulesConstants.REPORTING + "/";
			}
		}

		return modulesConstants.getModulesReportingURL(getDiseaseId()) + ModulesConstants.REPORTING + "/";
	}

	public ModulesConstants getModuleConstants() {
		return modulesConstants;
	}

	public String getModulesDdtUrl() {
		String result = this.modulesConstants.getModulesDDTURL();
		return result;
	}

	public String getModulesVTURL() {
		if (modulesConstants.getModulesVTURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesVTURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesVTURL() + modulesConstants.getModulesPortalRoot() + "/";
			}
		}

		return modulesConstants.getModulesVTURL(getDiseaseId()) + modulesConstants.getModulesPortalRoot() + "/";
	}

	public String getModulesGTURL() {
		if (modulesConstants.getModulesGTURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesGTURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesGTURL() + modulesConstants.getModulesPortalRoot() + "/";
			}
		}

		return modulesConstants.getModulesGTURL(getDiseaseId()) + modulesConstants.getModulesPortalRoot() + "/";
	}

	public String getModulesSTURL() {
		if (modulesConstants.getModulesSTURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesSTURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesSTURL() + modulesConstants.getModulesPortalRoot() + "/";
			}
		}

		return modulesConstants.getModulesSTURL(getDiseaseId()) + modulesConstants.getModulesPortalRoot() + "/";
	}

	public String getModulesOMURL() {
		if (modulesConstants.getModulesOMURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesOMURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesOMURL() + modulesConstants.getModulesPortalRoot() + "/";
			}
		}

		return modulesConstants.getModulesOMURL(getDiseaseId()) + modulesConstants.getModulesPortalRoot() + "/";
	}

	public String getModulesMSURL() {
		if (modulesConstants.getModulesMSURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesMSURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesMSURL() + modulesConstants.getModulesPortalRoot() + "/";
			}
		}

		return modulesConstants.getModulesMSURL(getDiseaseId()) + modulesConstants.getModulesPortalRoot() + "/";
	}

	public String getModulesWSURL() {
		if (modulesConstants.getModulesWSURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesWSURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesWSURL() + modulesConstants.getModulesPortalRoot() + "/";
			}
		}

		return modulesConstants.getModulesWSURL(getDiseaseId()) + modulesConstants.getModulesPortalRoot() + "/";
	}

	public String getModulesUDTURL() {
		if (modulesConstants.getModulesUDTURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesUDTURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesUDTURL() + modulesConstants.getModulesPortalRoot() + "/";
			}
		}

		return modulesConstants.getModulesUDTURL(getDiseaseId()) + modulesConstants.getModulesPortalRoot() + "/";
	}

	public String getModulesDTURL() {
		if (modulesConstants.getModulesDTURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesDTURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesDTURL() + modulesConstants.getModulesPortalRoot() + "/";
			}
		}

		return modulesConstants.getModulesDTURL(getDiseaseId()) + modulesConstants.getModulesPortalRoot() + "/";
	}

	public String getModulesAccountURL() {
		if (modulesConstants.getModulesAccountURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesAccountURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesAccountURL() + modulesConstants.getModulesPortalRoot() + "/";
			}
		}

		return modulesConstants.getModulesAccountURL(getDiseaseId()) + modulesConstants.getModulesPortalRoot() + "/";
	}

	public String getPortalHostURL() {
		if (modulesConstants.getModulesAccountURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesAccountURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesAccountURL();
			}
		}

		return modulesConstants.getModulesAccountURL(getDiseaseId());
	}

	public String getWebstart64BitJVMArgs() {
		return MessageFormat.format(JVM_ARGS, modulesConstants.get64BitJVMArgs());
	}

	protected List<EntityMap> getEntitiesForCurrentUser(PermissionType permission, EntityType entity, boolean getPublic)
			throws UnsupportedEncodingException {
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
		List<EntityMap> nonWSList = restProvider.listUserAccessEntities(entity, permission, getPublic);

		return nonWSList;
	}

	/**
	 * Before this method returns the value of property
	 * modules.account.removeUnused, this is basically the same as to check if it is
	 * PDBP instance. We need to remove this property since it's unnecessary.
	 * 
	 * @return true if it is a PDBP instance
	 */
	public boolean alternateWorkflow() {
		return (BricsInstanceType.PDBP == getInstanceType());
	}

	public boolean isGuidOnly() {
		BricsInstanceType currInstType = getInstanceType();

		return ((BricsInstanceType.NIA == currInstType) || (BricsInstanceType.NINDS == currInstType)
				|| (BricsInstanceType.GRDR == currInstType));
	}

	public BricsInstanceType getInstanceType() {
		String orgName = modulesConstants.getModulesOrgName();
		if (!StringUtils.isEmpty(orgName)) {
			return BricsInstanceType.getByInstanceName(orgName);
		}

		return null;
	}

	/**
	 * Retrieves the base URL for the universal DOI resolver.
	 * 
	 * @return The URL of the universal DOI resolver.
	 */
	public String getDoiResolverUrl() {
		return PortalConstants.DOI_RESOLVER_BASE_URL;
	}

	public Boolean getIsDoiEnabled() {
		return modulesConstants.isDoiEnabled();
	}

	public String getWebstartCodebase() {
		String result = this.getModulesAccountURL() + "jnlps";
		return result;
	}

	public boolean getIsNTRRInstance() {
		return (BricsInstanceType.NTRR == getInstanceType());
	}

	public String getGuidJwt()
			throws JsonSyntaxException, ProcessingException, InvalidJwtException, AuthenticationException {
		GuidJwt guidJwt = (GuidJwt) getSession().getAttribute(GuidJwt.SESSION_KEY);

		// if it exists, check expiration
		// if it has not expired, return it
		// if it has, get a new one, save to session, and return
		if (guidJwt != null && !guidJwt.isAlmostExpired()) {
			return guidJwt.toString();
		}

		Account account = getAccount();
		boolean globalAdmin = accountManager.hasRole(account, RoleType.ROLE_ADMIN);
		boolean hasGuidAccess = globalAdmin || accountManager.hasRole(account, RoleType.ROLE_GUID);
		boolean hasGuidAdminAccess = globalAdmin || accountManager.hasRole(account, RoleType.ROLE_GUID_ADMIN);

		if (hasGuidAccess || hasGuidAdminAccess) {
			GuidJwt userJwt = guidServerAuthUtil.getUserJwt(account);
			// save user's jwt to session. This method checks expiration when used again
			getSession().setAttribute(GuidJwt.SESSION_KEY, userJwt);
			return userJwt.toString();
		}

		return ServiceConstants.EMPTY_STRING;
	}

	public String getModulesStyleKey() {
		return modulesConstants.getModulesStyleKey();
	}
	
	public String getMicroserviceBaseUrl() {
		return modulesConstants.getMicroserviceDomain();
	}
	
    public String getMicroserviceJwt() {
    	String accountUsername = getAccount().getUserName();
        String passwd = HashMethods.getServerHash(accountUsername, HashMethods.convertFromByte(getAccount().getPassword()));
        String authenticationUrl = modulesConstants.getMicroserviceDomain() + GET_MICROSERVICE_JWT_URL_TAIL;

        WebClient client = WebClient.create(authenticationUrl);
        client.type(MediaType.APPLICATION_FORM_URLENCODED);
        String data = String.format(GET_MICROSERVICE_JWT_PARAMS_FORMAT, accountUsername, passwd);
        String jwtResponse = client.post(data, String.class);
        HttpServletResponse response = getResponse();

		try {
	        response.setContentType(ContentType.TEXT_PLAIN.toString());
			response.getWriter().write(jwtResponse);
		} catch (Exception e) {
			logger.error("Couldn't get a JWT. Cause: " + e.getMessage());

			try {
				response.sendError(401);
			} catch (IOException f) {
				// can't do anything if we can't write out
				f.printStackTrace();
				return null;
			}
		}
		return null;
    }
    
	// Used to return the NEI Public site 
	public String getTemplatePublicURL() {
		return modulesConstants.getTemplatePublicURL();
	}
}
