package gov.nih.tbi.dictionary.model;

public class CalculationRule {
	
	private String eform_Name;
	private String section_Name;
	private String question_Name;
	private String calculationRule;
	
	
	
	public CalculationRule(String eform_Name, String section_Name, String question_Name, String calculationRule) {
		super();
		this.eform_Name = eform_Name;
		this.section_Name = section_Name;
		this.question_Name = question_Name;
		this.calculationRule = calculationRule;
	}
	public String getEform_Name() {
		return eform_Name;
	}
	public void setEform_Name(String eform_Name) {
		this.eform_Name = eform_Name;
	}
	public String getSection_Name() {
		return section_Name;
	}
	public void setSection_Name(String section_Name) {
		this.section_Name = section_Name;
	}
	public String getQuestion_Name() {
		return question_Name;
	}
	public void setQuestion_Name(String question_Name) {
		this.question_Name = question_Name;
	}
	public String getCalculationRule() {
		return calculationRule;
	}
	public void setCalculationRule(String calculationRule) {
		this.calculationRule = calculationRule;
	}
	
	

}
