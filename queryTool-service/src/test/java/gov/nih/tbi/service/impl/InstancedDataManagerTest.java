package gov.nih.tbi.service.impl;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.testng.annotations.Test;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.CellValue;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.repository.model.RepeatingCellValue;
import gov.nih.tbi.service.cache.QueryMetaDataCache;
import gov.nih.tbi.service.impl.InstancedDataManagerImpl;
import gov.nih.tbi.service.model.MetaDataCache;

public class InstancedDataManagerTest {

	InstancedDataManagerImpl instancedDataManager = new InstancedDataManagerImpl();

	@Test
	public void padRepeatableGroupsTest() {
		MetaDataCache metaDataCache = new QueryMetaDataCache();
		FormResult form = new FormResult();

		form.setShortName("form1");

		List<RepeatableGroup> rgs = new ArrayList<>();

		RepeatableGroup rg1 = new RepeatableGroup();
		rg1.setName("rg1");
		DataElement rg1de1 = new DataElement();
		rg1de1.setName("rg1de1");
		rg1de1.setType(DataType.ALPHANUMERIC);
		rg1.addDataElement(metaDataCache, rg1de1);
		DataElement rg1de2 = new DataElement();
		rg1de2.setName("rg1de2");
		rg1de2.setType(DataType.ALPHANUMERIC);
		rg1.addDataElement(metaDataCache, rg1de2);
		DataElement rg1de3 = new DataElement();
		rg1de3.setName("rg1de3");
		rg1de3.setType(DataType.ALPHANUMERIC);
		rg1.addDataElement(metaDataCache, rg1de3);

		RepeatableGroup rg2 = new RepeatableGroup();
		rg2.setName("rg2");
		DataElement rg2de1 = new DataElement();
		rg2de1.setName("rg2de1");
		rg2de1.setType(DataType.ALPHANUMERIC);
		rg2.addDataElement(metaDataCache, rg2de1);
		DataElement rg2de2 = new DataElement();
		rg2de2.setName("rg2de2");
		rg2de2.setType(DataType.ALPHANUMERIC);
		rg2.addDataElement(metaDataCache, rg2de2);
		DataElement rg2de3 = new DataElement();
		rg2de3.setName("rg2de3");
		rg2de3.setType(DataType.ALPHANUMERIC);
		rg2.addDataElement(metaDataCache, rg2de3);

		rgs.add(rg1);
		rgs.add(rg2);
		form.setRepeatableGroups(rgs);

		InstancedDataTable table = new InstancedDataTable();
		InstancedRecord record1 = new InstancedRecord("row1");
		List<InstancedRecord> records = new ArrayList<>();
		records.add(record1);
		table.setInstancedRecords(records);
		InstancedRow row1 = new InstancedRow("row1");
		record1.addSelectedRow(row1);

		LinkedHashMap<DataTableColumn, CellValue> rowMap1 = new LinkedHashMap<>();

		DataTableColumn column1 = new DataTableColumn("form1", "rg1", null);
		RepeatingCellValue rcv1 = new RepeatingCellValue(DataType.ALPHANUMERIC, 3, 3);
		InstancedRepeatableGroupRow rg1Row1 = new InstancedRepeatableGroupRow();
		rg1Row1.insertCell(new RepeatingCellColumn("form1", "rg1", "de1"), "value");
		rg1Row1.insertCell(new RepeatingCellColumn("form1", "rg1", "de2"), "value");
		rg1Row1.insertCell(new RepeatingCellColumn("form1", "rg1", "de3"), "value");
		InstancedRepeatableGroupRow rg1Row2 = new InstancedRepeatableGroupRow();
		rg1Row2.insertCell(new RepeatingCellColumn("form1", "rg1", "de1"), "value");
		rg1Row2.insertCell(new RepeatingCellColumn("form1", "rg1", "de2"), "value");
		rg1Row2.insertCell(new RepeatingCellColumn("form1", "rg1", "de3"), "value");
		InstancedRepeatableGroupRow rg1Row3 = new InstancedRepeatableGroupRow();
		rg1Row3.insertCell(new RepeatingCellColumn("form1", "rg1", "de1"), "value");
		rg1Row3.insertCell(new RepeatingCellColumn("form1", "rg1", "de2"), "value");
		rg1Row3.insertCell(new RepeatingCellColumn("form1", "rg1", "de3"), "value");
		rcv1.addRow(rg1Row1);
		rcv1.addRow(rg1Row2);
		rcv1.addRow(rg1Row3);
		rowMap1.put(column1, rcv1);

		DataTableColumn column2 = new DataTableColumn("form1", "rg2", null);
		RepeatingCellValue rcv2 = new RepeatingCellValue(DataType.ALPHANUMERIC, 3, 3);
		InstancedRepeatableGroupRow rg2Row1 = new InstancedRepeatableGroupRow();
		rg2Row1.insertCell(new RepeatingCellColumn("form1", "rg2", "de1"), "value");
		rg2Row1.insertCell(new RepeatingCellColumn("form1", "rg2", "de2"), "value");
		rg2Row1.insertCell(new RepeatingCellColumn("form1", "rg2", "de3"), "value");
		InstancedRepeatableGroupRow rg2Row2 = new InstancedRepeatableGroupRow();
		rg2Row2.insertCell(new RepeatingCellColumn("form1", "rg2", "de1"), "value");
		rg2Row2.insertCell(new RepeatingCellColumn("form1", "rg2", "de2"), "value");
		rg2Row2.insertCell(new RepeatingCellColumn("form1", "rg2", "de3"), "value");
		InstancedRepeatableGroupRow rg2Row3 = new InstancedRepeatableGroupRow();
		rg2Row3.insertCell(new RepeatingCellColumn("form1", "rg2", "de1"), "value");
		rg2Row3.insertCell(new RepeatingCellColumn("form1", "rg2", "de2"), "value");
		rg2Row3.insertCell(new RepeatingCellColumn("form1", "rg2", "de3"), "value");
		InstancedRepeatableGroupRow rg2Row4 = new InstancedRepeatableGroupRow();
		rg2Row3.insertCell(new RepeatingCellColumn("form1", "rg2", "de1"), "value");
		rg2Row3.insertCell(new RepeatingCellColumn("form1", "rg2", "de2"), "value");
		rg2Row3.insertCell(new RepeatingCellColumn("form1", "rg2", "de3"), "value");
		InstancedRepeatableGroupRow rg2Row5 = new InstancedRepeatableGroupRow();
		rg2Row3.insertCell(new RepeatingCellColumn("form1", "rg2", "de1"), "value");
		rg2Row3.insertCell(new RepeatingCellColumn("form1", "rg2", "de2"), "value");
		rg2Row3.insertCell(new RepeatingCellColumn("form1", "rg2", "de3"), "value");
		rcv2.addRow(rg2Row1);
		rcv2.addRow(rg2Row2);
		rcv2.addRow(rg2Row3);
		rcv2.addRow(rg2Row4);
		rcv2.addRow(rg2Row5);
		rowMap1.put(column2, rcv2);

		row1.setCell(rowMap1);

		instancedDataManager.padRepeatableGroups(form, 0, table);

		assertEquals(rcv1.getRows().size(), 5);
	}
}
