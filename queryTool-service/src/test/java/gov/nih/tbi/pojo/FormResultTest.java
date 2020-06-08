package gov.nih.tbi.pojo;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.RepeatingCellColumn;

public class FormResultTest {

	@Test
	public void columnCacheTest() {
		FormResult form = new FormResult();
		DataTableColumn column = form.getColumnFromString("form", "rg", "de");
		assertEquals(column.getForm(), "form");
		assertEquals(column.getRepeatableGroup(), "rg");
		assertEquals(column.getDataElement(), "de");

		DataTableColumn column2 = form.getColumnFromString("form", "rg", "de");

		// make sure the two objects are indeed the same (memory address)
		assertTrue(column == column2);
	}

	@Test
	public void columnCacheHardCodedTest() {
		FormResult form = new FormResult();
		DataTableColumn column = form.getColumnFromString("form", "rg", "de");
		assertEquals(column.getForm(), "form");
		assertEquals(column.getRepeatableGroup(), "rg");
		assertEquals(column.getDataElement(), "de");

		DataTableColumn column2 = form.getColumnFromString("form", "rg", "de");

		// make sure the two objects are indeed the same (memory address)
		assertTrue(column == column2);

		DataTableColumn hardCodedColumn = form.getColumnFromString("form", "study");
		assertEquals(hardCodedColumn.getForm(), "form");
		assertEquals(hardCodedColumn.getHardCoded(), "study");

		DataTableColumn hardCodedColumn2 = form.getColumnFromString("form", "study");
		assertTrue(hardCodedColumn == hardCodedColumn2);
	}

	@Test
	public void columnCacheRgColumnTest() {
		FormResult form = new FormResult();
		RepeatingCellColumn column = (RepeatingCellColumn) form.getColumnFromString("form", "rg", "de", DataType.ALPHANUMERIC);
		assertEquals(column.getForm(), "form");
		assertEquals(column.getRepeatableGroup(), "rg");
		assertEquals(column.getDataElement(), "de");
		assertEquals(column.getType(), DataType.ALPHANUMERIC);

		RepeatingCellColumn column2 = (RepeatingCellColumn) form.getColumnFromString("form", "rg", "de", DataType.ALPHANUMERIC);

		// make sure the two objects are indeed the same (memory address)
		assertTrue(column == column2);
	}
}
