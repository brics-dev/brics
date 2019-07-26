package gov.nih.tbi.iubiorepo.ws;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.ordermanager.dao.BiospecimenOrderDao;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.service.io.SftpClient;


public class RestBioRepositoryProvider {


	static Logger logger = Logger.getLogger(RestBioRepositoryProvider.class);

	@Autowired
	private BiospecimenOrderDao biospecimenOrderDaoImpl;

	@Autowired
	private MailEngine mailEngine;

	private String token = null;

	private String iuWSBase;
	private String username;
	private String password;
	private String biosampleManagerEmail;

	public RestBioRepositoryProvider(String iuWSBase, String username, String password, String biosampleManagerEmail) {
		this.iuWSBase = iuWSBase;
		this.username = username;
		this.password = password;
		this.biosampleManagerEmail = biosampleManagerEmail;
	}



	/**
	 * gets the IU token that is necessary to invoke other IU web services
	 * 
	 * @return
	 */
	private String getToken() {
		if (token == null) {

			String url = iuWSBase + "/tokens";
			String authField = username + ":" + password;
			
			String authorizationHeader =
					"Basic " + org.apache.cxf.common.util.Base64Utility.encode(authField.getBytes());

			logger.info("Token: " + authorizationHeader);
			WebClient client =
					WebClient.create(url).header("Authorization", authorizationHeader).header("Accept", "text/plain")
							.header("Content-Type", "application/json");


			String body = "{\"role\":\"external\",\"actions\":[\"create\",\"modify\", \"view\"]}";


			Response r = client.post(body, Response.class);

			token = r.readEntity(String.class);

		}


		return token;


	}



	/**
	 * get cataolg function
	 * 
	 * takes in either NINDS or bioFind
	 * 
	 * 
	 * @param repo
	 * @return
	 */
	public String getCatalog(String repo) {
		String url = iuWSBase + "/catalogs/" + repo + ".csv";
		logger.info("Connecting to " + url);
		// String url = iuWSBase + "/catalogs/catalog.csv";
		String wsToken = getToken();
		String authorizationHeader = "JWT " + wsToken;

		WebClient client = WebClient.create(url).header("Authorization", authorizationHeader);

		Response r = client.get(Response.class);
		logger.info("Response: " + r.getStatus());
		String csv = r.readEntity(String.class);


		return csv;

	}



	/**
	 * Gets the IU status of an order
	 * 
	 * @param orderId
	 * @return
	 */
	public JsonObject getIUStatus(Long orderId) {
		String url = iuWSBase + "/orders/" + orderId + "/status.json";
		String wsToken = getToken();
		String authorizationHeader = "JWT " + wsToken;

		WebClient client = WebClient.create(url).header("Authorization", authorizationHeader);

		Response r = client.get(Response.class);


		String jsonObjString = r.readEntity(String.class);


		JsonParser parser = new JsonParser();
		JsonObject jsonObj = parser.parse(jsonObjString).getAsJsonObject();



		return jsonObj;
	}



	/**
	 * Gets the manifest for an order that is in shipped status
	 * 
	 * @param orderId
	 * @return
	 */
	public Response getManifest(Long orderId) {
		String url = iuWSBase + "/orders/" + orderId + "/manifest.xml";
		String wsToken = getToken();
		String authorizationHeader = "JWT " + wsToken;

		WebClient client = WebClient.create(url).header("Authorization", authorizationHeader);

		Response r = client.get(Response.class);

		return r;
	}



	public int submitOrder(String orderXML, String orderXMLFileName, List<UserFile> attachedFiles,
			SftpClient bricsClient) {

		String url = iuWSBase + "/orders";
		String wsToken = getToken();
		String authorizationHeader = "JWT " + wsToken;

		String charset = "UTF-8";

		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		String CRLF = "\r\n"; // Line separator required by multipart/form-data.

		URLConnection connection = null;

		int responseCode = -1;

		try {
			connection = new URL(url).openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			connection.setRequestProperty("Authorization", authorizationHeader);


			OutputStream output = connection.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);


			// Send order file.
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"order\"; filename=\"" + orderXMLFileName + "\"")
					.append(CRLF);
			writer.append("Content-Type: application/xml; charset=" + charset).append(CRLF); // Text file itself must be
																								// saved in this
																								// charset!
			writer.append(CRLF).flush();
			// Files.copy(orderFile.toPath(), output);
			output.write(orderXML.getBytes(Charset.forName("UTF-8")));
			output.flush(); // Important before continuing with writer!
			writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

			// Send attachment file.
			if (attachedFiles != null) {
				Iterator<UserFile> iter = attachedFiles.iterator();
				byte[] data = null;
				while (iter.hasNext()) {
					UserFile uf = (UserFile) iter.next();
					data = null;
					try {
						data = bricsClient.downloadBytes(uf.getName(), uf.getPath());
						String attachmentFileName = uf.getName();
						writer.append("--" + boundary).append(CRLF);
						writer.append(
								"Content-Disposition: form-data; name=\"attachments\"; filename=\""
										+ attachmentFileName + "\"").append(CRLF);
						writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(attachmentFileName))
								.append(CRLF);
						writer.append("Content-Transfer-Encoding: binary").append(CRLF);
						writer.append(CRLF).flush();
						// Files.copy(attachmentFile.toPath(), output);
						output.write(data);
						output.flush(); // Important before continuing with writer!
						writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}// end attachments


			// End of multipart/form-data.
			writer.append("--" + boundary + "--").append(CRLF).flush();


			responseCode = ((HttpURLConnection) connection).getResponseCode();
			String responseMessage = ((HttpURLConnection) connection).getResponseMessage();

			logger.info("Order submission response: " + responseCode + ":" + responseMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return responseCode;

	}



}
