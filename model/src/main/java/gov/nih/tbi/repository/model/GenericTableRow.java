
package gov.nih.tbi.repository.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenericTableRow {

	Map<String, Object> cell = new LinkedHashMap<String, Object>();

	public Map<String, Object> getCell() {

		return cell;
	}

	public void setCell(Map<String, Object> cell) {

		this.cell = cell;
	}

	public Object getValueByColumnName(String columnName) {

		if (cell != null) {
			return cell.get(columnName);
		}

		return null;
	}
	
	/**
	 * Get the column value by column name as a String, does null checks before calling toString
	 * @param columnName
	 * @return
	 */
	public String getStringByColumnName(String columnName) {

		if (cell != null) {
			Object value = cell.get(columnName);
			
			if (value != null) {
				return value.toString();
			}
		}

		return null;
	}

	public void addCell(String columnName, Object value) {

		cell.put(columnName, value);
	}
}
