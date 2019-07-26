package gov.nih.tbi.repository.model;

public class RepeatingCellColumn extends DataTableColumn {
	private static final long serialVersionUID = -2533398848285790874L;

	private String type;

	public RepeatingCellColumn(String form, String repeatableGroup, String dataElement) {
		super(form, repeatableGroup, dataElement);
	}

	public RepeatingCellColumn(String form, String repeatableGroup, String dataElement, String type) {
		super(form, repeatableGroup, dataElement);
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
