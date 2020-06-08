package gov.nih.tbi.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;

public class FreeFormFilterTest {

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

		FreeFormFilter f1 = new FreeFormFilter(form, rg, de, "test", "f", null, null, null, FilterMode.EXACT);
		FreeFormFilter f2 = new FreeFormFilter(form, rg, de, "test", "f", null, null, null, FilterMode.EXACT);
		assertEquals(f1, f2);

		f2.setValue("test123");

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

		Filter f1 = new FreeFormFilter(form, rg, de, "test", "f", null, null, null, FilterMode.EXACT);
		Filter f2 = new FreeFormFilter(form, rg, de, "test", "f", null, null, null, FilterMode.EXACT);
		assertEquals(f1.hashCode(), f2.hashCode());
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

		Filter f = new FreeFormFilter(form, rg, de, "test", "f", null, null, null, FilterMode.EXACT);
		JsonObject j = f.toJson();
		assertEquals("testRg", j.get("groupUri").getAsString());
		assertEquals("testDe", j.get("elementUri").getAsString());
		assertEquals("test", j.get("freeFormValue").getAsString());
		assertEquals("exact", j.get("mode").getAsString());
		assertEquals(FilterType.FREE_FORM.name(), j.get("filterType").getAsString());
	}

	@Test
	public void testExactEval1() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "test", "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "test");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testExactEval2() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "test", "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "123test123");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void testExactEval3() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "test", "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "random stuff");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void testExactEval4() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "test", "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "123test");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void testExactEval5() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "test123", "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "tEsT123");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testExactEval6() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "Mayo", "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "Mayo Clinic");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}


	@Test
	public void testExactEval7() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "Mayo Clinic", "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "Mayo Clinic");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testExactFileEval() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.FILE);

		Filter f = new FreeFormFilter(form, rg, de,
				"TBI_INVUD366VHA_ImagingCT_1410797710238_PI-1145_--_Ct_Head_Or_Brain_Witho_-_0_--_DR_20_______AXIALS_3.zip",
				"f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC,
				"/image_output/TBI_INVUD366VHA_ImagingCT_1410797710238_PI-1145_--_Ct_Head_Or_Brain_Witho_-_0_--_DR_20_______AXIALS_3.zip");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}
	
	@Test
	public void testExactTriplanarEval() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.TRIPLANAR);

		Filter f = new FreeFormFilter(form, rg, de,
				"TBI_INVUD366VHA_ImagingCT_1410797710238_PI-1145_--_Ct_Head_Or_Brain_Witho_-_0_--_DR_20_______AXIALS_3.zip",
				"f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC,
				"/image_output/TBI_INVUD366VHA_ImagingCT_1410797710238_PI-1145_--_Ct_Head_Or_Brain_Witho_-_0_--_DR_20_______AXIALS_3.zip");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}
	
	@Test
	public void testExactThumbnailEval() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.THUMBNAIL);

		Filter f = new FreeFormFilter(form, rg, de,
				"TBI_INVUD366VHA_ImagingCT_1410797710238_PI-1145_--_Ct_Head_Or_Brain_Witho_-_0_--_DR_20_______AXIALS_3.zip",
				"f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC,
				"/image_output/TBI_INVUD366VHA_ImagingCT_1410797710238_PI-1145_--_Ct_Head_Or_Brain_Witho_-_0_--_DR_20_______AXIALS_3.zip");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}
	
	@Test
	public void testExactFileEval2() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.THUMBNAIL);

		Filter f = new FreeFormFilter(form, rg, de,
				"TBI_INVUD366VHA_ImagingCT_1410797710238_PI-1145_--_Ct_Head_Or_Brain_Witho_-_0_--_DR_20_______AXIALS_3.zip",
				"f", null, null, null, FilterMode.INCLUSIVE);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC,
				"/image_output/TBI_INVUD366VHA_ImagingCT_1410797710238_PI-1145_--_Ct_Head_Or_Brain_Witho_-_0_--_DR_20_______AXIALS_3.zip");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testInclusiveEval1() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "rom1", "f", null, null, null, FilterMode.INCLUSIVE);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "prom1");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);
		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testInclusiveEval2() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "rom1", "f", null, null, null, FilterMode.INCLUSIVE);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "prom");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);
		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void testInclusiveEval3() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "rom1", "f", null, null, null, FilterMode.INCLUSIVE);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "I went to prom1");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);
		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testInclusiveAndGroupEval() throws FilterEvaluatorException {
		String testExpression = "f1 && f2";

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement de = mock(DataElement.class);
		when(de.getName()).thenReturn("de");
		when(de.getType()).thenReturn(DataType.ALPHANUMERIC);

		Filter f1 = new FreeFormFilter(form, rg, de, "Harvard", "f1", null, null, null, FilterMode.INCLUSIVE);
		Filter f2 = new FreeFormFilter(form, rg, de, "BRIGHAM", "f2", null, null, null, FilterMode.INCLUSIVE);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);
		filters.add(f2);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(testExpression, forms);

		InstancedRecord record = mock(InstancedRecord.class);

		List<InstancedRow> rows = new ArrayList<>();
		InstancedRow row = mock(InstancedRow.class);
		rows.add(row);

		when(record.getSelectedRows()).thenReturn(rows);

		NonRepeatingCellValue nameCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC,
				"the Brigham and women's hospitals, INC and Harvard Medical school (Boston, MA)");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(nameCellValue);

		boolean result = evaluator.evaluate(record);
		assertTrue(result);
	}

	@Test
	public void testInclusiveAndGroupEval2() throws FilterEvaluatorException {
		String testExpression = "f1 && f2";

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement de = mock(DataElement.class);
		when(de.getName()).thenReturn("de");
		when(de.getType()).thenReturn(DataType.ALPHANUMERIC);

		Filter f1 = new FreeFormFilter(form, rg, de, "Harvard", "f1", null, null, null, FilterMode.INCLUSIVE);
		Filter f2 = new FreeFormFilter(form, rg, de, "BRIGHAM", "f2", null, null, null, FilterMode.INCLUSIVE);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);
		filters.add(f2);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(testExpression, forms);

		InstancedRecord record = mock(InstancedRecord.class);

		List<InstancedRow> rows = new ArrayList<>();
		InstancedRow row = mock(InstancedRow.class);
		rows.add(row);

		when(record.getSelectedRows()).thenReturn(rows);

		NonRepeatingCellValue nameCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "the Brigham and women's hospitals");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(nameCellValue);

		boolean result = evaluator.evaluate(record);
		assertFalse(result);
	}

	@Test
	public void testInclusiveOrGroupEval3() throws FilterEvaluatorException {
		String testExpression = "f1 || f2";

		FormResult form = mock(FormResult.class);
		when(form.getShortNameAndVersion()).thenReturn("form");

		RepeatableGroup rg = mock(RepeatableGroup.class);
		when(rg.getName()).thenReturn("rg");

		DataElement de = mock(DataElement.class);
		when(de.getName()).thenReturn("de");
		when(de.getType()).thenReturn(DataType.ALPHANUMERIC);

		Filter f1 = new FreeFormFilter(form, rg, de, "Harvard", "f1", null, null, null, FilterMode.INCLUSIVE);
		Filter f2 = new FreeFormFilter(form, rg, de, "BRIGHAM", "f2", null, null, null, FilterMode.INCLUSIVE);

		List<Filter> filters = new ArrayList<>();
		filters.add(f1);
		filters.add(f2);

		when(form.getFilters()).thenReturn(filters);
		when(form.hasFilter()).thenReturn(true);

		List<FormResult> forms = new ArrayList<>();
		forms.add(form);

		FilterEvaluator evaluator = new FilterEvaluator(testExpression, forms);

		InstancedRecord record = mock(InstancedRecord.class);

		List<InstancedRow> rows = new ArrayList<>();
		InstancedRow row = mock(InstancedRow.class);
		rows.add(row);

		when(record.getSelectedRows()).thenReturn(rows);

		NonRepeatingCellValue nameCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "the Brigham and women's hospitals");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(nameCellValue);

		boolean result = evaluator.evaluate(record);
		assertTrue(result);
	}

	@Test
	public void testEvalExactText() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "rom", "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "I went to prom last week");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);
		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void testEvalExactText2() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f =
				new FreeFormFilter(form, rg, de, "I went to prom last week", "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "I went to prom last week");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);
		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testNullEval() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		Filter f = new FreeFormFilter(form, rg, de, null, "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "test");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testToString() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		Filter f = new FreeFormFilter(form, rg, de, "test", "f", null, null, null, FilterMode.EXACT);

		String actualString = f.toString();
		String expectedString = "(testForm.testRg.testDe = 'test')";

		assertEquals(actualString, expectedString);
	}

	@Test
	public void testFreeTextEval() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "Doge wow", "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue =
				new NonRepeatingCellValue(DataType.ALPHANUMERIC, "This is doge gone crazy!");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void testFreeTextEval2() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "Doge wow", "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "doge wow");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testFreeTextEval3() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		Filter f = new FreeFormFilter(form, rg, de, "5345 AD.pdf", "f", null, null, null, FilterMode.EXACT);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.ALPHANUMERIC, "1234 AD.pdf");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	/**
	 * Make sure pattern gets set in the constructor
	 */
	@Test
	public void testRegexPatternSingleConstructor() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		FreeFormFilter f = new FreeFormFilter(form, rg, de, "Doge", "f", null, null, null, FilterMode.EXACT);
		assertEquals("(Doge)", f.regexPattern.toString());
	}

	/**
	 * Make sure pattern gets set in the constructor
	 */
	@Test
	public void testRegexPatternConstructor() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		FreeFormFilter f = new FreeFormFilter(form, rg, de, "Doge      wow     such    tokens", "f", null, null, null,
				FilterMode.EXACT);
		assertEquals("(Doge      wow     such    tokens)", f.regexPattern.toString());
	}

	@Test
	public void testRegexPatternConstructorNull() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		FreeFormFilter f = new FreeFormFilter(form, rg, de, null, "f", null, null, null, FilterMode.EXACT);
		assertNull(f.regexPattern);

		FreeFormFilter f1 = new FreeFormFilter(form, rg, de, "", "f", null, null, null, FilterMode.EXACT);
		assertNull(f1.regexPattern);
	}

	/**
	 * Make sure pattern gets set in the setValue call
	 */
	@Test
	public void testRegexPatternSetter() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		FreeFormFilter f = new FreeFormFilter(form, rg, de, null, "f", null, null, null, FilterMode.EXACT);
		assertNull(f.regexPattern);
		f.setValue("Doge     wow");
		assertEquals("(Doge     wow)", f.regexPattern.toString());
	}

	@Test
	public void testRegexPatternSetterNull() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType(DataType.ALPHANUMERIC);

		FreeFormFilter f = new FreeFormFilter(form, rg, de, "Doge       wow", "f", null, null, null, FilterMode.EXACT);
		assertEquals("(Doge       wow)", f.regexPattern.toString());
		f.setValue(null);
		assertNull(f.regexPattern);
		f.setValue("");
		assertNull(f.regexPattern);
	}
}
