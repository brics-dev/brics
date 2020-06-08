package gov.nih.nichd.ctdb.common;

import org.springframework.beans.factory.annotation.Value;

public class ModulesConstants {
	
	/******************** Mail Messages *****************************/
	public final static String MAIL_RESOURCE_EMAIL_PSR = "emailPsr";

	public final static String MAIL_RESOURCE_TO = ".to";
	public final static String MAIL_RESOURCE_SUBJECT = ".subject";
	public final static String MAIL_RESOURCE_HEADER = ".header";
	public final static String MAIL_RESOURCE_BODY = ".body";
	public final static String MAIL_RESOURCE_FOOTER = ".footer";
	public final static String MAIL_RESOURCE_PD = ".pd";

	
	@Value("#{applicationProperties['modules.org.name']}")
	private String modulesOrgName;
	
	@Value("#{applicationProperties['modules.account.url.server']}")
	private String modulesAccountURL;
	
	@Value("#{applicationProperties['modules.administrator.username']}")
	private String administratorName;
	@Value("#{applicationProperties['modules.administrator.password']}")
	private String administratorPassword;
	@Value("#{applicationProperties['modules.administrator.saltedpassword']}")
	private String saltedAdministratorPassword;
	// Secure web services true or false (false on local only)
	// There is a hack here to support autowiring of a static field. (The set property is defined in context.xml)
	@Value("#{applicationProperties['modules.webservices.secured']}")
	private boolean wsSecured;
	
	public String getModulesOrgName() {
		return modulesOrgName;
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
	
	public String getModulesAccountURL() {
		return modulesAccountURL;
	}
	public boolean getWsSecured() {
		return wsSecured;
	}
}
