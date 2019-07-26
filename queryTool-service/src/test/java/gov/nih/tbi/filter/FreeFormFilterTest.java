package gov.nih.tbi.filter;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.repository.model.RepeatingCellValue;

import static org.mockito.Mockito.*;

public class FreeFormFilterTest {

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

		Filter f1 = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		FreeFormFilter f2 = new FreeFormFilter(form, rg, de, false, "test");

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

		FreeFormFilter f1 = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		FreeFormFilter f2 = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
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

		Filter f1 = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		Filter f2 = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
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

		Filter f = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		JsonObject j = f.toJson();
		assertEquals("testRg", j.get("groupUri").getAsString());
		assertEquals("testDe", j.get("elementUri").getAsString());
		assertEquals("test", j.get("freeFormValue").getAsString());
		assertEquals(false, j.get("blank").getAsBoolean());
		assertEquals(FilterType.FREE_FORM.name(), j.get("filterType").getAsString());
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

		Filter f = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("test");
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

		Filter f = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("123test123");
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

		Filter f = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("random stuff");
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

		Filter f = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("123test");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
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

		Filter f = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("tEsT123");
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

		FreeFormFilter f = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		f.setBlank(true);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

	@Test
	public void testEval7() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");

		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");

		FreeFormFilter f = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		f.setBlank(false);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
	}

	@Test
	public void testRgEval() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");
		rg.setThreshold(0);
		rg.setType(QueryToolConstants.RG_EXACTLY);
		
		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType("type");

		FreeFormFilter f = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		f.setBlank(false);
		InstancedRow row = mock(InstancedRow.class);

		RepeatingCellValue rcv = new RepeatingCellValue("type", 1, 3);
		rcv.setExpanded(true);
		List<InstancedRepeatableGroupRow> rgRows = new ArrayList<>();
		InstancedRepeatableGroupRow rgRow1 = new InstancedRepeatableGroupRow();
		rgRow1.insertCell(new RepeatingCellColumn("testFormV1.0", "testRg", "testDe", "type"), "test");
		InstancedRepeatableGroupRow rgRow2 = new InstancedRepeatableGroupRow();
		rgRow2.insertCell(new RepeatingCellColumn("testFormV1.0", "testRg", "testDe", "type"), "test2");
		InstancedRepeatableGroupRow rgRow3 = new InstancedRepeatableGroupRow();
		rgRow3.insertCell(new RepeatingCellColumn("testFormV1.0", "testRg", "testDe", "type"), "blah");

		rgRows.add(rgRow1);
		rgRows.add(rgRow2);
		rgRows.add(rgRow3);
		
		rcv.setRows(rgRows);

		when(f.getRepeatingData(row)).thenReturn(rcv);

		f.evaluate(row);

		assertEquals(rcv.getRows().size(), 2);
	}
	
	@Test
	public void testRgEval2() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");
		rg.setThreshold(0);
		rg.setType(QueryToolConstants.RG_EXACTLY);
		
		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType("type");

		FreeFormFilter f = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		f.setBlank(false);
		InstancedRow row = mock(InstancedRow.class);

		RepeatingCellValue rcv = new RepeatingCellValue("type", 1, 3);
		rcv.setExpanded(true);
		List<InstancedRepeatableGroupRow> rgRows = new ArrayList<>();
		InstancedRepeatableGroupRow rgRow1 = new InstancedRepeatableGroupRow();
		rgRow1.insertCell(new RepeatingCellColumn("testFormV1.0", "testRg", "testDe", "type"), "test");
		InstancedRepeatableGroupRow rgRow2 = new InstancedRepeatableGroupRow();
		rgRow2.insertCell(new RepeatingCellColumn("testFormV1.0", "testRg", "testDe", "type"), "test2");
		InstancedRepeatableGroupRow rgRow3 = new InstancedRepeatableGroupRow();
		rgRow3.insertCell(new RepeatingCellColumn("testFormV1.0", "testRg", "testDe", "type"), "blah");

		rgRows.add(rgRow1);
		rgRows.add(rgRow2);
		rgRows.add(rgRow3);
		
		rcv.setRows(rgRows);

		when(f.getRepeatingData(row)).thenReturn(rcv);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}
	
	@Test
	public void testRgEval3() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");
		rg.setThreshold(0);
		rg.setType(QueryToolConstants.RG_EXACTLY);
		
		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType("type");

		FreeFormFilter f = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		f.setBlank(false);
		InstancedRow row = mock(InstancedRow.class);

		RepeatingCellValue rcv = new RepeatingCellValue("type", 1, 3);
		rcv.setExpanded(true);
		List<InstancedRepeatableGroupRow> rgRows = new ArrayList<>();
		InstancedRepeatableGroupRow rgRow1 = new InstancedRepeatableGroupRow();
		rgRow1.insertCell(new RepeatingCellColumn("testFormV1.0", "testRg", "testDe", "type"), "dog");
		InstancedRepeatableGroupRow rgRow2 = new InstancedRepeatableGroupRow();
		rgRow2.insertCell(new RepeatingCellColumn("testFormV1.0", "testRg", "testDe", "type"), "cow");
		InstancedRepeatableGroupRow rgRow3 = new InstancedRepeatableGroupRow();
		rgRow3.insertCell(new RepeatingCellColumn("testFormV1.0", "testRg", "testDe", "type"), "cat");

		rgRows.add(rgRow1);
		rgRows.add(rgRow2);
		rgRows.add(rgRow3);
		
		rcv.setRows(rgRows);

		when(f.getRepeatingData(row)).thenReturn(rcv);

		f.evaluate(row);

		assertEquals(rcv.getRows().size(), 0);
	}
	
	@Test
	public void testRgEval4() {
		FormResult form = new FormResult();
		form.setShortName("testForm");
		form.setVersion("1.0");

		RepeatableGroup rg = new RepeatableGroup();
		rg.setUri("testRg");
		rg.setName("testRg");
		rg.setThreshold(0);
		rg.setType(QueryToolConstants.RG_EXACTLY);
		
		DataElement de = new DataElement();
		de.setUri("testDe");
		de.setName("testDe");
		de.setType("type");

		FreeFormFilter f = FilterFactory.createFreeFormFilter(form, rg, de, false, "test");
		f.setBlank(false);
		InstancedRow row = mock(InstancedRow.class);

		RepeatingCellValue rcv = new RepeatingCellValue("type", 1, 3);
		rcv.setExpanded(true);
		List<InstancedRepeatableGroupRow> rgRows = new ArrayList<>();
		InstancedRepeatableGroupRow rgRow1 = new InstancedRepeatableGroupRow();
		rgRow1.insertCell(new RepeatingCellColumn("testFormV1.0", "testRg", "testDe", "type"), "dog");
		InstancedRepeatableGroupRow rgRow2 = new InstancedRepeatableGroupRow();
		rgRow2.insertCell(new RepeatingCellColumn("testFormV1.0", "testRg", "testDe", "type"), "cow");
		InstancedRepeatableGroupRow rgRow3 = new InstancedRepeatableGroupRow();
		rgRow3.insertCell(new RepeatingCellColumn("testFormV1.0", "testRg", "testDe", "type"), "cat");

		rgRows.add(rgRow1);
		rgRows.add(rgRow2);
		rgRows.add(rgRow3);
		
		rcv.setRows(rgRows);

		when(f.getRepeatingData(row)).thenReturn(rcv);

		boolean eval = f.evaluate(row);

		assertFalse(eval);
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

		Filter f = FilterFactory.createFreeFormFilter(form, rg, de, false, null);
		InstancedRow row = mock(InstancedRow.class);
		NonRepeatingCellValue testCellValue = new NonRepeatingCellValue("test");
		when(row.getCellValue(form.getShortNameAndVersion(), rg.getName(), de.getName())).thenReturn(testCellValue);

		boolean eval = f.evaluate(row);

		assertTrue(eval);
	}

}
