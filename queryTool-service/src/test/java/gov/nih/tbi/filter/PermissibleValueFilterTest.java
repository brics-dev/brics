package gov.nih.tbi.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;

public class PermissibleValueFilterTest {
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

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");

		Filter f1 = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, "such other specified");
		PermissibleValueFilter f2 = new PermissibleValueFilter(form, rg, de, false, values, "such other specified");

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

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");

		Filter f1 = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, "such other specified");
		Filter f2 = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, "such other specified");
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

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");

		Filter f1 = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, "such other specified");
		Filter f2 = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, "such other specified!");
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

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");

		Filter f1 = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, "such other specified");
		Filter f2 = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, "such other specified");
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

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");

		List<String> values2 = new ArrayList<>();
		values2.add("doge");
		values2.add("wow!");
		values2.add("very list!");
		values2.add("such collection");

		Filter f1 = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, "such other specified");
		Filter f2 = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values2, "such other specified");
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

		List<String> list = new ArrayList<>();
		list.add("wow");
		list.add("much");
		list.add("very");
		list.add("such");

		PermissibleValueFilter f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, list, "doge");
		JsonObject j = f.toJson();
		assertEquals("testRg", j.get("groupUri").getAsString());
		assertEquals("testDe", j.get("elementUri").getAsString());
		assertEquals(false, j.get("blank").getAsBoolean());
		assertEquals(FilterType.PERMISSIBLE_VALUE.name(), j.get("filterType").getAsString());
		JsonArray jArray = new JsonArray();
		jArray.add(new JsonPrimitive("wow"));
		jArray.add(new JsonPrimitive("much"));
		jArray.add(new JsonPrimitive("very"));
		jArray.add(new JsonPrimitive("such"));
		j.addProperty("freeFormValue", "doge");
		assertEquals(jArray, j.get("permissibleValues").getAsJsonArray());
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

		List<String> list = new ArrayList<>();
		list.add("wow");
		list.add("much");
		list.add("very");
		list.add("such");

		PermissibleValueFilter f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, list, "doge");
		assertFalse(f.isEmpty());
		f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, null, null);
		assertTrue(f.isEmpty());
		f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, new ArrayList<String>(), "");
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
		de.setInputRestrictions(InputRestrictions.SINGLE);

		Filter f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, null, null);
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
		de.setInputRestrictions(InputRestrictions.SINGLE);

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");
		Filter f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("very list");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
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
		de.setInputRestrictions(InputRestrictions.SINGLE);

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");
		PermissibleValueFilter f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, null);
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
		de.setInputRestrictions(InputRestrictions.SINGLE);

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");
		PermissibleValueFilter f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, null);
		f.setBlank(false);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void testEval5() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setInputRestrictions(InputRestrictions.SINGLE);

		List<String> values = new ArrayList<>();
		PermissibleValueFilter f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("the quick brown fox");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testEvalOtherSpecify() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setInputRestrictions(InputRestrictions.FREE_FORM);

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");
		PermissibleValueFilter f =
				FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, "much other");
		f.setBlank(false);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("much other");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testCompilePattern() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setInputRestrictions(InputRestrictions.MULTIPLE);

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");
		PermissibleValueFilter f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, null);

		String expectedPattern = ".*doge.*|.*wow!.*|.*very list.*|.*such collection.*";

		assertEquals(f.compileRegexPattern().pattern(), expectedPattern);
	}

	@Test
	public void testEvalMulti() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setInputRestrictions(InputRestrictions.MULTIPLE);

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");
		PermissibleValueFilter f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("doge;blablabla");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}
	
	@Test
	public void testEvalMulti2() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setInputRestrictions(InputRestrictions.MULTIPLE);

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");
		PermissibleValueFilter f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("cats;blablabla");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}
	
	@Test
	public void testEvalMulti3() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setInputRestrictions(InputRestrictions.MULTIPLE);

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");
		PermissibleValueFilter f = FilterFactory.createPermissibleValueFilter(form, rg, de, false, values, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("doges;blablabla");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}
}
