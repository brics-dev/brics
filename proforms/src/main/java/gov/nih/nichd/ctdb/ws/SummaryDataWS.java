package gov.nih.nichd.ctdb.ws;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.tbi.query.model.SummaryResult;



@Component
@Path("/public/summaryData/")
public class SummaryDataWS {
	private static final Logger logger = Logger.getLogger(SummaryDataWS.class);
	
	@GET
	@Path("program/{chartName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSummaryData(@PathParam("chartName") String chartName) throws CtdbException, JSONException  {
		ResponseManager rm = new ResponseManager();
		Map<String, String> map = rm.getSummaryDataResult(chartName);
		JSONArray SumarryResults = new JSONArray();
		//convert collections to JSONArray
		for (Map.Entry<String, String> entry : map.entrySet()) {
		    JSONObject obj = new JSONObject();
		    obj.put("count", entry.getValue());
		    obj.put("category", entry.getKey());
		    SumarryResults.put(obj);
		}
		
		return Response.ok().entity(SumarryResults.toString()).build();
	}
	
	


}