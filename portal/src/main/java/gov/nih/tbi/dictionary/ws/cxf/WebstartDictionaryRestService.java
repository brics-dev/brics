
package gov.nih.tbi.dictionary.ws.cxf;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.cxf.StatelessAccountRestProvider;
import gov.nih.tbi.account.ws.cxf.StatelessAccountRestService;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.account.ws.model.UserLogin;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.StructuralFormStructureList;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@Path("/dictionary/")
public class WebstartDictionaryRestService extends DictionaryRestService {

	private static Logger logger = Logger.getLogger(WebstartDictionaryRestService.class);

	@Autowired
	protected ModulesConstants modulesConstants;
	@Autowired
	private AccountManager accountManager;

	protected static final String ANONYMOUS_USER_NAME = "anonymous";
	protected static final String ADMIN_USER_NAME = "administrator";

	Account requestedAccount;

	/**
	 * Returns the account of a user who authenticating themselves using the credential supplied through the JNLP. If
	 * the credentials supplied are not correct, then null is returned.
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	protected Account getRequestedAccount(String portalDomain, String userName, String password) {

		if (userName.equals("anonymous")) {
			return null;
		}

		StatelessAccountRestProvider accountRestProvider =
				new StatelessAccountRestProvider(portalDomain, userName, password);

		Account account = accountRestProvider.getAccountByUserName(userName);

		return account;
	}

	protected Account getAuthenticatedAccount() {

		return requestedAccount;
	}

	/**************************************************************************************************************************************
	 * 
	 * 
	 * Webstart Rest Services
	 * 
	 * 
	 *************************************************************************************************************************************/
	/**
	 * 
	 * A convience pass through method for webstart services
	 * 
	 * @param userName
	 * @param password
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("/formStructure/details")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces("text/xml")
	public StructuralFormStructureList getDataStructuresDetails(@HeaderParam("userName") String userName,
			@HeaderParam("pass") String password, @QueryParam("dsName") List<String> dsNameList)
			throws UnsupportedEncodingException {

		StructuralFormStructureList list = new StructuralFormStructureList();
		list.addAll(getDataStructureDetails(dsNameList));
		return list;

	}

	@GET
	@Path("DataElement/name/{name}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getDataElementByName(@PathParam("name") String name,
			@QueryParam("portalDomain") String portalDomain, @HeaderParam("userName") String userName,
			@HeaderParam("pass") String password) throws UserAccessDeniedException{
		logger.info("Username: " + userName);
		logger.info("Password: " + password);
		this.requestedAccount = getRequestedAccount(portalDomain, userName, password);
		return getDataElementByName(name);
	}
}
