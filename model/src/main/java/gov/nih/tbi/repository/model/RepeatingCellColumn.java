package gov.nih.tbi.repository.model;

import gov.nih.tbi.commons.model.DataType;

public class RepeatingCellColumn extends DataTableColumn {
	private static final long serialVersionUID = -2533398848285790874L;

	private DataType type;

	public RepeatingCellColumn(String form, String repeatableGroup, String dataElement) {
		super(form, repeatableGroup, dataElement);
	}

	public RepeatingCellColumn(String form, String repeatableGroup, String dataElement, DataType type) {
		super(form, repeatableGroup, dataElement);
		this.type = type;
	}

	public DataType getType() {
		return type;
	}

	public void setType(DataType type) {
		this.type = type;
	}
}
