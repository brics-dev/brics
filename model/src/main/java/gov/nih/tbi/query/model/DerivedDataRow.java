package gov.nih.tbi.query.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This represents a row of data inside the derived data returned by query tool
 * @author fchen
 *
 */
public class DerivedDataRow {
	//this is used as map of data element to its value in the row
	private Map<String, String> row = new LinkedHashMap<String, String>();

	public Map<String, String> getRow() {
		return row;
	}

	public void setRow(Map<String, String> row) {
		this.row = row;
	}

	public void put(String key, String value) {
		this.row.put(key, value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((row == null) ? 0 : row.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DerivedDataRow other = (DerivedDataRow) obj;
		if (row == null) {
			if (other.row != null)
				return false;
		} else if (!row.equals(other.row))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DerivedDataRow [row=" + row + "]";
	}
}
