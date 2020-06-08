package gov.nih.tbi.dictionary.ws.cxf;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.SchemaMappingManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.dao.CategoryDao;
import gov.nih.tbi.dictionary.dao.DiseaseDao;
import gov.nih.tbi.dictionary.dao.DomainDao;
import gov.nih.tbi.dictionary.dao.FormStructureDao;
import gov.nih.tbi.dictionary.dao.FormStructureSqlDao;
import gov.nih.tbi.dictionary.dao.PopulationDao;
import gov.nih.tbi.dictionary.dao.SubgroupDao;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.CategoryList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.ClassificationList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.DEValueRangeMap;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.DataElementList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.DataStructureList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.DiseaseList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.DomainList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.PopulationList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.SchemaList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.SemanticFormStructureList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.StringList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.StringWrapper;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.SubDomainList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.SubgroupList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.UserFileWrapper;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;
import gov.nih.tbi.dictionary.model.hibernate.DictionarySupportingDocumentation;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.PublishedFormStructure;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.dictionary.model.restful.CreateTableFromFormStructurePayload;
import gov.nih.tbi.dictionary.model.restful.StructuralFormStructureListItem;
import gov.nih.tbi.dictionary.service.DictionaryServiceInterface;
import gov.nih.tbi.portal.PortalUtils;

@Path("/dictionary/")
public class DictionaryRestService extends AbstractRestService {

	/***************************************************************************************************/

	private static Logger logger = Logger.getLogger(DictionaryRestService.class);

	/***************************************************************************************************/

	@Autowired
	private DictionaryToolManager dictionaryToolManager;

	@Autowired
	protected DictionaryServiceInterface dictionaryService;

	@Autowired
	private RepositoryManager repositoryManager;

	@Autowired
	private PopulationDao populationDao;

	@Autowired
	CategoryDao categoryDao;

	@Autowired
	DiseaseDao diseaseDao;

	@Autowired
	SubgroupDao subgroupDao;

	@Autowired
	DomainDao domainDao;

	@Autowired
	FormStructureDao formStructureDao;

	@Autowired
	FormStructureSqlDao formStructureSqlDao;

	@Resource
	private WebServiceContext wsContext;

	@Autowired
	ModulesConstants modulesConstants;
	
	@Autowired
	SchemaMappingManager schemaMappingManager;
	
	@Autowired
	StaticReferenceManager staticManager;
	
	@Autowired
	MessageSource messageSource;
	
	@Autowired
	MailEngine mailEngine;

	/***************************************************************************************************/
	// TODO: MV 6/6/2014 - There are a lot of commented out functions in this file that need to be rewritten to use the
	// new form structure and data element search.
	/*******************************************************
	 * 
	 * 
	 * Data Structures
	 * 
	 * 
	 *******************************************************/

	/**
	 * Although this method uses IDs much like getDataStruturesDetailsById, its works slightly different, it uses the
	 * ids to create list of datastructures which in turn used to build another list using the shortname and version. m
	 * 
	 * @param dataStructureIds
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("FormStructure/details")
	// Undecided
	@Produces("text/xml")
	public DataStructureList getDataStructuresDetails(@QueryParam("dsId") List<Long> dataStructureIds)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getDataStructureDetails().");

		List<FormStructure> dataStructureList = new ArrayList<FormStructure>();
		for (Long dataStructureId : dataStructureIds) {
			dataStructureList.add(dictionaryToolManager.getDataStructure(dataStructureId));
		}

		// if (!HashMethods.getClientHash(user.getUserName()).equals(user.getHash1()))
		// {
		// throw new RuntimeException("Failed to validate Username.");
		// }

		// Account account = accountManager.getAccountByUserName(user.getUserName());

		DataStructureList dsList = new DataStructureList();

		if (dataStructureList != null && !dataStructureList.isEmpty()) {
			for (FormStructure ads : dataStructureList) {
				logger.debug(ads);

				FormStructure dataStructureTemp =
						dictionaryToolManager.getDataStructure(ads.getShortName(), ads.getVersion());

				if (dataStructureTemp != null) {
					logger.debug("\tElements: " + dataStructureTemp.getDataElements().size());

					dsList.add(dataStructureTemp);
				}
			}
		}

		return dsList;
	}


	@GET
	@Path("FormStructure/names")
	// Undecided
	@Produces("text/xml")
	public DataStructureList getFormStructureNames(@QueryParam("dsId") List<Long> formStructureIds)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getFormStructureNames().");

		List<String> nameList = dictionaryToolManager.getFormStructureNames(formStructureIds);

		DataStructureList dsList = new DataStructureList();

		for (String shortName : nameList) {
			FormStructure formStructure = new FormStructure();
			formStructure.setShortName(shortName);
			dsList.add(formStructure);
		}

		return dsList;
	}


	@GET
	@Path("FormStructure/ids")
	@Produces("text/xml")
	public List<FormStructure> getDataStructureDetailsByIds(@QueryParam("dsId") List<Long> dsIdList)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getDataStructureDetailsByIds().");

		List<FormStructure> dsList = dictionaryToolManager.getDataStructureByIds(dsIdList);

		return dsList;
	}

	@GET
	@Path("FormStructure/dataLoader/{dsId}")
	@Produces("text/xml")
	public FormStructure getDataStructureDetailsById(@PathParam("dsId") Long dsIdList)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getDataStructureDetailsByIds().");

		FormStructure ds = dictionaryToolManager.getDataStructure(dsIdList);

		return ds;
	}

	@GET
	@Path("FormStructure/latest/{name}")
	@Produces("text/xml")
	public FormStructure getLatestDataStructureByName(@PathParam("name") String name)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getDataStructureDetailsByIds().");

		FormStructure ds = dictionaryToolManager.getDataStructureLatestVersion(name);

		return ds;
	}

	/**
	 * Purposely obscure web service method which is used by the Dictionary Webstart Rest Webservice. Note: There is no
	 * validation because only the validation tool should be able to use it
	 * 
	 * @param dsNameList
	 * @return
	 */
	public List<StructuralFormStructure> getDataStructureDetails(List<String> dsNameList) {

		if (dsNameList == null || dsNameList.isEmpty()) {
			return new ArrayList<StructuralFormStructure>();
		}

		List<StructuralFormStructure> dataStructureList = new ArrayList<StructuralFormStructure>();

		for (String name : dsNameList) {
			StructuralFormStructure fs = formStructureSqlDao.getLatestVersionByShortName(name);
			if (fs != null) {
				dataStructureList.add(fs);
			}

		}
		return dataStructureList;
	}

