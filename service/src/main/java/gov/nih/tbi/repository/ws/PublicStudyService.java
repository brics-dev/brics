package gov.nih.tbi.repository.ws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.nio.file.Files;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.service.QueryToolManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.repository.model.StudySubmittedFormCache;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;
import gov.nih.tbi.repository.model.hibernate.UserFile;

@Path("/study/")
public class PublicStudyService extends AbstractRestService{
	
	private static Logger logger = Logger.getLogger(PublicStudyService.class);
	
	@Autowired
	protected RepositoryManager repositoryManager;
	
	@Autowired
	private QueryToolManager queryToolManager;
	
	@GET   	
	@Path("search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response studySearch(){
		logger.debug("PUBLIC REST WS CALL: studySearch");
		
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able studies
		if(!modulesConstants.getIsRepositoryPublic()){
			String msg = "PUBLIC REST WS CALL: studySearch. The service is forbidden on this instance.";
			logger.error(msg + "Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		return Response.ok().entity(repositoryManager.getPublicSiteSearchBasicStudies()).build();
	}

	@GET   	
	@Path("{studyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStudyById(@PathParam("studyId") Long studyId){
		logger.debug("REST WS CALL: getStudyById ( " + studyId + " ) ");
		
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able studies
		if(!modulesConstants.getIsRepositoryPublic()){
			String msg = "PUBLIC REST WS CALL: getStudyById ( " + studyId + " ). The service is forbidden on this instance.";
			logger.error( msg + "Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		if(studyId != null){
			Study queryStudy = repositoryManager.getPublicStudySearchById(studyId);
			
			if(queryStudy.getStudyStatus() == StudyStatus.PUBLIC){
				return Response.ok().entity(repositoryManager.getPublicStudySearchById(studyId)).build();
			} else {
				String msg = "PUBLIC REST WS CALL: getStudyById ( " + studyId + " ). The study status is not Public.";
				logger.error(msg);
				Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
				throw new ForbiddenException(errRes);
			}
		} else {
			String msg = "PUBLIC REST WS CALL: getStudyById. Didn't complete because the ID is null/empty. Study ID = " + studyId;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
	}
	
	@GET
	@Path("{studyId}/publications")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPublicationListByStudyId(@PathParam("studyId") Long studyId){
		logger.debug("REST WS CALL: getPublicationListByStudyId ( " + studyId + " ) ");
		
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able studies
		if(!modulesConstants.getIsRepositoryPublic()){
			String msg = "PUBLIC REST WS CALL: getStudyById ( " + studyId + " ). The service is forbidden on this instance.";
			logger.error(msg + " Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
	
		
		if(studyId != null){
			if(repositoryManager.isStudyPublic(studyId)){
				JsonArray dataJson = repositoryManager.getPublicationJsonByStudyId(studyId);	
				if(dataJson.size() == 0){
					return Response.ok().build();
				} else {
					return Response.ok().entity(dataJson.toString()).build();
				}
			} else {
				String msg = "PUBLIC REST WS CALL: getPublicationListByStudyId ( " + studyId + " ). The study status is not Public.";
				logger.error(msg);
				Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
				throw new ForbiddenException(errRes);
			}
		} else {
			String msg = "PUBLIC REST WS CALL: getStudyById. Didn't complete because the ID is null/empty. Study ID = " + studyId;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
	}
	
	@GET
	@Path("{studyId}/publication/{publicationId}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadPublication(@PathParam("studyId") Long studyId, @PathParam("publicationId") Long publicationId){
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able studies
		if(!modulesConstants.getIsRepositoryPublic()){
			String msg = "PUBLIC REST WS CALL: downloadPublication ( " + studyId + " publicationId = " + publicationId  + " ). The service is forbidden on this instance.";
			logger.error(msg + " Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		if(!repositoryManager.isStudyPublic(studyId)){
			String msg  = "PUBLIC REST WS CALL: downloadPublication ( " + studyId + " ). The study status is not Public.";
			logger.error(msg);
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		SupportingDocumentation publication = repositoryManager.getPublicationDocument(studyId,publicationId);
		
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
				String msg = "PUBLIC REST WS CALL: downloadPublication. Didn't complete because an exception was thrown getting the file.";
				logger.error(msg, e);
				Response errRes = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
				throw new InternalServerErrorException(errRes);
			}
		} else {
			String msg = "PUBLIC REST WS CALL: downloadPublication. Didn't complete because the ID is null/empty. Study ID = " + studyId + " publicationId = " + publicationId;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
	}
	
	@GET
	@Path("{studyId}/primary_investigator/image")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getStudyPrimaryInvestigatorImage(@PathParam("studyId") Long studyId) throws WebApplicationException {
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able studies
		if(!modulesConstants.getIsRepositoryPublic()){
			String msg = "PUBLIC REST WS CALL: getStudyPrimaryInvestigatorImage ( " + studyId + " ). The service is forbidden on this instance.";
			logger.error(msg + " Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		if(!repositoryManager.isStudyPublic(studyId)){
			String msg = "PUBLIC REST WS CALL: getStudyPrimaryInvestigatorImage ( " + studyId + " ). The study status is not Public.";
			logger.error(msg);
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		ResearchManagement pi = repositoryManager.getPrimaryInvestigatorByStudyId(studyId);

		if(pi != null){
			try {
				UserFile userPictureFile = pi.getPictureFile();
				if (userPictureFile != null) {
					byte[] fileBytes = repositoryManager.getFileByteArray(userPictureFile);
					ResponseBuilder response = Response.ok(fileBytes);
					response.header("Content-Disposition", "inline; filename=" + userPictureFile.getName());
					return response.build();
				}
				else {
					String msg = "Primary Investigator's image is not found";
					Response errRes = Response.status(Response.Status.NOT_FOUND).entity(msg).build();
					throw new NotFoundException(errRes);
				}
				//return Response.ok(encodeByteArrayToBase64(repositoryManager.getFileByteArray(pi.getPictureFile()),"piImage")).build();
			} catch (Exception e) {
				String errorMessage = "There was an error retrieving the image " + pi.getId();
				logger.error(errorMessage, e);
				Response errRes = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
				throw new InternalServerErrorException(errRes);
			}
		} else {
			String msg = "PUBLIC REST WS CALL: getStudyPrimaryInvestigatorImage. We didn't find a PI image for Study ID = " + studyId;
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
		
		if(!repositoryManager.isStudyPublic(studyId)){
			String msg = "PUBLIC REST WS CALL: getStudyPrimaryInvestigatorImage ( " + studyId + " ). The study status is not Public.";
			logger.error(msg);
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		ResearchManagement pi = repositoryManager.getStudyManagementImage(studyId, researcherId);

		if(pi != null){
			try {
				UserFile userPictureFile = pi.getPictureFile();
				if (userPictureFile != null) {
					byte[] fileBytes = repositoryManager.getFileByteArray(userPictureFile);
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
	@Path("{studyId}/submitted/forms")
	@Produces(MediaType.APPLICATION_JSON)
	public Response studySubmittedFormsForStudy(@PathParam("studyId") Long studyId) {
		logger.debug("PUBLIC REST WS CALL: studySubmittedFormsForStudy");
		
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able studies
		if(!modulesConstants.getIsRepositoryPublic()){
			String msg = "PUBLIC REST WS CALL: studySearch ( " + studyId + " ). The service is forbidden on this instance.";
			logger.error(msg + " Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		if(studyId == null){
			String msg = "PUBLIC REST WS CALL: studySubmittedFormsForStudy. Didn't complete because the IDs are null/empty";
			logger.error(msg);
			Response errRes = Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
			throw new BadRequestException(errRes);
		}
		
		if(!repositoryManager.isStudyPublic(studyId)){
			String msg = "PUBLIC REST WS CALL: studySubmittedFormsForStudy ( " + studyId + " ). The study status is not Public.";
			logger.error(msg);
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		StudySubmittedFormCache studySubmittedFormCache = StudySubmittedFormCache.getInstance(repositoryManager,queryToolManager);
		JsonObject jsonOutput =  studySubmittedFormCache.getStudySubmittedFormsWithoutData(studyId);
		
		return Response.ok().entity(jsonOutput.toString()).build();
	}
	
	@DELETE
	@Path("submitted/forms/resetCache")
	public Response resetCache() throws UnsupportedEncodingException {
		StudySubmittedFormCache studySubmittedFormCache = StudySubmittedFormCache.getInstance(repositoryManager,queryToolManager);
		studySubmittedFormCache.clearCache();
		
		logger.info("StudySubmittedForm cache cleared and rebuilt.");
		return Response.ok().build();
	}
	
	@GET
	@Path("{studyId}/graphic")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getStudyGraphic(@PathParam("studyId") String studyId) {
		
		//need a check to make sure that you can only get this if the feature is turned on
		//this allows us to control access since fitbir is the only instance with publicly search-able studies
		if(!modulesConstants.getIsRepositoryPublic()){
			String msg = "PUBLIC REST WS CALL: getStudyGraphic ( " + studyId + " ). The service is forbidden on this instance.";
			logger.error(msg + " Check modules.properties");
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		if(!repositoryManager.isStudyPublic(Long.valueOf(studyId))){
			String msg = "PUBLIC REST WS CALL: getStudyGraphic ( " + studyId + " ). The study status is not Public.";
			logger.error(msg);
			Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errRes);
		}
		
		Study study = repositoryManager.getStudyGraphicFileById(Integer.valueOf(studyId));

		if(study != null){
			try {
				UserFile userPictureFile = study.getGraphicFile();
				if (userPictureFile != null) {
					byte[] fileBytes = repositoryManager.getFileByteArray(userPictureFile);
					ResponseBuilder response = Response.ok(fileBytes);
					response.header("Content-Disposition", "inline; filename=" + userPictureFile.getName());
					return response.build();
				}
				else {
					String msg = "There is no graphic stored for study: " + studyId;
					logger.info(msg);
					Response errRes = Response.status(Response.Status.NOT_FOUND).entity(msg).build();
					throw new NotFoundException(errRes);
				}
			} catch (Exception e) {
				String errorMessage = "There was an error retrieving the graphic for study " + studyId;
				logger.error(errorMessage, e);
				Response errRes = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
				throw new InternalServerErrorException(errRes);
			}
		} else {
			String msg = "PUBLIC REST WS CALL: getStudyGraphic. We didn't find a graphic for Study ID: " + studyId ;
			logger.error(msg);
			Response errRes = Response.status(Response.Status.NO_CONTENT).entity(msg).build();
			throw new WebApplicationException(errRes);
		}
	}
	

}
