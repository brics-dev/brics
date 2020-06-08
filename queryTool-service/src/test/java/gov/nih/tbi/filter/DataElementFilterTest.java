package gov.nih.tbi.filter;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class DataElementFilterTest {

	@Test
	public void testReadableFilterName() {
		FormResult form = new FormResult();
		form.setShortName("MDS_UPDRS");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setName("Required Fields");

		DataElement de = new DataElement();
		de.setName("GUID");

		DataElementFilter f1 = new FreeFormFilter(form, rg, de, "blah", "foo", null, null, null, FilterMode.EXACT);
		String actual = f1.getReadableFilterName();
		String expected = "MDS_UPDRS.RequiredFields.GUID";
		assertEquals(actual, expected);
	}
}
