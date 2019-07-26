package gov.nih.tbi.commons.reporting;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.opensymphony.xwork2.ActionSupport;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.exceptions.PermissionException;
import gov.nih.tbi.commons.model.BricsInstanceType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.constants.ReportingModulesConstants;
import gov.nih.tbi.constants.ReportingPortalConstants;
import gov.nih.tbi.constants.ReportingServiceConstants;
import gov.nih.tbi.reporting.authentication.AccountUserDetails;

public class BaseAction extends ActionSupport {

	private static final long serialVersionUID = -3339107809449648314L;

	private static final String ANONYMOUS_USER_NAME = "anonymous";

	static Logger logger = Logger.getLogger(BaseAction.class);

	private String deploymentVersion;

	protected String username;

	@Autowired
	protected ReportingModulesConstants modulesConstants;

	public User getUser() {

		return getAccount().getUser();
	}
	
	public String loginCheck() {

		if (getRequest().getRemoteUser() == null) {
			return ReportingPortalConstants.ACTION_IN;
		}
		return ReportingPortalConstants.ACTION_OUT;
	}
	public Account getAccount() {

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

		return account;
	}

	/******************************************************************************************************/

	public Long getDiseaseId() {


			Authentication auth = (SecurityContextHolder.getContext().getAuthentication());
			Long diseaseId = null;
			if (auth.getPrincipal() instanceof String && ((String) auth.getPrincipal()).equals("guest")) {
				diseaseId = -1l;
			} else {
				diseaseId = ((AccountUserDetails) auth.getPrincipal()).getDiseaseId();
			}

		return diseaseId;
	}
	
	@Override
	public boolean hasActionErrors() {

		boolean test = super.hasActionErrors();

		for (String key : this.getFieldErrors().keySet()) {
			logger.debug("This is the map key " + key);
			for (String value : this.getFieldErrors().get(key)) {
				logger.debug("This is the map value: " + value);
			}
		}

		return test;
	}

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

	/**
	 * Convenience method to get the current URL
	 * 
	 * @return
	 */
	protected String getCurrentUrl() {

		return getRequest().getRequestURL().toString();
	}

	/**
	 * Convenience method to get the session. This will create a session if one doesn't exist.
	 * 
	 * @return the session from the request (request.getSession()).
	 */
	protected HttpSession getSession() {

		return getRequest().getSession();
	}

	public String getDeploymentVersion() {

		if (deploymentVersion == null) {
			try {
				ServletContext aContext = getSession().getServletContext();
				InputStream fis = aContext.getResourceAsStream("/META-INF/MANIFEST.MF");

				Properties p = new Properties();
				p.load(fis);

				deploymentVersion = p.getProperty("Implementation-Build");
			} catch (Exception ex) {
				deploymentVersion = "Unreadable";
			}
		}

		return deploymentVersion;
	}