	/**
	 * Current used by ProFoRMS
	 * 
	 * @param shortName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("FormStructure/{name}")
	@Produces("text/xml")
	public FormStructure getDataStructureDetailsByShortName(@PathParam("name") String shortName)
			throws UnsupportedEncodingException, UserAccessDeniedException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.info(requestingAccount.getUserName() + " called getDataStructureDetailsByShortName().");
		String variables[] = {"name"};
		String parameters[] = {shortName};
		logger.info("getDataStructureDetailsByShortName: " + buildlLogger(variables, parameters));

		FormStructure dataStructureTemp = dictionaryToolManager.getDataStructureLatestVersion(shortName);

		// Check users permission to the given FS
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		Long accountId = null;
		String proxyTicket = null;
		if (requestingAccount != null) {
			accountId = requestingAccount.getId();
			proxyTicket = PortalUtils.getProxyTicket(accountUrl);
		}
		RestAccountProvider restProvider = new RestAccountProvider(accountUrl, proxyTicket);

		EntityMap em = restProvider.getAccess(accountId, EntityType.DATA_STRUCTURE, dataStructureTemp.getId());
		if (em == null) {
			logger.info("EM is null");
		} else {
			logger.info(em.toString());
		}
		PermissionType permissionType = em.getPermission();
		if (permissionType == null || PermissionType.compare(permissionType, PermissionType.READ) == null) {
			// User does not have read access to the given data structure
			throw new RuntimeException("User does not have access to FS with id " + dataStructureTemp.getId());
		}
		// Sort the permissible values of every data element. Requested by proforms. This will
		// impact performance
		for (DataElement de : dataStructureTemp.getDataElements().values()) {
			de.setValueRangeList(new LinkedHashSet<ValueRange>(dictionaryToolManager.orderValueRange(de)));
		}

		return dataStructureTemp;
	}

	/*
	 * The web service call will retrieve the first version of a FS for upload submission
	 */
	@GET
	@Path("FormStructure/First/{shortName}")
	@Produces("text/xml")
	public FormStructure getFormStructureFirstVersion(@PathParam("shortName") String shortName)
			throws UnsupportedEncodingException {

		// Account requestingAccount = getAuthenticatedAccount();
		// logger.debug(requestingAccount.getUserName() + " called getDataStructureDetailsByShortName().");
		String variables[] = {"name"};
		String parameters[] = {shortName};
		logger.debug("getDataStructureDetailsByShortName: " + buildlLogger(variables, parameters));

		// always look for version 1.0. This is the first version in the system.
		FormStructure dataStructureTemp = dictionaryToolManager.getDataStructure(shortName, "1.0");

		// Check users permission to the given FS
		/*
		 * String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId()); Long accountId = null; String
		 * proxyTicket = null; if (requestingAccount != null) { accountId = requestingAccount.getId(); proxyTicket =
		 * PortalUtils.getProxyTicket(accountUrl); } RestAccountProvider restProvider = new
		 * RestAccountProvider(accountUrl, proxyTicket);
		 * 
		 * PermissionType permissionType = restProvider.getAccess(accountId, EntityType.DATA_STRUCTURE,
		 * dataStructureTemp.getId()).getPermission(); if (permissionType == null ||
		 * PermissionType.compare(permissionType, PermissionType.READ) < 0) { // User does not have read access to the
		 * given data structure throw new RuntimeException("User does not have access to FS with id " +
		 * dataStructureTemp.getId()); }
		 */

		return dataStructureTemp;
	}

	/*
	 * MV: Removed for usage testing 2015-02-05. If it is not currently used (I think its not) then it should be
	 * refactored and become our official authenticated fetch. it should work with des and not des
	 * 
	 * @SuppressWarnings("unchecked")
	 * 
	 * @GET
	 * 
	 * @Path("FormStructure/list")
	 * 
	 * @Produces("text/xml") /** This webservice is used by the public site, therefore cannot depend on a proxy ticket
	 * 
	 * @param page
	 * 
	 * @param pageSize
	 * 
	 * @param ascending
	 * 
	 * @param sort
	 * 
	 * @return
	 * 
	 * @throws UnsupportedEncodingException
	 * 
	 * public DataStructureList listDataStructures(@QueryParam("page") Integer page,
	 * 
	 * @DefaultValue("null") @QueryParam("pageSize") Integer pageSize,
	 * 
	 * @DefaultValue("true") @QueryParam("ascending") Boolean ascending,
	 * 
	 * @DefaultValue("null") @QueryParam("sort") String sort) throws UnsupportedEncodingException {
	 * 
	 * String[] variables = { "page", "pageSize", "ascending", "sort" }; String[] parameters = { page.toString(),
	 * pageSize.toString(), ascending.toString(), sort };
	 * 
	 * Account requestingAccount = getAuthenticatedAccount(); logger.info(requestingAccount.getUserName() +
	 * " called listDataStructure()"); logger.info("listPublishedDataStructure: " + buildlLogger(variables,
	 * parameters));
	 * 
	 * DataStructureList bdsl = new DataStructureList(); PaginationData pageData = new PaginationData(page, pageSize,
	 * ascending, sort);
	 * 
	 * // get proxy ticket for web service call found in composite search String accountUrl =
	 * modulesConstants.getModulesAccountURL(getDiseaseId()); String proxyTicket = null; if (requestingAccount != null)
	 * { proxyTicket = PortalUtils.getProxyTicket(accountUrl); }
	 * 
	 * Map<FormStructureFacet, Set<String>> selectedFacets = new HashMap<FormStructureFacet, Set<String>>(); Set<String>
	 * searchTerms = new HashSet<String>();
	 * 
	 * List<FormStructure> list = dictionaryService.compositeFormStructureSearch(requestingAccount, selectedFacets,
	 * searchTerms, false, false, pageData, false, proxyTicket); bdsl.addAll(list); return bdsl;
	 * 
	 * }
	 */
	@GET
	@Path("/DataElement/LinkedFormStructures/{deName}/{deVersion}")
	@Produces("text/xml")
	/**
	 * This webservice is used by the public site, therefore cannot depend on a proxy ticket
	 * @param page
	 * @param pageSize
	 * @param ascending
	 * @param sort
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public DataStructureList getAttachedDataStructure(@PathParam("deName") String deName,
			@PathParam("deVersion") String deVersion) throws UnsupportedEncodingException {

		DataStructureList bdsl = new DataStructureList();

		List<FormStructure> list = dictionaryService.getAttachedDataStructure(deName, deVersion, true);
		bdsl.addAll(list);
		return bdsl;

	}

	/*******************************************************
	 * 
	 * 
	 * Elements
	 * 
	 * @throws UnsupportedEncodingException
	 * 
	 * @throws MalformedURLException
	 * 
	 * 
	 *******************************************************/
	@GET
	@Path("MapElement/{mapId}")
	@Produces("text/xml")
	public MapElement getMapElementById(@PathParam("mapId") Long mapElementId) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getMapElementbyId(). mapElementId: " + mapElementId);

