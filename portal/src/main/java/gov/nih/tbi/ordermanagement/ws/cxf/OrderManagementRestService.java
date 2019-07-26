package gov.nih.tbi.ordermanagement.ws.cxf;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.model.ItemQueue;
import gov.nih.tbi.ordermanager.service.ItemQueueService;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@Path("/")
public class OrderManagementRestService extends AbstractRestService {

	private static Logger logger = Logger.getLogger(OrderManagementRestService.class);

	@Autowired
	private ItemQueueService itemQueueService;

	@Autowired
	private AccountDao accountDao;

	@GET
	@Path("biosampleQueue")
	@Produces("text/xml")
	public String getBiosampleQueue() throws UnsupportedEncodingException {
		Account account = getAuthenticatedAccount();

		if (account == null) {
			logger.error("User was not authenticated to call getSampleIdFromQueue()");
		}

		ItemQueue userQueue = itemQueueService.getItemQueueForUser(account.getUser());

		List<String> biosampleIds = new ArrayList<String>();

		if(userQueue != null){
			for (BiospecimenItem item : userQueue.getItems()) {
				biosampleIds.add(item.getCoriellId());
			}
		}

		return BRICSStringUtils.concatWithDelimiter(biosampleIds, ",");
	}

	@POST
	@Path("addItem")
	public Response addItem(@QueryParam("repoId") Integer repoId, @QueryParam("GUID") String guid,
			@QueryParam("VisitTypPDBP") String visitTypePDBP, @QueryParam("AgeYrs") String ageYrs,
			@QueryParam("AgeVal") String ageVal, @QueryParam("BiosampleDataOriginator") String biosampleDataOriginator,
			@QueryParam("BioreposTubeID") String bioreposTubeId, @QueryParam("SampCollTyp") String sampCollTyp,
			@QueryParam("SampleAliquotMass") String sampleAliquotMass,
			@QueryParam("SampleAliquotMassUnits") String sampleAliquotMassUnits,
			@QueryParam("SampleAliquotVol") String sampleAliquotVol,
			@QueryParam("SampleAliquotVolUnits") String sampleAliquotVolUnits,
			@QueryParam("SampleAvgHemoglobinVal") String sampleAvgHemoglobinVal,
			@QueryParam("ConcentrationUoM") String concentrationUom,
			@QueryParam("Repository_Biosample") String repositoryBiosample, String requestData)
			throws UnsupportedEncodingException {

		Account account = getAuthenticatedAccount();

		if (logger.isDebugEnabled()) {
			logger.debug("***order managerment rest service invoked with the following data***");
			logger.debug(requestData);
		}

		String[] pieces = requestData.split("&");
		if (pieces == null) {
			String msg = "The service can not have null argument";
			logger.error(msg);
			Response errResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
			throw new WebApplicationException(errResponse);
		}

		/* TODO need to use the return value to respond back to the caller of success or failure */
		this.itemQueueService.addItemToUserQueue(account.getUser(), requestData);

		return Response.ok().build();
	}

	public ItemQueueService getItemQueueService() {

		return itemQueueService;
	}

	public void setItemQueueService(ItemQueueService itemQueueService) {

		this.itemQueueService = itemQueueService;
	}

	public AccountDao getAccountDao() {

		return accountDao;
	}

	public void setAccountDao(AccountDao accountDao) {

		this.accountDao = accountDao;
	}
}
