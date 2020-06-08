package gov.nih.tbi.repository.ws;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.model.MetaStudyStatus;
import gov.nih.tbi.commons.model.ResearchManagementRole;
import gov.nih.tbi.commons.service.MetaStudyManager;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.metastudy.model.hibernate.ResearchManagementMeta;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.commons.service.RepositoryManager;
@Path("/metastudy/")
public class PublicMetaStudyService extends AbstractRestService{
	
	private static Logger logger = Logger.getLogger(PublicMetaStudyService.class);
	
	@Autowired
	protected MetaStudyManager metaStudyManager;
	
	@Autowired
	protected RepositoryManager repositoryManager;
	
	@GET   	
	@Path("search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response metaStudySearch(){
		logger.debug("PUBLIC REST WS CALL: metaStudySearch");
		
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able meta studies
		if(!modulesConstants.getIsMetaStudyPublic()){
			String msg = "PUBLIC REST WS CALL: metaStudySearch. The service is forbidden on this instance.";
			logger.error( msg + "Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		List<MetaStudy> metaStudyList = metaStudyManager.metaStudyPublicSearch();
		return Response.ok().entity(metaStudyList).build();
	}

	@GET   	
	@Path("{metaStudyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMetaStudyById(@PathParam("metaStudyId") Long metaStudyId){
		
		logger.debug("REST WS CALL: getMetaStudyById ( " + metaStudyId + " ) ");
		
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able meta studies
		if(!modulesConstants.getIsMetaStudyPublic()){
			String msg = "PUBLIC REST WS CALL: getMetaStudyById ( " + metaStudyId + " ). The service is forbidden on this instance.";
			logger.error( msg + "Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		if(metaStudyId != null){
			MetaStudy metaStudy = metaStudyManager.getPublicMetaStudyById(metaStudyId);
			if(metaStudy.getStatus() == MetaStudyStatus.PUBLISHED){
				return Response.ok().entity(metaStudy).build();
			} else {
				String msg = "PUBLIC REST WS CALL: getMetaStudyById ( " + metaStudyId + " ). The metastudy status is not Public.";
				logger.error( msg );
				Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
				throw new ForbiddenException(errRes);
			}
		} else {
			String msg = "PUBLIC REST WS CALL: getMetaStudyById. Didn't complete because the ID is null/empty. MetaStudy ID = " + metaStudyId;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
	}
	
	@GET
	@Path("{metaStudyId}/primary_investigator/image")
	public Response getMetastudyPrimaryInvestigatorImage(@PathParam("metaStudyId") Long metaStudyId) throws WebApplicationException {
		
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able meta studies
		if(!modulesConstants.getIsMetaStudyPublic()){
			String msg = "PUBLIC REST WS CALL: getMetastudyPrimaryInvestigatorImage ( " + metaStudyId + " ). The service is forbidden on this instance.";
			logger.error(msg);
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		if(metaStudyId == null){
			String msg = "PUBLIC REST WS CALL: getMetastudyPrimaryInvestigatorImage. Didn't complete because the ID is null/empty. MetaStudy ID = " + metaStudyId;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
		
		if(!metaStudyManager.isMetaStudyPublic(metaStudyId)){
			String msg = "PUBLIC REST WS CALL: studySubmittedFormsForStudy ( " + metaStudyId + " ). The metastudy status is not Public.";
			logger.error(msg);
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		ResearchManagementMeta image = metaStudyManager.getMetastudyPrimaryInvestigatorImage(metaStudyId);
		
		if(image != null){
			try {
				UserFile imageUserFile = image.getPictureFile();
				if (imageUserFile != null) {
					byte[] fileBytes = metaStudyManager.getFileByteArray(imageUserFile);
					// Get the file's mime/media type.
					FileNameMap fileNameMap = URLConnection.getFileNameMap();
					String mediaType = fileNameMap.getContentTypeFor(imageUserFile.getName());
					ResponseBuilder response = Response.ok(fileBytes, mediaType);
					response.header("Content-Disposition", "inline; filename=" + imageUserFile.getName());
					return response.build();
				}
				else {
					String msg = "Primary Investigator's image is not found";
					Response errRes = Response.status(Response.Status.NOT_FOUND).entity(msg).build();
					throw new NotFoundException(errRes);
				}
			} catch (Exception e) {
				String errorMessage = "There was an error retrieving the image: " + image.getId();
				logger.error(errorMessage, e);
				Response errRes = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
				throw new InternalServerErrorException(errRes);
			}
		} else {
			String msg = "PUBLIC REST WS CALL: getMetastudyPrimaryInvestigatorImage. We didn't find a PI image for MetaStudy ID = " + metaStudyId;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.NO_CONTENT).entity(msg).build();
			throw new WebApplicationException(errRes);
		}
	}
	
	@GET
	@Path("researcher/{studyId}/{rmId}/image")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getResearcherImage(
			@PathParam("studyId") Long studyId, 
			@PathParam("rmId") Long researcherId) {
		
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able studies
		if(!modulesConstants.getIsRepositoryPublic()){
			String msg = "PUBLIC REST WS CALL: getStudyPrimaryInvestigatorImage ( " + studyId + " ). The service is forbidden on this instance.";
			logger.error(msg + " Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		if(!metaStudyManager.isMetaStudyPublic(studyId)){
			String msg = "PUBLIC REST WS CALL: getStudyPrimaryInvestigatorImage ( " + studyId + " ). The study status is not Public.";
			logger.error(msg + " Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		ResearchManagementMeta pi = metaStudyManager.getMetaStudyManagementImage(studyId, researcherId);

		if(pi != null){
			try {
				UserFile userPictureFile = pi.getPictureFile();
				if (userPictureFile != null) {
					byte[] fileBytes = metaStudyManager.getFileByteArray(userPictureFile);
					ResponseBuilder response = Response.ok(fileBytes);
					response.header("Content-Disposition", "inline; filename=" + userPictureFile.getName());
					return response.build();
				}
				else {
					String msg = "There is no image stored for Research Management: " + pi.getId();
					Response errRes = Response.status(Response.Status.NOT_FOUND).entity(msg).build();
					throw new NotFoundException(errRes);
				}
			} catch (Exception e) {
				String errorMessage = "There was an error retrieving the image " + pi.getId();
				logger.error(errorMessage, e);
				Response errRes = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
				throw new InternalServerErrorException(errRes);
			}
		} else {
			String msg = "PUBLIC REST WS CALL: getStudyPrimaryInvestigatorImage. We didn't find a researcher image for Study ID: " + studyId + " and Researcher ID: "+researcherId;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.NO_CONTENT).entity(msg).build();
			throw new WebApplicationException(errRes);
		}
	}
	
	@GET
	@Path("{metaStudyId}/publications")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPublicationListByMetastudyId(@PathParam("metaStudyId") Long metaStudyId){
		logger.debug("REST WS CALL: getPublicationListByMetastudyId ( " + metaStudyId + " ) ");
		
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able studies
		if(!modulesConstants.getIsMetaStudyPublic()){
			String msg = "PUBLIC REST WS CALL: getPublicationListByMetastudyId ( " + metaStudyId + " ). The service is forbidden on this instance.";
			logger.error(msg + " Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		if(metaStudyId != null){
			if(!metaStudyManager.isMetaStudyPublic(metaStudyId)){
				String msg = "PUBLIC REST WS CALL: getPublicationListByMetastudyId ( " + metaStudyId + " ). The metastudy status is not Public.";
				logger.error(msg);
				Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
				throw new ForbiddenException(errRes);
			}
			return Response.ok().entity(metaStudyManager.getPublicationsByMetastudyId(metaStudyId)).build();
		} else {
			String msg = "PUBLIC REST WS CALL: getPublicationListByMetastudyId. Didn't complete because the ID is null/empty. Metastudy ID = " + metaStudyId;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
	}
	
	@GET
	@Path("{metaStudyId}/publication/{publicationId}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadPublication(@PathParam("metaStudyId") Long metaStudyId, @PathParam("publicationId") Long publicationId) throws Exception{
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able studies
		if(!modulesConstants.getIsMetaStudyPublic()){
			String msg = "PUBLIC REST WS CALL: downloadPublication ( " + metaStudyId + " publicationId = " + publicationId  + " ). The service is forbidden on this instance.";
			logger.error(msg + " Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		if(metaStudyId == null){
			String msg = "PUBLIC REST WS CALL: downloadPublication. Didn't complete because the ID is null/empty. MetaStudy ID = " + metaStudyId;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
		
		if(!metaStudyManager.isMetaStudyPublic(metaStudyId)){
			String msg = "PUBLIC REST WS CALL: downloadPublication ( " + metaStudyId + " ). The metastudy status is not Public.";
			logger.error(msg);
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		SupportingDocumentation publication = metaStudyManager.getPublicationDocument(metaStudyId, publicationId);
		
		if(publication != null){
				try {
					String fileName = publication.getName();
					UserFile userFile = publication.getUserFile();
					byte[] downloadBytes = repositoryManager.getFileByteArray(userFile);
					File downloadFile = new File("Downloads/"+fileName);
					FileUtils.writeByteArrayToFile(downloadFile, downloadBytes);
					String contentType = Files.probeContentType(downloadFile.toPath());
					return Response.ok()
							.type(contentType)
							.entity((Object) downloadFile)
							.header("Content-Disposition", "attachment; filename= \"" + fileName+"\"")
							.build();
				} catch (Exception e) {
					String msg = "PUBLIC REST WS CALL: downloadPublication. Didn't complete because an exception was thrown getting the file. Metastudy ID = " + metaStudyId + " publicationId = " + publicationId;
					logger.error(msg);
					Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
					throw new BadRequestException(errRes);
				}		
			} else {
				String msg = "PUBLIC REST WS CALL: downloadPublication. Didn't complete because the ID is null/empty. Metastudy ID = " + metaStudyId + " publicationId = " + publicationId;
				logger.error(msg);
				Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
				throw new BadRequestException(errRes);
		}
	}
}
