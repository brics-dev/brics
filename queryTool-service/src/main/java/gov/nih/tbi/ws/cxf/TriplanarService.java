package gov.nih.tbi.ws.cxf;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;

import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.service.TriplanarManager;
import gov.nih.tbi.service.model.TriplanarQueue;

@Path("/triplanar/")
public class TriplanarService extends QueryBaseRestService {

	private static final Logger log = Logger.getLogger(TriplanarService.class);
	private static final String TRIPLANAR_FILE_NAME_PREFIX = "TRIPLANAR_";

	@Autowired
	TriplanarManager triplanarManager;

	@Autowired
	TriplanarQueue triplanarQueue;

	@GET
	@Path("viewTriplanarImg")
	@Produces(MediaType.APPLICATION_JSON)
	@Encoded
	public String viewTriplanarImg(@QueryParam("studyName") String studyPrefixedId,
			@QueryParam("datasetName") String datasetName, @QueryParam("triplanarName") String triplanarName)
			throws WebApplicationException {

		JsonObject respObj = new JsonObject();

		// Validate the HTTP query parameters.
		if (ValUtil.isBlank(studyPrefixedId) || ValUtil.isBlank(datasetName) || ValUtil.isBlank(triplanarName)) {
			String errMsg = "studyName, datasetName and triplanarName cannot be empty.";
			log.error(errMsg);
			throw new BadRequestException(errMsg);
		}
		
		// Decode the HTTP query parameters.
		try {
			datasetName = URLDecoder.decode(datasetName, "UTF-8");
			studyPrefixedId = URLDecoder.decode(studyPrefixedId, "UTF-8");
			triplanarName = URLDecoder.decode(triplanarName, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			String errMsg = "There was an error docoding the parameters from UTF-8 datasetName " + datasetName
					+ " studyName " + studyPrefixedId + " triplanarName " + triplanarName;
			log.error(errMsg, e1);
			throw new BadRequestException(errMsg);
		}

		boolean isTriplanarReady = false;

		// Check if the triplanar image is ready.
		try {
			isTriplanarReady = triplanarManager.isTriplanarReady(studyPrefixedId, datasetName, triplanarName);
			respObj.addProperty("triplanarReady", isTriplanarReady);

			if (isTriplanarReady) {
				// Check if it's a 4D image set
				int timeSlices = triplanarManager.check4DImage(studyPrefixedId, datasetName, triplanarName);

				respObj.addProperty("imagePrefix", TRIPLANAR_FILE_NAME_PREFIX);
				respObj.addProperty("timeSlices", timeSlices);
				respObj.addProperty("is4DImage", timeSlices > 1);
			}
		} catch (Exception e) {
			log.error("Error occurred while starting or checking on triplanar generation status.", e);
			throw new InternalServerErrorException(e.getMessage());
		}

		return respObj.toString();
	}


	@GET
	@Path("downloadTriplanarImg")
	@Produces("image/jpeg")
	public Response downloadTriplanarImg(@QueryParam("studyName") String studyName,
			@QueryParam("datasetName") String datasetName, @QueryParam("triplanarName") String triplanarName,
			@QueryParam("imageName") String imageName) throws WebApplicationException {

		if (StringUtils.isBlank(imageName)) {
			throw new BadRequestException("The \"imageName\" query parameter is blank or missing.");
		}

		log.debug("Downloading triplanar image " + imageName);

		String filePath = triplanarManager.getOutputFilePath(studyName, datasetName, triplanarName);
		File imageFile = new File(filePath + imageName + ".jpg");
		ResponseBuilder response = Response.ok(imageFile)
				.header("Content-Disposition", "inline;filename=" + imageName)
				.header("Content-Length", imageFile.length());

		return response.build();
	}

	@GET
	@Path("clearTriplanarQueue")
	public Response clearTriplanarQueue() {
		log.debug("Clearing TriplanarQueue ");
		triplanarQueue.emptyTriplanarQueue();

		return Response.noContent().build();
	}
}
