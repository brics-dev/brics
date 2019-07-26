package gov.nih.tbi.ws.cxf;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.service.DataCartManager;

@Path("/fileTypeDE/")
public class FileTypeDataElementService extends QueryBaseRestService{

	private static final Logger log = Logger.getLogger(FileTypeDataElementService.class);
	
	@Autowired
	DataCartManager dataCartManager;
	
	@GET
	@Path("getFileSize")
	@Produces(MediaType.APPLICATION_JSON)
	public String getFileSize(@FormParam("studyName") String studyPrefixedId,
			@FormParam("datasetName") String datasetName, @FormParam("fileName") String fileName) throws UnauthorizedException, UnsupportedEncodingException{
		
		getAuthenticatedAccount();
		log.debug("Getting file size " + fileName);
		
		Long filesize = null;

		if(!StringUtils.isBlank(studyPrefixedId) && !StringUtils.isBlank(datasetName) && !StringUtils.isBlank(fileName)){
			filesize = dataCartManager.getFileSize(studyPrefixedId, datasetName, fileName);
		}
		
		JSONObject fileSizeJson = new JSONObject();
		fileSizeJson.put("fileSize", filesize);
		
		return fileSizeJson.toString();
	}
	
	@GET
	@Path("download")
	public Response downloadFile(@FormParam("studyName") String studyPrefixedId,
			@FormParam("datasetName") String datasetName, @FormParam("fileName") String fileName) throws UnauthorizedException, UnsupportedEncodingException {
		
		getAuthenticatedAccount();
		log.debug("Downloading file " + fileName);
		
		if (StringUtils.isBlank(studyPrefixedId) || StringUtils.isBlank(datasetName) || StringUtils.isBlank(fileName)) {
			String msg = "Parameters studyName, datasetName and imageName cannot be empty.";
			log.error(msg);
			Response errResponse = Response.status(Status.NOT_ACCEPTABLE).entity(msg).build();
			throw new NotAcceptableException(errResponse);
		}
	
		byte[] fileBytes = dataCartManager.downloadFileBytes(studyPrefixedId, datasetName, fileName);
		
		if (fileBytes == null || fileBytes.length == 0) {
			String msg = "Unable to download image for " + fileName;
			log.error(msg);
			Response errResponse = Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
			throw new InternalServerErrorException(errResponse);
		}


		StreamingOutput fileStream =  new StreamingOutput() 
        {
            @Override
            public void write(java.io.OutputStream output) throws IOException, WebApplicationException 
            {     
                	byte[] fileBytes = dataCartManager.downloadFileBytes(studyPrefixedId, datasetName, fileName);             	
                    output.write(fileBytes);
                    output.flush();
    
            }
        };
        
        ResponseBuilder response = Response.ok(fileStream);
        response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.header("Content-Type", "application/octet-stream");
        
        return response.build();
    }

}
