package gov.nih.tbi.doi.ws;

import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.socket.PortFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import gov.nih.tbi.commons.model.BricsInstanceType;
import gov.nih.tbi.commons.model.exceptions.DoiWsValidationException;
import gov.nih.tbi.doi.model.OSTIRecord;

public class DoiMockWSTest {
	private static final String DOI_DATA_FILE_ROOT = "src/test/resources/doi/";
	private static final String WS_PATH = "/iadtest/dataapi";

	private static int port;
	private static MockServerClient mockServer;
	private static JsonParser jsonParser;

	@BeforeClass
	public static void setUp() {
		port = PortFactory.findFreePort();
		mockServer = ClientAndServer.startClientAndServer(port);

		// Configure mocked server.
		HttpRequest request = HttpRequest.request().withMethod("POST").withPath(WS_PATH)
				.withHeader("Content-Type", MediaType.APPLICATION_XML + "; charset=utf-8");
		HttpCallback callback = HttpCallback.callback().withCallbackClass(MockRequestHandler.class.getName());

		mockServer.when(request).callback(callback);

		// Initialize the JSON parser.
		jsonParser = new JsonParser();
	}

	@AfterClass
	public static void cleanUp() {
		mockServer.stop();
	}

	@Test
	public void createDoiSmokeTest() throws JsonParseException, FileNotFoundException {
		JsonObject data =
				jsonParser.parse(new FileReader(DOI_DATA_FILE_ROOT + "Create_Record_Seed_Data.json")).getAsJsonObject();
		OSTIRecord record = new OSTIRecord();

		// Populate the record object.
		DoiCommonUtil.applyRequiredData(record, data);
		record.setDoiInfix(BricsInstanceType.PDBP);

		// Register the DOI with the mocked server.
		DoiMinterProvider mintClient =
				new DoiMinterProvider("http://localhost:" + port + WS_PATH, "brics", "Pa$$w0rd");

		try {
			mintClient.saveDoiToIad(record);

			// Test DOI.
			Assert.assertTrue((record.getOstiId() != null) && (record.getOstiId() > 0),
					"The returned OSTI ID(" + record.getOstiId() + ") is not valid.");
			Assert.assertTrue(
					(record.getDoi() != null) && !record.getDoi().isEmpty()
							&& record.getDoi().matches(DoiCommonUtil.DOI_REGEX),
					"The returned DOI (" + record.getDoi() + ") is not valid.");
		} catch (WebApplicationException wae) {
			Assert.fail("Got a web service error while creating a DOI.", wae);
		} catch (Exception e) {
			Assert.fail("Error when creating a DOI.", e);
		}
	}

	@Test()
	public void createDoiErrorTest() {
		OSTIRecord record = new OSTIRecord();

		// Construct incomplete record.
		record.setDoiInfix(BricsInstanceType.PDBP);
		record.setTitle("Bad Record");

		// Register the DOI with the mocked server.
		DoiMinterProvider mintClient =
				new DoiMinterProvider("http://localhost:" + port + WS_PATH, "brics", "Pa$$w0rd");

		try {
			mintClient.saveDoiToIad(record);
		} catch (WebApplicationException wae) {
			Assert.fail("Got a web service error.", wae);
		} catch (DoiWsValidationException iae) {
			return;
		}
		catch (Exception e) {
			Assert.fail("Got an unexpected error.", e);
		}

		Assert.fail("No error was thrown for this invalid DOI record.");
	}
}
