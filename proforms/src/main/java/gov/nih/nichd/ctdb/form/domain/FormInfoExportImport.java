package gov.nih.nichd.ctdb.form.domain;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
@XmlType(propOrder = { "fromId", "name", "description", "lockFlag","copyRight","allowMultipleCollectionInstances", "singleDoubleKeyFlag", "dataStructureVersion", "dataStructureName", "formGroupNames",
		"formType","formTypeName","inCtss","tabDisplay","dataSpring","attachFiles","hasSkipRule","hasCalculationRule","hasImageMap","orderValue","numQuestions",
		"formVersion","formBorder","sectionBorder","formColor","sectionColor","formFont","sectionFont","accessFlag","formFontSize","cellpadding","formHeader",
		"formFooter","formDataEntryWorkFlowTypeIntVal","legacy", "formLayoutInfo","sections"})
public class FormInfoExportImport {
	
	//String Fields that Form requires
	private String name;
	private String description;
	private String dataStructureName = "";
    private String dataStructureVersion = "";
    private String formGroupNames;
	private String formTypeName;
	private String formHeader;
	private String formFooter;
	
	
	//this are coming from FormHtmlAttributes
	private String formColor = "Black";
	private String sectionColor = "Black";
	private String formFont = "Arial";
	private String sectionFont = "Arial";
	
	
	//Boolean Fields that create form requires
	private boolean inCtss = false;
	private boolean tabDisplay = false;
	private boolean dataSpring = false;
	private boolean attachFiles = false;
	private boolean hasSkipRule = false;
	private boolean hasCalculationRule = false;
	private boolean hasImageMap = false;
	private boolean lockFlag = false;
	private boolean copyRight = false;
	private boolean allowMultipleCollectionInstances = false;
	//this are coming from FormHtmlAttributes
	private boolean formBorder = true;
	private boolean sectionBorder = true;
	
	//Int Fields that form requires
	private int fromId;
	private int accessFlag = 1;
	private int orderValue;
	private int numQuestions;

	private int formVersion;
	private int formType;
	private int singleDoubleKeyFlag = 1;
	private int formDataEntryWorkFlowTypeIntVal;
	
	//this are coming from FormHtmlAttributes
	private int formFontSize = 10;
    private int cellpadding = 2;
    
    private boolean legacy = true;

	
	
	
	
	//FormLayout Object to hold information for form from formlayout table
	private ArrayList<FormLayout> formLayoutInfo;
	
