package gov.nih.tbi.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;

public class ShowBlanksFilterTest {

	@Test
	public void evalBlankTest() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		Filter f = new ShowBlanksFilter(form, rg, de, "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);
		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void evalNonBlankTest() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		Filter f = new ShowBlanksFilter(form, rg, de, "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "test");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);
		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void evalNullTest() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		Filter f = new ShowBlanksFilter(form, rg, de, "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		String value = null;
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, value);
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);
		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void toJsonTest() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		Filter f = new ShowBlanksFilter(form, rg, de, "f", null, null, null);
		JsonObject actualJson = f.toJson();
		JsonObject expectedJson = new JsonObject();
		expectedJson.addProperty("name", "f");
		expectedJson.addProperty("groupUri", "testRg");
		expectedJson.addProperty("elementUri", "testDe");
		expectedJson.addProperty("filterType", "SHOW_BLANKS");
		assertEquals(actualJson, expectedJson);
	}

	@Test
	public void toStringTest() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		Filter f = new ShowBlanksFilter(form, rg, de, "f", null, null, null);
		String actual = f.toString();
		String expected = "testForm.testRg.testDe = ''";
		assertEquals(actual, expected);
	}
}
