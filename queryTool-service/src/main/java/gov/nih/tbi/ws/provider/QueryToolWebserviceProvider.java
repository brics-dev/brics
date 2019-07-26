package gov.nih.tbi.ws.provider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import gov.nih.tbi.commons.util.BRICSFilesUtils;
import gov.nih.tbi.exceptions.QueryToolDownloadException;
import gov.nih.tbi.exceptions.TriplanarException;
import gov.nih.tbi.query.model.QTDownloadPackage;

public class QueryToolWebserviceProvider extends RestAuthenticationProvider {

	private static final Logger log = LogManager.getLogger(QueryToolWebserviceProvider.class.getName());

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
	public QueryToolWebserviceProvider(String serverLocation, String proxyTicket) {

		super(serverLocation, proxyTicket);
	}

	/**
	 * FOR QUERY TOOL USE ONLY. MUST PASS ApplicationConstants.isWebservicesSecured() INTO THE THIRD ARG.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @param wsSecure
	 */
	public QueryToolWebserviceProvider(String serverLocation, String proxyTicket, boolean wsSecure) {
		super(serverLocation, proxyTicket, wsSecure);
	}

	public static List<byte[]> divideArray(byte[] source, int chunksize) {

		List<byte[]> result = new ArrayList<byte[]>();
		int start = 0;
		while (start < source.length) {
			int end = Math.min(source.length, start + chunksize);
			byte[] chunk = Arrays.copyOfRange(source, start, end);
			start += chunksize;
			result.add(chunk);
		}

		return result;
	}

	/**
	 * Sends the given download package to the query tool rest service See the QueryToolRestService.java class
	 * 
	 * @param qtDownloadPackage
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void sendDownloadPackage(QTDownloadPackage qtDownloadPackage,
			String path /* constants.getUploadDownloadPackageWebServiceURL() */) {
		HttpURLConnection http = null;
		OutputStream out = null;

		try {
			File file = File.createTempFile(qtDownloadPackage.getDirectoryName(), ".xml");
			file.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(file);

			JAXBContext jc = JAXBContext.newInstance(QTDownloadPackage.class);
			jc.createMarshaller().marshal(qtDownloadPackage, writer);
			writer.close();

			String filePath = file.getAbsolutePath();

			URL url = new URL(path + "?filePath=" + URLEncoder.encode(filePath, "UTF-8"));
			log.debug("Download package URL: " + url.toString());

			if (url.getProtocol().equals("https") == true) {
				http = (HttpsURLConnection) url.openConnection();
			} else {
				http = (HttpURLConnection) url.openConnection();
			}
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			http.connect();
			out = http.getOutputStream();

			if (http.getResponseCode() != 200) {
				log.error("HTTP toString: " + http.toString());
				log.error("Error Stream: " + http.getErrorStream() != null ? http.getErrorStream()
						.toString() : "Nothing");
				throw new QueryToolDownloadException();
			} else {
				log.trace("Sending of Download Package was successful.");
				log.trace("HTTP toString: " + http.toString());
			}

		} catch (JAXBException e) {
			e.printStackTrace();
			throw new QueryToolDownloadException();
		} catch (ProtocolException e1) {
			e1.printStackTrace();
			throw new QueryToolDownloadException();
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new QueryToolDownloadException();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			http.disconnect();
		}
	}

	/**
	 * Creates an access record for each key in this map (the value for each key is the number of records).
	 * 
	 * @param arMap
	 * @param assertion : Provided to make authenticated web service calls.
	 */
	// TODO: Fix proxy ticket. Had to remove proxy ticket attached to this webservice call because it would invalidate
	// with user's session timeout/logout.
	public void sendAccessRecordPackages(Map<Long, Long> arMap, String username,
			String path /* constants.getAccessRecordPackageWebServiceURL() */) {
		long queueDate = new Date().getTime();

		HttpURLConnection http = null;
		OutputStream out = null;
		
		String payload = new Gson().toJson(arMap);

		try {
			String urlString = path + "?time=" + queueDate + "&username=" + username;
			log.trace("accessReport URL: " + urlString);
			URL url = new URL(urlString);
			if (url.getProtocol().equals("https") == true) {
				http = (HttpsURLConnection) url.openConnection();
			} else {
				http = (HttpURLConnection) url.openConnection();
			}
			http.setRequestMethod("POST");
			http.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);
			http.setDoOutput(true);
			http.connect();
			out = http.getOutputStream();
			out.write(payload.getBytes());

			if (http.getResponseCode() != 200) {
				log.error("HTTP toString: " + http.toString());
				log.error("Error Stream: " + http.getErrorStream() != null ? http.getErrorStream()
						.toString() : "Nothing");
				throw new QueryToolDownloadException();
			} else {
				log.trace("AccessRecord Creation was successful.");
				log.trace("HTTP toString: " + http.toString());
			}
		} catch (MalformedURLException e) {
			log.error(e);
			throw new QueryToolDownloadException();
		} catch (IOException e) {
			log.error(e);
			throw new QueryToolDownloadException();

		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (http != null) {
				http.disconnect();
			}
		}
	}


	public boolean generateTriplanarImages(String originalPath, String originalFile, String newPath, String newFile,
			String wsPath /* constants.getTriplanarImageGenerateWebServiceUrl() */,
			String accountUrl /* constants.getModulesAccountURL() */)
			throws TriplanarException, UnsupportedEncodingException {

		WebClient client = WebClient.create(wsPath);

		HTTPConduit http = WebClient.getConfig(client).getHttpConduit();
		HTTPClientPolicy policy = http.getClient();
		policy.setConnectionTimeout(1000000);
		policy.setReceiveTimeout(1000000);

		originalPath = originalPath.replaceAll(" ", "\\ ").replaceAll("'", "\\'");
		Form form = new Form();
		log.info("This is the image file right now " + originalFile);

		form.param("originalPath", originalPath);
		form.param("originalFile", originalFile);
		form.param("newPath", newPath);
		form.param("newFile", newFile);
		form.param("host", accountUrl);

		client.type(MediaType.APPLICATION_FORM_URLENCODED);

		Response response = client.post(form);

		log.info("generateTriplanarImages web service call returns status code " + response.getStatus());

		if (response.getStatus() >= 400) {
			throw new TriplanarException(
					"generateTriplanarImages web service call failed status code " + response.getStatus());
		}

		return true;
	}

}
