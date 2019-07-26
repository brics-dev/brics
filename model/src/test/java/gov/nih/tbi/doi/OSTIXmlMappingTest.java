package gov.nih.tbi.doi;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.nih.tbi.doi.model.OSTIProductType;
import gov.nih.tbi.doi.model.OSTIRecord;
import gov.nih.tbi.doi.model.OSTIRelatedIdentifierDetail;
import gov.nih.tbi.doi.model.OSTIRelationType;
import gov.nih.tbi.doi.model.OSTIRestServiceModel.DoiRecordList;
import gov.nih.tbi.doi.model.OSTIRestServiceModel.DoiResponseList;

public class OSTIXmlMappingTest {
	private static final String DOI_DATA_FILE_ROOT = "src/test/resources/doi/";

	private static Unmarshaller recordListUnmarshaller;
	private static Unmarshaller responseUnmarshaller;
	private static Marshaller recordListMarshaller;
	private static Marshaller responseMarshaller;
	private static JsonParser jsonParser;

	@BeforeClass
	public static void setUp() throws Exception {
		JAXBContext jc = JAXBContext.newInstance(DoiRecordList.class);

		recordListUnmarshaller = jc.createUnmarshaller();
		recordListMarshaller = jc.createMarshaller();

		JAXBContext jc2 = JAXBContext.newInstance(DoiResponseList.class);

		responseUnmarshaller = jc2.createUnmarshaller();
		responseMarshaller = jc2.createMarshaller();

		jsonParser = new JsonParser();
	}

	@Test
	public void doiRecordReadSmokeTest() throws Exception {
		File xmlCreateFile = new File(DOI_DATA_FILE_ROOT + "OSTI-DOI-Create_Example-1.xml");
		File xmlUpdateFile = new File(DOI_DATA_FILE_ROOT + "OSTI-DOI-Update_Example-1.xml");

		performCreateUpdateReadTests(xmlCreateFile, xmlUpdateFile);
	}

	@Test
	public void doiRecordReadFullTest() throws Exception {
		File xmlCreateFile = new File(DOI_DATA_FILE_ROOT + "OSTI-DOI-Full_Create_Example-1.xml");
		File xmlUpdateFile = new File(DOI_DATA_FILE_ROOT + "OSTI-DOI-Full_Update_Example-1.xml");

		performCreateUpdateReadTests(xmlCreateFile, xmlUpdateFile);
	}

	@Test
	public void doiRecordReadReserveTest() throws Exception {
		File xmlFile = new File(DOI_DATA_FILE_ROOT + "OSTI-DOI-Reserved_Example-1.xml");
		DoiRecordList records = (DoiRecordList) recordListUnmarshaller.unmarshal(xmlFile);
		Element orgRecord = Jsoup.parse(xmlFile, "UTF-8").select("record").first();

		// Test if an object was created from the create XML file.
		Assert.assertNotNull(records);

		// Create a DOM object from the create DoiRequestList object.
		StringWriter xmlStrWriter = new StringWriter();
		recordListMarshaller.marshal(records, xmlStrWriter);
		String newXmlStr = xmlStrWriter.toString();
		Element newRecord = Jsoup.parse(newXmlStr).select("record").first();

		Assert.assertTrue(isOstiXmlEqual(orgRecord, newRecord),
				"The unmarshalled Java object doesn't equal the original reserve XML file.");
	}

