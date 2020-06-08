package gov.nih.nichd.ctdb.workspace.domain;

/**
 * OverallStatusForm DomainObject for the Proforms
 * 
 * @author Sireesha Kolla
 * 
 */
public class DashboardOverallStatus {
	
	private String assessmentType;
	private String completeVal;
	private String lockedVal;
	private String inProgressVal;
	private String deviationsVal;
	
	/**
	 * Default Constructor for the OverallStatusForm Domain Object
	 */
	public DashboardOverallStatus() {
		// default constructor
	}

	/**
	 * @return the assessmentType
	 */
	public String getAssessmentType() {
		return assessmentType;
	}

	/**
	 * @param assessmentType the assessmentType to set
	 */
	public void setAssessmentType(String assessmentType) {
		this.assessmentType = assessmentType;
	}

	/**
	 * @return the completeVal
	 */
	public String getCompleteVal() {
		return completeVal;
	}

	/**
	 * @param completeVal the completeVal to set
	 */
	public void setCompleteVal(String completeVal) {
		this.completeVal = completeVal;
	}

	public String getLockedVal() {
		return lockedVal;
	}

	public void setLockedVal(String lockedVal) {
		this.lockedVal = lockedVal;
	}

	/**
	 * @return the inProgressVal
	 */
	public String getInProgressVal() {
		return inProgressVal;
	}

	/**
	 * @param inProgressVal the inProgressVal to set
	 */
	public void setInProgressVal(String inProgressVal) {
		this.inProgressVal = inProgressVal;
	}

	/**
	 * @return the deviationsVal
	 */
	public String getDeviationsVal() {
		return deviationsVal;
	}

	/**
	 * @param deviationsVal the deviationsVal to set
	 */
	public void setDeviationsVal(String deviationsVal) {
		this.deviationsVal = deviationsVal;
	}

}
