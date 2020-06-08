package gov.nih.tbi.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.PermissibleValue;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;

public class SingleSelectFilterTest {
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

		Filter f1 = new SingleSelectFilter(form, rg, de, values, "such other specified", "f", null, null, null);
		Filter f2 = new SingleSelectFilter(form, rg, de, values, "such other specified", "f", null, null, null);
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

		Filter f1 = new SingleSelectFilter(form, rg, de, values, "such other specified", "f", null, null, null);
		Filter f2 = new SingleSelectFilter(form, rg, de, values, "such other specified!", "f", null, null, null);
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

		Filter f1 = new SingleSelectFilter(form, rg, de, values, "such other specified", "f", null, null, null);
		Filter f2 = new SingleSelectFilter(form, rg, de, values, "such other specified", "f", null, null, null);
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

		Filter f1 = new SingleSelectFilter(form, rg, de, values, "such other specified", "f", null, null, null);
		Filter f2 = new SingleSelectFilter(form, rg, de, values2, "such other specified", "f", null, null, null);
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

		SingleSelectFilter f = new SingleSelectFilter(form, rg, de, list, "doge", "f", null, null, null);
		JsonObject j = f.toJson();
		assertEquals("testRg", j.get("groupUri").getAsString());
		assertEquals("testDe", j.get("elementUri").getAsString());
		assertEquals(FilterType.SINGLE_SELECT.name(), j.get("filterType").getAsString());
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

		SingleSelectFilter f = new SingleSelectFilter(form, rg, de, list, "doge", "f", null, null, null);
		assertFalse(f.isEmpty());
		f = new SingleSelectFilter(form, rg, de, null, null, "f", null, null, null);
		assertTrue(f.isEmpty());
		f = new SingleSelectFilter(form, rg, de, new ArrayList<String>(), "", "f", null, null, null);
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

		Filter f = new SingleSelectFilter(form, rg, de, null, null, "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "doge");
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
		
		List<PermissibleValue> pvList = new ArrayList<PermissibleValue>();
		
		PermissibleValue pvDoge = new PermissibleValue();
		pvDoge.setValueLiteral("doge");
		pvDoge.setValueDescription("ok meme");
		
		PermissibleValue pvWow = new PermissibleValue();
		pvWow.setValueLiteral("wow!");
		pvWow.setValueDescription("fine meme");
		
		PermissibleValue pvVery = new PermissibleValue();
		pvVery.setValueLiteral("very list");
		pvVery.setValueDescription("good meme");
		
		PermissibleValue pvSuch = new PermissibleValue();
		pvSuch.setValueLiteral("such collection");
		pvSuch.setValueDescription("great meme");
		
		pvList.add(pvDoge);
		pvList.add(pvWow);
		pvList.add(pvVery);
		pvList.add(pvSuch);

		de.setPermissibleValues(pvList);
		LinkedList<DataElement> deListDummy = new LinkedList<DataElement>();
		deListDummy.add(de);
		rg.setDataElements(deListDummy);
		
		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");
		Filter f = new SingleSelectFilter(form, rg, de, values, null, "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "very list");
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
		SingleSelectFilter f = new SingleSelectFilter(form, rg, de, values, null, "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "");
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
		SingleSelectFilter f = new SingleSelectFilter(form, rg, de, values, null, "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "the quick brown fox");
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
		SingleSelectFilter f = new SingleSelectFilter(form, rg, de, values, "much other", "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "much other");
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
		de.setInputRestrictions(InputRestrictions.SINGLE);

		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("wow!");
		values.add("very list");
		values.add("such collection");
		values.add("much array");
		values.add("wow!");
		SingleSelectFilter f = new SingleSelectFilter(form, rg, de, values, null, "f", null, null, null);

		assertEquals(f.toString(),
				"(testForm.testRg.testDe IN ('doge', 'wow!', 'very list', 'such collection', 'much array'...))");
	}
}
