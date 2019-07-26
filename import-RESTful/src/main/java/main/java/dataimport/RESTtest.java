package main.java.dataimport;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/RESTtest")
public class RESTtest {

	@GET
	@Path("/{param}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getMsg(@PathParam("param") String msg) {

		String output = "Jersey say : " + msg;

		return Response.status(200).entity(output).build();
	}
}
