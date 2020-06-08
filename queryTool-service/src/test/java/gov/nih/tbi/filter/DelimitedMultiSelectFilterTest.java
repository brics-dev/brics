package gov.nih.tbi.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.filter.DelimitedMultiSelectFilter.DelimitedMultiSelectMode;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;

public class DelimitedMultiSelectFilterTest {

	@Test
	public void testEquals() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		String values = "doge;wow!;very list;such collection";

		Filter f1 = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		Filter f2 = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		assertEquals(f1, f2);
	}

	@Test
	public void testEquals2() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		String values = "doge;wow!;very list;such collection";
		String values2 = "doge;wow!;very list;such collection!";

		Filter f1 = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		Filter f2 = new DelimitedMultiSelectFilter(form, rg, de, values2, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		assertNotEquals(f1, f2);
	}

	@Test
	public void testHashCode() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		String values = "doge;wow!;very list;such collection";

		Filter f1 = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		Filter f2 = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		assertEquals(f1.hashCode(), f2.hashCode());
	}

	@Test
	public void testHashCode2() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		String values = "doge;wow!;very list;such collection";
		String values2 = "doge;wow!;very list;such collection!";

		Filter f1 = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		Filter f2 = new DelimitedMultiSelectFilter(form, rg, de, values2, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		assertNotEquals(f1.hashCode(), f2.hashCode());
	}


	@Test
	public void testToJson() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		String values = "doge;wow!;very list;such collection";

		Filter f = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		JsonObject j = f.toJson();
		assertEquals("testRg", j.get("groupUri").getAsString());
		assertEquals("testDe", j.get("elementUri").getAsString());
		assertEquals(j.get("filterType").getAsString(), FilterType.DELIMITED_MULTI_SELECT.name());
		assertEquals("doge;wow!;very list;such collection", j.get("freeFormValue").getAsString());
	}

	@Test
	public void testIsEmpty() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		String values = "doge;wow!;very list;such collection";

		Filter f = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		assertFalse(f.isEmpty());
	}

	@Test
	public void testIsEmpty2() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		String values = "";

		Filter f = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		assertTrue(f.isEmpty());
	}

	@Test
	public void testEvalExact1() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		String values = "doge;wow!;very list;such collection";
		DelimitedMultiSelectFilter f = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "doge");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testEvalExact2() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		String values = "doge;wow!;very list;such collection";
		DelimitedMultiSelectFilter f = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "wow");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void testEvalExact4() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		String values = "doge;wow!;very list;such collection";
		DelimitedMultiSelectFilter f = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}
	
	@Test
	public void testEvalInclusive1() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		String values = "doge;wow!;very list;such collection";
		DelimitedMultiSelectFilter f = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.INCLUSIVE);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "booowow!ssa");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
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

		String values = "doge;wow!;very list;such collection";
		DelimitedMultiSelectFilter f = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		String actual = f.toString();
		String expected = "(testForm.testRg.testDe IN ('doge', 'wow!', 'very list', 'such collection'))";

		assertEquals(actual, expected);
	}

	@Test
	public void toStringTruncateTest() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		String values = "doge;wow!;very list;such collection;much elements;SO doge;wow";
		DelimitedMultiSelectFilter f = new DelimitedMultiSelectFilter(form, rg, de, values, "f", null, null, null,
				DelimitedMultiSelectMode.EXACT);
		String actual = f.toString();
		String expected =
				"(testForm.testRg.testDe IN ('doge', 'wow!', 'very list', 'such collection', 'much elements'...))";

		assertEquals(actual, expected);
	}
}
