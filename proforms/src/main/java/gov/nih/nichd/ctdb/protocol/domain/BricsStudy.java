package gov.nih.nichd.ctdb.protocol.domain;

import java.util.Date;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class BricsStudy extends CtdbDomainObject
{
	private static final long serialVersionUID = -2046316039036979672L;
	
	private String title;
	private String prefixedId; // the BRICS study ID to be displayed instead of the primary key
	private String abstractText;
	private String recruitmentStatus;
	private String principalInvestigator;
	private String principalInvestigatorEmail;
	private Date dateCreated;
	private String studyStatus;
	private String studyPermission;
	private boolean linkedInOtherStudy;
	private String studyType;
	private String studyStartDate;
	private String studyEndDate;
	private int studyNumberSubjects;

	public BricsStudy() {
		super();
		
		// Initialize the member variables
		title = "";
		prefixedId = "";
		abstractText = "";
		recruitmentStatus = "";
		principalInvestigator = "";
		principalInvestigatorEmail = "";
		dateCreated = null;
		studyStatus = "";
		linkedInOtherStudy = false;
		studyType = "";
	}

	@Override
	public Document toXML() throws TransformationException {
		throw new UnsupportedOperationException("toXML() not supported in BricsStudy.");
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the prefixedId
	 */
	public String getPrefixedId() {
		return prefixedId;
	}

	/**
	 * @param prefixedId the prefixedId to set
	 */
	public void setPrefixedId(String prefixedId) {
		this.prefixedId = prefixedId;
	}

	/**
	 * @return the abstractText
	 */
	public String getAbstractText() {
		return abstractText;
	}

	/**
	 * @param abstractText the abstractText to set
	 */
	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	/**
	 * @return the recruitmentStatus
	 */
	public String getRecruitmentStatus() {
		return recruitmentStatus;
	}

	/**
	 * @param recruitmentStatus the recruitmentStatus to set
	 */
	public void setRecruitmentStatus(String recruitmentStatus) {
		this.recruitmentStatus = recruitmentStatus;
	}

	/**
	 * @return the principalInvestigator
	 */
	public String getPrincipalInvestigator() {
		return principalInvestigator;
	}

	/**
	 * @param principalInvestigator the principalInvestigator to set
	 */
	public void setPrincipalInvestigator(String principalInvestigator) {
		this.principalInvestigator = principalInvestigator;
	}

	/**
	 * @return the principalInvestigatorEmail
	 */
	public String getPrincipalInvestigatorEmail() {
		return principalInvestigatorEmail;
	}

	/**
	 * @param principalInvestigatorEmail the principalInvestigatorEmail to set
	 */
	public void setPrincipalInvestigatorEmail(String principalInvestigatorEmail) {
		this.principalInvestigatorEmail = principalInvestigatorEmail;
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return the studyStatus
	 */
	public String getStudyStatus() {
		return studyStatus;
	}

	/**
	 * @param studyStatus the studyStatus to set
	 */
	public void setStudyStatus(String studyStatus) {
		this.studyStatus = studyStatus;
	}

	/**
	 * @return the linkedInOtherStudy
	 */
	public boolean isLinkedInOtherStudy() {
		return linkedInOtherStudy;
	}

	/**
	 * @param linkedInOtherStudy the linkedInOtherStudy to set
	 */
	public void setLinkedInOtherStudy(boolean linkedInOtherStudy) {
		this.linkedInOtherStudy = linkedInOtherStudy;
	}

	/**
	 * @return the studyPermission
	 */
	public String getStudyPermission() {
		return studyPermission;
	}

	/**
	 * @param studyPermission the studyPermission to set
	 */
	public void setStudyPermission(String studyPermission) {
		this.studyPermission = studyPermission;
	}

	
	public String getStudyType() {
		return studyType;
	}

	public void setStudyType(String studyType) {
		this.studyType = studyType;
	}
	
	
	public int getStudyNumberSubjects() {
		return studyNumberSubjects;
	}

	public void setStudyNumberSubjects(int studyNumberSubjects) {
		this.studyNumberSubjects = studyNumberSubjects;
	}
	
	
	
	public String getStudyStartDate() {
		return studyStartDate;
	}

	public void setStudyStartDate(String studyStartDate) {
		this.studyStartDate = studyStartDate;
	}

	public String getStudyEndDate() {
		return studyEndDate;
	}

	public void setStudyEndDate(String studyEndDate) {
		this.studyEndDate = studyEndDate;
	}


}
