package gov.nih.tbi.doi.ws;

import com.google.gson.JsonObject;

import gov.nih.tbi.doi.model.OSTIProductType;
import gov.nih.tbi.doi.model.OSTIRecord;

public class DoiCommonUtil {
	public static final String DOI_REGEX = "10\\.[0-9.]+/\\w+/\\d+";

	/**
	 * Apply data from the JSON object to the required fields of the OSTI record object.
	 * 
	 * @param record - The OSTIRecord object to apply the data to.
	 * @param data - The JSON object that contains the source data.
	 */
	public static synchronized void applyRequiredData(OSTIRecord record, JsonObject data) {
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

}
