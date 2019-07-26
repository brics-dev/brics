package gov.nih.nichd.ctdb.question.domain;

public class QuestionOptionsExportImport {
	
	private String display;
    private double score;
    private String submittedValue;
    private boolean includeOtherOption;
	public String getSubmittedValue() {
		return submittedValue;
	}
	public void setSubmittedValue(String submittedValue) {
		this.submittedValue = submittedValue;
	}
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public boolean isIncludeOtherOption() {
		return includeOtherOption;
	}
	public void setIncludeOtherOption(boolean includeOtherOption) {
		this.includeOtherOption = includeOtherOption;
	} 
	

}
