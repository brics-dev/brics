
package gov.nih.tbi.repository.model;

import gov.nih.tbi.ModelConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A single instanced data record, which may contain multiple rows of data
 * 
 * @author Francis Chen
 * 
 */
@XmlRootElement(name = "instancedRecord")
@XmlAccessorType(XmlAccessType.FIELD)
public class InstancedRecord implements Serializable {

	private static final long serialVersionUID = 4389909394258935075L;

	/**
	 * List of selected rows. The first item of the list is considered to be the selected (left) form. All other forms
	 * will be the joined forms.
	 */
	@XmlElementWrapper(name = "selectedRowList")
	@XmlElement(name = "instancedRow", type = InstancedRow.class)
	private List<InstancedRow> selectedRows;

	/**
	 * for non-joined forms, this would be the submission ID, for joined form, this would be which ever value they're
	 * joined on
	 */
	@XmlAttribute()
	private String primaryKey;

	protected InstancedRecord() {
		this.selectedRows = new ArrayList<InstancedRow>(ModelConstants.DEFAULT_NUM_JOINS);
	}

	/**
	 * Creates a new InstancedRecord object with the given primary key. Also an empty list will automatically be set for
	 * the selected row listing.
	 * 
	 * @param primaryKey - The primary key to use for this record object.
	 */
	public InstancedRecord(String primaryKey) {
		this.selectedRows = new ArrayList<InstancedRow>(ModelConstants.DEFAULT_NUM_JOINS);
		this.primaryKey = primaryKey;
	}

	public void addAll(InstancedRecord record) {
		if (selectedRows == null) {
			selectedRows = new ArrayList<>();
		}

		if (record != null) {
			selectedRows.addAll(record.getSelectedRows());
		}
	}

	public String getPrimaryKey() {

		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {

		this.primaryKey = primaryKey;
	}

	/**
	 * Adds a row to the list of selected rows for this record object. Null is no longer considered a valid indicator
	 * for records with no rows. Instead assign an empty list to {@link #setSelectedRows(List)}.
	 * 
	 * @param newRow - a InstancedRow object to be added to the selected rows list.
	 * @throws IllegalArgumentException When null is passed in for the new row object.
	 */
	public void addSelectedRow(InstancedRow newRow) throws IllegalArgumentException {
		this.selectedRows.add(newRow);
	}

	/**
	 * Replaces the row at the given index with the given new row
	 * 
	 * @param index
	 * @param newRow
	 * @throws IllegalArgumentException
	 */
	public void setSelectedRow(int index, InstancedRow newRow) throws IllegalArgumentException {
		this.selectedRows.set(index, newRow);
	}

	public List<InstancedRow> getSelectedRows() {

		return selectedRows;
	}

	/**
	 * Assigns the given listing of InstancedRow objects as the new list of selected rows for this record object. If
	 * null is given, the current list will be replaced with a new empty list.
	 * 
	 * @param selectedRows - The new list of selected rows to be assigned to this object.
	 */
	public void setSelectedRows(List<InstancedRow> selectedRows) {
		if (this.selectedRows == null) {
			this.selectedRows = new ArrayList<>();
		}

		this.selectedRows.clear();

		if (selectedRows != null) {
			this.selectedRows.addAll(selectedRows);
		}
	}


	public boolean hasSubmissionId(String submissionId) {

		if (selectedRows.isEmpty()) {
			return false;
		}

		boolean idFound = false;

		for (InstancedRow row : selectedRows) {
			if ((row != null) && row.getSubmissionId().equals(submissionId)) {
				idFound = true;
				break;
			}
		}

		return idFound;
	}
}
