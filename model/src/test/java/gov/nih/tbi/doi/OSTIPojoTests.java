package gov.nih.tbi.doi;

import org.testng.Assert;
import org.testng.annotations.Test;

import gov.nih.tbi.doi.model.OSTIRecord;

public class OSTIPojoTests {

	@Test
	public void descriptionTest() {
		OSTIRecord record = new OSTIRecord();

		// Set a description that is within the allowable character length limit.
		try {
			record.setDescription("This record description is legal.");
		} catch (Exception e) {
			Assert.fail("Unexpected error when setting an allowable description.", e);
		}

		// Set a description that is beyond the character limit.
		boolean isValid = true;
		int strLength = OSTIRecord.DOI_DESCRIPTION_MAX_LENGTH + 1;
		StringBuffer longDesc = new StringBuffer(strLength);

		for (int i = 0; i < strLength; i++) {
			longDesc.append("A");
		}

		try {
			record.setDescription(longDesc.toString());
		} catch (Exception e) {
			isValid = false;
		}

		Assert.assertFalse(isValid, "A description with a length greater than the limit of "
				+ OSTIRecord.DOI_DESCRIPTION_MAX_LENGTH + " characters was accepted.");
	}

	@Test
	public void publicationDateTest() {
		OSTIRecord record = new OSTIRecord();

		// Set a valid date in the correct format.
		try {
			record.setPublicationDate("03/01/2017");
		} catch (Exception e) {
			Assert.fail("Unexpected error when setting a valid publication date.", e);
		}

		// Set a valid date, but is not in the correct format.
		boolean isValid = true;

		try {
			record.setPublicationDate("3/1/17");
		} catch (Exception e) {
			isValid = false;
		}

		Assert.assertFalse(isValid, "A date in an invalid format was accepted.");

		// Set a bunch of numbers, but the numbers are in the write format.
		isValid = true;

		try {
			record.setPublicationDate("45/93/9584");
		} catch (Exception e) {
			isValid = false;
		}

		Assert.assertFalse(isValid, "An invalid date, which is formatted correctly was accepted.");

		// Set a string that is clearly not a date.
		isValid = true;

		try {
			record.setPublicationDate("This is not a date.");
		} catch (Exception e) {
			isValid = false;
		}

		Assert.assertFalse(isValid, "A non-date string was accepted as a valid date.");
	}

}
