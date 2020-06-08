package gov.nih.tbi.repository.ws.cxf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.DataSource;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.BadParameterException;
import gov.nih.tbi.commons.service.HibernateManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.dictionary.exceptions.DictionaryVersioningException;
import gov.nih.tbi.dictionary.model.UpdateFormStructureReferencePayload;
import gov.nih.tbi.dictionary.model.hibernate.PublishedFormStructure;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.restful.CreateTableFromFormStructurePayload;
import gov.nih.tbi.dictionary.ws.RestDictionaryProvider;
import gov.nih.tbi.metastudy.dao.MetaStudyDao;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.repository.dao.StudyDao;
import gov.nih.tbi.repository.model.RepositoryRestServiceModel.DownloadableWrapper;
import gov.nih.tbi.repository.model.SubmissionTicket;
import gov.nih.tbi.repository.model.alzped.AlzPed;
import gov.nih.tbi.repository.model.alzped.ModelName;
import gov.nih.tbi.repository.model.alzped.ModelType;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.TherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.TherapyType;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Downloadable;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudySite;
import gov.nih.tbi.repository.model.hibernate.UserFile;

@Path("/repository/")
public class RepositoryRestService extends AbstractRestService {

	private static Logger logger = Logger.getLogger(RepositoryRestService.class);

	@Autowired
	protected RepositoryManager repositoryManager;

	@Autowired
	private AccountManager accountManager;

	@Autowired
	protected SessionAccount sessionAccount;

	@Autowired
	protected ModulesConstants modulesConstants;

	@Autowired
	private StudyDao studyDao;
	
	@Autowired
	private MetaStudyDao metaStudyDao;

	@Resource
	private WebServiceContext wsContext;

	@Autowired
	private HibernateManager hibernateManager;

	UriInfo uriInfo;

	private String DOWNLOADED_PACKAGE_PATH = "downloadPackages" + File.separator;
	protected String DOWNLOADED_DATA_DIRECTORY =
			System.getProperty("user.home") + File.separator + DOWNLOADED_PACKAGE_PATH;

	/**
	 * Gets a study based on the study id. All users can see public studies, a user needs at least READ to see a study
	 * in any other state.
	 * 
	 * @param studyId
	 * @return
	 * @throws UserPermissionException
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("Study/get/{studyId}")
	@Produces("text/xml")
	public Study getStudyByStudyId(@PathParam("studyId") String studyId)
			throws UserPermissionException, UnsupportedEncodingException, UserAccessDeniedException {

		logger.debug(
				"REST WS CALL: getStudyByStudyId ( " + studyId + " ) by " + getAuthenticatedAccount().getUserName());

		// get the study
		Study study = null;
		study = repositoryManager.getStudyByPrefixedId(getAuthenticatedAccount(), studyId);

		// verify existence of study
		if (study == null) {
			throw new RuntimeException("Study: " + studyId + " cannot be found.");
		}

		// There is no need to check permissions if this study is public (anyone
		// can see it)
		if (study.getIsPublic()) {
			return study;
		}
		// make sure the user has access to this study if the web services are
		// secure (not local)
		if (study != null && ModulesConstants.getWsSecured()) {
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider =
					new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			PermissionType permission = restProvider
					.getAccess(getAuthenticatedAccount().getId(), EntityType.STUDY, study.getId()).getPermission();
			if (PermissionType.compare(permission, PermissionType.READ) < 0) {
				throw new UserPermissionException(
						getAuthenticatedAccount().getUserName() + " does not have access to view study:" + studyId);
			}
		}

		return study;

	}

	/**
	 * Returns a list of studies. If a username is supplied AND the authenticated user is a repository admin, then the
	 * list of studies for "username" will be supplied. Studies of any state will be returned (PRIVATE, PUBLIC,
	 * REQUESTED, REJECTED)
	 * 
	 * The user can request that REJECTED studies are filtered out of the results with the parameter: noRejected=true
	 * 
	 * A list of BasicStudies is returned rather than a list of studies. I large list of full studies (including all
	 * datasets) contains a large amount of unnecessary information.
	 * 
	 * @param username
	 * @param permission
	 * @param noRejected - A flag to indicate whether or not to include rejected studies from the list in the response.
	 *        The default value is false.
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UserPermissionException
	 */
	@GET
	// This path makes username optional (Study/list and Study/list/ are both
	// valid)
	@Path("Study/list{username: .*}")
	@Produces(MediaType.APPLICATION_XML)
	public List<Study> listStudiesByUserAndPermission(@PathParam("username") String username,
			@QueryParam("permission") PermissionType permission,
			@DefaultValue("false") @QueryParam("noRejected") Boolean noRejected) throws WebApplicationException {

		// Find which user you are getting studies for and if you have
		// permission
		Account account = null;

		try {
			if (username == null || username.equals(ServiceConstants.EMPTY_STRING)
					|| getAuthenticatedAccount().getUserName().equals(username)) {
				account = getAuthenticatedAccount();
			} else if (accountManager.hasRole(getAuthenticatedAccount(), RoleType.ROLE_STUDY_ADMIN)) {
				// Because of the if statement above (username == null...) we
				// can assume that username is not null in
				// this block
				account = accountManager.getAccountByUserName(username);
			} else {
				throw new ForbiddenException("The user " + getAuthenticatedAccount().getUserName()
						+ " does not have permission to get the study list for " + username);
			}
		} catch (UnsupportedEncodingException e) {
			throw new BadRequestException(e);
		}

		// The default permission type is READ
		if (permission == null) {
			permission = PermissionType.READ;
		}

		List<Study> studies = null;

		try {
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			String proxyTicket = PortalUtils.getProxyTicket(accountUrl);

			studies = repositoryManager.listStudies(account, proxyTicket, permission);

			// Filter out the rejected studies, if needed.
			if (noRejected) {
				for (Iterator<Study> it = studies.iterator(); it.hasNext();) {
					Study s = it.next();

					if (s.getStudyStatus() == StudyStatus.REJECTED) {
						it.remove();
					}
				}
			}
		} catch (HibernateException e) {
			throw new InternalServerErrorException("Database error occurred while getting the list of studies.", e);
		}

		return studies;
	}

