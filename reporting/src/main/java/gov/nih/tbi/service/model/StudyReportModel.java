package gov.nih.tbi.service.model;

import java.util.Date;

public class StudyReportModel {

	private String studyTitle;

	private String studyPrefixID;

	private String studyDOI;

	private String studyType;

	private String studyURL;

	private String fundingSource;

	private String studyStarted;

	private String studyEnded;

	private String studyDuration;

	private String studyStatus;

	private String primarySiteName;

	private String primaryPrincipalInvestigator;

	private String principalInvestigators;

	private String associatePrincipalInvestigators;

	private String recruitmentStatus;

	private String numberOfSubjects;

	private String keywords;

	public StudyReportModel() {

	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getNumberOfSubjects() {
		return numberOfSubjects;
	}

	public void setNumberOfSubjects(String numberOfSubjects) {
		this.numberOfSubjects = numberOfSubjects;
	}

	public String getStudyTitle() {
		return studyTitle;
	}

	public void setStudyTitle(String studyTitle) {
		this.studyTitle = studyTitle;
	}

	public String getStudyPrefixID() {
		return studyPrefixID;
	}

	public void setStudyPrefixID(String studyPrefixID) {
		this.studyPrefixID = studyPrefixID;
	}

	public String getStudyDOI() {
		return studyDOI;
	}

	public void setStudyDOI(String studyDOI) {
		this.studyDOI = studyDOI;
	}

	public String getStudyType() {
		return studyType;
	}

	public void setStudyType(String studyType) {
		this.studyType = studyType;
	}

	public String getStudyURL() {
		return studyURL;
	}

	public void setStudyURL(String studyURL) {
		this.studyURL = studyURL;
	}

	public String getFundingSource() {
		return fundingSource;
	}

	public void setFundingSource(String fundingSource) {
		this.fundingSource = fundingSource;
	}

	public String getStudyStarted() {
		return studyStarted;
	}

	public void setStudyStarted(String studyStarted) {
		this.studyStarted = studyStarted;
	}

	public String getStudyEnded() {
		return studyEnded;
	}

	public void setStudyEnded(String studyEnded) {
		this.studyEnded = studyEnded;
	}

	public String getStudyDuration() {
		return studyDuration;
	}

	public void setStudyDuration(String studyDuration) {
		this.studyDuration = studyDuration;
	}

	public String getStudyStatus() {
		return studyStatus;
	}

	public void setStudyStatus(String studyStatus) {
		this.studyStatus = studyStatus;
	}

	public String getPrimarySiteName() {
		return primarySiteName;
	}

	public void setPrimarySiteName(String primarySiteName) {
		this.primarySiteName = primarySiteName;
	}

	public String getPrimaryPrincipalInvestigator() {
		return primaryPrincipalInvestigator;
	}

	public void setPrimaryPrincipalInvestigator(String primaryPrincipalInvestigator) {
		this.primaryPrincipalInvestigator = primaryPrincipalInvestigator;
	}

	public String getPrincipalInvestigators() {
		return principalInvestigators;
	}

	public void setPrincipalInvestigators(String principalInvestigators) {
		this.principalInvestigators = principalInvestigators;
	}

	public String getAssociatePrincipalInvestigators() {
		return associatePrincipalInvestigators;
	}

	public void setAssociatePrincipalInvestigators(String associatePrincipalInvestigators) {
		this.associatePrincipalInvestigators = associatePrincipalInvestigators;
	}

	public String getRecruitmentStatus() {
		return recruitmentStatus;
	}

	public void setRecruitmentStatus(String recruitmentStatus) {
		this.recruitmentStatus = recruitmentStatus;
	}

}
