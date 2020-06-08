package gov.nih.tbi.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.PermissibleValue;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;

public class MultiSelectFilterTest {

	@Test
	public void convertToLowercasedSetTest() {
		List<String> values = new ArrayList<>();
		values.add("doge");
		values.add("Wow!");
		values.add("very List");
		values.add("sUch collection");

		Set<String> expectedSet = new HashSet<>();
		expectedSet.add("doge");
		expectedSet.add("wow!");
		expectedSet.add("very list");
		expectedSet.add("such collection");

		assertEquals(MultiSelectFilter.convertToLowercasedSet(values), expectedSet);
	}

	@Test
	public void inclusiveEvalTest() {
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
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.INCLUSIVE, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "doge;blablabla");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void inclusiveEvalTest2() {
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
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.INCLUSIVE, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "cats;blablabla");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void exactEvalTest() {
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
		values.add("Asian");
		values.add("African American");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "african American;asian");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void exactEvalTest2() {
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
		values.add("Asian");
		values.add("African American");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "african American;asian;white");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void exactEvalTest3() {
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
		values.add("Asian");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "african American;");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void exactEvalTest4() {
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
		values.add("Asian");
		values.add("African American");
		values.add("White");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "african American;wHite;aSiaN");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void exactEvalTest5() {
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
		values.add("Asian");
		values.add("White");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "White");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void exactEvalNumericTest() {
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
		values.add("1");
		values.add("2");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.NUMERIC, "1; 2; 3");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void exactEvalNumericTest2() {
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
		values.add("1");
		values.add("2");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.INCLUSIVE, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.NUMERIC, "1; 2; 3");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void exactEvalNumericTest3() {
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
		values.add("4");
		values.add("5");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.INCLUSIVE, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.NUMERIC, "1; 2; 3");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void multiDataInclusiveEvalTest() {
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
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.INCLUSIVE, true, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "doge;blablabla");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void multiDataInclusiveEvalTest2() {
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
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.INCLUSIVE, true, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "doge;");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void multiDataExactEvalTest() {
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
		values.add("Asian");
		values.add("African American");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, true, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "african American;asian");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void multiDataExactEvalTest2() {
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
		values.add("African American");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, true, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "african American;");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void multiDataExactEvalTest3() {
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
		values.add("Asian");
		values.add("African American");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, true, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "african American;asian;whiTE");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void multiDataExactEvalTest4() {
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
		values.add("African American");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, true, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "african American");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void multiDataExactEvalTest5() {
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
		values.add("African American");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.INCLUSIVE, true, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "african American;white");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void multiDataExactEmptyEvalTest() {
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

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, null, null, "f", FilterMode.INCLUSIVE, true, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "african American");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void multiDataExactEmptyEvalTest2() {
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

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, null, null, "f", FilterMode.INCLUSIVE, true, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "african American;white");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void exactToStringTest() {
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
		values.add("Asian");
		values.add("African American");
		values.add("White");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);