	/**
	 * 
	 * @param studyId
	 * @return
	 * @throws WebApplicationException
	 */
	@GET
	@Path("Study/studySites")
	@Produces(MediaType.APPLICATION_XML)
	public Response getStudySites(@QueryParam("studyId") String studyId)
			throws WebApplicationException, UserAccessDeniedException {
		Set<StudySite> studySiteSet = Collections.emptySet();

		try {
			// check permissions
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider =
					new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			PermissionType permission =
					restProvider.getAccess(getAuthenticatedAccount().getId(), EntityType.STUDY, Long.valueOf(studyId))
							.getPermission();
			if (PermissionType.compare(permission, PermissionType.READ) < 0) {
				throw new ForbiddenException(
						getAuthenticatedAccount().getUserName() + " does not have access to view study:" + studyId);
			}

			// retrieve list of study sites
			Study study = repositoryManager.getStudySites(Long.valueOf(studyId));

			if (study != null) {
				studySiteSet = study.getStudySiteSet();
			} else {
				logger.error("No study found with ID: " + studyId);
				throw new NotFoundException("No study found with ID: " + studyId);
			}
		} catch (NumberFormatException e) {
			throw new BadRequestException("The given study ID is not a number.", e);
		} catch (UnsupportedEncodingException e) {
			throw new InternalServerErrorException("Error while checking user permission.", e);
		}

		return Response.ok(new StudySiteSet(studySiteSet), MediaType.APPLICATION_XML).build();
	}

	@XmlRootElement(name = "StudySites")
	public static class StudySiteSet {

		@XmlElement(name = "StudySite")
		Set<StudySite> studySiteSet;

		public StudySiteSet() {
			studySiteSet = new HashSet<StudySite>();
		}

		public StudySiteSet(Collection<StudySite> siteCollection) throws NullPointerException {
			studySiteSet = new HashSet<StudySite>(siteCollection);
		}

		public void addAll(Collection<StudySite> m) {
			studySiteSet.clear();

			if (m != null) {
				studySiteSet.addAll(m);
			}
		}

		public void add(StudySite m) {
			studySiteSet.add(m);
		}

		public Set<StudySite> getList() {
			return studySiteSet;
		}
	}

	/**
	 * Given the name of a study, this webservice will archive all the datasets in the study.
	 * 
	 * This webservice has NO security on it because it should not be accessible outside of the local vpn network.
	 * 
	 * This ws will return a 200 if the the study was found and the datasets were archived.
	 * 
	 * @param name
	 * @throws UnsupportedEncodingException
	 * @throws UserPermissionException
	 */
	@GET
	@Path("Study/archiveAll/{studyName}")
	public Response archiveAllDatasetsInStudy(@PathParam("studyName") String name)
			throws UnsupportedEncodingException, UserPermissionException {

		String decodedStudyTitle = URLDecoder.decode(name.trim(), "UTF-8");
		Study study = repositoryManager.getStudyWithDatasetsByName(decodedStudyTitle);
		Account account = accountManager.getAccountByUserName(ADMIN_USER_NAME);

		if (study == null) {
			throw new RuntimeException("The requested study does not exist.");
		}

		for (Dataset d : study.getDatasetSet()) {
			if (DatasetStatus.PRIVATE.equals(d.getDatasetStatus())
					|| DatasetStatus.SHARED.equals(d.getDatasetStatus())) {
				d.setDatasetStatus(DatasetStatus.ARCHIVED);
				d.setDatasetRequestStatus(null);
			}
		}

		repositoryManager.saveStudy(account, study);

		return Response.ok().build();
	}

