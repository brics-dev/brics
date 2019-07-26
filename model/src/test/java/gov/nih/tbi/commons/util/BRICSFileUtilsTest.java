package gov.nih.tbi.commons.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BRICSFileUtilsTest {

	@Test
	public void testModifyFileExtension() {
		Assert.assertEquals(BRICSFilesUtils.modifyFileExtension("C:\\blabla\\gdagsdg.csv", ".xml"),
				"C:\\blabla\\gdagsdg.xml");
		Assert.assertEquals(BRICSFilesUtils.modifyFileExtension("testFile.csv", ".xml"), "testFile.xml");
	}
}
