package gov.nih.tbi.repository.ws.cxf;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.model.UserLogin;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.BadParameterException;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.repository.model.SubmissionTicket;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Study;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component(value = "webstartRestServiceBean")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Path("/")
public class WebstartRepositoryWebService extends RepositoryRestService {

	private static Logger logger = Logger.getLogger(AbstractRestService.class);

	@Autowired
	protected RepositoryManager repositoryManager;

	@Autowired
	protected ModulesConstants modulesConstants;
	@Autowired
	private AccountManager accountManager;

	protected static final String ANONYMOUS_USER_NAME = "anonymous";
	protected static final String ADMIN_USER_NAME = "administrator";

	private Account requestedAccount;

	/**
	 * Returns the account of a user who authenticating themselves using the credential supplied through the JNLP. If
	 * the credentials supplied are not correct, then null is returned.
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	protected Account getRequestedAccount(String userName, String password) {

		UserLogin user =
				new UserLogin(userName, HashMethods.getClientHash(userName), HashMethods.getClientHash(
						HashMethods.getServerHash(userName), password));
		if (user.getUserName().equals("anonymous")) {
			return null;
		}

		Account account = accountManager.getAccountByUserName(user.getUserName());
		
		logger.debug("getting requested account: " + account);
		
		if (account == null) {
			throw new RuntimeException("User with name " + user.getUserName() + " does not exist.");
		}

		String acctPassword = HashMethods.convertFromByte(account.getPassword());
		acctPassword = HashMethods.getServerHash(user.getUserName(), acctPassword);

		if ((HashMethods.validateClientHash(user.getHash1(), user.getUserName()))
				&& (HashMethods.validateClientHash(user.getHash2(), HashMethods.getServerHash(user.getUserName()),
						acctPassword))) {
			return account;
		}

		return null;
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
	 * @param userName
	 * @param password
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("Study/getStudies")
	@Produces("text/xml")
	public Set<Study> getStudies(@HeaderParam("userName") String userName, @HeaderParam("pass") String password)
			throws UnsupportedEncodingException {

		requestedAccount = getRequestedAccount(userName, password);
		return getStudies();
	}

	/**
	 * Given the name of a study, a list of Datasets are returned. If the study doesnt exist, null will be returned.
	 * 
	 * @param name
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("Dataset/getDatasets")
	@Produces("text/xml")
	public Set<Dataset> getDatasets(@QueryParam("study") String name, @HeaderParam("UserName") String userName,
			@HeaderParam("Pass") String password) throws UnsupportedEncodingException {

		requestedAccount = getRequestedAccount(userName, password);
		
		//we need to re-encode the name again before calling our normal rest webservice
		name = URLEncoder.encode(name, "UTF-8"); 
		return getDatasets(name);

	}

	/**
	 * Processes a submission ticket, which results in the returning the of a Dataset
	 * 
	 * @param subProcTckt
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@POST
	@Path("dataset/processSubmissionTicket/")
	@Consumes({MediaType.APPLICATION_XML, "application/json"})
	public Response processSubmissionTicket(@HeaderParam("Pass") String password,
			@HeaderParam("userName") String userName, SubmissionTicket subProcTckt) throws UnsupportedEncodingException {

		requestedAccount = getRequestedAccount(userName, password);
		return processSubmissionTicket(subProcTckt);
	}

	/**
	 * Retrieves a list of Datasets that are currently being uploaded to repository
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("Dataset/getUploadingDatasets")
	@Produces("text/xml")
	public List<Dataset> getUploadingDataset(@HeaderParam("UserName") String userName,
			@HeaderParam("Pass") String password) throws UnsupportedEncodingException {

		requestedAccount = getRequestedAccount(userName, password);
		return getUploadingDataset();

	}

	/**
	 * Given a Dataset id, A set of DatasetFiles are returned belonging to the dataset
	 * 
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("dataset/{id}/datasetFiles")
	@Produces("text/xml")
	public Set<DatasetFile> getDatasetFiles(@PathParam("id") Long id, @HeaderParam("UserName") String userName,
			@HeaderParam("Pass") String password) throws UnsupportedEncodingException {

		requestedAccount = getRequestedAccount(userName, password);

		return getDatasetFiles(id);
	}

	/**
	 * Used in the Upload Manager, given a DatasetFile id, the DatasetFile's status is set to complete.
	 * 
	 * @param id
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("DatasetFile/setToComplete")
	@Produces("text/xml")
	public Response setDatasetFileToComplete(@QueryParam("id") Long id, @HeaderParam("UserName") String userName,
			@HeaderParam("Pass") String password) throws MalformedURLException, UnsupportedEncodingException {

		requestedAccount = getRequestedAccount(userName, password);
		return setDatasetFileToComplete(id);
	}

	/**
	 * Given a DatasetFile id, the study that is connected to it is returned.
	 * 
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("Study/getFromDataset")
	@Produces("text/xml")
	public BasicStudy getStudyFromDatasetFile(@QueryParam("id") Long id, @HeaderParam("UserName") String userName,
			@HeaderParam("Pass") String password) throws UnsupportedEncodingException {

		requestedAccount = getRequestedAccount(userName, password);
		return getStudyFromDatasetFile(id);
	}

	/**
	 * Given both a Study and Dataset name, the dataset is deleted. Note: If that dataset is in someone's Download Queue
	 * the deletion will fail with error.
	 * 
	 * @param studyName
	 * @param datasetName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@DELETE
	@Path("Study/{studyName}/deleteByName")
	@Produces("text/xml")
	public Response deleteDatasetByName(@PathParam("studyName") String studyName,
			@QueryParam("dataset") String datasetName, @HeaderParam("UserName") String userName,
			@HeaderParam("Pass") String password) throws UnsupportedEncodingException {

		requestedAccount = getRequestedAccount(userName, password);
		studyName = URLDecoder.decode(studyName, "UTF-8");
		datasetName = URLDecoder.decode(datasetName, "UTF-8");

		return deleteDatasetByName(studyName, datasetName);
	}

	/**
	 *	 * Given both a Study and Dataset name, the dataset is deleted. Note: Cascade has been added to database tables
	 * 
	 * @param studyName
	 * @param datasetName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@DELETE
	@Path("Study/{studyName}/deleteCascadeByName")
	@Produces("text/xml")
	public Response deleteDatasetCascadeByName(@PathParam("studyName") String studyName,
			@QueryParam("dataset") String datasetName, @HeaderParam("UserName") String userName,
			@HeaderParam("Pass") String password) throws UnsupportedEncodingException {

		requestedAccount = getRequestedAccount(userName, password);
		studyName = URLDecoder.decode(studyName, "UTF-8");
		datasetName = URLDecoder.decode(datasetName, "UTF-8");

		return deleteDatasetCascadeByName(studyName, datasetName);
	}


	/**
	 * Given an id of a dataset, a string representation of the number of files that have completed uploading
	 * 
	 * @param datasetId
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UserPermissionException
	 */
	@GET
	@Path("upload/completedFiles")
	@Produces("text/xml")
	public String getNumOfCompletedFiles(@QueryParam("dataset") String dataset,
			@HeaderParam("UserName") String userName, @HeaderParam("Pass") String password)
			throws UnsupportedEncodingException, UserPermissionException, BadParameterException {

		requestedAccount = getRequestedAccount(userName, password);
		dataset = URLDecoder.decode(dataset, "UTF-8");
		return getNumOfCompletedFiles(dataset);
	}

