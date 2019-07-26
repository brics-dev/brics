package gov.nih.tbi.ws.provider;

import java.io.UnsupportedEncodingException;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class RestRepositoryProvider extends RestAuthenticationProvider {
	private static final Logger log = LogManager.getLogger(RestRepositoryProvider.class);

	public RestRepositoryProvider(String serverLocation, String proxyTicket) {

		super(serverLocation, proxyTicket);
	}

	/**
	 * FOR QUERY TOOL USE ONLY. MUST PASS
	 * ApplicationConstants.isWebservicesSecured() INTO THE THIRD ARG.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @param wsSecure
	 */
	public RestRepositoryProvider(String serverLocation, String proxyTicket, boolean wsSecure) {
		super(serverLocation, proxyTicket, wsSecure);
	}

	/**
	 * Returns the SFTP file path of the given file name, dataset, and study
	 * 
	 * @param path
	 * @param studyPrefixedId
	 * @param datasetName
	 * @param fileName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getDatasetFilePath(String path, String studyPrefixedId, String datasetName, String fileName)
			throws UnsupportedEncodingException {

		log.info("Dataset File Path Webservice: " + path);

		WebClient client = createWebClient(path);

		// add query parameters
		client.query("ticket", getEncodedTicket());
		
		studyPrefixedId = studyPrefixedId.replace("\\'", "'");
		
		client.query("studyPrefixedId", studyPrefixedId);
		client.query("datasetName", datasetName);
		client.query("fileName", fileName);

		String datasetFilePath = client.post(null, String.class);

		return datasetFilePath;
	}
}