	/**
	 * @inheritDoc
	 */
	public String checkUrlPrefix(String url) {

		if (url != null
				&& !(url.toLowerCase().startsWith(ReportingServiceConstants.URL_PREFIX_HTTP)
						|| url.toLowerCase().startsWith(ReportingServiceConstants.URL_PREFIX_HTTPS) || url.toLowerCase()
						.startsWith(ReportingServiceConstants.URL_PREFIX_FTP))) {
			return ReportingServiceConstants.URL_PREFIX_HTTP + url;
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

	public String permissionError() throws PermissionException {

		throw new PermissionException("");
	}

	public String uploadButton() {

		return "uploadButton";
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

		String result = modulesConstants.getModulesVTURL();
		return result;
	}

	// Used for webstart JNLPs
	public String getGuidToolServerName() {

		return modulesConstants.getModulesGTURL();
	}

	// Used for webstart JNLPs
	public String getWebstartSftpBasedir() {

		String result = modulesConstants.getWebstartSftpBasedir();
		return result;
	}

	// Used for webstart JNLPs
	public String getWebstartSftpName() {

		String result = modulesConstants.getWebstartSftpName();
		return result;
	}

	// Used for webstart JNLPs
	public String getWebstartSftpPort() {

		String result = modulesConstants.getWebstartSftpPort();
		return result;
	}

	// Used for webstart JNLPs
	public String getWebstartSftpUrl() {

		String result = modulesConstants.getWebstartSftpUrl();
		return result;
	}

	// Used for webstart JNLPs
	public String getWebstartSftpUser() {

		String result = modulesConstants.getWebstartSftpUser();
		return result;
	}

	// Used for webstart JNLPs
	public String getWebstartSftpPassword() {

		String result = modulesConstants.getWebstartSftpPassword();
		return result;
	}

	// Used for webstart JNLPs
	public String getWebstartPortalRoot() {

		String result = modulesConstants.getWebstartPortalRoot();
		return result;
	}

	// Used to return the MIPAV client link for the given enviornment
	public String getmIPAVClientURL() {

		return modulesConstants.getMIPAVURL();
	}

	// Enabled flag identifies if functionality is enabled on this server
	// URL Flag says what server to point to for this functionality (if

	public String getModulesPublicURL() {

		logger.debug("Public URL - " + modulesConstants.getModulesPublicURL());
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

		logger.debug("PF URL - " + modulesConstants.getModulesPFURL(getDiseaseId()));
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

		logger.debug("DDT URL - " + modulesConstants.getModulesDDTURL());
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
				return modulesConstants.getModulesQTURL() + ReportingModulesConstants.QUERY_ROOT + "/";
			}

		}

		return modulesConstants.getModulesQTURL(getDiseaseId()) + ReportingModulesConstants.QUERY_ROOT + "/";

	}
	
	
	public String getModulesReportingURL() {

		logger.debug("Reporting URL - " + modulesConstants.getModulesReportingURL());
		if (modulesConstants.getModulesReportingURL(getDiseaseId()).isEmpty()) {
			if (modulesConstants.getModulesReportingURL().isEmpty()) {
				return "";
			} else {
				return modulesConstants.getModulesReportingURL() + ReportingModulesConstants.REPORTING + "/";
			}

		}

		return modulesConstants.getModulesReportingURL(getDiseaseId()) + ReportingModulesConstants.REPORTING + "/";

	}

	public ReportingModulesConstants getModuleConstants() {

		return modulesConstants;
	}

	public String getModulesDdtUrl() {
		String result = this.modulesConstants.getModulesDDTURL();
		return result;
	}

	public String getModulesVTURL() {

		logger.debug("VT URL - " + modulesConstants.getModulesVTURL(getDiseaseId()));
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

		logger.debug("GT URL - " + modulesConstants.getModulesGTURL(getDiseaseId()));
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

		logger.debug("ST URL - " + modulesConstants.getModulesSTURL(getDiseaseId()));
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

		logger.debug("OM URL - " + modulesConstants.getModulesOMURL(getDiseaseId()));
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


	public String getModulesUDTURL() {

		logger.debug("UDT URL - " + modulesConstants.getModulesUDTURL(getDiseaseId()));
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

		logger.debug("DT URL - " + modulesConstants.getModulesDTURL(getDiseaseId()));
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

		logger.debug("Account URL - " + modulesConstants.getModulesAccountURL(getDiseaseId()));
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




	/**
	 * Before this method returns the value of property modules.account.removeUnused, this is 
	 * basically the same as to check if it is PDBP instance. We need to remove this property 
	 * since it's unnecessary.
	 * 
	 * @return true if it is a PDBP instance
	 */
	public boolean alternateWorkflow() {
		return (BricsInstanceType.PDBP == getInstanceType());
	}
	
	public BricsInstanceType getInstanceType() {
		String orgName = modulesConstants.getModulesOrgName();
		if (!StringUtils.isEmpty(orgName)) {
			return BricsInstanceType.valueOf(orgName);
		}
		
		return null;
	}
	
	/**
	 * Retrieves the base URL for the universal DOI resolver.
	 * 
	 * @return The URL of the universal DOI resolver.
	 */
	public String getDoiResolverUrl() {
		return ReportingPortalConstants.DOI_RESOLVER_BASE_URL;
	}

	public Boolean getIsDoiEnabled() {
		return modulesConstants.isDoiEnabled();
	}

	public String getWebstartCodebase() {
		String result = this.getModulesAccountURL() + "jnlps";
		return result;
}
	
	
}
