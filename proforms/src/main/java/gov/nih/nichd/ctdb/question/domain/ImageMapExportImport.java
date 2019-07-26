package gov.nih.nichd.ctdb.question.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class ImageMapExportImport {
	private int id;
	private String imageFileName;
    private int gridResolution = 1;
    private String gridFileName;
    private int height;
    private int width;
    private boolean showGrid = false;
    //private List options;   // list of imageMapOption objects
    private List<ImageMapValuesExportImport>  iMapValues;
    private int imageMapId = Integer.MIN_VALUE;
    private int version;
    private QuestionGraphic mapGraphic;
    
	public QuestionGraphic getMapGraphic() {
		return mapGraphic;
	}
	public void setMapGraphic(QuestionGraphic mapGraphic) {
		this.mapGraphic = mapGraphic;
	}
	public String getImageFileName() {
		return imageFileName;
	}
	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}
	public int getGridResolution() {
		return gridResolution;
	}
	public void setGridResolution(int gridResolution) {
		this.gridResolution = gridResolution;
	}
	public String getGridFileName() {
		return gridFileName;
	}
	public void setGridFileName(String gridFileName) {
		this.gridFileName = gridFileName;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public boolean isShowGrid() {
		return showGrid;
	}
	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}
	public int getImageMapId() {
		return imageMapId;
	}
	public void setImageMapId(int imageMapId) {
		this.imageMapId = imageMapId;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	@XmlElementWrapper(name = "ImageMapValues")
	public List<ImageMapValuesExportImport> getiMapValues() {
		return iMapValues;
	}
	@XmlElement(name = "ImageMapValue")
	public void setiMapValues(List<ImageMapValuesExportImport> iMapValues) {
		this.iMapValues = iMapValues;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