	/**
	 * Returns a list of studies.
	 * 
	 * If a requesting account is an actual user then their studies are then returned.
	 * 
	 * Open Questions: What if an unauthenticated user accesses this service.
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("Study/getStudies")
	@Produces("text/xml")
	public Set<Study> getStudies() throws UnsupportedEncodingException {

		Account acct;
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		Long accountId = null;
		String proxyTicket = null;
		RestAccountProvider restProvider;

		acct = getAuthenticatedAccount();
		if (acct != null) {
			logger.debug(acct.getUserName() + " called getStudies().");
			accountId = acct.getId();
		}

		Set<Study> userStudies = new HashSet<Study>();
		proxyTicket = PortalUtils.getProxyTicket(accountUrl);
		restProvider = new RestAccountProvider(accountUrl, proxyTicket);
		Set<Long> ids = restProvider.listUserAccess(accountId, EntityType.STUDY, PermissionType.WRITE, false);
		for (Long id : ids) {
			Study currStudy = repositoryManager.getLazyStudy(id);
			if (StudyStatus.PRIVATE.equals(currStudy.getStudyStatus())
					|| StudyStatus.PUBLIC.equals(currStudy.getStudyStatus())) {
				userStudies.add(currStudy);
			}
		}
		// studies.addAll(userStudies);
		return userStudies;
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
	public Set<Dataset> getDatasets(@QueryParam("study") String name) throws UnsupportedEncodingException {

		if (name.isEmpty()) {
			return new HashSet<Dataset>();
		}
		name = URLDecoder.decode(name, "UTF-8");
		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getDatasets().");

		Study study = repositoryManager.getStudyByName(name);

		if (ANONYMOUS_USER_NAME.equals(requestingAccount.getUserName())) {
			throw new RuntimeException("Web Service can only be accessed by logged in users");
		}

		List<Dataset> datasets = repositoryManager.getDatasetsByStudy(study);

		Set<Dataset> updatedDataset = new HashSet<Dataset>();
		for (Dataset dataset : datasets) {
			Dataset clonedDataset = dataset.getClonedDataset(dataset);
			clonedDataset
					.setDatasetFileSet(new HashSet<DatasetFile>(repositoryManager.getDatasetFiles(dataset.getId())));
			updatedDataset.add(clonedDataset);
		}
		return updatedDataset;
	}

	/**
	 * Processes a submission ticket, which results in the returning the of a Dataset
	 * 
	 * @param subProcTckt
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("dataset/processSubmissionTicket/")
	public Response processSubmissionTicket(SubmissionTicket subProcTckt) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called processSubmissionTicket().");

		Dataset dataset = repositoryManager.processSubmissionTicket(subProcTckt, subProcTckt.getLocalPath(),
				subProcTckt.getServerPath(), requestingAccount.getUserName(), subProcTckt.getStudyName(),
				subProcTckt.getDatasetName(), false, false, null, null, false);

		if (dataset == null) {
			return Response.noContent().build();
		}

		return Response.ok().build();
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
	public List<Dataset> getUploadingDataset() throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getUploadingDataset().");
		return repositoryManager.getUploadingDataset(requestingAccount.getUserName());

	}

	/**
	 * Given a Dataset id, A set of DatasetFiles are returned belonging to the dataset
	 * 
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("Dataset/{id}/getDatasetFiles")
	@Produces("text/xml")
	public Set<DatasetFile> getDatasetFiles(@PathParam("id") Long id) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getDatasetFiles().");

		// check permissions
		if (ANONYMOUS_USER_NAME.equals(requestingAccount.getUserName())) {
			throw new RuntimeException("Web Service can only be accessed by logged in users");
		}

		return repositoryManager.getDatasetFileFromDatasetId(id);
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
	public Response setDatasetFileToComplete(@QueryParam("id") Long id)
			throws MalformedURLException, UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called setDatasetFileToComplete().");
		repositoryManager.setDatasetFileToComplete(id, requestingAccount);
		return Response.ok().build();
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
	public BasicStudy getStudyFromDatasetFile(@QueryParam("id") Long id) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getStudyFromDatasetFile().");
		return repositoryManager.getStudyFromDatasetFile(id);
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
	public Response deleteDatasetByName(@PathParam("studyName") String studyName,
			@QueryParam("dataset") String datasetName) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();

		studyName = URLDecoder.decode(studyName, "UTF-8");
		datasetName = URLDecoder.decode(datasetName, "UTF-8");
		logger.debug(requestingAccount.getUserName() + " called deleteDatasetByName().");

		if (repositoryManager.doesDatasetExist(studyName, datasetName)) {

			User user = requestingAccount.getUser();

			repositoryManager.deleteDatasetByName(studyName, datasetName, user);
		}

		return Response.ok().build();
	}

	/**
	 * Given both a Study and Dataset name, the dataset is deleted. Note that cascade constraints are added to the
	 * database the deletion will fail with error.
	 * 
	 * @param studyName
	 * @param datasetName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@DELETE
	@Path("Study/{studyName}/deleteCascadeByName")
	public Response deleteDatasetCascadeByName(@PathParam("studyName") String studyName,
			@QueryParam("dataset") String datasetName) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();

		studyName = URLDecoder.decode(studyName, "UTF-8");
		datasetName = URLDecoder.decode(datasetName, "UTF-8");
		logger.debug(requestingAccount.getUserName() + " called deleteDatasetCascadeByName().");

		if (repositoryManager.doesDatasetExist(studyName, datasetName)) {
			repositoryManager.deleteDatasetCascadeByName(studyName, datasetName);
		}

		return Response.ok().build();
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
	public String getNumOfCompletedFiles(@QueryParam("dataset") String datasetId)
			throws UnsupportedEncodingException, UserPermissionException, BadParameterException {

		Account requestingAccount = getAuthenticatedAccount();
		datasetId = URLDecoder.decode(datasetId, "UTF-8");
		logger.debug(requestingAccount.getUserName() + " called getNumOfCompletedFiles().");

		return repositoryManager.getNumOfCompletedFiles(datasetId, requestingAccount);
	}

	/**
	 * Creates table values in the database from a Form Structure being passed in.
	 * 
	 * @param datastructure
	 * @param userLogin
	 * @return
	 * @throws JAXBException
	 * @throws UnsupportedEncodingException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Path("DataStructure/createTable")
	public Response createTableFromDataStructure(CreateTableFromFormStructurePayload payLoad, @HeaderParam("authorization") String authString)
			throws JAXBException, UnsupportedEncodingException {

		StructuralFormStructure formstructure = payLoad.getFormStructure();
		StatusType statusType = payLoad.getStatusType();
		Long diseaseId = payLoad.getDiseaseId();
		
		if(!isServiceAuthenticated(authString)){
			throw new RuntimeException("Unauthenticatied Service call!!!!");
        }
		
		Account requestingAccount = accountManager.getAccountByUserName(modulesConstants.getAdministratorUsername());
		
		RestDictionaryProvider	restDictonaryProvider = new RestDictionaryProvider (modulesConstants.getModulesDDTURL(), 
				PortalUtils.getProxyTicket(modulesConstants.getModulesDDTURL()));
	
		Thread createTableThread = new Thread(new Runnable() {
			public void run() {
				
			   try {
				   
				   repositoryManager.createTableFromDataStructure(formstructure, requestingAccount);
				   restDictonaryProvider.publishFormStructure(payLoad);		  
				
				} catch (SQLException | UserPermissionException | UnsupportedEncodingException e) {
					cleanRepoTables(formstructure,e.getMessage(),diseaseId,statusType);
					repositoryManager.emailDevPublicationError(formstructure, e.getMessage());
					logger.error("Exception occured when creating repo tables!",e);
								
				}
		   }
		});

		createTableThread.setName("ThreadWithTimeStamp" + System.currentTimeMillis());
		createTableThread.start();
		
		return Response.ok().build();
	}
	
	
	/**
	 * Checks if the web service call is authenticated
	 * @param authString
	 * @return
	 */
	public boolean isServiceAuthenticated(String authString) {
		
		String userName = modulesConstants.getAdministratorUsername();
		String passWord = modulesConstants.getSaltedAdministratorPassword();
		
		if(authString.equals(HashMethods.getServiceAuthentication(userName, passWord))) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Cleans the table for a FormStructure in repository database if the publication failed for any reason
	 * and saves an audit in dictionary database with the error for failure
	 * @param formStructure
	 * @param errorMessage
	 * @param diseaseId
	 * @param statusType
	 */
	public void cleanRepoTables(StructuralFormStructure formStructure, String errorMessage,Long diseaseId, StatusType statusType) {
		try {		
	        //clean table in the repo
			repositoryManager.cleanRepoTables(formStructure);
			
			RestDictionaryProvider	restDictonaryProvider= new RestDictionaryProvider (modulesConstants.getModulesDDTURL(), 
					PortalUtils.getProxyTicket(modulesConstants.getModulesDDTURL()));
			
			PublishedFormStructure publishedFormStructure = new PublishedFormStructure(formStructure.getId(),diseaseId);
			publishedFormStructure.setPublished(false);
			publishedFormStructure.setPublicationDate(new Date());
			publishedFormStructure.setErrorMessage(errorMessage);
			
			restDictonaryProvider.savePublishedFormStructure(publishedFormStructure);
			
			revertFormStructureStatus (formStructure,statusType);
			
		} catch (SQLException e) {			
			logger.error("SQLException occured when cleaning repo tables!",e);
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException occured when cleaning repo tables!",e);
		} 	
	}
	
	/**
	 * Reverts the Formstructure to either draft or awaiting publication when publication fails
	 * @param formStructure
	 * @param statusType
	 */
	public void revertFormStructureStatus (StructuralFormStructure formStructure,StatusType statusType) {
		
		RestDictionaryProvider restDictonaryProvider = new RestDictionaryProvider (modulesConstants.getModulesDDTURL(), 
				PortalUtils.getProxyTicket(modulesConstants.getModulesDDTURL()));
		
		restDictonaryProvider.editFormStructureStatus(formStructure.getId(), statusType.getId());
		
	}
	

	@GET
	@Path("Download/getDownloadPackages")
	@Produces("text/xml")
	public List<DownloadPackage> getDownloadPackage() throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getDownloadPackage().");

		return repositoryManager.getDownloadPackageByUser(requestingAccount.getUser());
	}