	@Test
	public void doiResponseReadSmokeTest() throws Exception {
		File successXmlFile = new File(DOI_DATA_FILE_ROOT + "OSTI-DOI-Response-Success_Example-1.xml");
		File failureXmlFile = new File(DOI_DATA_FILE_ROOT + "OSTI-DOI-Response-Failure_Example-1.xml");

		// ****** Test using the success response XML markup. ******
		DoiResponseList response = (DoiResponseList) responseUnmarshaller.unmarshal(successXmlFile);
		Element orgResp = Jsoup.parse(successXmlFile, "UTF-8").select("record").first();

		// Test if an object was created from the success XML file.
		Assert.assertNotNull(response);

		// Create a DOM object from the success response object.
		StringWriter xmlStrWriter = new StringWriter();
		responseMarshaller.marshal(response, xmlStrWriter);
		String newXmlStr = xmlStrWriter.toString();
		Element newResp = Jsoup.parse(newXmlStr).select("record").first();

		Assert.assertTrue(isOstiXmlEqual(orgResp, newResp),
				"The unmarshalled Java object doesn't equal the original success reponse XML file.");
		// ****** End of testing the success response XML markup. ******

		// ****** Test using the update record XML markup. ******
		response = (DoiResponseList) responseUnmarshaller.unmarshal(failureXmlFile);
		orgResp = Jsoup.parse(failureXmlFile, "UTF-8").select("record").first();

		// Test if an object was created from the failure XML file.
		Assert.assertNotNull(response);

		// Create a DOM object from the failure response object.
		xmlStrWriter = new StringWriter();
		responseMarshaller.marshal(response, xmlStrWriter);
		newXmlStr = xmlStrWriter.toString();
		newResp = Jsoup.parse(newXmlStr).select("record").first();

		Assert.assertTrue(isOstiXmlEqual(orgResp, newResp),
				"The unmarshalled Java object doesn't equal the original failure reponse XML file.");
		// ****** End of testing the update XML markup. ******
	}

	@Test
	public void doiRecordWriteSmokeTest() throws Exception {
		JsonObject data =
				jsonParser.parse(new FileReader(DOI_DATA_FILE_ROOT + "Create_Record_Smoke_Test-Seed_Data.json")).getAsJsonObject();
		OSTIRecord record = new OSTIRecord();

		// Add in the required data.
		applyRequiredData(record, data);

		// Add in optional data.
		record.setProjectNumbers(data.getAsJsonPrimitive("product_nos").getAsString());
		record.setDescription(data.getAsJsonPrimitive("description").getAsString());
		record.setContractNumbers(data.getAsJsonPrimitive("contract_nos").getAsString());

		//// Apply related identifiers listing.
		List<OSTIRelatedIdentifierDetail> identifiers = new ArrayList<OSTIRelatedIdentifierDetail>();

		for (JsonElement e : data.getAsJsonArray("relatedidentifiersblock")) {
			JsonObject jRId = e.getAsJsonObject();
			OSTIRelatedIdentifierDetail ri = new OSTIRelatedIdentifierDetail();

			ri.setRelatedIdentifier(jRId.getAsJsonPrimitive("related_identifier").getAsString());
			ri.setIdentifierType(jRId.getAsJsonPrimitive("related_identifier_type").getAsString());
			ri.setRelationType(OSTIRelationType.getByVocabTerm(jRId.getAsJsonPrimitive("relation_type").getAsString()));

			identifiers.add(ri);
		}

		record.setRelatedIdentifiers(identifiers);

		// Compare the new object with what the XML output should be.
		compareRecordWithFile(record, new File(DOI_DATA_FILE_ROOT + "OSTI-DOI-Write_Example-1.xml"));
	}

