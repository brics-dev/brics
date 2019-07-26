package main.java.dataimport;

import java.io.File;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class ImportTest {
	private static final Logger logger = Logger.getLogger(ImportTest.class);

	public static void main(String[] args) {
		logger.info("Testing the data submission directly...");
		ImportTest.localTest();

		logger.info("Testing the data submission on the server...");
		ImportTest.serverTest();
	}

	private static void localTest() {
		try {
			ImportService localService = new ImportService();
			//String COMMON_PROPERTIES_LOCATION = "C:/brics/common.properties";
			//String MODULES_PROPERTIES_LOCATION = "C:/brics/modules.properties";

			//localService.COMMON_PROPERTIES_LOCATION = COMMON_PROPERTIES_LOCATION;
			//localService.MODULES_PROPERTIES_LOCATION = MODULES_PROPERTIES_LOCATION;

			// Run the test
			String inputFilePath = "C:/TEMP/testFiles/SubTest2.csv";
			String output =
					localService.postDataSubmission(FileUtils.readFileToString(new File(inputFilePath)), false, null);

			logger.info("Status of the submission: " + output);
		} catch (Exception e) {
			logger.error("Local test failed.", e);
		}
	}

	private static void serverTest() {
		try {
			String url = "http://pdbp-host-stage.cit.nih.gov:8081/import-RESTful/rest/DataSubmission/csvInputStream";
			File inputFile = new File("C:/TEMP/testFiles/SubTest.csv");
			Client client = ClientBuilder.newClient();
			WebTarget wt = client.target(url);
			Entity<String> entity = Entity.entity(FileUtils.readFileToString(inputFile), MediaType.TEXT_PLAIN);

			// POST over the test file to the server.
			logger.info("POSTing over the test file at " + inputFile.getAbsolutePath() + "...");
			String response = wt.request(MediaType.TEXT_PLAIN).post(entity, String.class);

			// Read the response out to the log.
			logger.info("Response from Server: " + response);
		} catch (Exception e) {
			logger.error("Server Test failed.", e);
		}
	}
}
