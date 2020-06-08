package gov.nih.tbi.export.json;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.CellValue;
import gov.nih.tbi.repository.model.CellValueCode;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.repository.model.RepeatingCellValue;

public class SimpleJsonRecordSerializer extends AbstractJsonRecordSerializer implements JsonRecordSerializer {

	public SimpleJsonRecordSerializer(InstancedDataTable instancedDataTable) {
		super(instancedDataTable);
	}

	//if we integrate this with query tool, we will need to implement the visible column check
	@Override
	public JsonObject serializeRecord(InstancedRecord record) {
		JsonObject jObject = new JsonObject();

		if (instancedDataTable.isJoined()) {
			jObject.addProperty("guid", record.getPrimaryKey());
		}

		JsonArray formsArray = new JsonArray();
		List<FormResult> forms = instancedDataTable.getForms();

		for (int i = 0; i < record.getSelectedRows().size(); i++) {
			FormResult currentForm = forms.get(i);
			InstancedRow currentRow = record.getSelectedRows().get(i);
			JsonObject rowJson = serializeRow(currentForm, currentRow);
			formsArray.add(rowJson);
		}

		jObject.add("forms", formsArray);

		return jObject;
	}

	protected JsonObject serializeRow(FormResult form, InstancedRow row) {
		if (row == null) {
			return null;
		}

		JsonObject jObject = new JsonObject();
		jObject.addProperty("name", row.getFormShortName());
		jObject.addProperty("studyId", row.getStudyPrefixedId());
		jObject.addProperty("datasetId", row.getReadableDatasetId());

		ListMultimap<String, DataTableColumn> rgColumnMap = ArrayListMultimap.create();

		for (DataTableColumn currentColumn : row.getCell().keySet()) {
			rgColumnMap.put(currentColumn.getRepeatableGroup(), currentColumn);
		}

		JsonArray rgArray = new JsonArray();
		for (RepeatableGroup group : form.getRepeatableGroups()) {
			Collection<DataTableColumn> columns = rgColumnMap.get(group.getName());
			if (columns != null) {
				JsonObject rgObject = serializeGroup(group, columns, row);
				rgArray.add(rgObject);
			}
		}

		jObject.add("repeatableGroups", rgArray);

		return jObject;
	}

	protected JsonObject serializeGroup(RepeatableGroup group, Collection<DataTableColumn> columns, InstancedRow row) {
		JsonObject jObject = new JsonObject();

		jObject.addProperty("name", group.getName());
		JsonArray dataArray = new JsonArray();

		for (DataTableColumn column : columns) {
			CellValue cv = row.getCellValue(column);
			dataArray.add(cellValueToJson(column, cv));
		}

		int dataSize = dataArray.size();

		if (dataSize == 0) { // no data added, don't do anything
			return jObject;
		} else if (group.doesRepeat()) {
			jObject.add("data", dataArray);
		} else {
			// Doesn't repeat
			// Ryan says it's better to wrap non-repeating data in an array so it's more
			// consistent.
			JsonArray wrapArray = new JsonArray();
			wrapArray.add(dataArray);
			jObject.add("data", wrapArray);
		}

		return jObject;
	}

	/**
	 * Converts the given CellValue into a JsonElement. If CellValue does not repeat, return the singular value as a
	 * JsonPrimitive. If CellValue repeats, then return the values as a JsonArray.
	 * 
	 * @param cv
	 * @return
	 */
	protected JsonElement cellValueToJson(DataTableColumn parentColumn, CellValue cv) {
		JsonElement jElement = null;

		if (cv == null) {
			return new JsonPrimitive("");
		}

		if (cv instanceof NonRepeatingCellValue) {
			NonRepeatingCellValue ncv = (NonRepeatingCellValue) cv;
			jElement = new JsonObject();
			((JsonObject) jElement).addProperty(parentColumn.getDataElement(), ncv.getValue(super.displayOption));
		} else if (cv instanceof RepeatingCellValue) {
			jElement = new JsonArray();
			RepeatingCellValue rcv = (RepeatingCellValue) cv;
			for (InstancedRepeatableGroupRow row : rcv.getRows()) {
				JsonArray currentArray = new JsonArray();
				for (Entry<RepeatingCellColumn, CellValueCode> rcvEntry : row.getCell().entrySet()) {
					RepeatingCellColumn currentColumn = rcvEntry.getKey();
					CellValueCode cellValueCode = rcvEntry.getValue();

					String propertyName = currentColumn.getDataElement();
					String currentCellValue = cellValueCode.getValue(super.displayOption);

					JsonObject currentObject = new JsonObject();
					currentObject.addProperty(propertyName, currentCellValue);
					currentArray.add(currentObject);
				}
				((JsonArray) jElement).add(currentArray);
			}
		}

		return jElement;
	}
}
