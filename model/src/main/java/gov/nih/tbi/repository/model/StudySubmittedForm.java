package gov.nih.tbi.repository.model;

public class StudySubmittedForm {
	
	private String studyTitle;
	private String fSTitle;
	private String fSShortName;
	private String numberOfRecords;
	private String datasetStatus;
	
	
	
	public StudySubmittedForm(String studyTitle, String fSTitle, String fSShortName, String numberOfRecords, String datasetStatus) {
		super();
		this.studyTitle = studyTitle;
		this.fSTitle = fSTitle;
		this.fSShortName = fSShortName;
		this.numberOfRecords = numberOfRecords;
		this.datasetStatus = datasetStatus;
	}
	public String getStudyTitle() {
		return studyTitle;
	}
	public void setStudyTitle(String studyTitle) {
		this.studyTitle = studyTitle;
	}
	public String getfSTitle() {
		return fSTitle;
	}
	public void setfSTitle(String fSTitle) {
		this.fSTitle = fSTitle;
	}
	public String getfSShortName() {
		return fSShortName;
	}
	public void setfSShortName(String fSShortName) {
		this.fSShortName = fSShortName;
	}
	public String getNumberOfRecords() {
		return numberOfRecords;
	}
	public void setNumberOfRecords(String numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}
	public String getDatasetStatus() {
		return datasetStatus;
	}
	public void setDatasetStatus(String datasetStatus) {
		this.datasetStatus = datasetStatus;
	}
	
}
