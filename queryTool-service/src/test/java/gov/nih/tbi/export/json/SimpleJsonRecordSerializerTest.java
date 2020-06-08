package gov.nih.tbi.export.json;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import org.testng.annotations.Test;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.repository.model.RepeatingCellValue;

public class SimpleJsonRecordSerializerTest {
	@Test
	public void cellValueToJsonTest() {
		DataTableColumn column = new DataTableColumn("form", "rg", "de");
		NonRepeatingCellValue cellValue = mock(NonRepeatingCellValue.class);
		when(cellValue.getValue("pv")).thenReturn("testValue");
		InstancedDataTable table = new InstancedDataTable();
		table.setDisplayOption("pv");
		FormResult form = new FormResult();
		List<FormResult> forms = new ArrayList<FormResult>();
		forms.add(form);

		List<RepeatableGroup> rgs = new ArrayList<>();
		RepeatableGroup group = new RepeatableGroup();
		group.setName("rg");
		rgs.add(group);
		DataElement de = new DataElement();
		List<DataElement> des = new ArrayList<>();
		de.setName("de");
		des.add(de);
		table.setForms(forms);
		SimpleJsonRecordSerializer serializer = new SimpleJsonRecordSerializer(table);
		String actual = serializer.cellValueToJson(column, cellValue).toString();
		String expected = "{\"de\":\"testValue\"}";
		assertEquals(actual, expected);
	}

	@Test
	public void cellValueToJsonTest2() {
		DataTableColumn column = new DataTableColumn("form", "rg", null);
		RepeatingCellValue cellValue = mock(RepeatingCellValue.class);
		List<InstancedRepeatableGroupRow> rows = new ArrayList<InstancedRepeatableGroupRow>();
		InstancedRepeatableGroupRow r1 = new InstancedRepeatableGroupRow();

		RepeatingCellColumn c1 = new RepeatingCellColumn("form", "rg", "de1");
		r1.insertCell(c1, "value1");
		RepeatingCellColumn c2 = new RepeatingCellColumn("form", "rg", "de2");
		r1.insertCell(c2, "value2");
		rows.add(r1);

		InstancedRepeatableGroupRow r2 = new InstancedRepeatableGroupRow();
		r2.insertCell(c1, "value1");
		r2.insertCell(c2, "value2");
		rows.add(r2);

		when(cellValue.getRows()).thenReturn(rows);
		InstancedDataTable table = new InstancedDataTable();
		table.setDisplayOption("pv");
		FormResult form = new FormResult();
		List<FormResult> forms = new ArrayList<FormResult>();
		forms.add(form);

		List<RepeatableGroup> rgs = new ArrayList<>();
		RepeatableGroup group = new RepeatableGroup();
		group.setName("rg");
		rgs.add(group);
		DataElement de = new DataElement();
		List<DataElement> des = new ArrayList<>();
		de.setName("de");
		des.add(de);
		table.setForms(forms);
		SimpleJsonRecordSerializer serializer = new SimpleJsonRecordSerializer(table);
		String actual = serializer.cellValueToJson(column, cellValue).toString();
		String expected = "[[{\"de1\":\"value1\"},{\"de2\":\"value2\"}],[{\"de1\":\"value1\"},{\"de2\":\"value2\"}]]";
		assertEquals(actual, expected);
	}

	@Test
	public void serializeRecordTest() {
		NonRepeatingCellValue cellValue = mock(NonRepeatingCellValue.class);
		when(cellValue.getValue("pv")).thenReturn("testValue");
		InstancedDataTable table = new InstancedDataTable();
		table.setDisplayOption("pv");
		FormResult form = new FormResult();
		form.setShortName("form");
		List<FormResult> forms = new ArrayList<FormResult>();
		forms.add(form);

		List<RepeatableGroup> rgs = new ArrayList<>();
		RepeatableGroup group = new RepeatableGroup();
		group.setName("rg");
		rgs.add(group);
		form.setRepeatableGroups(rgs);
		DataElement de = new DataElement();
		List<DataElement> des = new ArrayList<>();
		de.setName("de");
		des.add(de);
		table.setForms(forms);
		SimpleJsonRecordSerializer serializer = new SimpleJsonRecordSerializer(table);

		InstancedRecord record = new InstancedRecord("uri");
		InstancedRow row = new InstancedRow("uri");
		row.setReadableDatasetId("DATA-0000123");
		row.setStudyPrefixedId("STUDY-0000123");
		row.setGuid("GUID");
		row.setFormShortName("form");
		DataTableColumn column = new DataTableColumn("form", "rg", "de");
		row.insertCell(column, cellValue);
		record.addSelectedRow(row);

		String expected = "{\"forms\":[{\"name\":\"form\",\"studyId\":\"STUDY-0000123\",\"datasetId\":\"DATA-0000123\",\"repeatableGroups\":[{\"name\":\"rg\",\"data\":[[{\"de\":\"testValue\"}]]}]}]}";
		String actual = serializer.serializeRecord(record).toString();
		assertEquals(actual, expected);
	}
}