	//Section information  for form
	//private SectionExportImport sectionInfo;
	private ArrayList<SectionExportImport> sections;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getFromId() {
		return fromId;
	}
	public void setFromId(int fromId) {
		this.fromId = fromId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	

	public boolean isLegacy() {
		return legacy;
	}
	public void setLegacy(boolean legacy) {
		this.legacy = legacy;
	}
	/*public SectionExportImport getSectionInfo() {
		return sectionInfo;
	}
	public void setSectionInfo(SectionExportImport sectionInfo) {
		this.sectionInfo = sectionInfo;
	}*/
	public boolean isLockFlag() {
		return lockFlag;
	}
	public void setLockFlag(boolean lockFlag) {
		this.lockFlag = lockFlag;
	}
	public boolean isCopyRight() {
		return copyRight;
	}
	public void setCopyRight(boolean copyRight) {
		this.copyRight = copyRight;
	}
	public int getSingleDoubleKeyFlag() {
		return singleDoubleKeyFlag;
	}
	public void setSingleDoubleKeyFlag(int singleDoubleKeyFlag) {
		this.singleDoubleKeyFlag = singleDoubleKeyFlag;
	}

	public String getDataStructureVersion() {
		return dataStructureVersion;
	}
	public void setDataStructureVersion(String version) {
		this.dataStructureVersion = version;
	}
	public String getDataStructureName() {
		return dataStructureName;
	}
	public void setDataStructureName(String dataStructureName) {
		this.dataStructureName = dataStructureName;
	}
/*	public boolean isMandatory() {
		return isMandatory;
	}
	public void setMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}*/
	public String getFormGroupNames() {
		return formGroupNames;
	}
	public void setFormGroupNames(String formGroupNames) {
		this.formGroupNames = formGroupNames;
	}
	public int getFormType() {
		return formType;
	}
	public void setFormType(int formType) {
		this.formType = formType;
	}
	public String getFormTypeName() {
		return formTypeName;
	}
	public void setFormTypeName(String formTypeName) {
		this.formTypeName = formTypeName;
	}
	public boolean isInCtss() {
		return inCtss;
	}
	public void setInCtss(boolean inCtss) {
		this.inCtss = inCtss;
	}
	public boolean isTabDisplay() {
		return tabDisplay;
	}
	public void setTabDisplay(boolean tabDisplay) {
		this.tabDisplay = tabDisplay;
	}
	public boolean isDataSpring() {
		return dataSpring;
	}
	public void setDataSpring(boolean dataSpring) {
		this.dataSpring = dataSpring;
	}
	public boolean isAttachFiles() {
		return attachFiles;
	}
	public void setAttachFiles(boolean attachFiles) {
		this.attachFiles = attachFiles;
	}
	public boolean isHasSkipRule() {
		return hasSkipRule;
	}
	public void setHasSkipRule(boolean hasSkipRule) {
		this.hasSkipRule = hasSkipRule;
	}
	public boolean isHasCalculationRule() {
		return hasCalculationRule;
	}
	public void setHasCalculationRule(boolean hasCalculationRule) {
		this.hasCalculationRule = hasCalculationRule;
	}
	public boolean isHasImageMap() {
		return hasImageMap;
	}
	public void setHasImageMap(boolean hasImageMap) {
		this.hasImageMap = hasImageMap;
	}
	public int getOrderValue() {
		return orderValue;
	}
	public void setOrderValue(int orderValue) {
		this.orderValue = orderValue;
	}
	public int getNumQuestions() {
		return numQuestions;
	}
	public void setNumQuestions(int numQuestions) {
		this.numQuestions = numQuestions;
	}
	/*public FormLayout getFormLayoutInfo() {
		return formLayoutInfo;
	}
	public void setFormLayoutInfo(FormLayout formLayoutInfo) {
		this.formLayoutInfo = formLayoutInfo;
	}*/
	public int getFormVersion() {
		return formVersion;
	}
	public void setFormVersion(int formVersion) {
		this.formVersion = formVersion;
	}
	public boolean isFormBorder() {
		return formBorder;
	}
	public void setFormBorder(boolean formBorder) {
		this.formBorder = formBorder;
	}
	public boolean isSectionBorder() {
		return sectionBorder;
	}
	public void setSectionBorder(boolean sectionBorder) {
		this.sectionBorder = sectionBorder;
	}
	public String getFormColor() {
		return formColor;
	}
	public void setFormColor(String formColor) {
		this.formColor = formColor;
	}
	public String getSectionColor() {
		return sectionColor;
	}
	public void setSectionColor(String sectionColor) {
		this.sectionColor = sectionColor;
	}
	public String getFormFont() {
		return formFont;
	}
	public void setFormFont(String formFont) {
		this.formFont = formFont;
	}
	public String getSectionFont() {
		return sectionFont;
	}
	public void setSectionFont(String sectionFont) {
		this.sectionFont = sectionFont;
	}
	public int getAccessFlag() {
		return accessFlag;
	}
	public void setAccessFlag(int accessFlag) {
		this.accessFlag = accessFlag;
	}
	public String getFormHeader() {
		return formHeader;
	}
	public void setFormHeader(String formHeader) {
		this.formHeader = formHeader;
	}
	public String getFormFooter() {
		return formFooter;
	}
	public void setFormFooter(String formFooter) {
		this.formFooter = formFooter;
	}
	public int getFormFontSize() {
		return formFontSize;
	}
	public void setFormFontSize(int formFontSize) {
		this.formFontSize = formFontSize;
	}
	public int getCellpadding() {
		return cellpadding;
	}
	public void setCellpadding(int cellpadding) {
		this.cellpadding = cellpadding;
	}
	public int getFormDataEntryWorkFlowTypeIntVal() {
		return formDataEntryWorkFlowTypeIntVal;
	}
	public void setFormDataEntryWorkFlowTypeIntVal(
			int formDataEntryWorkFlowTypeIntVal) {
		this.formDataEntryWorkFlowTypeIntVal = formDataEntryWorkFlowTypeIntVal;
	}
	public boolean isAllowMultipleCollectionInstances() {
		return allowMultipleCollectionInstances;
	}
	public void setAllowMultipleCollectionInstances(
			boolean allowMultipleCollectionInstances) {
		this.allowMultipleCollectionInstances = allowMultipleCollectionInstances;
	}
	
	@XmlElementWrapper(name = "sections")
	public ArrayList<SectionExportImport> getSections() {
		return sections;
	}
	@XmlElement(name = "section")
	public void setSections(ArrayList<SectionExportImport> sections) {
		this.sections = sections;
	}
	@XmlElementWrapper(name = "formLayoutInformations")
	public ArrayList<FormLayout> getFormLayoutInfo() {
		return formLayoutInfo;
	}
	@XmlElement(name = "formLayoutInformation")
	public void setFormLayoutInfo(ArrayList<FormLayout> formLayoutInfo) {
		this.formLayoutInfo = formLayoutInfo;
	}


}
