package gov.nih.nichd.ctdb.question.domain;

public class ImageMapValuesExportImport {
	private int imageMapId;
	private int imageMapRow;
	private int imageMapColumn;
	private String imageOption;
	public int getImageMapRow() {
		return imageMapRow;
	}
	public void setImageMapRow(int imageMapRow) {
		this.imageMapRow = imageMapRow;
	}
	public int getImageMapColumn() {
		return imageMapColumn;
	}
	public void setImageMapColumn(int imageMapColumn) {
		this.imageMapColumn = imageMapColumn;
	}
	public String getImageOption() {
		return imageOption;
	}
	public void setImageOption(String imageOption) {
		this.imageOption = imageOption;
	}
	public int getImageMapId() {
		return imageMapId;
	}
	public void setImageMapId(int imageMapId) {
		this.imageMapId = imageMapId;
	}

}
