package gov.nih.nichd.ctdb.ws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;



@Component
@Path("/public/datacollection/")
public class DataCollectionRestService {
	
	
	@GET
	@Path("areAnyDataCollections/{eformShortName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response areAnyDataCollections(@PathParam("eformShortName") String eformShortName) throws CtdbException  {
		ResponseManager rm = new ResponseManager();
		boolean areThereDataCollectionsInAnyProtocol = rm.areThereDataCollectionsInAnyProtocol(eformShortName);
		return Response.ok().entity(String.valueOf(areThereDataCollectionsInAnyProtocol)).build();
	}
	
	
	@GET
	@Path("areThereEformsAttachedToAnyVisitType/{eformShortName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response areThereEformsAttachedToAnyVisitType(@PathParam("eformShortName") String eformShortName) throws CtdbException  {
		ResponseManager rm = new ResponseManager();
		boolean areThereEformsAttachedToAnyVisitTypeInAnyProtocol = rm.areThereEformsAttachedToAnyVisitType(eformShortName);
		return Response.ok().entity(String.valueOf(areThereEformsAttachedToAnyVisitTypeInAnyProtocol)).build();
	}

}
