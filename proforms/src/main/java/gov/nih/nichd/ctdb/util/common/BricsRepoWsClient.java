package gov.nih.nichd.ctdb.util.common;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.rs;
import gov.nih.nichd.ctdb.protocol.domain.BricsStudy;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.tbi.account.model.hibernate.Account;

public class BricsRepoWsClient
{
	private static final Logger logger = Logger.getLogger(BricsRepoWsClient.class);
	
	/**
	 * The BricsRepoWsClient constructor.
	 */
	public BricsRepoWsClient()
	{
		
	}
	
	
	/**
	 * Get a hash table of BRICS studies that the specified user as access to.
	 * 
	 * @param currUser - The current logged in user.
	 * @param request - The request from an action class.
	 * @param currStudy - The current study data from the user.
	 * @return	A hash table of BRICS studies or an empty hash table if no studies are found.
	 * @throws ParserConfigurationException	When an error occurred while generating the XML parser or parsing the public studies XML file
	 * @throws SAXException	When an error occurred during parsing the XML or while generating the parser
	 * @throws IOException	When an error occurred while retrieving the XML response from the web service for public studies
	 * @throws CtdbException When an error occurred while the proxy ticket was being added to the web service URL
	 */
	public Hashtable<String, BricsStudy> getBricsStudiesForUser(User currUser, HttpServletRequest request, Protocol currStudy) throws ParserConfigurationException, SAXException, IOException, CtdbException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		BricsStudyXmlHandler parserHandler = new BricsStudyXmlHandler();
		SecuritySessionUtil ssu = new SecuritySessionUtil(request);
		Locale userLocal = request.getLocale();
		String urlDomain = SysPropUtil.getProperty("webservice.usermgmt.url");
		//String urlDomain = "http://fitbir-portal-local.cit.nih.gov:8080/";
		String baseUrl = urlDomain + SysPropUtil.getProperty("webservice.repository.base.url") + SysPropUtil.getProperty("webservice.repository.studyList.relPath");
		String webServiceUrl = baseUrl;
		
		try
		{
			// Prep work
			String proxyTicket = ssu.getProxyTicket(urlDomain);
			
			// Check if the user has the study admin role in BRICS
			if ( !isUserStudyAdmin(currUser, request) )
			{
				// Construct the authenticated web service URL for studies that the user can only read
				webServiceUrl += currUser.getUsername() + "?permission=READ&noRejected=true";
				webServiceUrl = ssu.compileProxiedWebserviceUrl(webServiceUrl, proxyTicket);
				parserHandler.setStudyPermission(rs.getValue("study.dataRepo.studyPermission.read", userLocal));
				
				// Parse the XML file
				logger.info("Parsing XML from:  " + webServiceUrl);
				saxParser.parse(webServiceUrl, parserHandler);
				
				// Construct the authenticated web service URL for studies that user can only write to
				proxyTicket = ssu.getProxyTicket(urlDomain);
				webServiceUrl = baseUrl + currUser.getUsername() + "?permission=WRITE&noRejected=true";
				webServiceUrl = ssu.compileProxiedWebserviceUrl(webServiceUrl, proxyTicket);
				parserHandler.setStudyPermission(rs.getValue("study.dataRepo.studyPermission.write", userLocal));
				
				// Parse the XML file
				logger.info("Parsing XML from:  " + webServiceUrl);
				saxParser.parse(webServiceUrl, parserHandler);
				
				// Construct the authenticated web service URL for studies that user as admin permissions to
				proxyTicket = ssu.getProxyTicket(urlDomain);
				webServiceUrl = baseUrl + currUser.getUsername() + "?permission=ADMIN&noRejected=true";
				webServiceUrl = ssu.compileProxiedWebserviceUrl(webServiceUrl, proxyTicket);
				parserHandler.setStudyPermission(rs.getValue("study.dataRepo.studyPermission.admin", userLocal));
				
				// Parse the XML file
				logger.info("Parsing XML from:  " + webServiceUrl);
				saxParser.parse(webServiceUrl, parserHandler);
				
				// Construct the authenticated web service URL for studies that user owns
				proxyTicket = ssu.getProxyTicket(urlDomain);
				webServiceUrl = baseUrl + currUser.getUsername() + "?permission=OWNER&noRejected=true";
				webServiceUrl = ssu.compileProxiedWebserviceUrl(webServiceUrl, proxyTicket);
				parserHandler.setStudyPermission(rs.getValue("study.dataRepo.studyPermission.owner", userLocal));
				
				// Parse the XML file
				logger.info("Parsing XML from:  " + webServiceUrl);
				saxParser.parse(webServiceUrl, parserHandler);
			}
			else
			{
				// Construct the authenticated web service URL for studies that user as admin permissions to
				proxyTicket = ssu.getProxyTicket(urlDomain);
				webServiceUrl = baseUrl + currUser.getUsername() + "?permission=ADMIN&noRejected=true";
				webServiceUrl = ssu.compileProxiedWebserviceUrl(webServiceUrl, proxyTicket);
				parserHandler.setStudyPermission(rs.getValue("study.dataRepo.studyPermission.admin", userLocal));
				
				// Parse the XML file
				logger.info("Parsing XML from:  " + webServiceUrl);
				saxParser.parse(webServiceUrl, parserHandler);
			}
		}
		catch ( Exception e )
		{
			// Log the error
			logger.warn("Could not use authenticated web service URL(s) for " + currUser.getUsername() + 
					". Falling back to using public or published BRICS studies.", e);
			
			// Fall back to getting only the public, published BRICS studies
			parserHandler.getStudyTable().clear();
			parserHandler.setStudyPermission("");
			saxParser.parse(baseUrl, parserHandler);
		}
		
		return parserHandler.getStudyTable();
	}
	
	/**
	 * Checks if the specified user has the study admin role in BRICS.
	 * 
	 * @param currUser - The current logged in user
	 * @param request - The request object from an action class.
	 * @return	True if the specified user has the study admin role, or false otherwise
	 * @throws CtdbException	When an error occurs while calling the account web service.
	 * @throws UnknownHostException 
	 * @throws NoRouteToHostException 
	 */
	private boolean isUserStudyAdmin(User currUser, HttpServletRequest request) throws CtdbException, NoRouteToHostException, UnknownHostException
	{
		boolean isStudyAdmin = false;
		SecuritySessionUtil ssu = new SecuritySessionUtil(request);
		
		try
		{
			List<Account> allStudyAdminAccounts = ssu.accountRestWs("ROLE_STUDY_ADMIN"); 
			
			// Check if the current user has the "ROLE_STUDY_ADMIN" role
			for ( Account a : allStudyAdminAccounts )
			{
				if ( a.getUserName().equalsIgnoreCase(currUser.getUsername()) )
				{
					isStudyAdmin = true;
					break;
				}
			}
		}
		catch ( RuntimeException re )
		{
			logger.warn("Could not get list of study admins. Assuming " + currUser.getUsername() + " is not a study admin: " + re.getLocalizedMessage());
		}
		
		return isStudyAdmin;
	}
}
