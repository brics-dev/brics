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
	private String inCompleteVal;
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

	/**
	 * @return the inCompleteVal
	 */
	public String getInCompleteVal() {
		return inCompleteVal;
	}

	/**
	 * @param inCompleteVal the inCompleteVal to set
	 */
	public void setInCompleteVal(String inCompleteVal) {
		this.inCompleteVal = inCompleteVal;
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
