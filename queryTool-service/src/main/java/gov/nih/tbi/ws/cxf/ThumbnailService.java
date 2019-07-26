package gov.nih.tbi.ws.cxf;

import java.io.UnsupportedEncodingException;

import gov.nih.tbi.service.DataCartManager;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

@Path("/thumbnail/")
public class ThumbnailService extends QueryBaseRestService {

	private static final Logger log = Logger.getLogger(ThumbnailService.class);

	@Autowired
	DataCartManager dataCartManager;

	@GET
	@Path("download")
	@Produces("image/jpeg")
	public Response downloadThumbnailImg(@QueryParam("studyName") String studyPrefixedId,
			@QueryParam("datasetName") String datasetName, @QueryParam("imageName") String imageName) throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		log.debug("Downloading image " + imageName);
		if (StringUtils.isBlank(studyPrefixedId) || StringUtils.isBlank(datasetName) || StringUtils.isBlank(imageName)) {
			String msg = "Parameters studyName, datasetName and imageName cannot be empty.";
			log.error(msg);
			Response errResponse = Response.status(Status.NOT_ACCEPTABLE).entity(msg).build();
			throw new NotAcceptableException(errResponse);
		}

		byte[] fileBytes = dataCartManager.downloadThumbnailBytes(studyPrefixedId, datasetName, imageName);

		if (fileBytes == null || fileBytes.length == 0) {
			String msg = "Unable to download image for " + imageName;
			log.error(msg);
			Response errResponse = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
			throw new InternalServerErrorException(errResponse);
		}

		ResponseBuilder response = Response.ok(fileBytes, "image/jpeg");
		response.header("Content-Disposition", "inline; filename=\"" + imageName + "\"");
		response.header("Content-Type", "image/jpeg");

		return response.build();
	}

}
