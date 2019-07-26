package gov.nih.nichd.ctdb.workspace.domain;

public class StudyInformation {
	
	private String enrolledSubjects;
	private String totSubjects;
	private String studyDuration;
	/**
	 * @return the enrolledSubjects
	 */
	public String getEnrolledSubjects() {
		return enrolledSubjects;
	}
	/**
	 * @param enrolledSubjects the enrolledSubjects to set
	 */
	public void setEnrolledSubjects(String enrolledSubjects) {
		this.enrolledSubjects = enrolledSubjects;
	}
	
	/**
	 * @return the totSubjects
	 */
	public String getTotSubjects() {
		return totSubjects;
	}
	/**
	 * @param totSubjects the totSubjects to set
	 */
	public void setTotSubjects(String totSubjects) {
		this.totSubjects = totSubjects;
	}
	/**
	 * @return the studyDuration
	 */
	public String getStudyDuration() {
		return studyDuration;
	}
	/**
	 * @param studyDuration the studyDuration to set
	 */
	public void setStudyDuration(String studyDuration) {
		this.studyDuration = studyDuration;
	}
	
}
