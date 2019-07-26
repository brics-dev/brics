package gov.nih.tbi.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.manager.RboxManager;
import gov.nih.tbi.query.model.RboxRequest;
import gov.nih.tbi.query.model.RboxResponse;


public class RboxService {
	
	static Logger logger = Logger.getLogger(RboxService.class);
	
	@Autowired
	private RboxManager rboxManager;
	
	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Path("/process")
	public Response process(RboxRequest request){
		
		String datastring = new String(request.getDataBytes()).replaceAll("'", "`");
		
		RboxResponse response = rboxManager.executeRScript(request.getScript(), datastring);
		
		return Response.ok(response).build();
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/helloworld")
	public Response helloWorld(){
		return Response.ok("Hello World!").build();
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/rprint")
	public Response rPrint(){
		
		RboxResponse response = rboxManager.executeRScript("print('Test Print')", "col1, col2, col3");
		
		return Response.ok(response.getConsoleOutput()).build();
	}
}
