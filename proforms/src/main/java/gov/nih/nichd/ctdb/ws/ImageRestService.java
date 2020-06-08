package gov.nih.nichd.ctdb.ws;

import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.nih.nichd.ctdb.security.util.CasServiceUserHelper;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

@Component
@Path("/public/image/")
public class ImageRestService {
	
	private static final Logger logger = Logger.getLogger(ImageRestService.class);
	
	@GET
	@Path("question/{id}/document/{fileName}")
	public Response getQuestionImage(@PathParam("id") long questionId,
			@PathParam("fileName") String questionDocumentFileName) throws WebApplicationException {
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.eform.ddt.url")+ "/question/" + questionId + "/document/" + questionDocumentFileName;
		Client client = ClientBuilder.newClient();
		
		
		String loginUrl = SysPropUtil.getProperty("webservice.cas.ticket.url");
		String serviceUrl = SysPropUtil.getProperty("webservice.cas.dd.service.url");
		String username = SysPropUtil.getProperty("webservice.cas.proforms.username");
		String password = SysPropUtil.getProperty("webservice.cas.proforms.password");
		
		byte[] rBytes = null;
		try {
			String sgtUrl = CasServiceUserHelper.login(client, loginUrl, serviceUrl, username, password);
			String serviceTicket = CasServiceUserHelper.getServiceTicket(client, sgtUrl, serviceUrl);
			WebTarget target = client.target(restfulUrl);
			target = target.queryParam("ticket", serviceTicket);
			
			InputStream rIn = target.request(MediaType.APPLICATION_OCTET_STREAM).get(InputStream.class);
			rBytes = IOUtils.toByteArray(rIn);

			CasServiceUserHelper.logout(client, loginUrl, serviceTicket);
		} catch (Exception e) {
			logger.error("Error getting question image", e);
			throw new InternalServerErrorException("Error getting question image");
		}
		
		// Get the file's mime/media type.
		MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
		String mediaType = fileTypeMap.getContentType(questionDocumentFileName);
		
		return Response.ok(rBytes, mediaType).header("Content-Disposition", "inline; filename=" + questionDocumentFileName).build();		
	}


}
