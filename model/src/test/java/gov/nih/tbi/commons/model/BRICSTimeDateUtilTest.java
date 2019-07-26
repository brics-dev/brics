package gov.nih.tbi.commons.model;

import java.util.Date;

import org.testng.annotations.Test;

import junit.framework.Assert;

public class BRICSTimeDateUtilTest {

	@Test
	public void parseRepositoryDateTest() {
		String testString = "2018-03-13 11:50:11.984569";
		Date testOutput = BRICSTimeDateUtil.parseRepositoryDate(testString);
		String expectedString = "2018-03-13 11:50:11.9";
		Assert.assertEquals(expectedString, BRICSTimeDateUtil.repositoryDateToString(testOutput));
	}
}
