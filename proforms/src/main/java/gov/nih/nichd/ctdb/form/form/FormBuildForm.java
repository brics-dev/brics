package gov.nih.nichd.ctdb.form.form;

import gov.nih.nichd.ctdb.common.CtdbForm;

public class FormBuildForm extends CtdbForm {
	
	private String mode;
	
	private String formBuildFormId;
	
	private String sectionsJSON;
	
	private String questionsJSON;
	
	private String existingSectionIdsToDeleteJSON;
	
	private String existingQuestionIdsToDeleteJSON;

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getFormBuildFormId() {
		return formBuildFormId;
	}

	public void setFormBuildFormId(String formBuildFormId) {
		this.formBuildFormId = formBuildFormId;
	}

	public String getSectionsJSON() {
		return sectionsJSON;
	}

	public void setSectionsJSON(String sectionsJSON) {
		this.sectionsJSON = sectionsJSON;
	}

	public String getQuestionsJSON() {
		return questionsJSON;
	}

	public void setQuestionsJSON(String questionsJSON) {
		this.questionsJSON = questionsJSON;
	}

	public String getExistingSectionIdsToDeleteJSON() {
		return existingSectionIdsToDeleteJSON;
	}

	public void setExistingSectionIdsToDeleteJSON(
			String existingSectionIdsToDeleteJSON) {
		this.existingSectionIdsToDeleteJSON = existingSectionIdsToDeleteJSON;
	}

	public String getExistingQuestionIdsToDeleteJSON() {
		return existingQuestionIdsToDeleteJSON;
	}

	public void setExistingQuestionIdsToDeleteJSON(
			String existingQuestionIdsToDeleteJSON) {
		this.existingQuestionIdsToDeleteJSON = existingQuestionIdsToDeleteJSON;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
