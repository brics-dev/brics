package gov.nih.tbi.doi.ws;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;

import gov.nih.tbi.commons.model.exceptions.DoiWsValidationException;
import gov.nih.tbi.doi.model.OSTIRecord;
import gov.nih.tbi.doi.model.OSTIResponse;
import gov.nih.tbi.doi.model.OSTIRestServiceModel.DoiRecordList;
import gov.nih.tbi.doi.model.OSTIRestServiceModel.DoiResponseList;

public class DoiMinterProvider {
	private String serviceUrl;
	private String username;
	private String password;

	/**
	 * Primary constructor for this provider. Used to initialize the base URL to the IAD web services, and the
	 * credentials needed to log into that service.
	 * 
	 * @param serviceUrl - The fully qualified URL for the IAD web service.
	 * @param username - The IAD username needed to connect to the web service.
	 * @param password - The password for the IAD web service account.
	 */
	public DoiMinterProvider(String serviceUrl, String username, String password) {
		this.serviceUrl = serviceUrl;
		this.username = username;
		this.password = password;
	}

	/**
	 * Mints a DOI for the given record by attempting to create a new OSTI record via the IAD web service. If successful
	 * the given OSTIRecord object will be updated with the returned DOI and OSTI ID number.
	 * 
	 * @param record - The record data that will be sent to OSTI for DOI creation.
	 * @throws WebApplicationException When the IAD web service returns an error response without a readable XML body.
	 * @throws DoiWsValidationException When the status from the IAD web servers came back as "FAILURE." The reason for
	 *         the failure will be in the exception message.
	 */
	public void saveDoiToIad(OSTIRecord record) throws WebApplicationException, DoiWsValidationException {
		WebClient client = WebClient.create(serviceUrl, username, password, null);
		DoiRecordList records = new DoiRecordList();
		DoiResponseList responseList = null;

		records.add(record);

		responseList = client.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML + "; charset=utf-8")
				.post(records, DoiResponseList.class);

		OSTIResponse response = responseList.getList().get(0);

		// Check if the response is in the IAD v1 format.
		if (!StringUtils.isEmpty(response.getStatus())) {
			if (response.getStatus().contains("SUCCESS")) {
				// Record the new DOI and OSTI ID.
				record.setDoi(response.getDoi());
				record.setOstiId(response.getOstiId());
			} else {
				throw new DoiWsValidationException(response.getStatus());
			}
		}
		// The response is in the IAD v2 format.
		else {
			if (!StringUtils.isEmpty(response.getDoi()) && response.getId() != null) {
				// Record the new DOI and OSTI ID.
				record.setDoi(response.getDoi());
				record.setOstiId(response.getId());
			} else {
				throw new DoiWsValidationException("Couldn't create a DOI because of a server error.");
			}
		}
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
