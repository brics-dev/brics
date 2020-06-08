package gov.nih.nichd.ctdb.security.util;

import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.protocol.action.IntervalAction;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.dictionary.model.EformRestServiceModel.EformList;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;

public class CasServiceUserHelper {
	private static final Logger logger = Logger.getLogger(IntervalAction.class);

	
	public static void main(String[] args) {
		String username = "abcd";
		String password = "abcd";
		String loginUrl = "http://fitbir-cas-local.cit.nih.gov:8083/cas/v1/tickets";
		String service = "http://fitbir-dd-local.cit.nih.gov:8081/portal/j_spring_cas_security_check";
		String formShortName = "AdverseEvents_7";
		String serviceUrl = service;
		
		Eform form = CasServiceUserHelper.getEformWithoutSession(loginUrl, serviceUrl, username, password, formShortName);
	    System.out.println("complete");
	}
	
	/**
	 * Retrieve an Eform through the dictionary webservice using the System Credentials instead of the 
	 * user's credentials.   This should be used ONLY when the user cannot be logged in.  Otherwise,
	 * get a proxy ticket and request the form that way.
	 * 
	 * @param loginUrl CAS Rest API login URL (/cas/v1/tickets)
	 * @param serviceUrl Service URL for the service that needs login (/portal/j_spring_cas_security_check)
	 * @param username login username (plain text)
	 * @param password login password (plain text).  Sent in the URL so sending to only local servers is recommended
	 * @param formShortName Short name of form to retrieve
	 * @return Eform the eform retrieved or null if not found
	 */
	public static Eform getEformWithoutSession(
			String loginUrl, 
			String serviceUrl, 
			String username,
			String password, 
			String formShortName) {
		
		Eform eform = null;
		
		try {
			serviceUrl = URLEncoder.encode(serviceUrl, "UTF-8");
		}
		catch(Exception e) {
			// if this fails, fall back to using the serviceUrl itself without encoding
		}
		
		Client client = ClientBuilder.newClient();
		try {
			logger.debug("-- begin get form without user session -- ");
			logger.debug("login URL  : " + loginUrl);
			logger.debug("service URL: " + serviceUrl);
			logger.debug("username   : " + username);
			logger.debug("----------------------------------------- ");
			try {
				String sgtUrl = CasServiceUserHelper.login(client, loginUrl, serviceUrl, username, password);
				String serviceTicket = CasServiceUserHelper.getServiceTicket(client, sgtUrl, serviceUrl);
				eform = CasServiceUserHelper.getEform(client, formShortName, serviceTicket);
				CasServiceUserHelper.logout(client, loginUrl, serviceTicket);
			}
			catch(Exception e) {
				logger.error("retrieve eform process failed");
				throw e;
			}
		}
		catch(Exception e) {
			logger.error("Get Eform failed full process");
			e.printStackTrace();
		}
		finally {
			client.close();
		}
		logger.debug("form retrieval process complete");
		return eform;
	}
	
	public static String login(Client client, String loginUrl, String serviceUrl, String username, String password) throws Exception {
		
		logger.debug("begin log in to system user");
		String sgtUrl = "";
		try {
			WebTarget target = client.target(loginUrl);
			target = target.queryParam("username", username);
			target = target.queryParam("password", password);
			target = target.queryParam("service", serviceUrl);
			Response loginResponse = target.request(MediaType.APPLICATION_FORM_URLENCODED)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.post(null, Response.class);
	
			sgtUrl = loginResponse.getHeaderString("location");
			if (sgtUrl == null) {
				throw new IllegalStateException("The service granting ticket is empty.  This normally means the user did not authenticate correctly");
			}
			logger.debug("logged in as system user with token location: " + sgtUrl);
		}
		catch(Exception e) {
			logger.error("-- LOG IN FAILED --");
			logger.error("Log In URL: " + loginUrl);
			logger.error("Service URL: " + serviceUrl);
			logger.error("Username: " + username);
			logger.error("-- FAILED --");
			throw e;
		}
		return sgtUrl;
	}
	
	public static String getServiceTicket(Client client, String sgtUrl, String serviceUrl) throws Exception {
		logger.debug("begin get service ticket for system user");
		String serviceTicket = "";
		try {
			WebTarget target = client.target(sgtUrl);
			target = target.queryParam("service", serviceUrl);
			Response stResponse = target.request(MediaType.APPLICATION_FORM_URLENCODED)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.post(null, Response.class);
			
			serviceTicket = stResponse.readEntity(String.class);
			logger.debug("service ticket: " + serviceTicket);
		}
		catch(Exception e) {
			logger.error("-- GET SERVICE TICKET FAILED --");
			logger.error("Service Grating Ticket URL: " + sgtUrl);
			logger.error("Service URL: " + serviceUrl);
			logger.error("-- FAILED --");
			throw e;
		}
		return serviceTicket;
	}
	
	private static Eform getEform(Client client, String shortName, String serviceTicket) throws Exception {
		logger.debug("begin get eform through system user");
		Eform result = null;
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.eform.ddt.url") + "/" + shortName;
//		String restfulDomain = "http://fitbir-dd-local.cit.nih.gov:8081/";
//		String restfulUrl = restfulDomain + "portal/ws/ddt/dictionary/eforms" + "/"  + shortName;
		try {
			WebTarget target = client.target(restfulUrl);
			target = target.queryParam("ticket", serviceTicket);
			EformList eformList = target.request(MediaType.APPLICATION_XML, MediaType.TEXT_XML).get(EformList.class);
			List<Eform> eforms = eformList.getList();
	        result = eforms.get(0);
	        logger.debug("eforms retrieved: " + eforms.size());
		}
		catch(Exception e) {
			logger.error("-- GET FORM FAILED --");
			logger.error("Rest URL: " + restfulUrl);
			logger.error("service ticket: " + serviceTicket);
			logger.error("ShortName: " + shortName);
			logger.error("-- FAILED --");
			throw e;
		}
		return result;
	}
	
	public static void logout(Client client, String loginUrl, String serviceTicket) throws Exception {
		try {
			logger.debug("begin system user log out");
			String logoutUrl = loginUrl + "/" + serviceTicket;
			WebTarget logoutTarget = client.target(logoutUrl);
			logoutTarget.request(MediaType.APPLICATION_FORM_URLENCODED)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.delete();
			logger.debug("end system user log out");
        }
		catch(Exception e) {
			logger.error("-- LOG OUT FAILED --");
			logger.error("Login URL: " + loginUrl);
			logger.error("service ticket: " + serviceTicket);
			logger.error("-- FAILED --");
			throw e;
		}
	}
	
	private static void verify() {
		//String validateUrl = "http://fitbir-cas-local.cit.nih.gov:8083/cas/serviceValidate";
//		try {
//		// validate
//		System.out.println("validate ticket");
//		Client validateClient = ClientBuilder.newClient();
//		WebTarget validateTarget = validateClient.target(validateUrl);
//		validateTarget = validateTarget.queryParam("service", service);
//		validateTarget = validateTarget.queryParam("ticket", serviceTicket);
//		validateTarget = validateTarget.queryParam("pgtUrl", service);
//		Response validateResponse = validateTarget.request(MediaType.APPLICATION_FORM_URLENCODED)
//				.header("Content-Type", "application/x-www-form-urlencoded")
//				.post(null, Response.class);
//		
//		System.out.println(validateResponse.readEntity(String.class));
//		validateClient.close();
//	}
//	catch(Exception e) {
//		System.out.println("validation failed");
//		e.printStackTrace();
//	}
	}
}