	@Test
	public void invalidXmlCharTest() throws Exception {
		JsonObject data =
				jsonParser.parse(new FileReader(DOI_DATA_FILE_ROOT + "Create_Record_Smoke_Test-Seed_Data.json"))
						.getAsJsonObject();
		OSTIRecord record = new OSTIRecord();

		// Add in the required data.
		applyRequiredData(record, data);
		record.setTitle("Henry & Jenson\u2019s Foundation Study");

		// Add in optional data.
		record.setDescription("The Henry & Jenson\u2019s Foundation studies multiple diseases like: "
				+ "blah, foo, zah \u2026 The bi-directional \u2013 sample: <*>#=./");

		// Try to marshall to XML.
		DoiRecordList records = new DoiRecordList();
		String newXmlStr = "";

		records.add(record);

		try {
			StringWriter xmlStrWriter = new StringWriter();
			recordListMarshaller.marshal(records, xmlStrWriter);
			newXmlStr = xmlStrWriter.toString();
		} catch (JAXBException e) {
			Assert.fail("Couldn't marshall record to XML.", e);
		}

		Assert.assertFalse(newXmlStr.isEmpty(), "No XML output detected.");
		records.getList().clear();

		// Try to unmarshall the XML string.
		try {
			StringReader xmlStrReader = new StringReader(newXmlStr);
			records = (DoiRecordList) recordListUnmarshaller.unmarshal(xmlStrReader);
		} catch (JAXBException e) {
			Assert.fail("Couldn't unmarshall XML to DoiRecordList object.", e);
		}

		Assert.assertFalse((records == null) || records.getList().isEmpty(), "No record object detected.");
	}

	/**
	 * Apply data from the JSON object to the required fields of the OSTI record object.
	 * 
	 * @param record - The OSTIRecord object to apply the data to.
	 * @param data - The JSON object that contains the source data.
	 */
	private void applyRequiredData(OSTIRecord record, JsonObject data) {
		record.setTitle(data.getAsJsonPrimitive("title").getAsString());
		record.setCreators(data.getAsJsonPrimitive("creators").getAsString());
		record.setPublisher(data.getAsJsonPrimitive("publisher").getAsString());
		record.setPublicationDate(data.getAsJsonPrimitive("publication_date").getAsString());
		record.setLandingPageUrl(data.getAsJsonPrimitive("site_url").getAsString());
		record.setProductType(OSTIProductType.getByVocabTerm(data.getAsJsonPrimitive("product_type").getAsString()));
		record.setProductTypeSpecific(data.getAsJsonPrimitive("product_type_specific").getAsString());
		record.setContactName(data.getAsJsonPrimitive("contact_name").getAsString());
		record.setContactOrganization(data.getAsJsonPrimitive("contact_org").getAsString());
		record.setContactEmail(data.getAsJsonPrimitive("contact_email").getAsString());
		record.setContactPhone(data.getAsJsonPrimitive("contact_phone").getAsString());
	}

	/**
	 * Executes tests on the OSTI Record mapping objects to verify that they can read and write XML for creating a
	 * record, and updating an existing record. This test uses XML markup that is shown in the IAD specification
	 * document.
	 * 
	 * @param xmlCreateFile - The XML file for creating a DOI record.
	 * @param xmlUpdateFile - The XML file for updating a DOI record.
	 * @throws Exception If there are any errors.
	 */
	private void performCreateUpdateReadTests(File xmlCreateFile, File xmlUpdateFile) throws Exception {
		// ****** Test using the create record XML markup. ******
		DoiRecordList records = (DoiRecordList) recordListUnmarshaller.unmarshal(xmlCreateFile);
		Element orgRecord = Jsoup.parse(xmlCreateFile, "UTF-8").select("record").first();

		// Test if an object was created from the create XML file.
		Assert.assertNotNull(records);

		// Create a DOM object from the create DoiRequestList object.
		StringWriter xmlStrWriter = new StringWriter();
		recordListMarshaller.marshal(records, xmlStrWriter);
		String newXmlStr = xmlStrWriter.toString();
		Element newRecord = Jsoup.parse(newXmlStr).select("record").first();

		Assert.assertTrue(isOstiXmlEqual(orgRecord, newRecord),
				"The unmarshalled Java object doesn't equal the original create XML file.");
		// ****** End of testing the create XML markup. ******

		// ****** Test using the update record XML markup. ******
		records = (DoiRecordList) recordListUnmarshaller.unmarshal(xmlUpdateFile);
		orgRecord = Jsoup.parse(xmlUpdateFile, "UTF-8").select("record").first();

		// Test if an object was created from the update XML file.
		Assert.assertNotNull(records);

		// Create a DOM object from the update DoiRequestList object.
		xmlStrWriter = new StringWriter();
		recordListMarshaller.marshal(records, xmlStrWriter);
		newXmlStr = xmlStrWriter.toString();
		newRecord = Jsoup.parse(newXmlStr).select("record").first();

		Assert.assertTrue(isOstiXmlEqual(orgRecord, newRecord),
				"The unmarshalled Java object doesn't equal the original update XML file.");
		// ****** End of testing the update XML markup. ******
	}

