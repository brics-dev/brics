package gov.nih.tbi.repository.ws.cxf;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.repository.xml.exception.XmlValidationException;
import gov.nih.tbi.repository.xml.model.XmlInstanceMetaData;
import gov.nih.tbi.repository.xml.model.XmlValidationResponse;

@Path("/repository/")
public class RepositoryAPIRestService extends AbstractRestService {

	private static Logger logger = Logger.getLogger(RepositoryAPIRestService.class);

	@Autowired
	protected RepositoryManager repositoryManager;

	@POST
	@Consumes("text/xml")
	@Path("/validate")
	public Response validate(byte[] collection) {

		logger.info("Repository web service validation invoked");
		logger.debug(new String(collection));

		// Parse for Metadata

		XmlValidationResponse response = null;
		try {
			XmlInstanceMetaData meta;
			meta = repositoryManager.inspectXmlData(collection);

			// Web service call to dictionary.
			byte[] schemaBytes = repositoryManager.getDictionarySchema(meta.getFsName(),
					modulesConstants.getModulesDDTURL(getDiseaseId()));

			// Validate data
			response = repositoryManager.validateXmlData(collection, schemaBytes);
		} catch (XmlValidationException e) {
			XmlValidationResponse errorResponse = new XmlValidationResponse();
			errorResponse.setValid(false);
			errorResponse.setReason(e.getMessage());
			return Response.ok(errorResponse.toXMLString()).build();
		}

		if (response == null) {
			// Even with all our exception checking something still went wrong?
			XmlValidationResponse errorResponse = new XmlValidationResponse();
			errorResponse.setValid(false);
			errorResponse.setReason("An unknown exception has occured. Please contact the helpdesk.");
			return Response.ok(errorResponse.toXMLString()).build();
		}

		logger.info("Validation complete valid: " + response.isValid());
		if (logger.isDebugEnabled() && response.isValid() == false) {
			for (String e : response.getErrors()) {
				logger.debug(e);
			}
		}
		return Response.ok(response.toXMLString()).build();


	}

}