		// Account account = accountManager.getAccountByUserName(user.getUserName());

		MapElement me = (MapElement) dictionaryToolManager.getMapElement(mapElementId);
		return me;

	}

	/**
	 * Retrieves a data element object that matches the given short name. The account requesting the data element must
	 * be registered with the system. In other words, any unauthenticated access will result in a 403 HTTP error
	 * response.
	 * 
	 * @param name - The short name of the requested data element.
	 * @return The data element that matches the requested short name, or nothing if it was not found.
	 * @throws WebApplicationException When there is an error validating the permissions of the requesting account.
	 */
	@GET
	@Path("DataElement/name/{name}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getDataElementByName(@PathParam("name") String name) throws WebApplicationException, UserAccessDeniedException {
		DataElement dataElement = null;

		try {
			Account requestingAccount = getAuthenticatedAccount();

			// Verify that the requesting account object is present and not anonymous.
			if (requestingAccount == null
					|| requestingAccount.getUserName().equals(CoreConstants.UNAUTHENTICATED_USER)) {
				logger.warn("User account is not valid.");
				throw new ForbiddenException("Only users registered with the system are allowed.");
			}

			logger.debug(requestingAccount.getUserName() + " called getDataElementByName( " + name + " ).");
			dataElement = dictionaryToolManager.getLatestDataElementByName(name);

			// Check if the account's permissions to the data element will need to be validated.
			if (dataElement != null && dataElement.getStatus() != DataElementStatus.PUBLISHED) {
				String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
				String proxyTicket = PortalUtils.getProxyTicket(accountUrl);
				RestAccountProvider restProvider = new RestAccountProvider(accountUrl, proxyTicket);
				PermissionType permissionType =
						restProvider.getAccess(requestingAccount.getId(), EntityType.DATA_ELEMENT, dataElement.getId())
								.getPermission();

				// Validate the returned permission.
				if (permissionType == null || PermissionType.compare(permissionType, PermissionType.READ) < 0) {
					throw new BadRequestException("User does not have access to DE with id " + dataElement.getId());
				}
			}
		}
		catch (UnsupportedEncodingException e) {
			logger.error("Couldn't lookup data element named " + name + " because " + e.getMessage());
			throw new InternalServerErrorException(e);
		}

		return Response.ok(dataElement).build();
	}

	@GET
	@Path("DataElement/temp/{type}")
	@Produces("text/xml")
	public StringList getDataElementNamesByType(@PathParam("type") Long typeId) {
		DataType dataType = DataType.getById(typeId);
		if (dataType == null) {
			return null;
		}

		List<String> items = dictionaryToolManager.getDataElementNamesByType(dataType);

		StringList returnValue = new StringList();
		returnValue.addAll(items);
		return returnValue;

	}

	@GET
	@Path("DataElement/{dataId}")
	@Produces("text/xml")
	public DataElement getDataElementById(@PathParam("dataId") Long dataElementId) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getDataElementById(). dataId: " + dataElementId);

		// Account account = accountManager.getAccountByUserName(user.getUserName());

		DataElement me = (DataElement) dictionaryToolManager.getDataElement(dataElementId);
		return me;
	}


	@SuppressWarnings("unchecked")
	@GET
	@Path("FormStructure/Published/list")
	@Produces("text/xml")
	/**
	 * This webservice is used by the public site, therefore cannot depend on a proxy ticket. Also used by MIPAV.  This web-service includes the full data element lists for each form structure returned.
	 * @param page
	 * @param pageSize
	 * @param ascending
	 * @param sort
	 * @param type
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public DataStructureList listPublishedDataStructure(@DefaultValue("1") @QueryParam("page") Integer page,
			@QueryParam("pageSize") Integer pageSize, @DefaultValue("true") @QueryParam("ascending") Boolean ascending,
			@QueryParam("sort") String sort, @QueryParam("type") String type,
			@DefaultValue("true") @QueryParam("incDEs") boolean incDEs) throws UnsupportedEncodingException,
			MalformedURLException {

		String[] variables = {"page", "pageSize", "ascending", "sort"};
		String[] parameters =
				{(page != null ? page.toString() : ServiceConstants.EMPTY_STRING),
						(pageSize != null ? pageSize.toString() : ServiceConstants.EMPTY_STRING),
						(ascending != null ? ascending.toString() : ServiceConstants.EMPTY_STRING),
						(sort != null ? sort : ServiceConstants.EMPTY_STRING)};

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called listPublishedDataStructure()");
		logger.debug("DiseaseKey: " + requestingAccount.getDiseaseKey());
		logger.debug("listPublishedDataStructure: " + buildlLogger(variables, parameters));

		DataStructureList bdsl = new DataStructureList();
		PaginationData pageData = new PaginationData(page, pageSize, ascending, sort);

		// The part of the code that makes only published results come back
		Map<FormStructureFacet, Set<String>> selectedFacets = new HashMap<FormStructureFacet, Set<String>>();
		Set<String> statusSet = new HashSet<String>();
		statusSet.add("Published");
		selectedFacets.put(FormStructureFacet.STATUS, statusSet);

		// If a type was included then we want to filter by it.
		if (type != null) {

			Set<String> typeSet = new HashSet<String>();
			typeSet.add(type);
			selectedFacets.put(FormStructureFacet.SUBMISSION_TYPE, typeSet);

		}

		Set<String> searchTerms = new HashSet<String>();

		bdsl.addAll(dictionaryService.compositeFormStructureSearch(requestingAccount, selectedFacets, searchTerms,
				false, false, pageData, incDEs, null));

		return bdsl;

	}
	
	@GET
	@Path("FormStructure/Published/listAll")
	@Produces("text/xml")
	/**
	 * This webservice is used by the public site, therefore cannot depend on a proxy ticket. Also used by MIPAV.  This web-service includes the full data element lists for each form structure returned.
	 * @param page
	 * @param pageSize
	 * @param ascending
	 * @param sort
	 * @param type
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public SemanticFormStructureList listAllPublishedDataStructure(@DefaultValue("1") @QueryParam("page") Integer page,
			@QueryParam("pageSize") Integer pageSize, @DefaultValue("true") @QueryParam("ascending") Boolean ascending,
			@QueryParam("sort") String sort, @QueryParam("type") String type,
			@DefaultValue("true") @QueryParam("incDEs") boolean incDEs) throws UnsupportedEncodingException,
			MalformedURLException {

		String[] variables = {"page", "pageSize", "ascending", "sort"};
		String[] parameters =
				{(page != null ? page.toString() : ServiceConstants.EMPTY_STRING),
						(pageSize != null ? pageSize.toString() : ServiceConstants.EMPTY_STRING),
						(ascending != null ? ascending.toString() : ServiceConstants.EMPTY_STRING),
						(sort != null ? sort : ServiceConstants.EMPTY_STRING)};

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called listAllPublishedDataStructure()");
		logger.debug("DiseaseKey: " + requestingAccount.getDiseaseKey());
		logger.debug("listPublishedDataStructure: " + buildlLogger(variables, parameters));

		SemanticFormStructureList bdsl = new SemanticFormStructureList();
		PaginationData pageData = new PaginationData(page, pageSize, ascending, sort);

		// The part of the code that makes only published results come back
		Map<FormStructureFacet, Set<String>> selectedFacets = new HashMap<FormStructureFacet, Set<String>>();
		Set<String> statusSet = new HashSet<String>();
		statusSet.add("Published");
		selectedFacets.put(FormStructureFacet.STATUS, statusSet);

		// If a type was included then we want to filter by it.
		if (type != null) {

			Set<String> typeSet = new HashSet<String>();
			typeSet.add(type);
			selectedFacets.put(FormStructureFacet.SUBMISSION_TYPE, typeSet);

		}

		Set<String> searchTerms = new HashSet<String>();
		
		bdsl.addAll(dictionaryService.semanticFormStructureSearch(requestingAccount, selectedFacets, searchTerms, false, false, pageData, null));
		return bdsl;
	}
	
	@GET
	@Path("FormStructure/public/details")
	@Produces("text/xml")
	public Response getSemanticFormStuctureByIdList(@QueryParam("formStructureList")List<Long> formStructureList){
		//guest access will allow for published form structures only
		if(formStructureList == null || formStructureList.isEmpty()){
			String msg = "Argument formstructureList cannot be null or empty.";
			logger.error(msg);
			Response errRes = Response.status(Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
		List<String> formStructureNames = dictionaryToolManager.getFormStructureNames(formStructureList);
		SemanticFormStructureList listWrapper = new SemanticFormStructureList();
		
		List<SemanticFormStructure> fsList = dictionaryToolManager.getLatestFormStructuresByIdAndStatus(formStructureNames);
		Comparator <SemanticFormStructure> formComparator = new Comparator <SemanticFormStructure>(){

			@Override
			public int compare(SemanticFormStructure o1, SemanticFormStructure o2) {
				// TODO Auto-generated method stub
				return o1.getTitle().compareTo(o2.getTitle());
			} 
 			
 		};
 		
 		Collections.sort(fsList,formComparator);
		listWrapper.addAll(fsList);
		return Response.status(Status.OK).entity(listWrapper).build();
	}
	

	@GET
	@Path("DataElement/count")
	@Produces("text/xml")
	/**
	 * This webservice is being used by the public site, Throw away call
	 */
	public Integer searchDataElementsCount(@QueryParam("disease") String diseaseSelection,
			@QueryParam("general") Boolean generalSearch, @QueryParam("domain") String domainSelection,
			@QueryParam("subDomain") String subDomainSelection, @QueryParam("population") String populationSelection,
			@QueryParam("subGroup") String subgroupSelection,
			@QueryParam("classification") String classificationSelection, @QueryParam("filterId") Long filterId,
			@QueryParam("category") Category category, @QueryParam("searchKey") String searchKey,
			@QueryParam("page") Integer page, @QueryParam("pageSize") Integer pageSize,
			@QueryParam("ascending") Boolean ascending, @QueryParam("sort") String sort,
			@QueryParam("searchLocations") String searchLocations) throws UnsupportedEncodingException {

		diseaseSelection = decodeUrl(diseaseSelection);
		domainSelection = decodeUrl(domainSelection);
		subDomainSelection = decodeUrl(subDomainSelection);
		populationSelection = decodeUrl(populationSelection);
		subgroupSelection = decodeUrl(subgroupSelection);
		classificationSelection = decodeUrl(classificationSelection);
		searchKey = decodeUrl(searchKey);
		sort = decodeUrl(sort);
		searchLocations = decodeUrl(searchLocations);

		Account requestingAccount = getAuthenticatedAccount();

		// List of variables with their respective values
		String[] variables =
				{"disease", "general", "domain", "subDomain", "population", "subGroup", "classification", "filterId",
						"category", "searchKey", "page", "pageSize", "ascending", "sort", "searchLocations"};
		String[] parameters =
				{diseaseSelection, (generalSearch != null) ? generalSearch.toString() : null, domainSelection,
						subDomainSelection, populationSelection, subgroupSelection, classificationSelection,
						(filterId != null) ? filterId.toString() : null,
						(category != null) ? category.toString() : null, searchKey,
						(page != null) ? page.toString() : null, (pageSize != null) ? pageSize.toString() : null,
						(ascending != null) ? ascending.toString() : null, sort, searchLocations};

		logger.debug(requestingAccount.getUserName() + " called searchDataElementsCount()");
		logger.debug("searchDataElementsCount: " + buildlLogger(variables, parameters));

		PaginationData pageData =
				new PaginationData(page, pageSize, ascending, sort.replaceAll(" ", ""),
						dictionaryToolManager.locations(searchLocations));

		// Escape the searchKey
		// searchKey = dictionaryToolManager.escapeForILike(searchKey);

		try {
			dictionaryToolManager.searchElements(null, diseaseSelection, generalSearch, domainSelection,
					subDomainSelection, populationSelection, subgroupSelection, classificationSelection, filterId,
					category, searchKey, pageData, null);
		} catch (MalformedURLException e) {
			// because the account argument in the function above is null, the accountProvider will not be created so
			// this exception should never occur
			e.printStackTrace();
		}

		return pageData.getNumSearchResults();
	}


	@GET
	@Path("DataElement/list")
	@Produces("text/xml")
	// auto-generated nightmare
	public DataElementList searchDataElements(@QueryParam("disease") String diseaseSelection,
			@QueryParam("general") Boolean generalSearch, @QueryParam("domain") String domainSelection,
			@QueryParam("subDomain") String subDomainSelection, @QueryParam("population") String populationSelection,
			@QueryParam("subGroup") String subgroupSelection,
			@QueryParam("classification") String classificationSelection, @QueryParam("filterId") Long filterId,
			@QueryParam("category") Category category, @QueryParam("searchKey") String searchKey,
			@QueryParam("page") Integer page, @QueryParam("pageSize") Integer pageSize,
			@QueryParam("ascending") Boolean ascending, @QueryParam("sort") String sort,
			@QueryParam("searchLocations") String searchLocations) throws UnsupportedEncodingException {

		diseaseSelection = decodeUrl(diseaseSelection);
		domainSelection = decodeUrl(domainSelection);
		subDomainSelection = decodeUrl(subDomainSelection);
		populationSelection = decodeUrl(populationSelection);
		subgroupSelection = decodeUrl(subgroupSelection);
		classificationSelection = decodeUrl(classificationSelection);
		searchKey = decodeUrl(searchKey);
		sort = decodeUrl(sort);
		searchLocations = decodeUrl(searchLocations);

		Account requestingAccount = getAuthenticatedAccount();

		// List of variables with their respective values
		String[] variables =
				{"disease", "general", "domain", "subDomain", "population", "subGroup", "classification", "filterId",
						"category", "searchKey", "page", "pageSize", "ascending", "sort", "searchLocations"};
		String[] parameters =
				{diseaseSelection, (generalSearch != null) ? generalSearch.toString() : null, domainSelection,
						subDomainSelection, populationSelection, subgroupSelection, classificationSelection,
						(filterId != null) ? filterId.toString() : null,
						(category != null) ? category.toString() : null, searchKey,
						(page != null) ? page.toString() : null, (pageSize != null) ? pageSize.toString() : null,
						(ascending != null) ? ascending.toString() : null, sort, searchLocations};

		logger.debug(requestingAccount.getUserName() + " called searchDataElements(...)");
		logger.debug("searchDataElements: " + buildlLogger(variables, parameters));

		DataElementList bdel = new DataElementList();
		String sortString = null;
		if (sort != null) {
			sortString = sort.replaceAll(" ", "");
		}
		PaginationData pageData =
				new PaginationData(page, pageSize, ascending, sortString,
						dictionaryToolManager.locations(searchLocations));

		// Escape the searchKey
		// searchKey = dictionaryToolManager.escapeForILike(searchKey);

		try {

			List<DataElement> list =
					dictionaryToolManager.searchElements(null, diseaseSelection, generalSearch, domainSelection,
							subDomainSelection, populationSelection, subgroupSelection, classificationSelection,
							filterId, category, searchKey, pageData, null);
			bdel.addAll(list);
			return bdel;
		} catch (MalformedURLException e) {
			// because the account argument in the function above is null, the accountProvider will not be created so
			// this exception should never occur
			e.printStackTrace();
		}

		return null;
	}

	@GET
	@Path("Domain/")
	@Produces("text/xml")
	public DomainList getDomainList() throws UnsupportedEncodingException {

		logger.debug("ws call to getDomainList()");
		DomainList dl = new DomainList();
		dl.addAll(domainDao.getAll());
		return dl;
	}

	@GET
	@Path("Disease/")
	@Produces("text/xml")
	public DiseaseList getDiseaseList() throws UnsupportedEncodingException {

		logger.debug("ws call to getDiseaseList()");
		DiseaseList dl = new DiseaseList();
		dl.addAll(diseaseDao.getAll());
		return dl;
	}

	@GET
	@Path("Subgroups/")
	@Produces("text/xml")
	public SubgroupList getSubgroupList() throws UnsupportedEncodingException {

		logger.debug("ws call to getSubgroupList()");
		SubgroupList sg = new SubgroupList();
		sg.addAll(subgroupDao.getAll());
		return sg;
	}

	@GET
	@Path("Population/")
	@Produces("text/xml")
	public PopulationList getPopulationList() throws UnsupportedEncodingException {

		logger.debug("ws call to getPopulationList()");
		PopulationList pL = new PopulationList();
		pL.addAll(populationDao.getAll());
		return pL;
	}

	@GET
	@Path("Category/")
	@Produces("text/xml")
	public CategoryList getCategoryList() throws UnsupportedEncodingException {

		logger.debug("ws call to getCategoryList()");
		CategoryList cL = new CategoryList();
		cL.addAll(categoryDao.getAll());
		return cL;
	}

	@GET
	@Path("Disease/{id}")
	@Produces("text/xml")
	public String getDiseasePrefix(@PathParam("id") Long diseaseId) throws UnsupportedEncodingException {

		logger.debug("ws call to getDiseasePrefix()");
		return dictionaryToolManager.getDiseasePrefix(diseaseId);
	}

	@GET
	@Path("Classification/{disease}")
	@Produces("text/xml")
	@Consumes("application/xml")
	public ClassificationList getClassificationList(@PathParam("disease") Long diseaseId,
			@QueryParam("isAdmin") Boolean isAdmin) throws MalformedURLException, UnsupportedEncodingException {

		logger.debug("ws call to getClassificationList()");
		String variables[] = {"disease", "isAdmin"};
		String parameters[] = {diseaseId.toString(), isAdmin.toString()};
		logger.debug("getClassificationList: " + buildlLogger(variables, parameters));

		ClassificationList cl = new ClassificationList();
		Disease disease = diseaseDao.get(diseaseId);
		cl.addAll(dictionaryToolManager.getClassificationList(disease, isAdmin));
		return cl;
	}

	@GET
	@Path("Subgroups/{disease}")
	@Produces("text/xml")
	@Consumes("application/xml")
	public SubgroupList getSubgroupsByDisease(@PathParam("disease") Long diseaseId) throws MalformedURLException,
			UnsupportedEncodingException {

		logger.debug("ws call to getSubgroupsByDisease(). diseaseId = " + diseaseId);
		SubgroupList sg = new SubgroupList();
		Disease disease = diseaseDao.get(diseaseId);
		sg.addAll(dictionaryToolManager.getSubgroupsByDisease(disease));
		return sg;
	}

	@GET
	@Path("Domain/{disease}")
	@Produces("text/xml")
	@Consumes("application/xml")
	public DomainList getDomainsByDisease(@PathParam("disease") Long diseaseId) throws MalformedURLException,
			UnsupportedEncodingException {

		logger.debug("ws call to getDomainsByDisease(). diseaseId: " + diseaseId);
		DomainList dl = new DomainList();
		Disease disease = diseaseDao.get(diseaseId);
		dl.addAll(dictionaryToolManager.getDomainsByDisease(disease));
		return dl;
	}

	/**
	 * Query Parameters Domain={Domain}&Diesease={Disease}
	 * 
	 * @param domain
	 * @param disease
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("SubDomain/list")
	@Produces("text/xml")
	@Consumes("application/xml")
	public SubDomainList getSubDomainsList(@QueryParam("domainId") Long domainId,
			@QueryParam("diseaseId") Long diseaseId) throws MalformedURLException, UnsupportedEncodingException {

		SubDomainList sdl = new SubDomainList();

		Disease disease = diseaseDao.get(diseaseId);
		Domain domain = domainDao.get(domainId);
		sdl.addAll(dictionaryToolManager.getSubDomainList(domain, disease));
		return sdl;
	}

	/**
	 * Given a documentID, a name should be returned
	 * 
	 * TODO Once the Repository Rest Web Service is created, this method should be put in there.
	 * 
	 * @param fileId
	 * @return
	 */

	@GET
	@Path("Repository/Documents/{id}")
	@Produces("text/xml")
	@Consumes("application/xml")
	public UserFileWrapper getDocumentFileName(@PathParam("id") Long id) {

		UserFileWrapper ufw = new UserFileWrapper();
		ufw.setUserFile(repositoryManager.getFileById(id));
		return ufw;
	}

	@GET
	@Path("Repository/Documents/getName/{id}")
	@Produces("text/xml")
	@Consumes("application/xml")
	public StringWrapper getDocument(@PathParam("id") Long id) throws MalformedURLException,
			UnsupportedEncodingException {

		StringWrapper sw = new StringWrapper();
		sw.setStr(repositoryManager.getFileById(id).getName().toString());
		return sw;
	}

	/**
	 * This is used to build logging statements in an efficient manner to not wasted memory.
	 * 
	 * @param variables
	 * @param parameters
	 * @param ticketProperty
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String buildlLogger(String[] variables, String[] parameters) throws UnsupportedEncodingException {

		StringBuilder builder = new StringBuilder();

		if (variables.length != parameters.length) {
			throw new RuntimeException("Number of variables and parameters must be the same!");
		}

		// counts the number of parameters
		for (int i = 0; i < variables.length; i++) {
			if (parameters[i] == null || ServiceConstants.EMPTY_STRING.equals(parameters[i])) {
				continue;
			}

			// variable=parameter
			if (i < variables.length - 1) {
				builder.append(variables[i]).append("=").append(parameters[i])
						.append(System.getProperty("line.separator")).append("\t");
			} else {
				builder.append(variables[i]).append("=").append(parameters[i]);
			}
		}

		return builder.toString();
	}



	/**
	 * For the given data element name list, this web service method queries dictionary database to generate the
	 * marshalled map of data element name to map of permissible values
	 * 
	 * @param deNames - List of data element names passed in as url query parameters
	 */
	@POST
	@Path("/getDEValueRangeMap")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_XML)
	public DEValueRangeMap getDEValueRangeMap(@FormParam("deNames") List<String> deNames) {

		Set<String> deNameSet = new HashSet<String>();
		deNameSet.addAll(deNames);

		DEValueRangeMap map = new DEValueRangeMap();
		map.putAll(dictionaryToolManager.getDEValueRangeMap(deNameSet));
		return map;
	}

	/**
	 * Returns the marshalled list of Schemas defined in the system.
	 */
	@GET
	@Path("/schemas")
	@Produces("text/xml")
	public SchemaList getAllSchemas() {
		
		SchemaList schemaList = new SchemaList();
		schemaList.addAll(dictionaryToolManager.getAllSchemas());
		return schemaList;
	}

	@POST
	@Path("DataElement/create")
	@Consumes(MediaType.APPLICATION_XML)
	public Response createDataElement(DataElement element) {

		try {
			Account account = getAuthenticatedAccount();

			List<String> errors = new ArrayList<String>();
			List<String> warnings = new ArrayList<String>();

			String[] proxyTix = { null, null };

			dictionaryToolManager.saveDataElement(account, element, errors, warnings, SeverityLevel.MINOR, proxyTix, DataElementStatus.DRAFT, false);

			return Response.noContent().build();

		} catch(Exception uee) {
			logger.debug("Some exception...");
		}

		return Response.noContent().build();
	}

	@POST
	@Path("FormStructure/create")
	@Consumes(MediaType.APPLICATION_XML)
	public Response createFormStructure(FormStructure structure) {

		try {
			Account account = getAuthenticatedAccount();

			PermissionType permType = PermissionType.OWNER;

			List<String> errors = new ArrayList<String>();
			List<String> warnings = new ArrayList<String>();

			//String[] proxyTix = { null, null };

			dictionaryToolManager.saveDataStructure(account, permType, structure, errors, warnings, SeverityLevel.MINOR, null);

			return Response.noContent().build();

		} catch(Exception uee) {
			logger.debug("Some exception...");
		}

		return Response.noContent().build();
	}
	
	/**
	 * Returns all of the structural form structures
	 */
	@GET
	@Path("/allStructuralFormStructures")
	@Produces("text/xml")
	public List<StructuralFormStructureListItem> getAllStructuralFormStructures() {
		
		List<StructuralFormStructure> nativeList = this.dictionaryToolManager.getAllSqlFormStructures();

		List<StructuralFormStructureListItem> resultList = new ArrayList<StructuralFormStructureListItem>();

		for(StructuralFormStructure nativeItem : nativeList) {
			StructuralFormStructureListItem sfsli = new StructuralFormStructureListItem( nativeItem.getId(), 
					nativeItem.getShortName(), nativeItem.getVersion(),	nativeItem.getStatus(), nativeItem.getOrganization());
			resultList.add(sfsli);
		}
		
		return resultList;
	}
	
	@GET
	@Path("FormStructure/byStatus")
	@Produces("text/xml")
	public List<SemanticFormStructure> getPublishedAndAwaitingFormStructures(@QueryParam("status") String status) throws MalformedURLException {
		List<SemanticFormStructure> output = new ArrayList<SemanticFormStructure>();
		try {
			Account account = getAuthenticatedAccount();
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			String proxyTicket = null;
			if (account != null) {
				PaginationData pageData = new PaginationData();
				Map<FormStructureFacet, Set<String>> selectedFacets = new HashMap<FormStructureFacet, Set<String>>();
				
				// build the facets
				//selectedFacets.put(FormStructureFacet.STANDARDIZATION, new HashSet<String>());
				Set<String> statusSet = new HashSet<String>();
				// status is split by comma in the queryParam, so split them up
				String[] statuses = status.split(",");
				for (String statusEntry : statuses) {
					statusSet.add(statusEntry);
				}
				selectedFacets.put(FormStructureFacet.STATUS, statusSet);		
				Set<String> diseaseSet = new HashSet<String>();
				
				List<Disease> diseaseList = staticManager.getDiseaseList();
				
				for(Disease disease:diseaseList ){
					diseaseSet.add(disease.getName());
				}

				selectedFacets.put(FormStructureFacet.DISEASE, diseaseSet);

				proxyTicket = PortalUtils.getProxyTicket(accountUrl);
				output = dictionaryService.semanticFormStructureSearch(account, selectedFacets, new HashSet<String>(),
								false, false, pageData, proxyTicket);
			}
			
		} catch(UnsupportedEncodingException e) {
			logger.debug("Some exception...");
		}
		return output;
	}
	
	@GET
	@Path("schemas/byFormNames")
	@Produces("text/xml")
	public SchemaList getSchemasFromFormStructures(@QueryParam("formName") List<String> formNames) {
		List<Schema> schemas = schemaMappingManager.getSchemasByFormStructureNames(formNames);
		SchemaList schemaList = new SchemaList();
		schemaList.addAll(schemas);
		return schemaList;
	}
	
	/**
	 * Method to keep dictionary session alive, this will be called by QT
	 * 
	 * @return Response with certain status but no body
	 */
	@GET
	@Path("keepDictAlive")
	@Produces(MediaType.TEXT_PLAIN)
	public Response keepDictionarySessionAlive(@Context HttpServletRequest request) {
		logger.debug("Keep Dictionary session alive!");
		
		if (request != null && request.getSession() != null) {
			return Response.ok().build();
		} else {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
	
	@POST
	@Path("FormStructure/publish")
	@Consumes(MediaType.APPLICATION_XML)
	public Response publishFormStructure(CreateTableFromFormStructurePayload payLoad) throws UnsupportedEncodingException, 
								UserAccessDeniedException, MalformedURLException, UserPermissionException, MessagingException {
		
		String statusChangeComment = payLoad.getStatusChangeComment();
		String approveReason = payLoad.getApproveReason();
		Set<DictionarySupportingDocumentation> docList = payLoad.getDocList();
		StructuralFormStructure formStructure = payLoad.getFormStructure();
		StatusType oldStatus = payLoad.getStatusType();
		Long diseaseId = payLoad.getDiseaseId();
		Long userId = payLoad.getUserId();
		
		//check if the form structure for current instance is published or not in prior attempt
		boolean isPublishedOnCurrentInstance = dictionaryService.isFormStructurePublished(formStructure.getId(), diseaseId);
			if(!isPublishedOnCurrentInstance) {
				
				PublishedFormStructure publishedFormStructure = new PublishedFormStructure(formStructure.getId(),diseaseId);
				publishedFormStructure.setPublished(true);
				publishedFormStructure.setPublicationDate(new Date());
				
				dictionaryService.saveFormStructurePublished(publishedFormStructure);			
			}
		
		Set<Long> keys = modulesConstants.getModulesSTMap().keySet();
		
		boolean isPublished = false;
		if (keys.size() == 1) {
			isPublished = dictionaryService.isFormStructurePublished(formStructure.getId(), diseaseId);
		} else {
			for(Long key:keys) {
				isPublished = dictionaryService.isFormStructurePublished(formStructure.getId(), key);
			}
		}
		
		if(isPublished) {
			
			FormStructure dataStructure = dictionaryToolManager.getDataStructure(formStructure.getId());
			
			dataStructure.setStatus(StatusType.PUBLISHED);
			dataStructure.setPublicationDate(new Date());
			
			dictionaryToolManager.saveFormStructure(dataStructure);
			
			//Save eventlog for publishing a form structure
			DictionaryEventLog eventLog = new DictionaryEventLog(getDiseaseId(), userId);	
			EventType eventType;
			
			if(oldStatus == StatusType.DRAFT) {
				eventType = EventType.STATUS_CHANGE_TO_PUBLISHED;
				
			} else {
				eventType = EventType.REQUESTED_PUBLISH_APPROVED;
			}
			
			dictionaryService.saveEventLog(eventLog,dataStructure, docList, statusChangeComment,eventType);
			
			//send approval email to owner if the form structure was in awaiting publication status
			if(oldStatus==StatusType.AWAITING_PUBLICATION) {
		
				  Account currentDataStructureAccount = getEntityOwnerAccountRestful(formStructure.getId(), EntityType.DATA_STRUCTURE);
				  String ownerName = currentDataStructureAccount.getUser().getFullName();
				  
				  Object[] subjectArg = new Object[] {formStructure.getReadableName()};
				  String subject = messageSource.getMessage(PortalConstants.MAIL_RESOURCE_ACCEPTED_DATASTRUCTURE
										+ PortalConstants.MAIL_RESOURCE_SUBJECT, subjectArg,null);
				  
				  Object[] bodyArg = new Object[] {modulesConstants.getModulesOrgName(getDiseaseId()),modulesConstants.getModulesOrgPhone(getDiseaseId()),
						  ownerName,approveReason, formStructure.getReadableName()};
				  String header = messageSource.getMessage(PortalConstants.MAIL_RESOURCE_COMMON
							+ PortalConstants.MAIL_RESOURCE_HEADER, bodyArg,null);
				  
				  String emailBody = messageSource.getMessage(PortalConstants.MAIL_RESOURCE_ACCEPTED_DATASTRUCTURE
							+ PortalConstants.MAIL_RESOURCE_BODY, bodyArg,null);
				  
				  String footer = messageSource.getMessage(PortalConstants.MAIL_RESOURCE_COMMON
							+ PortalConstants.MAIL_RESOURCE_FOOTER, bodyArg,null);
				  

				  String from = modulesConstants.getModulesOrgEmail(getDiseaseId());

						mailEngine.sendMail(subject, header+emailBody+footer, from, currentDataStructureAccount.getUser().getEmail());
			}
				  
		}
		
		return Response.noContent().build();
	}
	
	public Account getEntityOwnerAccountRestful(Long entityId, EntityType type)
			throws MalformedURLException, UnsupportedEncodingException {

		Set<Long> keys = modulesConstants.getModulesAccountMap().keySet();

		// Case: There is only 1 account module listed
		if (keys.size() == 1) {
			RestAccountProvider anonAccountProvider = new RestAccountProvider(
					modulesConstants.getModulesAccountURL(ServiceConstants.DEFAULT_PROVIDER), null);
			return anonAccountProvider.getEntityOwnerAccount(entityId, type);
		}

		Account owner = null;

		for (Long k : keys) {
			RestAccountProvider anonAccountProvider =
					new RestAccountProvider(modulesConstants.getModulesAccountURL(k), null);
			owner = anonAccountProvider.getEntityOwnerAccount(entityId, type);
			if (owner != null && owner.getId() != null) {
				break;
			}
		}

		return owner;

	}
	
	@POST
	@Path("FormStructure/savePublishedFormStructure")
	@Consumes(MediaType.APPLICATION_XML)
	public Response savePublishedFormStructure (PublishedFormStructure publishedFormStructure) throws UnsupportedEncodingException {
		
	
		dictionaryService.saveFormStructurePublished(publishedFormStructure);
	
		return Response.noContent().build();
	}
	
	@GET
	@Path("FormStructure/editFormStructureStatus/{formStructureId}/{statusId}")
	@Produces("text/xml")
	public FormStructure editFormStructureStatus(@PathParam("formStructureId") Long formStructureId,@PathParam("statusId") Long statusId) throws UnsupportedEncodingException, UserAccessDeniedException, MalformedURLException, UserPermissionException {
		
		FormStructure formStructure = dictionaryToolManager.getDataStructure(formStructureId);
		StatusType statusType = StatusType.statusOf(statusId);
		
		//revert formStructure status
		formStructure.setStatus(statusType);
		
		return dictionaryToolManager.saveFormStructure(formStructure);
	
	}
	
	@POST
	@Path("DataElement/getDataElementInfo")
	@Produces("text/xml")
	public DataElementList getDataElementInfo(@FormParam("deNames") List<String> deNames) {
		Set<String> deSet = new HashSet<>();
		deSet.addAll(deNames);
		List<DataElement> deList = dictionaryToolManager.getLatestDataElementByNameList(deSet);
		DataElementList deReturnList = new DataElementList();
		deReturnList.addAll(deList);
	
		
		return deReturnList;
	}

}