	private void compareRecordWithFile(OSTIRecord record, File xmlFile) throws Exception {
		DoiRecordList recordList = new DoiRecordList();

		// Create a DOM object from the record object
		recordList.add(record);
		StringWriter xmlStrWriter = new StringWriter();
		recordListMarshaller.marshal(recordList, xmlStrWriter);
		String newXmlStr = xmlStrWriter.toString();
		Assert.assertFalse(newXmlStr.isEmpty(), "The record object couldn't marshall into a XML string.");
		Element newRecord = Jsoup.parse(newXmlStr).select("record").first();

		// Create DOM from the XML file.
		Element orgRecord = Jsoup.parse(xmlFile, "UTF-8").select("record").first();

		Assert.assertTrue(isOstiXmlEqual(orgRecord, newRecord),
				"The constructed Java object doesn't equal the target XML file.");
	}

	/**
	 * Compares the XML markup from two OSTI record objects, and checks if they contain the same data. The two records
	 * are considered equal if they contain the same markup tags, and the data contained in those are also equal. The
	 * equality check is also done independent of the ordering of the markup tags.
	 * 
	 * @param r1 - The Element object of the first OSTI record.
	 * @param r2 - The Element object of the second OSTI record.
	 * @return True if and only if the two record Element objects contain the same markup and data.
	 */
	private boolean isOstiXmlEqual(Element r1, Element r2) {
		// Check if the two elements are the same object, or are both null.
		if (r1 == r2) {
			return true;
		}

		// Check if either element is null.
		if ((r1 != null) && (r2 == null)) {
			System.out.println("The second record is null, while the first is not.");
			return false;
		} else if (r1 == null) {
			System.out.println("The first record is null, while the second is not.");
			return false;
		}

		Elements firstChildren = r1.children();
		Elements secondChildren = r2.children();

		// Check if the two elements have the same number of children.
		if (firstChildren.size() != secondChildren.size()) {
			System.out.println("The two records have a different number of child elements.");
			return false;
		}

		// Check if each children element is equal
		for (Element c1 : firstChildren) {
			boolean found = false;

			for (Element c2 : secondChildren) {
				if (c1.tagName().equals(c2.tagName())) {
					found = true;

					// Check for the "relatedidentifiersblock" tag, and check its children for equality.
					if (c1.tagName().equals("relatedidentifiersblock")) {
						Elements rIds1 = c1.children();
						Elements rIds2 = c2.children();

						if (rIds1.size() != rIds2.size()) {
							System.out.println(
									"The two \"relatedidentifiersblock\" elements contain a different amount of children.");
							return false;
						}

						for (int i = 0; i < rIds1.size(); i++) {
							if (!isOstiXmlEqual(rIds1.get(i), rIds2.get(i))) {
								System.out.println(
										"The child of the \"relatedidentifiersblock\" is not equal to the other child.");
								return false;
							}
						}
					} else if (!c1.hasSameValue(c2)) {
						System.out.println("The values of the " + c1.tagName() + " tags are different: \"" + c1.text()
								+ "\" vs \"" + c2.text() + "\".");
						return false;
					}

					break;
				}
			}

			// Check if found.
			if (!found) {
				System.out.println("The " + c1.tagName() + " tag was not found in the over record.");
				return false;
			}
		}

		return true;
	}

}
