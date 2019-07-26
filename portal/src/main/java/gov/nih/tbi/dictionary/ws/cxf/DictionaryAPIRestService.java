package gov.nih.tbi.dictionary.ws.cxf;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.model.exceptions.SchemaGenerationException;
import gov.nih.tbi.commons.service.DictionaryAPIManager;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.portal.PortalUtils;

@Path("/dictionary/")
public class DictionaryAPIRestService extends AbstractRestService {
	private static Logger logger = Logger.getLogger(DictionaryAPIRestService.class);

	@Autowired
	private DictionaryAPIManager dictionaryAPIManager;

	@GET
	@Path("FormStructure/{shortName}{version : (/version)?}")
	@Produces("text/xml")
	public Response getFormStructure(@PathParam("shortName") String shortName, @PathParam("version") String version) throws UserAccessDeniedException{

		Account requestingAccount = null;
		// There can be an exception when encoding the proxy ticket into UTF-8.
		// This exception can no longer be thrown in the API
		try {
			requestingAccount = getAuthenticatedAccount();

			// null check the shortName. it is required to retrieve any form structure
			if (shortName == null || shortName.equals("")) {
				String msg = "<error>A form strucrure was not specified by the request.</error>";
				Response errRes = Response.status(Response.Status.PRECONDITION_FAILED).entity(msg).build();
				throw new WebApplicationException(errRes);
			}

			//get form structure from the DB
			StructuralFormStructure formStructure = dictionaryAPIManager.getStructuralFormStructure(shortName, version);

			//null check the FS. if null the short name wasn't found in the DB
			if (formStructure == null) {
				logger.error("User requested " + shortName + " that was not found in the database.");
				String msg = "<error>The form structure was not found in our system. Please check the request for the proper parameters.</error>";
				Response errRes = Response.status(Response.Status.NOT_FOUND).entity(msg).build();
				throw new NotFoundException(errRes);
			}

			//no need to check permissions if the form structure is published
			if(!formStructure.getStatus().equals(StatusType.PUBLISHED) && !formStructure.getStatus().equals(StatusType.SHARED_DRAFT)){
				
				EntityMap em = checkPermissions(requestingAccount, formStructure.getId(), EntityType.DATA_STRUCTURE);
				PermissionType permissionType = em.getPermission();
				
				//if the user does not have permission to view the FS, they cannot view the schema
				if (permissionType == null || PermissionType.compare(permissionType, PermissionType.READ) < 0) {
					// User does not have read access to the given data structure
					String msg = "<error>Access to this form structure has been restricted. Please contact the form structure owner.</error>";
					Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
					throw new ForbiddenException(errRes);
				}
			}
			
			//return the schema bytes to the requester
			return Response.status(Response.Status.OK).entity(dictionaryAPIManager.createFormStructureSchema(formStructure)).build();
			
		//This can be thrown by two different methods. Getting the requesting account OR creating the schem from the form structure
		// in either case this SHOULD NOT happen. This is why the catch is lumped together. There are also two spots where the UnsupportedEncodingException
		// can be thrown. The getAuthenticatedAccount request and getting the Proxy ticket. 
		// NullPointerException can be thrown if a structural form structure is null when trying to create XSD
		} catch (SchemaGenerationException e) {
			String msg = "<error>There was an exception creating the form structure schema. The stack is to follow.</error>";
			logger.error(msg);
			Response errRes = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
			throw new InternalServerErrorException(errRes);
		} catch (UnsupportedEncodingException proxtTicketError){
			String msg = "<error>There was an exception creating the form structure schema. The stack is to follow.</error>";
			logger.error(msg, proxtTicketError);
			Response errRes = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
			throw new InternalServerErrorException(errRes);
		}

	}

