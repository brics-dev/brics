package gov.nih.tbi.commons.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SparqlConstructionUtilTest {

	@Test
	public void testRegexEscape() {
		final String TEST = "Full-time student (and unemployed)";
		final String EXPECTED = "Full-time student \\(and unemployed\\)";
		Assert.assertEquals(SparqlConstructionUtil.regexEscape(TEST), EXPECTED);
	}

	@Test
	public void testRegexEscape2() {
		final String TEST = "Tom's \"ice cream\"";
		final String EXPECTED = "Tom\\'s \\\"ice cream\\\"";
		Assert.assertEquals(SparqlConstructionUtil.regexEscape(TEST), EXPECTED);
	}
}