	@DELETE
	@Path("download/deleteDownloadables")
	public Response deleteDownloadablesByIds(@QueryParam("id") List<Long> selectedIds) {

		if (selectedIds.isEmpty() || selectedIds.size() == 0) {
			String msg = "Web service has empty argument";
			logger.error(msg);
			throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).entity(msg).build());
		}
		repositoryManager.deleteDownloadableByIds(selectedIds);
		return Response.ok().build();
	}

	@DELETE
	@Path("download/deleteDownloadPackages")
	public Response deleteDownloadPackagesByIds(@QueryParam("id") List<Long> selectedIds) {

		if (selectedIds.isEmpty() || selectedIds.size() == 0) {
			String msg = "Web service has empty argument";
			logger.error(msg);
			throw new WebApplicationException(Response.status(Response.Status.PRECONDITION_FAILED).entity(msg).build());
		}
		repositoryManager.deleteDownloadPackagesByIds(selectedIds);
		return Response.ok().build();
	}

	/**
	 * Given a filetype (Please see the file_type table for options), a list of userFiles that the requester has access
	 * to is returned
	 * 
	 * @param fileType
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("UserFiles/getAvalibleFiles")
	@Produces("text/xml")
	public List<UserFile> getAvailbleUserFiles(@QueryParam("fileType") String fileType)
			throws UnsupportedEncodingException {

		if (fileType.isEmpty()) {
			return new ArrayList<UserFile>();
		}

		fileType = URLDecoder.decode(fileType, "UTF-8");

		List<UserFile> userFile = new ArrayList<UserFile>();
		Account requestingAccount = getAuthenticatedAccount();

		userFile.addAll(repositoryManager.getAvailableUserFiles(requestingAccount.getUser().getId(), fileType));
		return userFile;
	}

	/**
	 * Returns a UserFile given the parameters.
	 * 
	 * Note: There are calls for security for this web service. But the lack of security is due to how the UserFiles are
	 * stored. While the information of the file is stored, the information of the user who uploaded the file is not
	 * stored.
	 * 
	 * @param fileId
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("UserFiles/getFileById")
	@Produces("text/xml")
	public UserFile getFileById(@QueryParam("fileId") Long fileId) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();

		return repositoryManager.getFileById(fileId);
	}

	/**
	 * Uploads a file to be stored in the database and Sftp file server,
	 * 
	 * 
	 * 
	 * The content type "multipart/form-data" should be used for submitting forms that contain files, non-ASCII data,
	 * and binary data.
	 * 
	 * @param uploadFile
	 * @param fileName
	 * @param fileDescription
	 * @param fileType
	 * @return
	 * @POST
	 * @Consumes(MediaType.MULTIPART_FORM_DATA) @Produces("text/plain") @Path("UserFile/uploadFile") public UserFile
	 *                                          uploadFile(File uploadFile, String fileName, String fileDescription,
	 *                                          String fileType) {
	 * 
	 *                                          return null; }
	 */
	@POST
	@Path("DataStore/DataStructure/{dataStructureId}/archive")
	@Produces("text/xml")
	public boolean setDataStoreInfoArchive(@PathParam("dataStructureId") Long dataStructureId,
			@QueryParam("isArchived") boolean isArchived) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();

		try {
			repositoryManager.setArchive(dataStructureId, isArchived);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
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
	@GET
	@Path("dataset/{studyName}/{dataset}")
	@Produces("text/xml")
	public Dataset getDatasetByName(@PathParam("studyName") String studyName, @PathParam("dataset") String datasetName)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		studyName = URLDecoder.decode(URLDecoder.decode(studyName, "UTF-8"), "UTF-8");
		datasetName = URLDecoder.decode(URLDecoder.decode(datasetName, "UTF-8"), "UTF-8");
		logger.debug(requestingAccount.getUserName() + " called deleteDatasetByName().");

		if (ANONYMOUS_USER_NAME.equals(requestingAccount.getUserName())) {
			throw new RuntimeException("Web Service can only be accessed by logged in users");
		}

		return repositoryManager.getDatasetByName(studyName, datasetName);
	}

	@POST
	@Path("/accessRecord/create/{dsId}/{recCount}")
	public Response createAccessRecords(@PathParam("dsId") Long dsId, @PathParam("recCount") Long count,
			@DefaultValue("0") @QueryParam("time") long time, @QueryParam("username") String username)
			throws UnsupportedEncodingException, MalformedURLException {
		Account account = accountManager.getAccountByUserName(username);
		logger.debug("createAccessRecord call by " + account.getUserName() + " dsId=" + dsId + " count=" + count);
		if (ANONYMOUS_USER_NAME.equals(account.getUserName())) {
			throw new RuntimeException("Web Service can only be accessed by logged in users");
		}
		Date queueDate = new Date();
		// when time is not supplied it gets set to -1 by the deault value
		if (time != 0) {
			queueDate = new Date(time);
		}
		BasicDataset d = repositoryManager.getBasicDataset(dsId);
		repositoryManager.createAccessRecord(d, account, DataSource.QUERY_TOOL, count, queueDate);

		return Response.ok().build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/accessRecord/bulk/create")
	public Response createBulkAccessRecords(@DefaultValue("0") @QueryParam("time") long time,
			@QueryParam("username") String username, String jsonPayload) {

		if (jsonPayload == null || jsonPayload.isEmpty()) {
			return Response.noContent().build();
		}

		Map<Long, Long> datasetIdToRecordCountMap =
				new Gson().fromJson(jsonPayload, new TypeToken<Map<Long, Long>>() {}.getType());

		logger.info("Creating access record for " + datasetIdToRecordCountMap.size() + " datasets.");

		Account account = accountManager.getAccountByUserName(username);

		Date queueDate = new Date();
		// when time is not supplied it gets set to -1 by the deault value
		if (time != 0) {
			queueDate = new Date(time);
		}

		Set<Long> datasetIdSet = datasetIdToRecordCountMap.keySet();

		List<BasicDataset> datasets = repositoryManager.getBasicDatasetsByIds(datasetIdSet);

		Map<Long, BasicDataset> datasetIdMap = new HashMap<>();

		for (BasicDataset dataset : datasets) {
			datasetIdMap.put(dataset.getId(), dataset);
		}

		Set<AccessRecord> newAccessRecords = new HashSet<>();
		for (Entry<Long, Long> datasetIdToRecordCount : datasetIdToRecordCountMap.entrySet()) {
			Long datasetId = datasetIdToRecordCount.getKey();
			Long recCount = datasetIdToRecordCount.getValue();
			BasicDataset dataset = datasetIdMap.get(datasetId);

			if (dataset != null) {
				AccessRecord ar = new AccessRecord(dataset, account, queueDate, recCount, DataSource.QUERY_TOOL);
				newAccessRecords.add(ar);
				// repositoryManager.createAccessRecord(dataset, account, DataSource.QUERY_TOOL, recCount, queueDate);
			}
		}

		repositoryManager.batchSaveAccessRecord(newAccessRecords);

		return Response.ok().build();
	}

	@POST
	@Path("study/dataset/dataset_file_path")
	@Produces(MediaType.TEXT_PLAIN)
	public String getDatasetFilePath(@QueryParam("studyPrefixedId") String studyPrefixedId,
			@QueryParam("datasetName") String datasetName, @QueryParam("fileName") String fileName)
			throws UnsupportedEncodingException {
		String datasetFilePath = repositoryManager.getDatasetFilePath(studyPrefixedId, datasetName, fileName);
		return datasetFilePath;
	}

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Path("dataset/update_form_structure_references")
	public Response updateDatasetDataStructureReferences(UpdateFormStructureReferencePayload payload)
			throws UnsupportedEncodingException {
		getAuthenticatedAccount();
		try {
			repositoryManager.updateFormStructureReferences(payload.getOldFormStructureId(),
					payload.getNewFormStructureId(), payload.getOldNewRepeatableGroupIdMap());
		} catch (DictionaryVersioningException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e);
		}

		return Response.ok().build();
	}

	/**
	 * **************For Performance benchmarking*************** Create study from given study in xml format
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Path("Study/createStudy")
	public Study createStudy(Study study) throws UnsupportedEncodingException {

		logger.info("Repository web service bulk study creation invoked for user: "
				+ getAuthenticatedAccount().getUserName());

		Study addedStudy = repositoryManager.createStudyForPB(study, getAuthenticatedAccount());

		return addedStudy;

	}

	@GET
	@Path("Download/downloadDataByStudyId/{studyId}/")
	@Produces("text/xml")
	public Response downloadDataByStudyId(@PathParam("studyId") String studyPrefixedId)
			throws UnsupportedEncodingException, JSchException, SftpException, UserPermissionException, JAXBException {

		Response response = null;

		Account account = getAuthenticatedAccount();
		logger.debug(account.getUserName() + " called downloadDatasets().");
		Study study = repositoryManager.getStudyByPrefixedId(account, studyPrefixedId);

		if (study == null) {
			String msg = "Study with study prefix id " + studyPrefixedId + " is not found";
			response = Response.status(404).entity(msg).build();
			logger.error(msg);
			throw new NotFoundException(response);
		} else {
			String dictionaryUrl = modulesConstants.getModulesDDTURL(ServiceConstants.DEFAULT_PROVIDER);
			String proxyTicket = PortalUtils.getProxyTicket(dictionaryUrl);
			List<Downloadable> downloadableList =
					repositoryManager.downloadDataByStudy(account, study, proxyTicket, DOWNLOADED_DATA_DIRECTORY);

			if (downloadableList != null && downloadableList.size() != 0) {
				DownloadableWrapper wrapper = new DownloadableWrapper(downloadableList);
				String xml = marshallListToXML(wrapper);
				response = Response.status(200).entity(xml).build();
			} else {
				String msg = "Web service get null or empty result.";
				response = Response.status(Response.Status.NOT_FOUND).entity(msg).build();
				throw new NotFoundException(response);
			}
		}
		return response;
	}

	@SuppressWarnings("resource")
	@POST
	@Path("Download/modifyFileByForm/{formName}")
	public Response modifyFileByForm(@PathParam("formName") String formName)
			throws IOException, UserPermissionException {

		Response response = null;
		String result = null;
		Account account = getAuthenticatedAccount();
		String targetPath = DOWNLOADED_DATA_DIRECTORY;
		logger.debug("targetPath: " + targetPath);
		File target = new File(targetPath);

		if (!target.exists()) {
			target.mkdirs();
		}
		if (target.isDirectory()) {
			String[] subFolders = target.list();

			for (String folderName : subFolders) {
				File subTarget = new File(targetPath + File.separator + folderName);

				if (subTarget.isDirectory()) {
					/* Get form structure name from the folder name */
					String dataSetName = folderName.substring(0, folderName.lastIndexOf("_"));
					String studyId = folderName.split("_")[folderName.split("_").length - 1];

					logger.info("subTarget folder: " + subTarget);
					String[] targetFiles = subTarget.list();

					String clonedTargetPath = "";

					for (String fileName : targetFiles) {

						if (fileName.endsWith(".csv")) {
							File dataFile =
									new File(targetPath + File.separator + folderName + File.separator + fileName);

							/* Read the form structure name from the file */
							BufferedReader brReader = new BufferedReader(new FileReader(dataFile));
							String formStructureName = brReader.readLine().split(",")[0];
							logger.info("form structure name in csv file: " + formStructureName);
							brReader.close();

							if (formName.equalsIgnoreCase(formStructureName)) {
								/*
								 * Modify the data csv file for non-cloned form
								 */
								String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
								String updatedDSName = formName + "_" + timestamp;
								File destFile =
										new File(subTarget + File.separator + updatedDSName + "_" + studyId + ".csv");
								logger.debug("destFileName: " + destFile.getName());

								result = repositoryManager.modifyCSVFile(account, dataFile, destFile, formName,
										updatedDSName, studyId);

								/* Get the data csv file for cloned form */
								// get formStructure id from the form name read
								// in the csv file
								String dictionaryUrl =
										modulesConstants.getModulesDDTURL(ServiceConstants.DEFAULT_PROVIDER);
								String proxyTicket = PortalUtils.getProxyTicket(dictionaryUrl);
								RestDictionaryProvider restProvider =
										new RestDictionaryProvider(dictionaryUrl, proxyTicket);
								String formStructureId =
										restProvider.getLatestDataStructureByName(formStructureName) != null ? String
												.valueOf(restProvider.getLatestDataStructureByName(formStructureName)
														.getId()) : null;

								if (formStructureId != null) {
									/*
									 * Get cloned form name from the non-cloned form structure id
									 */
									String clonedFormName = formStructureName + "_clone_" + formStructureId;

									String clonedDSName = clonedFormName + "_"
											+ new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

									/* Create cloned folder for cloned form */
									clonedTargetPath = targetPath + File.separator + clonedDSName + "_" + studyId;
									File clonedTarget = new File(clonedTargetPath);
									if (!clonedTarget.exists()) {
										if (!clonedTarget.mkdir()) {
											logger.error("Fail to create cloned directory.");
										}
									}
									/*
									 * Get the data csv file for cloned form in the cloned directory
									 */
									File clonedDestFile = new File(
											clonedTarget + File.separator + clonedDSName + "_" + studyId + ".csv");
									logger.info("clonedDestFile: " + clonedTarget + File.separator + clonedDSName + "_"
											+ studyId + ".csv" + "\nclonedFormName: " + clonedFormName
											+ "\nclonedDSName: " + clonedDSName);
									result = repositoryManager.modifyCSVFile(account, dataFile, clonedDestFile,
											clonedFormName, clonedDSName, studyId);
								}

								dataFile.delete(); // delete original data file
							}
						}
					}

					/* Copy non-csv file to cloned directory */
					for (String fileName : targetFiles) {
						if (!fileName.endsWith(".csv") && !clonedTargetPath.equals("")) {
							File sourceFile =
									new File(targetPath + File.separator + folderName + File.separator + fileName);
							File destDir = new File(clonedTargetPath);
							if (destDir.exists()) {
								FileUtils.copyFileToDirectory(sourceFile, destDir);
							}
						}
					}
				}
			}
		}

		if (result != null && result.contains("Success")) {
			response = response.status(200).build();
		} else if (result == null) {
			String msg = formName + " is not found in the data files.";
			response = response.status(Response.Status.NOT_FOUND).entity(msg).build();
			throw new NotFoundException(response);

		} else {
			String msg = "Error in modifying files: " + result;
			response = response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
			throw new InternalServerErrorException(response);
		}
		return response;
	}

	/**
	 * To double the amount of studies Created studies from the existing studies in the repo
	 */
	@POST
	@Path("Study/createStudies")
	@Produces("text/xml")
	public Set<Study> createStudies() throws UnsupportedEncodingException {

		Set<Study> addedStudyList = new HashSet<Study>();

		List<Study> existStudyList = repositoryManager.getStudyList();

		for (Study study : existStudyList) {
			Study addedStudy = repositoryManager.createStudyForPB(study, getAuthenticatedAccount());

			addedStudyList.add(addedStudy);
		}

		return addedStudyList;

	}

	private <T extends AlzPed> String formatJson(List<T> list) {


		JSONObject json = new JSONObject();
		JSONArray arr = new JSONArray();

		for (T agent : list) {

			JSONObject obj = new JSONObject();
			obj.put("id", agent.getId());
			obj.put("text", agent.getText());
			arr.put(obj);
		}
		json.put("results", arr);
		return json.toString();

	}

	@GET
	@Path("Study/getTherapeuticAgents")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllTherapeuticAgents() {

		List<TherapeuticAgent> therapeuticAgents = studyDao.getAllTherapeuticAgents();

		return formatJson(therapeuticAgents);
	}

	@GET
	@Path("Study/getTherapeuticTargets")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllTherapeuticTargets() {

		List<TherapeuticTarget> therapeuticTargets = studyDao.getAllTherapeuticTargets();

		return formatJson(therapeuticTargets);
	}

	@GET
	@Path("Study/getTherapyTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllTherapyTypes() {

		List<TherapyType> therapyTypes = studyDao.getAllTherapyTypes();

		return formatJson(therapyTypes);
	}

	@GET
	@Path("Study/getModelTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllModelTypes() {

		List<ModelType> modelTypes = studyDao.getAllModelTypes();

		return formatJson(modelTypes);
	}


	@GET
	@Path("Study/getModelNames")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllModelNames() {

		List<ModelName> modelNames = studyDao.getAllModelNames();

		return formatJson(modelNames);
	}

	@GET
	@Path("Study/getStudyTherapeuticAgents/{studyId}")
	public String getStudyTherapeuticAgents(@PathParam("studyId") String studyId) {
		List<TherapeuticAgent> therapeuticAgents = studyDao.getStudyTherapeuticAgents(studyId);

		return formatJson(therapeuticAgents);

	}

	@GET
	@Path("Study/getStudyTherapeuticTargets/{studyId}")
	public String getStudyTherapeuticTargets(@PathParam("studyId") String studyId) {
		List<TherapeuticTarget> therapeuticTarget = studyDao.getStudyTherapeuticTargets(studyId);

		return formatJson(therapeuticTarget);

	}

	@GET
	@Path("Study/getStudyTherapyTypes/{studyId}")
	public String getStudyTherapyTypes(@PathParam("studyId") String studyId) {
		List<TherapyType> therapeuticAgents = studyDao.getStudyTherapyTypes(studyId);

		return formatJson(therapeuticAgents);

	}

	@GET
	@Path("Study/getStudyModelNames/{studyId}")
	public String getStudyModelNames(@PathParam("studyId") String studyId) {
		List<ModelName> modelNames = studyDao.getStudyModelNames(studyId);

		return formatJson(modelNames);

	}

	@GET
	@Path("Study/getStudyModelTypes/{studyId}")
	public String getStudyModelTypes(@PathParam("studyId") String studyId) {
		List<ModelType> modelTypes = studyDao.getStudyModelTypes(studyId);

		return formatJson(modelTypes);

	}

	@GET
	@Path("Study/getMetaStudyTherapeuticAgents/{studyId}")
	public String getMetaStudyTherapeuticAgents(@PathParam("studyId") String studyId) {
		List<TherapeuticAgent> therapeuticAgents = metaStudyDao.getMetaStudyTherapeuticAgents(studyId);

		return formatJson(therapeuticAgents);

	}

	@GET
	@Path("Study/getMetaStudyTherapeuticTargets/{studyId}")
	public String getMetaStudyTherapeuticTargets(@PathParam("studyId") String studyId) {
		List<TherapeuticTarget> therapeuticTarget = metaStudyDao.getMetaStudyTherapeuticTargets(studyId);

		return formatJson(therapeuticTarget);

	}

	@GET
	@Path("Study/getMetaStudyTherapyTypes/{studyId}")
	public String getMetaStudyTherapyTypes(@PathParam("studyId") String studyId) {
		List<TherapyType> therapeuticAgents = metaStudyDao.getMetaStudyTherapyTypes(studyId);

		return formatJson(therapeuticAgents);

	}

	@GET
	@Path("Study/getMetaStudyModelNames/{studyId}")
	public String getMetaStudyModelNames(@PathParam("studyId") String studyId) {
		List<ModelName> modelNames = metaStudyDao.getMetaStudyModelNames(studyId);

		return formatJson(modelNames);

	}

	@GET
	@Path("Study/getMetaStudyModelTypes/{studyId}")
	public String getMetaStudyModelTypes(@PathParam("studyId") String studyId) {
		List<ModelType> modelTypes = metaStudyDao.getMetaStudyModelTypes(studyId);

		return formatJson(modelTypes);

	}



	@DELETE
	@Path("Study/deleteDupStudies")
	public Response deleteDupStudies() throws UnsupportedEncodingException {

		Account account = getAuthenticatedAccount();

		List<Study> existStudyList = repositoryManager.getStudyList();

		for (Study study : existStudyList) {
			if (study.getPrefixedId().endsWith(ModelConstants.SURFIX_PERFORM_BM)) {
				repositoryManager.deleteStudy(account, study, true);
			}

		}
		return Response.ok().build();
	}

	private static String marshallListToXML(Object wrapperObject) throws JAXBException {

		java.io.StringWriter sw = new StringWriter();

		JAXBContext context = JAXBContext.newInstance(wrapperObject.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshaller.marshal(wrapperObject, sw);

		String xml = sw.toString(); // logger.info("result: "+xml);

		return xml;
	}



	@DELETE
	@Path("hibernate/clearCache")
	public Response clearHibernateCache() {
		logger.info("Clearing hibernate Cache...");
		hibernateManager.clearMetaHibernateCache();
		return Response.ok().build();
	}
}
