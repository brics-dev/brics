package gov.nih.tbi.doi.ws;

import java.io.StringReader;
import java.io.StringWriter;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import gov.nih.tbi.doi.model.OSTIRecord;
import gov.nih.tbi.doi.model.OSTIResponse;
import gov.nih.tbi.doi.model.OSTIRestServiceModel.DoiRecordList;
import gov.nih.tbi.doi.model.OSTIRestServiceModel.DoiResponseList;

public class MockRequestHandler implements ExpectationCallback {
	private static final Integer SUCCESS_CODE = Integer.valueOf(200);
	private static final Integer INTERNAL_ERROR_CODE = Integer.valueOf(500);

	private String errorMsg;

	public MockRequestHandler() {}

	@Override
	public HttpResponse handle(HttpRequest request) {
		HttpResponse response = null;

		try {
			String body = request.getBodyAsString();
			StringReader strReader = new StringReader(body);
			JAXBContext jc = JAXBContext.newInstance(DoiRecordList.class);
			Unmarshaller um = jc.createUnmarshaller();
			DoiRecordList drl = (DoiRecordList) um.unmarshal(strReader);
			OSTIRecord rec = drl.getList().get(0);
			OSTIResponse recResponse = new OSTIResponse();

			// Set common response fields.
			recResponse.setTitle(rec.getTitle());
			recResponse.setProjectNumbers(rec.getProjectNumbers());
			recResponse.setContractNumbers(rec.getContractNumbers());

			// Check if the record is valid.
			if (isRecordValid(rec)) {
				// Create success response.
				recResponse.setStatus("SUCCESS Record added");
				recResponse.setStatusMessage("");

				String newDoi =
						rec.getDoiInfix() != null ? "10.5439/" + rec.getDoiInfix() + "/1035366" : "10.5439/1035366";
				recResponse.setDoi(newDoi);
				recResponse.setOstiId(Long.valueOf(1035366));

				response = genResponse(recResponse, MockRequestHandler.SUCCESS_CODE);
			} else {
				// Create error response.
				recResponse.setStatus("FAILURE: " + errorMsg);
				recResponse.setDoi("");
				recResponse.setOstiId(Long.valueOf(0));

				response = genResponse(recResponse, MockRequestHandler.SUCCESS_CODE);
			}
		} catch (JAXBException je) {
			String msg = "XML Translation error: " + je.getMessage();
			response = HttpResponse.response().withStatusCode(MockRequestHandler.INTERNAL_ERROR_CODE).withBody(msg);
		}

		return response;
	}

	private boolean isRecordValid(OSTIRecord rec) {

		// Check the required data fields.
		//// Validate the "title" field.
		if ((rec.getTitle() == null) || rec.getTitle().isEmpty()) {
			errorMsg = "The title is required.";
			return false;
		}

		//// Validate the "creators" field.
		if ((rec.getCreators() == null) || rec.getCreators().isEmpty()) {
			errorMsg = "The creators are required.";
			return false;
		}

		//// Validate the "publisher" field.
		if ((rec.getPublisher() == null) || rec.getPublisher().isEmpty()) {
			errorMsg = "The publisher is required.";
			return false;
		}

		//// Validate the "publication_date" field.
		if ((rec.getPublicationDate() == null) || rec.getPublicationDate().isEmpty()) {
			errorMsg = "The publication date is required.";
			return false;
		}
		
		//// Validate the "site_url" field.
		if ((rec.getLandingPageUrl() == null) || rec.getLandingPageUrl().isEmpty()) {
			if (rec.getSetReserved() == null) {
				errorMsg = "The site URL is required.";
				return false;
			}
		}
		else if (rec.getSetReserved() != null) {
			errorMsg = "The site URL cannot be included in reserved DOIs.";
			return false;
		}

		//// Validate the "product_type" field.
		if (rec.getProductType() == null) {
			errorMsg = "The product type is required.";
			return false;
		}

		//// Validate the "product_type_specific" field.
		if ((rec.getProductTypeSpecific() == null) || rec.getProductTypeSpecific().isEmpty()) {
			errorMsg = "The product type specific field is required.";
			return false;
		}

		//// Validate the "contact_name" field.
		if ((rec.getContactName() == null) || rec.getContactName().isEmpty()) {
			errorMsg = "The contact name is required.";
			return false;
		}

		//// Validate the "contact_org" field.
		if ((rec.getContactOrganization() == null) || rec.getContactOrganization().isEmpty()) {
			errorMsg = "The contact originazation is required.";
			return false;
		}

		//// Validate the "contact_email" field.
		if ((rec.getContactEmail() == null) || rec.getContactEmail().isEmpty()) {
			errorMsg = "The contact email is required.";
			return false;
		}

		//// Validate the "contact_phone" field.
		if ((rec.getContactPhone() == null) || rec.getContactPhone().isEmpty()) {
			errorMsg = "The contact phone is required.";
			return false;
		}

		return true;
	}

	/**
	 * Constructs a mock response object for the given OSTI response and error code.
	 * 
	 * @param recResponse - The OSTI response object.
	 * @param statusCode - The status code for the response.
	 * @return A mock response object containing the XML version of the given OSTI response object.
	 * @throws JAXBException When there is an error in translating the OSTI response object to XML.
	 */
	private HttpResponse genResponse(OSTIResponse recResponse, Integer statusCode) throws JAXBException {
		DoiResponseList responseList = new DoiResponseList();
		responseList.add(recResponse);

		JAXBContext jc = JAXBContext.newInstance(DoiResponseList.class);
		Marshaller m = jc.createMarshaller();
		StringWriter xmlStrWriter = new StringWriter();
		m.marshal(responseList, xmlStrWriter);
		String xmlStr = xmlStrWriter.toString();

		return HttpResponse.response().withStatusCode(statusCode)
				.withHeaders(new Header("Content-Type", MediaType.APPLICATION_XML + "; charset=utf-8"))
				.withBody(xmlStr);
	}

}
