package gov.nih.tbi.doi.ws;

import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.ws.rs.WebApplicationException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import gov.nih.tbi.commons.model.BricsInstanceType;
import gov.nih.tbi.commons.model.exceptions.DoiWsValidationException;
import gov.nih.tbi.doi.model.OSTIRecord;

public class DoiWSTest {
	private static final String DOI_DATA_FILE_ROOT = "src/test/resources/doi/";
	private static final String DOI_WS_URL = "https://www.osti.gov/iad2test/api/records/legacyxml";
	private static final String DOI_USERNAME = "abcd";
	private static final String DOI_PASSWORD = "abcd";

	private static JsonParser jsonParser;

	@BeforeClass
	public static void setUp() {
		// Initialize the JSON parser.
		jsonParser = new JsonParser();
	}

	@Test
	public void createDoiSmokeTest() throws JsonParseException, FileNotFoundException {
		DoiMinterProvider mintClient = new DoiMinterProvider(DOI_WS_URL, DOI_USERNAME, DOI_PASSWORD);
		// DoiMinterProvider mintClient = new DoiMinterTesterProvider(DOI_WS_URL, DOI_USERNAME, DOI_PASSWORD);
		OSTIRecord record = new OSTIRecord();
		JsonObject data =
				jsonParser.parse(new FileReader(DOI_DATA_FILE_ROOT + "Create_Record_Seed_Data.json")).getAsJsonObject();

		// Populate the record object.
		DoiCommonUtil.applyRequiredData(record, data);
		record.setDoiInfix(BricsInstanceType.PDBP);

		// Try to create the test DOI.
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

	@Test
	public void createDoiErrorTest() {
		DoiMinterProvider mintClient = new DoiMinterProvider(DOI_WS_URL, DOI_USERNAME, DOI_PASSWORD);
		// DoiMinterProvider mintClient = new DoiMinterTesterProvider(DOI_WS_URL, DOI_USERNAME, DOI_PASSWORD);
		OSTIRecord record = new OSTIRecord();

		// Construct incomplete record.
		record.setDoiInfix(BricsInstanceType.PDBP);
		record.setTitle("Bad Record");

		try {
			mintClient.saveDoiToIad(record);
		} catch (WebApplicationException wae) {
			Assert.fail("Got a web service error while testing the IAD service.", wae);
		} catch (DoiWsValidationException iae) {
			return;
		} catch (Exception e) {
			Assert.fail("Unexpected error while testing the IAD service.", e);
		}

		Assert.fail("No error was thrown for this invalid DOI record.");
	}

	@Test
	public void specialCharTest() throws JsonParseException, FileNotFoundException {
		DoiMinterProvider mintClient = new DoiMinterProvider(DOI_WS_URL, DOI_USERNAME, DOI_PASSWORD);
		// DoiMinterProvider mintClient = new DoiMinterTesterProvider(DOI_WS_URL, DOI_USERNAME, DOI_PASSWORD);
		OSTIRecord record = new OSTIRecord();
		JsonObject data =
				jsonParser.parse(new FileReader(DOI_DATA_FILE_ROOT + "Create_Record_Seed_Data.json")).getAsJsonObject();

		// Populate the record object.
		DoiCommonUtil.applyRequiredData(record, data);
		record.setDoiInfix(BricsInstanceType.PDBP);

		// Add in special character info.
		record.setTitle("The NINDS Parkinson\u2019s Disease Biomarkers Program");
		record.setDescription("The NINDS Parkinson\u2019s organization studies multiple diseases like: "
				+ "blah, foo, zah \u2026 The bi-directional \u2013 sample: <*>#=./");

		// Try to create the test DOI.
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
}
