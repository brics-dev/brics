package gov.nih.tbi.repository.model;

import java.util.ArrayList;
import java.util.List;

public class PublicSubmittedForm {
	
	private String fSTitle;
	private String fSShortName;
	private List<String> studyTitleList;
	private String numberOfStudies;
	private String numberOfRecords;
	
	public PublicSubmittedForm() {	
	}
	
	public PublicSubmittedForm(String fSTitle, String fSShortName, String numberOfRecords) {
		this.fSTitle = fSTitle;
		this.fSShortName = fSShortName;
		this.studyTitleList = new ArrayList<String>();
		this.numberOfStudies = String.valueOf(studyTitleList.size());
		this.numberOfRecords = numberOfRecords;
	}
	
	public PublicSubmittedForm(String fSTitle, String fSShortName,List<String> studyTitleList, String numberOfRecords) {
		this.fSTitle = fSTitle;
		this.fSShortName = fSShortName;
		this.studyTitleList = studyTitleList;
		this.numberOfStudies = String.valueOf(studyTitleList.size());
		this.numberOfRecords = numberOfRecords;
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
	public List<String> getStudyTitleList() {
		return studyTitleList;
	}
	public void setStudyTitleList(List<String> studyTitleList) {
		this.studyTitleList = studyTitleList;
	}
	public String getNumberOfStudies() {
		return numberOfStudies;
	}
	public void setNumberOfStudies(String numberOfStudiess) {
		this.numberOfStudies = numberOfStudies;
	}
	public String getNumberOfRecords() {
		return numberOfRecords;
	}
	public void setNumberOfRecords(String numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}

}
