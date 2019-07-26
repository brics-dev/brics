package gov.nih.tbi.metastudy.ws.cxf;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.MetaStudyStatus;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.MetaStudyManager;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.dictionary.model.QueryToolRestServiceModel.MetaStudyList;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.portal.PortalUtils;

public class MetaStudyRestService extends AbstractRestService {

	private static Logger logger = Logger.getLogger(MetaStudyRestService.class);

	@Autowired
	protected MetaStudyManager metaStudyManager;

	@Autowired
	protected SessionAccount sessionAccount;

	@Autowired
	protected ModulesConstants modulesConstants;
	
	/*** MetaStudy Providers ****/
	/**
	 * Returns a list of meta studies that are not published. the authenticated user is verified, then the list of
	 * meta studies for "proxyticket" will be supplied.
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UserPermissionException
	 */
	@GET
	@Path("getMetaStudyList")
	@Produces(MediaType.TEXT_XML)
	public MetaStudyList listMetaStudiesByUserAndPermission() throws UnsupportedEncodingException,
			UserPermissionException {
		
		// Find which user you are getting studies for and if you have permission
		Account account = getAuthenticatedAccount();
		

		if (account == null) {
			
			throw new RuntimeException("Argument account cannot be null");

		} 

		List<MetaStudy> metaStudies = null;

		Set<MetaStudyStatus> status = new HashSet<MetaStudyStatus>();
		//we are only looking for meta studies that are in draft or awaiting publication
		status.add(MetaStudyStatus.DRAFT);
		status.add(MetaStudyStatus.AWAITING_PUBLICATION);
		
		// System Admin will have full access of all saved queries.
		metaStudies = metaStudyManager.getMetaStudyListFilterByStatus(account, status);

		

		MetaStudyList dsList = new MetaStudyList();
		dsList.addAll(metaStudies);

		return dsList;

	}
	
	
	/**
	 * Returns a list of meta studies that are not published. the authenticated user is verified, then the list of
	 * meta studies for "proxyticket" will be supplied.
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UserPermissionException
	 */
	@GET
	@Path("linkSavedQueryMetaStudy/{savedQueryId}/{metaStudyId}")
	public Response linkSavedQueryMetaStudy(@PathParam("savedQueryId") Long savedQueryId,
			@PathParam("metaStudyId") Long metaStudyId) throws UnsupportedEncodingException,
			UserPermissionException {
		
		// Find which user you are and if you have permission
		Account account = getAuthenticatedAccount();
		
		
		if (account == null) {
			throw new RuntimeException("Argument account cannot be null");
		} 
		
		metaStudyManager.linkSavedQuery(account, metaStudyId, savedQueryId);
		
		return Response.noContent().build();
	}
	
	
	/**
	 * Returns true is the meta study data name is unique. If it is not than it will return false
	 * metaStudyId = the id of the meta study for verification
	 * fileName = the name of the file checked for uniqueness
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UserPermissionException
	 */
	@GET
	@Path("{metaStudyId}/isDataFileNameUnique/")
	@Produces(MediaType.TEXT_XML)
	public Response isDataFileNameUnique(@PathParam("metaStudyId") Long metaStudyId, @QueryParam("fileName") String fileName) throws UnsupportedEncodingException,
			UserPermissionException, UserAccessDeniedException {
		
		//null check parameters
		if(metaStudyId == null || fileName == null){
			String msg = "The service can not have null argument";
			logger.error(msg);
			Response errResponse = Response.status(Status.PRECONDITION_FAILED).entity(msg).build();
			throw new WebApplicationException(errResponse);
		}
		
		Account account = getAuthenticatedAccount();
		
		// make sure the user has access to this study if the web services are secure (not local)
		if (ModulesConstants.getWsSecured()) {
			
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			PermissionType permission = restProvider.getAccess(account.getId(), EntityType.META_STUDY, metaStudyId).getPermission();
					
			if (PermissionType.compare(permission, PermissionType.WRITE) < 0) {				
				String msg = "The user does not have the permission on this.";
				logger.error(msg);
				Response errRes = Response.status(Status.FORBIDDEN).entity(msg).build();
				throw new ForbiddenException(errRes);
			}
		}
		
		MetaStudy metaStudy = metaStudyManager.getMetaStudyById(metaStudyId);
		
		//do not allow the association if the meta study is published
		if(metaStudy.isPublished()){
			String msg = "The service for requested meta study is forbidden on this instance.";
			logger.error(msg);
			Response errResponse = Response.status(Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errResponse);
		}
		
		//do not allow the association if the meta study already has the file name
		if(!metaStudyManager.isMetaStudyDataTitleUnique(fileName, metaStudyId)){
			String msg = "The service for requested meta study is forbidden on this instance.";
			logger.error(msg);
			Response errResponse = Response.status(Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errResponse);
		}
		
		return Response.status(Status.OK).build();
		
	}
	
	/**
	 * This method stores a user file for data that has been persisted from the QT
	 * 
	 * @param downloadPackageString 
	 * @return
	 */
	@POST
	@Path("{metaStudyId}/metaStudyData")
	@Produces(MediaType.TEXT_HTML)
	public Response uploadQTDownloadPackage(@PathParam("metaStudyId") long metaStudyId, @QueryParam("fileDescription") String fileDescription, 
			@QueryParam("fileName") String fileName, @QueryParam("filePath") String filePath, @QueryParam("fileSize") String fileSize) throws UserAccessDeniedException{

		//null check parameters
		if(metaStudyId == 0 || fileDescription == null || fileName == null || filePath == null || fileSize == null){
			String msg = "The service can not have null argument.";
			logger.error(msg);
			Response errResponse = Response.status(Status.PRECONDITION_FAILED).entity(msg).build();
			throw new WebApplicationException(errResponse);
		}
		
		Account account = null;
		try {
			account = getAuthenticatedAccount();
			
			// make sure the user has access to this study if the web services are secure (not local)
			if (ModulesConstants.getWsSecured()) {
				
				String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
				RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
				PermissionType permission = restProvider.getAccess(account.getId(), EntityType.META_STUDY, metaStudyId).getPermission();
						
				if (PermissionType.compare(permission, PermissionType.WRITE) < 0) {
					String msg = "The service for requested meta study is forbidden on this instance.";
					logger.error(msg);
					Response errResponse = Response.status(Status.FORBIDDEN).entity(msg).build();
					throw new ForbiddenException(errResponse);
				}
			}
			
			MetaStudy metaStudy = metaStudyManager.assocaiteDataFileToMetaStudy(metaStudyId, fileDescription, fileName, filePath, 
					Long.parseLong(fileSize), account);
			
			if(metaStudy == null){
				String msg = "Web service error occurs when getting the meta study.";
				logger.error(msg);
				Response errResponse =  Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
				throw new InternalServerErrorException(errResponse);
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			String msg = "Web service error occurs when getting the meta study.";
			logger.error(msg);
			Response errResponse = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
			throw new InternalServerErrorException(errResponse);
		}
		return Response.status(Status.OK).build();
	}
}
