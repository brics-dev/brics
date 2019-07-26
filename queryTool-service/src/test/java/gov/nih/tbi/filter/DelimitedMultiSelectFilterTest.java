package gov.nih.tbi.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;

public class DelimitedMultiSelectFilterTest {
	@Test
	public void testFactory() {
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

		Filter f1 = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);
		DelimitedMultiSelectFilter f2 = new DelimitedMultiSelectFilter(form, rg, de, false, values);

		assertEquals(f1, f2);
	}

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

		Filter f1 = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);
		Filter f2 = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);
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

		Filter f1 = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);
		Filter f2 = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values2);
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

		Filter f1 = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);
		Filter f2 = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);
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

		Filter f1 = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);
		Filter f2 = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values2);
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

		Filter f = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);
		JsonObject j = f.toJson();
		assertEquals("testRg", j.get("groupUri").getAsString());
		assertEquals("testDe", j.get("elementUri").getAsString());
		assertEquals(false, j.get("blank").getAsBoolean());
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

		Filter f = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);
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

		Filter f = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);
		assertTrue(f.isEmpty());
	}

	@Test
	public void testEval1() {
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
		DelimitedMultiSelectFilter f = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("doge");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testEval2() {
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
		DelimitedMultiSelectFilter f = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("wow");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void testEval3() {
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
		DelimitedMultiSelectFilter f = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);
		f.setBlank(true);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testEval4() {
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
		DelimitedMultiSelectFilter f = FilterFactory.createDelimitedMultiSelectFilter(form, rg, de, false, values);
		f.setBlank(false);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

}
