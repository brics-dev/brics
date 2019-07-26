package gov.nih.tbi.ws.provider;

import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.DEValueRangeMap;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.SchemaList;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class DictionaryWebserviceProvider extends RestAuthenticationProvider {

	private static final Logger log = LogManager.getLogger(DictionaryWebserviceProvider.class.getName());

	/**
	 * A constructor for a rest provider in which the serverLocation provided is the domain of the web service call
	 * being made. In the case that any path information other than a domain (such as /portal), that information is
	 * stripped.
	 * 
	 * A proxyTicket provided can only be used one time. If RestAccountProvider attempts to use the proxy ticket
	 * multiple times, an exception is thrown. If a null or blank string is provided, then public calls may be made.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @throws MalformedURLException
	 */
	public DictionaryWebserviceProvider(String serverLocation, String proxyTicket) {

		super(serverLocation, proxyTicket);
	}

	/**
	 * FOR QUERY TOOL USE ONLY. MUST PASS ApplicationConstants.isWebservicesSecured() INTO THE THIRD ARG.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @param wsSecure
	 */
	public DictionaryWebserviceProvider(String serverLocation, String proxyTicket, boolean wsSecure) {
		super(serverLocation, proxyTicket, wsSecure);
	}

	/**
	 * This method loops through selected forms and collects all data elements with permissible value defined, and
	 * passes data element names through web service call and obtains a map of data element name to map of permissible
	 * values. This map will be used to draw the data table based on the user selected display option.
	 */
	public Map<String, Map<String, ValueRange>> getDataElementValueRangeMap(List<FormResult> selectedFormList,
			String path /* constants.getDataElementValueRangeMapURL() */) {

		Set<String> deNameSet = new HashSet<String>();
		Map<String, Map<String, ValueRange>> deValueRangeMap = null;

		for (FormResult formResult : selectedFormList) {
			for (RepeatableGroup rg : formResult.getRepeatableGroups()) {
				for (DataElement de : rg.getDataElements()) {

					if (de.hasPermissibleValues()) {
						deNameSet.add(de.getName());
					}
				}
			}
		}

		if (deNameSet.isEmpty()) {
			log.info("rebuildDEPVCodeMap - Data Element name set is empty.");
			return null;
		}

		// Establish connection to web service URL
		// log.info("Connecting to QueryToolService at address: " + constants.getDataElementValueRangeMapURL());

		DataOutputStream writer = null;
		HttpURLConnection connection = null;
		InputStream input = null;

		StringBuilder paramBuilder = new StringBuilder();

		for (String deName : deNameSet) {
			paramBuilder.append("deNames=").append(deName).append("&");
		}

		byte[] postData = paramBuilder.substring(0, paramBuilder.length() - 1).getBytes(Charset.forName("UTF-8"));

		try {
			URL url = new URL(path);

			if (url.getProtocol().equals("https") == true) {
				connection = (HttpsURLConnection) url.openConnection();
			} else {
				connection = (HttpURLConnection) url.openConnection();
			}

			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.addRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
			connection.addRequestProperty("charset", "utf-8");
			connection.connect();

			writer = new DataOutputStream(connection.getOutputStream());
			writer.write(postData);

			JAXBContext jc = JAXBContext.newInstance(DEValueRangeMap.class);

			if (connection.getResponseCode() != 200) {
				throw new HttpException(
						"Response code: " + connection.getResponseCode() + "\nContent: " + connection.getContent());
			} else {
				input = connection.getInputStream();
				DEValueRangeMap dvrMap = (DEValueRangeMap) jc.createUnmarshaller().unmarshal(input);
				deValueRangeMap = dvrMap.getMap();
			}

		} catch (IOException | JAXBException | HttpException e) {
			log.error("Exception occured when getting DataElementValueRangeMap " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}

		return deValueRangeMap;
	}

	/**
	 * This method calls dictionary web service to get a list of schema objects in the system.
	 * 
	 * @param assertion
	 * @return List of schema objects
	 * @throws UnsupportedEncodingException
	 */
	public List<Schema> getSchemaList(String path /* constants.getSchemaListURL() */)
			throws UnsupportedEncodingException {

		WebClient client = createWebClient(path);

		String ac = client.getCurrentURI().toString();
		createWebClient(ac);
		log.debug("unsecure url  " + ac);

		SchemaList sl = client.accept("text/xml").get(SchemaList.class);

		if (sl == null) {
			log.error("SchemaList object returned null.");
			return null;
		} else {
			return sl.getList();
		}
	}
	
	public String getDeDetailsPageHtml(String deName) throws UnsupportedEncodingException {
		InputStreamReader input = null;

		try {
			log.debug("This is the proxy Ticket: " + getEncodedTicket());
			String requestUrl = serverUrl + "portal/dictionary/dataElementAction!viewDetails.ajax?dataElementName=" + deName
					+ "&ticket=" + getEncodedTicket() + "&queryArea=true";
			
			URL url = new URL(requestUrl);
			input = new InputStreamReader(url.openStream());
			char[] inChars = new char[200];
			StringBuffer output = new StringBuffer(400);

			// Prime the input
			int size = input.read(inChars);
			while (size > 0) {
				output.append(inChars, 0, size);
				size = input.read(inChars);
			}
			return output.toString();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}