	@GET
	@Path("Element/{variableName}{version : (/version)?}")
	@Produces("text/xml")
	public Response getDataElement(@PathParam("variableName") String variableName, @PathParam("version") String version) throws UserAccessDeniedException{

		// There can be an exception when encoding the proxy ticket into UTF-8.
		// This exception can no longer be thrown in the API
		try {
			Account requestingAccount = getAuthenticatedAccount();
			
			// null check the variableName. it is required to retrieve any data
			// element
			if (variableName == null || variableName.equals("")) {
				String msg = "<error>An element was no specified by the request.</error>";
				Response errRes = Response.status(Response.Status.PRECONDITION_FAILED).entity(msg).build();
				throw new WebApplicationException(errRes);
			}
			
			//get the data element from the DB
			StructuralDataElement dataElement = dictionaryAPIManager.getStructuralDataElement(variableName, version);
			
			//if the DE is null the variable name was not found in the DB
			if (dataElement == null) {				
				logger.error("User requested " + variableName + " that was not found in the database.");
				String msg = "<error>The element was not found in our system. Please check the request for the proper parameters.</error>";
				Response errRes = Response.status(Response.Status.NOT_FOUND).entity(msg).build();
				throw new NotFoundException(errRes);
			}
			
			//no need to check permissions if the form structure is published
			if(!DataElementStatus.PUBLISHED.equals(dataElement.getStatus()) || !DataElementStatus.AWAITING.equals(dataElement.getStatus())) {

				EntityMap em = checkPermissions(requestingAccount, dataElement.getId(), EntityType.DATA_ELEMENT);
				PermissionType permissionType = em.getPermission();
				
				//if the user does not have permission to view the FS, they cannot veiw the schema
				if (permissionType == null || PermissionType.compare(permissionType, PermissionType.READ) < 0) {
					// User does not have read access to the given data structure
					String msg = "<error>Access to this form structure has been restricted. Please contact the form structure owner.</error>";
					Response errRes = Response.status(Response.Status.FORBIDDEN).entity(msg).build();
					throw new ForbiddenException(errRes);
				}
			}
			
			//return the schema bytes to the requester
			return Response.status(Response.Status.OK).entity(dictionaryAPIManager.createDataElementSchema(dataElement)).build();
			
		//This can be thrown by two different methods. Getting the requesting account OR creating the schem from the form structure
		// in either case this SHOULD NOT happen. This is why the catch is lumped together. There are also two spots where the UnsupportedEncodingException
		// can be thrown. The getAuthenticatedAccount request and getting the Proxy ticket.
		// NullPointerException can be thrown if a structural data element is null when trying to create XSD
		} catch (SchemaGenerationException e) {
			logger.error("<error>There was an exception creating the form structure schema. The stack is to follow.</error>");
			Response errRes = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An internal error occured. Please contact the helpdesk.").build();
			throw new InternalServerErrorException(errRes);
		} catch(UnsupportedEncodingException proxtTicketError){
			logger.error("<error>There was an exception encoding the proxy ticket. The stack is to follow.",proxtTicketError);
			Response errRes = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An internal error occured. Please contact the helpdesk.</error>").build();
			throw new InternalServerErrorException(errRes);
		}
	}
	
	/**
	 * convenience method the returns the entity map for checking permissions
	 * @param requestingAccount - account requesting access to the object
	 * @param objectId - id of the object you wish to check permission of
	 * @param objectType - type of the object you are checking permissions for (EX: data_structure, data_element, saved_query)
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private EntityMap checkPermissions(Account requestingAccount, long objectId, EntityType objectType) throws UnsupportedEncodingException, UserAccessDeniedException{
		// Check users permission to the given FS
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		Long accountId = null;
		String proxyTicket = null;
		if (requestingAccount != null) {
			accountId = requestingAccount.getId();
			proxyTicket = PortalUtils.getProxyTicket(accountUrl);
		}
		
		RestAccountProvider restProvider = new RestAccountProvider(accountUrl, proxyTicket);
		return restProvider.getAccess(accountId, objectType, objectId);
	}
	
	@DELETE
	@Path("DataElement/deletePartialDataElementByName/{deName}{version : (/version)?}")
	public Response deletePartialDataElementByName(@PathParam("deName") String deName, @PathParam("version") String version) 
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();

		deName = URLDecoder.decode(deName, "UTF-8");
		version = URLDecoder.decode(version, "UTF-8");

		logger.debug(requestingAccount.getUserName() + " called deleteSemanticPartByName().");

		if (deName == null || deName.equals("")) {
			String msg = "<error>An element was no specified by the request.</error>";
			Response errRes = Response.status(Response.Status.PRECONDITION_FAILED).entity(msg).build();
			throw new WebApplicationException(errRes);
		}
		
		//get the data element from the DB
		StructuralDataElement structuralDE = dictionaryAPIManager.getStructuralDataElement(deName, version);
		SemanticDataElement sementicDE = dictionaryAPIManager.getSemanticDataElement(deName, version);
		DataElementStatus deStatus = sementicDE.getStatus(); 
		if (structuralDE == null && sementicDE != null ) {
			dictionaryAPIManager.deleteDataElementSemanticPart(sementicDE);
		} else if (structuralDE != null && sementicDE == null ){
			dictionaryAPIManager.deleteDataElementStructuralPart(structuralDE);
		}

		return Response.ok().build();
	}
	
	@DELETE
	@Path("FormStructure/deleteFormStructureByName/{fsName}{version : (/version)?}")
	public Response deleteFormStructureByName(@PathParam("fsName") String fsName, @PathParam("version") String version) 
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();

		fsName = URLDecoder.decode(fsName, "UTF-8");
		version = URLDecoder.decode(version, "UTF-8");

		logger.debug(requestingAccount.getUserName() + " called deleteSemanticPartByName().");

		if (fsName == null || fsName.equals("")) {
			String msg = "<error>A form structure was no specified by the request.</error>";
			Response errRes = Response.status(Response.Status.PRECONDITION_FAILED).entity(msg).build();
			throw new WebApplicationException(errRes);
		}
		
		//get the data element from the DB
		StructuralFormStructure structuralFS = dictionaryAPIManager.getStructuralFormStructure(fsName, version);
		SemanticFormStructure sementicFS = dictionaryAPIManager.getSemanticFormStructure(fsName, version);

		if (structuralFS != null && structuralFS != null ) {
			dictionaryAPIManager.deleteFormStructureStructuralPart(structuralFS);
			dictionaryAPIManager.deleteFormStructureSemanticPart(fsName, version);
		}

		return Response.ok().build();
	}
}