	/**
	 * A list of UserDownloadQueue objects is retrieved that belong to account the web service caller
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("download/DownloadPackage")
	@Produces("text/xml")
	public List<DownloadPackage> getDownloadPackage(@HeaderParam("UserName") String userName,
			@HeaderParam("Pass") String password) throws UnsupportedEncodingException {

		requestedAccount = getRequestedAccount(userName, password);
		return getDownloadPackage();
	}


	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Path("download/deleteDownloadables")
	public Response deleteDownloadables(@FormParam("id") List<Long> selectedIds,
			@HeaderParam("UserName") String userName, @HeaderParam("Pass") String password)
			throws UnsupportedEncodingException {

		if (selectedIds.isEmpty() || selectedIds.size() == 0) {
			return Response.noContent().build();
		}
		requestedAccount = getRequestedAccount(userName, password);

		return deleteDownloadablesByIds(selectedIds);
	}

	/**
	 * Delete the download packages with the given IDs in the parameter.
	 * 
	 * @param selectedIds - id =
	 * @param userName
	 * @param password
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@DELETE
	@Path("download/deleteDownloadPackages")
	public Response deleteDownloadPackages(@QueryParam("id") List<Long> selectedIds,
			@HeaderParam("UserName") String userName, @HeaderParam("Pass") String password)
			throws UnsupportedEncodingException {

		// @QueryParam("id") List<Long> selectedIds,
		if (selectedIds.isEmpty() || selectedIds.size() == 0) {
			return Response.noContent().build();
		}
		requestedAccount = getRequestedAccount(userName, password);

		return deleteDownloadPackagesByIds(selectedIds);
	}

	@POST
	@Path("download/logger")
	@Consumes({MediaType.APPLICATION_XML, "application/json"})
	public void sendDownloadReport(String report) {

		logger.info(report);
	}

	@POST
	@Path("hibernate/clearCache")
	public Response clearHibernateCache(@HeaderParam("UserName") String userName, @HeaderParam("Pass") String password) {
		requestedAccount = getRequestedAccount(userName, password);
		
		return clearHibernateCache();
	}
}
