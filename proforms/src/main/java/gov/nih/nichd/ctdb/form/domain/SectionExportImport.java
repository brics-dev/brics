package gov.nih.nichd.ctdb.form.domain;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
//@XmlType(propOrder = { "sectionId", "orderValue", "row", "col","initRepeatedSections", "maxRepeatedSections", "repeatedSectionParent", "name", "description","instructionalText","altLabel","textDisplayed","intob","collapsable","isResponseImage","isRepeatable","responseImage","sectionQuestion" })
public class SectionExportImport {
	
	private int sectionId;
	private int orderValue;
	private int row;
	private int col;
	private int initRepeatedSections = -1;
	private int maxRepeatedSections = -1;
	private int repeatedSectionParent = -1;
	
	
	private String name;
	private String description;
	private String instructionalText;
	private String altLabel;
	
	private boolean textDisplayed = true;
	private boolean intob;
	private boolean collapsable = false;
	private boolean isResponseImage = false;
	private boolean isRepeatable = false;
	
	private boolean gridtype = false;
	
	private int tableGroupId = 0;
	
	private int tableHeaderType = 0;

	private ResponseImage responseImage;
	
	private ArrayList<QuestionSectionExportImport>  sectionQuestion;
	
	
	
	
	public int getTableGroupId() {
		return tableGroupId;
	}
	public void setTableGroupId(int tableGroupId) {
		this.tableGroupId = tableGroupId;
	}
	
	
	
	public int getTableHeaderType() {
		return tableHeaderType;
	}
	public void setTableHeaderType(int tableHeaderType) {
		this.tableHeaderType = tableHeaderType;
	}
	
	public boolean isGridtype() {
		return gridtype;
	}
	public void setGridtype(boolean gridtype) {
		this.gridtype = gridtype;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getOrderValue() {
		return orderValue;
	}
	public void setOrderValue(int orderValue) {
		this.orderValue = orderValue;
	}
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public boolean isTextDisplayed() {
		return textDisplayed;
	}
	public void setTextDisplayed(boolean textDisplayed) {
		this.textDisplayed = textDisplayed;
	}
	public String getInstructionalText() {
		return instructionalText;
	}
	public void setInstructionalText(String instructionalText) {
		this.instructionalText = instructionalText;
	}
	public boolean isIntob() {
		return intob;
	}
	public void setIntob(boolean intob) {
		this.intob = intob;
	}
	public String getAltLabel() {
		return altLabel;
	}
	public void setAltLabel(String altLabel) {
		this.altLabel = altLabel;
	}
	public boolean isCollapsable() {
		return collapsable;
	}
	public void setCollapsable(boolean collapsable) {
		this.collapsable = collapsable;
	}
	public boolean isResponseImage() {
		return isResponseImage;
	}
	public void setResponseImage(boolean isResponseImage) {
		this.isResponseImage = isResponseImage;
	}
	public ResponseImage getResponseImage() {
		return responseImage;
	}
	public void setResponseImage(ResponseImage responseImage) {
		this.responseImage = responseImage;
	}
	public boolean isRepeatable() {
		return isRepeatable;
	}
	public void setRepeatable(boolean isRepeatable) {
		this.isRepeatable = isRepeatable;
	}
	public int getInitRepeatedSections() {
		return initRepeatedSections;
	}
	public void setInitRepeatedSections(int initRepeatedSections) {
		this.initRepeatedSections = initRepeatedSections;
	}
	
	@XmlElementWrapper(name = "sectionQuestions")
	public ArrayList<QuestionSectionExportImport> getSectionQuestion() {
		return sectionQuestion;
	}
	@XmlElement(name = "sectionQuestion")
	public void setSectionQuestion(
			ArrayList<QuestionSectionExportImport> sectionQuestion) {
		this.sectionQuestion = sectionQuestion;
	}
	public int getMaxRepeatedSections() {
		return maxRepeatedSections;
	}
	public void setMaxRepeatedSections(int maxRepeatedSections) {
		this.maxRepeatedSections = maxRepeatedSections;
	}
	public int getRepeatedSectionParent() {
		return repeatedSectionParent;
	}
	public void setRepeatedSectionParent(int repeatedSectionParent) {
		this.repeatedSectionParent = repeatedSectionParent;
	}
	public int getSectionId() {
		return sectionId;
	}
	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}
}
