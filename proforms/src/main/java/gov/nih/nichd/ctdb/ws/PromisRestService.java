package gov.nih.nichd.ctdb.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

@Component
@Path("/public/promis/")
public class PromisRestService {
	private static final Logger logger = Logger.getLogger(PromisRestService.class);
	private static final String ASSESSMENTS = "Assessments/";
	private static final String PARTICIPANTS = "Participants/";
	private static final String RESULTS = "Results/";
	private static final String JSON = ".json";
	private static final String BATTERY = "Batteries/";
	private static final String PROMIS_API_BATTERY_URL = SysPropUtil.getProperty(CtdbConstants.PROMIS_API_URL) + PromisRestService.BATTERY;
	private static final String FORM = "Forms/";
	private static final String PROMIS_API_FORM_URL = SysPropUtil.getProperty(CtdbConstants.PROMIS_API_URL) + PromisRestService.FORM;
	private static final String ERROR_KEY = "Error";
	
	@GET
	@Path("getAsmtOID/{formOID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAsmtOID(@PathParam("formOID") String formOID) throws WebApplicationException{
		String apiUrl = SysPropUtil.getProperty(CtdbConstants.PROMIS_API_URL) + PromisRestService.ASSESSMENTS + formOID + PromisRestService.JSON;
		String jsonResponse = "{}";
		try {
			URL url = new URL(apiUrl);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			connection.addRequestProperty("Authorization", "Basic " + getEncodedToken());

			BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			try {
				jsonResponse = IOUtils.toString(br);
			}finally {
				br.close();
			}
			/*Battery form*/
			JSONObject jsonObj = new JSONObject(jsonResponse);
			if(jsonObj.has(PromisRestService.ERROR_KEY)) { 
				JSONObject batteryAsmtItems = getBatteryItemsAsmtIds(formOID);
				jsonResponse = batteryAsmtItems.toString();
			}
//			System.out.println("getAsmtOID->jsonResponse: "+jsonResponse);
		}catch(IOException e) {
			logger.error("Error getting assessment OID from Promis server",e);
			throw new InternalServerErrorException("Error getting assessment OID from Promis server"); 
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		return Response.ok(jsonResponse, MediaType.APPLICATION_JSON).build();
	}
	
	
	
	
	@POST
	@Path("renderQuestion/{currentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response renderQuestion(@PathParam("currentID") String currentID, String postedData) {
		String apiUrl = SysPropUtil.getProperty(CtdbConstants.PROMIS_API_URL) + PromisRestService.PARTICIPANTS + currentID + PromisRestService.JSON;
		String jsonResponse = "{}";
		try {
			URL url = new URL(apiUrl);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.addRequestProperty("Authorization", "Basic " + getEncodedToken());
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			try {
				writer.append(postedData);
				writer.flush();
			}
			finally {
				writer.close();
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			try {
				jsonResponse = IOUtils.toString(br);
			}finally {
				br.close();
			}
			
		}catch(IOException e) {
			logger.error("Error getting assessment OID from Promis server",e);
			throw new InternalServerErrorException("Error getting assessment OID from Promis server"); 
		}
		return Response.ok(jsonResponse, MediaType.APPLICATION_JSON).build();
	}
	
	
	
	@GET
	@Path("scoring/{assessmentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response scoring(@PathParam("assessmentId") String assessmentId) {
		String apiUrl = SysPropUtil.getProperty(CtdbConstants.PROMIS_API_URL) + PromisRestService.RESULTS + assessmentId + PromisRestService.JSON;
		String jsonResponse = "{}";
		try {
			URL url = new URL(apiUrl);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			connection.addRequestProperty("Authorization", "Basic " + getEncodedToken());

			BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
			try {
				jsonResponse = IOUtils.toString(br);
			}finally {
				br.close();
			}
		}catch(IOException e) {
			logger.error("Error getting assessment OID from Promis server",e);
			throw new InternalServerErrorException("Error getting assessment OID from Promis server"); 
		}
		return Response.ok(jsonResponse, MediaType.APPLICATION_JSON).build();	
	}
	
	
	private String getEncodedToken() {
		String token = SysPropUtil.getProperty(CtdbConstants.PROMIS_API_TOKEN);
		String encodedToken = Base64.getEncoder().encodeToString(token.getBytes());
		return encodedToken;
	}
	
	private JSONObject getBatteryItemsAsmtIds (String formOID) {
		JSONArray batteryAssessments = new JSONArray();
		JSONArray batteryAsmtItems = new JSONArray();
		JSONObject batteryAsmtItem = new JSONObject();
		String batteryApiUrl = PromisRestService.PROMIS_API_BATTERY_URL + formOID + PromisRestService.JSON;
		try {
			URL batteryUrl = new URL(batteryApiUrl);
			HttpsURLConnection batteryConn = (HttpsURLConnection) batteryUrl.openConnection();
			batteryConn.setDoOutput(true);
			batteryConn.setRequestMethod("GET");
			batteryConn.addRequestProperty("Authorization", "Basic " + getEncodedToken());
			
			BufferedReader batteryBr = new BufferedReader(new InputStreamReader((batteryConn.getInputStream())));
			JSONObject batteryFormObj = new JSONObject(IOUtils.toString(batteryBr));
			batteryConn.disconnect();
			batteryBr.close();
			
			JSONArray batteryFormArr = batteryFormObj.getJSONArray("Forms");
			for(int i = 0; i < batteryFormArr.length(); i++) {	
				JSONObject ob = (JSONObject) batteryFormArr.get(i);
				String batteryFormOid = ob.getString("FormOID");
				
				JSONObject batteryItemObj = new JSONObject();
				/*get assessment id*/
				String batteryAsmtApiUrl = SysPropUtil.getProperty(CtdbConstants.PROMIS_API_URL) + PromisRestService.ASSESSMENTS + batteryFormOid + PromisRestService.JSON;
				URL batteryAsmturl = new URL(batteryAsmtApiUrl);
				HttpsURLConnection batteryAsmtConn = (HttpsURLConnection) batteryAsmturl.openConnection();
				batteryAsmtConn.setDoOutput(true);
				batteryAsmtConn.setRequestMethod("GET");
				batteryAsmtConn.addRequestProperty("Authorization", "Basic "+getEncodedToken());
			
				BufferedReader batteryAsmtBr = new BufferedReader(new InputStreamReader((batteryAsmtConn.getInputStream())));
				JSONObject batteryAsmt = new JSONObject(IOUtils.toString(batteryAsmtBr));
				batteryAsmtConn.disconnect();
				batteryAsmtBr.close();
				batteryItemObj = batteryAsmt;
				batteryAssessments.put(batteryAsmt);
				
				/*get battery form items*/
				String batteryItemApiUrl = PROMIS_API_FORM_URL + batteryFormOid + PromisRestService.JSON;
				URL batteryItemUrl = new URL(batteryItemApiUrl);
				HttpsURLConnection batteryItemConn = (HttpsURLConnection) batteryItemUrl.openConnection();
				batteryItemConn.setDoOutput(true);
				batteryItemConn.setRequestMethod("GET");
				batteryItemConn.addRequestProperty("Authorization", "Basic "+getEncodedToken());
			
				BufferedReader batteryFormBr = new BufferedReader(new InputStreamReader((batteryItemConn.getInputStream())));
				JSONObject batteryForm = new JSONObject(IOUtils.toString(batteryFormBr));
				batteryItemConn.disconnect();
				batteryItemObj.put("Items", batteryForm.getJSONArray("Items"));
				
				batteryAsmtItems.put(batteryItemObj);				
			}
			batteryAsmtItem.put("batteryAssessments", batteryAssessments);
			batteryAsmtItem.put("batteryAsmtItems", batteryAsmtItems);
		} catch(IOException e) {
			logger.error("Error getting assessment OID from Promis server",e);
			throw new InternalServerErrorException("Error getting assessment OID from Promis server"); 
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return batteryAsmtItem;
	}
	

}
