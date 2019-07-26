package gov.nih.nichd.ctdb.importdata.thread;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.json.JSONObject;

@Path("/HL7") // sets the path for this service
public class ImportHL7Data {
	private static final Logger log = Logger.getLogger(ImportHL7Data.class);
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response importHl7Data(@FormParam("importInfo") String json) throws WebApplicationException {
		log.info("Importing HL7 data form JSON: " + json);
		
		try {
			JSONObject formJsonObj = new JSONObject(json);
			JsonDataImportingThread jsonDataImportingThread = new JsonDataImportingThread(formJsonObj);
			
			// Calling the import method directly instead of using the run() method. This is to block the current request process until
			// the import process is finished to ensure consistent error behavior.
			jsonDataImportingThread.jsonToDataCollection(formJsonObj);
		}
		catch (Exception e) {
			log.error("Could not import HL7 data.", e);
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
		
		return Response.noContent().build();
	}
}
