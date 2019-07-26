package gov.nih.nichd.ctdb.ws;

import java.text.SimpleDateFormat;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolClosingOut;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;

@Component
@Path("/protocol/")
public class ProtocolRestService {
	private static final Logger logger = Logger.getLogger(ProtocolRestService.class);

	@GET
	@Path("getProtocolList/{studyId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProtocolListByStudyId(@PathParam("studyId") String studyId) throws WebApplicationException {

		// TODO: Only a check for the "ROLE_PROFORMS" is done right now. Will need add a check for the proper
		// ProFoRMS user role (i.e. PI or Data Manager).

		JsonArray dataJson = new JsonArray();

		try {
			ProtocolManager protoMan = new ProtocolManager();

			for (ProtocolClosingOut pco : protoMan.getClosingOutListByStudyId(studyId)) {
				JsonObject jsonObj = new JsonObject();
				jsonObj.addProperty("bricsStudyId", pco.getBricsStudyId());
				jsonObj.addProperty("protocolName", pco.getProtocolName());
				jsonObj.addProperty("protocolNumber", pco.getProtocolNumber());

				String protoESignature = "";

				if (pco.getProtoEnableEsignature() && pco.getClosingUserId() != null) {
					protoESignature = "Signed";
				} else if (!pco.getProtoEnableEsignature()) {
					protoESignature = "N/A";
				}

				jsonObj.addProperty("protoESignature", protoESignature);
				jsonObj.addProperty("closingUserFullName", pco.getClosingUserFullName());
				jsonObj.addProperty("closingBricsUserId", pco.getClosingBricsUserId());

				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String closingDateStr =
						pco.getClosingOutDate() != null ? dateFormat.format(pco.getClosingOutDate()) : "";
				jsonObj.addProperty("closingOutDate", closingDateStr);

				dataJson.add(jsonObj);
			}
		} catch (CtdbException e) {
			String msg = "Protocol ClosingOut REST WS CALL: getProtoClosingOutListByStudyId ( " + studyId + " ).";
			logger.error(msg, e);
			Response errRes = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
			throw new InternalServerErrorException(errRes);
		}

		return Response.ok().entity(dataJson.toString()).build();
	}
}
