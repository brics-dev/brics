package gov.nih.tbi.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;

public class RangedNumericFilterTest {

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

		RangedNumericFilter f1 = new RangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);
		Filter f2 = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);

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

		RangedNumericFilter f1 = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);
		RangedNumericFilter f2 = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);
		assertEquals(f1, f2);
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

		RangedNumericFilter f1 = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);
		RangedNumericFilter f2 = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);
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

		RangedNumericFilter f = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);
		JsonObject j = f.toJson();
		assertEquals("testRg", j.get("groupUri").getAsString());
		assertEquals("testDe", j.get("elementUri").getAsString());
		assertEquals(1.2d, j.get("maximum").getAsDouble());
		assertEquals(1.1d, j.get("minimum").getAsDouble());
		assertEquals(false, j.get("blank").getAsBoolean());
		assertEquals(FilterType.RANGED_NUMERIC.name(), j.get("filterType").getAsString());
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

		RangedNumericFilter f = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);
		assertFalse(f.isEmpty());
		f = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, null);
		assertTrue(f.isEmpty());
		f = FilterFactory.createRangedNumericFilter(form, rg, de, false, null, 1.1d);
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

		RangedNumericFilter f = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("1.1");
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

		RangedNumericFilter f = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("1.2");
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

		RangedNumericFilter f = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("1.3");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
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

		RangedNumericFilter f = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("1.0");
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

		RangedNumericFilter f = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);

		f.setBlank(true);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testEval6() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		RangedNumericFilter f = FilterFactory.createRangedNumericFilter(form, rg, de, false, 1.2d, 1.1d);

		f.setBlank(false);

		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}
}
