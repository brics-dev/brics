
package gov.nih.tbi.repository.model;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.DataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CellValueAdapter extends XmlAdapter<CellValueAdapter.CellValueElements, LinkedHashMap<DataTableColumn, CellValue>> {

	/**
	 * Object that contains a list of cell value properties
	 * 
	 * @author fchen
	 * 
	 */
	protected static class CellValueElements {

		public List<CellValueElement> elements = new ArrayList<CellValueElement>();
	}

	/**
	 * Elements that contain properties of a single cell value of any type
	 * 
	 * @author fchen
	 * 
	 */
	protected static class CellValueElement {

		@XmlElement(name = "dataTableColumn")
		public DataTableColumn column;

		@XmlAttribute
		public boolean isRepeating;

		@XmlAttribute
		public int dataElementCount;

		@XmlAttribute
		public String dataElementType;

		@XmlElement(name = "cellValueCode", type = CellValueCode.class)
		public CellValueCode valueCode;

		@XmlElement(name = "instancedRepeatableGroupRow")
		public List<InstancedRepeatableGroupRow> repeatableGroupRows;
	}

	// unmarshall the xml element into cell value
	@Override
	public LinkedHashMap<DataTableColumn, CellValue> unmarshal(CellValueElements v) throws Exception {

		LinkedHashMap<DataTableColumn, CellValue> output = new LinkedHashMap<DataTableColumn, CellValue>();

		for (CellValueElement element : v.elements) {
			if (element.isRepeating) {
				RepeatingCellValue newCellValue = new RepeatingCellValue(DataType.getByValue(element.dataElementType),
						element.dataElementCount, 0);
				newCellValue.setRows(element.repeatableGroupRows);
				output.put(element.column, newCellValue);
			} else {
				NonRepeatingCellValue newCellValue =
						new NonRepeatingCellValue(DataType.getByValue(element.dataElementType), element.valueCode);
				output.put(element.column, newCellValue);
			}
		}

		return output;
	}

	// marshal the cell value into our xml elements
	@Override
	public CellValueElements marshal(LinkedHashMap<DataTableColumn, CellValue> v) throws Exception {

		CellValueElements output = new CellValueElements();

		for (Entry<DataTableColumn, CellValue> cellValueEntry : v.entrySet()) {
			CellValueElement element = new CellValueElement();
			output.elements.add(element);

			element.column = cellValueEntry.getKey();

			if (!cellValueEntry.getValue().isRepeating) {
				NonRepeatingCellValue cellValue = (NonRepeatingCellValue) cellValueEntry.getValue();

				if (cellValue.getValue() != null) {
					// remove 0x3 control value that somehow appeared in production data
					CellValueCode valueCode = cellValue.getValueCode();
					valueCode.setValue(valueCode.getValue().replaceAll("\u0003", ModelConstants.EMPTY_STRING));

					element.valueCode = valueCode;
				}

				element.isRepeating = false;
			} else
			// is a repeating cell value
			{
				RepeatingCellValue cellValue = (RepeatingCellValue) cellValueEntry.getValue();
				element.dataElementCount = cellValue.getDataElementCount();
				element.repeatableGroupRows = cellValue.getRows();
				element.isRepeating = true;
			}
		}

		return output;
	}
}
