package gov.nih.tbi.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;

public class DateFilterTest {

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

		DateFilter f1 = new DateFilter(form, rg, de, BRICSTimeDateUtil.parseTwoDigitSlashDate("11/12/11"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("11/11/11"), "f1", null, null, null);
		DateFilter f2 = new DateFilter(form, rg, de, BRICSTimeDateUtil.parseTwoDigitSlashDate("11/12/11"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("11/11/11"), "f1", null, null, null);

		Assert.assertEquals(f1, f2);

		f2.setDateMax(BRICSTimeDateUtil.parseTwoDigitSlashDate("11/13/11"));

		Assert.assertNotEquals(f1, f2);
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

		DateFilter f1 = new DateFilter(form, rg, de, BRICSTimeDateUtil.parseTwoDigitSlashDate("11/12/11"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("11/11/11"), "f1", null, null, null);
		DateFilter f2 = new DateFilter(form, rg, de, BRICSTimeDateUtil.parseTwoDigitSlashDate("11/12/11"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("11/11/11"), "f1", null, null, null);

		Assert.assertEquals(f1.hashCode(), f2.hashCode());

		f2.setDateMax(BRICSTimeDateUtil.parseTwoDigitSlashDate("11/13/11"));

		Assert.assertNotEquals(f1.hashCode(), f2.hashCode());
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

		DateFilter f = new DateFilter(form, rg, de, BRICSTimeDateUtil.parseTwoDigitSlashDate("11/12/11"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("11/11/11"), "f1", null, null, null);
		JsonObject j = f.toJson();
		Assert.assertEquals("testRg", j.get("groupUri").getAsString());
		Assert.assertEquals("testDe", j.get("elementUri").getAsString());
		Assert.assertEquals("2011-11-11", j.get("dateMin").getAsString());
		Assert.assertEquals("2011-11-12", j.get("dateMax").getAsString());
		Assert.assertEquals(FilterType.DATE.name(), j.get("filterType").getAsString());
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

		DateFilter f = new DateFilter(form, rg, de, BRICSTimeDateUtil.parseTwoDigitSlashDate("11/12/11"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("11/11/11"), "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.DATE, "2011-11-12T00:00:00Z");
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

		DateFilter f = new DateFilter(form, rg, de, BRICSTimeDateUtil.parseTwoDigitSlashDate("11/12/11"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("11/11/11"), "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.DATE, "2011-11-11T00:00:00Z");
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

		DateFilter f = new DateFilter(form, rg, de, BRICSTimeDateUtil.parseTwoDigitSlashDate("11/12/11"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("11/11/11"), "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.DATE, "2011-11-13T00:00:00Z");
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

		DateFilter f = new DateFilter(form, rg, de, BRICSTimeDateUtil.parseTwoDigitSlashDate("11/12/11"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("11/11/11"), "f", null, null, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue(DataType.DATE, "");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
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

		DateFilter f = new DateFilter(form, rg, de, BRICSTimeDateUtil.parseTwoDigitSlashDate("11/12/11"),
				BRICSTimeDateUtil.parseTwoDigitSlashDate("11/11/11"), "f", null, null, null);

		String actualString = f.toString();
		String expectedString = "(testForm.testRg.testDe >= 2011-11-11 AND testForm.testRg.testDe <= 2011-11-12)";
		assertEquals(actualString, expectedString);
	}
}