		String actual = f.toString();
		String expected =
				"(testForm.testRg.testDe = 'Asian' AND testForm.testRg.testDe = 'African American' AND testForm.testRg.testDe = 'White')";
		assertEquals(actual, expected);
	}

	@Test
	public void inclusiveToStringTest() {
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
		values.add("Asian");
		values.add("African American");
		values.add("White");
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.INCLUSIVE, null, null, null);

		String actual = f.toString();
		String expected = "(testForm.testRg.testDe IN ('Asian', 'African American', 'White'))";
		assertEquals(actual, expected);
	}

	@Test
	public void multiDataToStringTest() {
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

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, null, null, "f", FilterMode.INCLUSIVE, true, null, null, null);

		String actual = f.toString();
		String expected = "(testForm.testRg.testDe.size > 1)";
		assertEquals(actual, expected);
	}

	@Test
	public void multiDataInclusiveToStringTest() {
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
		values.add("Asian");
		values.add("African American");
		values.add("White");

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.INCLUSIVE, true, null, null, null);

		String actual = f.toString();
		String expected =
				"((testForm.testRg.testDe IN ('Asian', 'African American', 'White')) AND (testForm.testRg.testDe.size > 1))";
		assertEquals(actual, expected);
	}

	@Test
	public void multiDataExactToStringTest() {
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
		values.add("Asian");
		values.add("White");

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, true, null, null, null);

		String actual = f.toString();
		String expected =
				"((testForm.testRg.testDe = 'Asian' AND testForm.testRg.testDe = 'White') AND (testForm.testRg.testDe.size > 1))";
		assertEquals(actual, expected);
	}

	@Test
	public void inclusiveDescriptionEvalTest() {
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
		List<PermissibleValue> pvs = new ArrayList<>();
		PermissibleValue pv1 = new PermissibleValue();
		pv1.setValueLiteral("value1");
		pv1.setValueDescription("Description1");
		pvs.add(pv1);

		PermissibleValue pv2 = new PermissibleValue();
		pv2.setValueLiteral("value2");
		pv2.setValueDescription("Description2");
		pvs.add(pv2);

		PermissibleValue pv3 = new PermissibleValue();
		pv3.setValueLiteral("value3");
		pv3.setValueDescription("Description3");
		pvs.add(pv3);

		PermissibleValue pv4 = new PermissibleValue();
		pv4.setValueLiteral("value4");
		pv4.setValueDescription("Description4");
		pvs.add(pv4);

		de.setPermissibleValues(pvs);


		List<String> values = new ArrayList<>();
		values.add("value1");
		values.add("value2");
		values.add("value3");
		values.add("value4");

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.INCLUSIVE, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "description1;description2");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void inclusiveDescriptionCellEvalTest() {
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
		List<PermissibleValue> pvs = new ArrayList<>();
		PermissibleValue pv1 = new PermissibleValue();
		pv1.setValueLiteral("value1");
		pv1.setValueDescription("Description1");
		pvs.add(pv1);

		PermissibleValue pv2 = new PermissibleValue();
		pv2.setValueLiteral("value2");
		pv2.setValueDescription("Description2");
		pvs.add(pv2);

		PermissibleValue pv3 = new PermissibleValue();
		pv3.setValueLiteral("value3");
		pv3.setValueDescription("Description3");
		pvs.add(pv3);

		PermissibleValue pv4 = new PermissibleValue();
		pv4.setValueLiteral("value4");
		pv4.setValueDescription("Description4");
		pvs.add(pv4);

		de.setPermissibleValues(pvs);


		List<String> values = new ArrayList<>();
		values.add("description1");
		values.add("description2");
		values.add("description3");
		values.add("description4");

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.INCLUSIVE, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "value1;value2");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void inclusiveDescriptionCellEvalTest2() {
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
		List<PermissibleValue> pvs = new ArrayList<>();
		PermissibleValue pv1 = new PermissibleValue();
		pv1.setValueLiteral("value1");
		pv1.setValueDescription("description1");
		pvs.add(pv1);

		PermissibleValue pv2 = new PermissibleValue();
		pv2.setValueLiteral("value2");
		pv2.setValueDescription("description2");
		pvs.add(pv2);

		PermissibleValue pv3 = new PermissibleValue();
		pv3.setValueLiteral("value3");
		pv3.setValueDescription("description3");
		pvs.add(pv3);

		PermissibleValue pv4 = new PermissibleValue();
		pv4.setValueLiteral("value4");
		pv4.setValueDescription("description4");
		pvs.add(pv4);

		de.setPermissibleValues(pvs);


		List<String> values = new ArrayList<>();
		values.add("description1");
		values.add("description2");
		values.add("description3");
		values.add("description4");

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.INCLUSIVE, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "description2;description3");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void exactDescriptionEvalTest() {
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
		List<PermissibleValue> pvs = new ArrayList<>();
		PermissibleValue pv1 = new PermissibleValue();
		pv1.setValueLiteral("value1");
		pv1.setValueDescription("description1");
		pvs.add(pv1);

		PermissibleValue pv2 = new PermissibleValue();
		pv2.setValueLiteral("value2");
		pv2.setValueDescription("description2");
		pvs.add(pv2);

		PermissibleValue pv3 = new PermissibleValue();
		pv3.setValueLiteral("value3");
		pv3.setValueDescription("description3");
		pvs.add(pv3);

		PermissibleValue pv4 = new PermissibleValue();
		pv4.setValueLiteral("value4");
		pv4.setValueDescription("description4");
		pvs.add(pv4);

		de.setPermissibleValues(pvs);


		List<String> values = new ArrayList<>();
		values.add("value1");
		values.add("value2");
		values.add("value3");
		values.add("value4");

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "Description1;Description2");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void exactDescriptionEvalTest2() {
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
		List<PermissibleValue> pvs = new ArrayList<>();
		PermissibleValue pv1 = new PermissibleValue();
		pv1.setValueLiteral("value1");
		pv1.setValueDescription("Description1");
		pvs.add(pv1);

		PermissibleValue pv2 = new PermissibleValue();
		pv2.setValueLiteral("value2");
		pv2.setValueDescription("Description2");
		pvs.add(pv2);

		PermissibleValue pv3 = new PermissibleValue();
		pv3.setValueLiteral("value3");
		pv3.setValueDescription("Description3");
		pvs.add(pv3);

		PermissibleValue pv4 = new PermissibleValue();
		pv4.setValueLiteral("value4");
		pv4.setValueDescription("Description4");
		pvs.add(pv4);

		de.setPermissibleValues(pvs);


		List<String> values = new ArrayList<>();
		values.add("value1");
		values.add("value2");
		values.add("value3");
		values.add("value4");

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "description1;description2;description3;description4");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void exactDescriptionCellEvalTest() {
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
		List<PermissibleValue> pvs = new ArrayList<>();
		PermissibleValue pv1 = new PermissibleValue();
		pv1.setValueLiteral("value1");
		pv1.setValueDescription("description1");
		pvs.add(pv1);

		PermissibleValue pv2 = new PermissibleValue();
		pv2.setValueLiteral("value2");
		pv2.setValueDescription("description2");
		pvs.add(pv2);

		PermissibleValue pv3 = new PermissibleValue();
		pv3.setValueLiteral("value3");
		pv3.setValueDescription("description3");
		pvs.add(pv3);

		PermissibleValue pv4 = new PermissibleValue();
		pv4.setValueLiteral("value4");
		pv4.setValueDescription("description4");
		pvs.add(pv4);

		de.setPermissibleValues(pvs);


		List<String> values = new ArrayList<>();
		values.add("description1");
		values.add("description2");
		values.add("description3");
		values.add("description4");

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "value1;value2");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void exactDescriptionCellEvalTest2() {
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
		List<PermissibleValue> pvs = new ArrayList<>();
		PermissibleValue pv1 = new PermissibleValue();
		pv1.setValueLiteral("value1");
		pv1.setValueDescription("description1");
		pvs.add(pv1);

		PermissibleValue pv2 = new PermissibleValue();
		pv2.setValueLiteral("value2");
		pv2.setValueDescription("description2");
		pvs.add(pv2);

		PermissibleValue pv3 = new PermissibleValue();
		pv3.setValueLiteral("value3");
		pv3.setValueDescription("description3");
		pvs.add(pv3);

		PermissibleValue pv4 = new PermissibleValue();
		pv4.setValueLiteral("value4");
		pv4.setValueDescription("description4");
		pvs.add(pv4);

		de.setPermissibleValues(pvs);


		List<String> values = new ArrayList<>();
		values.add("description1");
		values.add("description2");
		values.add("description3");
		values.add("description4");

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "value1;value2;value3;value4");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void exactDescriptionCellEvalTest3() {
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
		List<PermissibleValue> pvs = new ArrayList<>();
		PermissibleValue pv1 = new PermissibleValue();
		pv1.setValueLiteral("value1");
		pv1.setValueDescription("description1");
		pvs.add(pv1);

		PermissibleValue pv2 = new PermissibleValue();
		pv2.setValueLiteral("value2");
		pv2.setValueDescription("description2");
		pvs.add(pv2);

		PermissibleValue pv3 = new PermissibleValue();
		pv3.setValueLiteral("value3");
		pv3.setValueDescription("description3");
		pvs.add(pv3);

		PermissibleValue pv4 = new PermissibleValue();
		pv4.setValueLiteral("value4");
		pv4.setValueDescription("description4");
		pvs.add(pv4);

		de.setPermissibleValues(pvs);


		List<String> values = new ArrayList<>();
		values.add("description1");
		values.add("description2");
		values.add("description3");
		values.add("description4");

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "description2;description3");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void exactDescriptionCellEvalTest4() {
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
		List<PermissibleValue> pvs = new ArrayList<>();
		PermissibleValue pv1 = new PermissibleValue();
		pv1.setValueLiteral("value1");
		pv1.setValueDescription("description1");
		pvs.add(pv1);

		PermissibleValue pv2 = new PermissibleValue();
		pv2.setValueLiteral("value2");
		pv2.setValueDescription("description2");
		pvs.add(pv2);

		PermissibleValue pv3 = new PermissibleValue();
		pv3.setValueLiteral("value3");
		pv3.setValueDescription("description3");
		pvs.add(pv3);

		PermissibleValue pv4 = new PermissibleValue();
		pv4.setValueLiteral("value4");
		pv4.setValueDescription("description4");
		pvs.add(pv4);

		de.setPermissibleValues(pvs);


		List<String> values = new ArrayList<>();
		values.add("description1");
		values.add("description2");
		values.add("description3");
		values.add("description4");

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "description2;description3;description1;description4");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void exactEmptyEvalTest() {
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
		List<PermissibleValue> pvs = new ArrayList<>();
		PermissibleValue pv1 = new PermissibleValue();
		pv1.setValueLiteral("value1");
		pv1.setValueDescription("description1");
		pvs.add(pv1);

		PermissibleValue pv2 = new PermissibleValue();
		pv2.setValueLiteral("value2");
		pv2.setValueDescription("description2");
		pvs.add(pv2);

		PermissibleValue pv3 = new PermissibleValue();
		pv3.setValueLiteral("value3");
		pv3.setValueDescription("description3");
		pvs.add(pv3);

		PermissibleValue pv4 = new PermissibleValue();
		pv4.setValueLiteral("value4");
		pv4.setValueDescription("description4");
		pvs.add(pv4);

		de.setPermissibleValues(pvs);

		List<String> values = new ArrayList<>();

		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "description2;description3;description1;description4");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void exactEmptyCellEvalTest() {
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
		List<PermissibleValue> pvs = new ArrayList<>();
		PermissibleValue pv1 = new PermissibleValue();
		pv1.setValueLiteral("value1");
		pv1.setValueDescription("description1");
		pvs.add(pv1);

		PermissibleValue pv2 = new PermissibleValue();
		pv2.setValueLiteral("value2");
		pv2.setValueDescription("description2");
		pvs.add(pv2);

		PermissibleValue pv3 = new PermissibleValue();
		pv3.setValueLiteral("value3");
		pv3.setValueDescription("description3");
		pvs.add(pv3);

		PermissibleValue pv4 = new PermissibleValue();
		pv4.setValueLiteral("value4");
		pv4.setValueDescription("description4");
		pvs.add(pv4);

		de.setPermissibleValues(pvs);

		List<String> values = new ArrayList<>();
		values.add("value");
		
		MultiSelectFilter f =
				new MultiSelectFilter(form, rg, de, values, null, "f", FilterMode.EXACT, null, null, null);
		InstancedRow row = mock(InstancedRow.class);

		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}
}
