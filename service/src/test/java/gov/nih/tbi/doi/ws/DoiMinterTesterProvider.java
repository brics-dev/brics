package gov.nih.tbi.doi.ws;

import java.io.StringReader;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.cxf.jaxrs.client.WebClient;

import gov.nih.tbi.doi.model.OSTIRecord;
import gov.nih.tbi.doi.model.OSTIResponse;
import gov.nih.tbi.doi.model.OSTIRestServiceModel.DoiRecordList;
import gov.nih.tbi.doi.model.OSTIRestServiceModel.DoiResponseList;

public class DoiMinterTesterProvider extends DoiMinterProvider {
	private static final Logger logger = Logger.getLogger(DoiMinterTesterProvider.class.getName());

	public DoiMinterTesterProvider(String serviceUrl, String username, String password) {
		super(serviceUrl, username, password);
	}

	@Override
	public void saveDoiToIad(OSTIRecord record) throws WebApplicationException, IllegalArgumentException {
		WebClient client = WebClient.create(getServiceUrl(), getUsername(), getPassword(), null);
		DoiRecordList records = new DoiRecordList();

		records.add(record);

		String xmlResponse = client.accept(MediaType.APPLICATION_XML)
				.type(MediaType.APPLICATION_XML + "; charset=utf-8").post(records, String.class);

		// Try to read the response from the IAD web service call.
		OSTIResponse response = null;

		try {
			JAXBContext jc = JAXBContext.newInstance(DoiResponseList.class);
			Unmarshaller um = jc.createUnmarshaller();
			DoiResponseList responseList = (DoiResponseList) um.unmarshal(new StringReader(xmlResponse));

			response = responseList.getList().get(0);
		} catch (JAXBException je) {
			logger.severe("Failed reading the returned XML: " + xmlResponse);
			throw new WebApplicationException("Couldn't read the reponse returned from the IAD server", je);
		}

		// Process the response POJO.
		if (response.getStatus().contains("SUCCESS")) {
			// Record the new DOI and OSTI ID.
			record.setDoi(response.getDoi());
			record.setOstiId(response.getOstiId());
		} else {
			throw new IllegalArgumentException(response.getStatus() + response.getStatusMessage());
		}
	}

}
