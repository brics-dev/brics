package gov.nih.nichd.ctdb.question.form;

import org.json.JSONException;
import org.json.JSONObject;

public class QuestionOption {

	private String option;
	private String score;
	private String submittedValue;
	
	
	public QuestionOption(JSONObject obj) throws JSONException {
		option = obj.getString("option");
		
		score = obj.getString("score");
		submittedValue = obj.getString("submittedValue");
	}
	
	public String getOption() {
		return option;
	}
	public void setOption(String option) {
		this.option = option;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public String getSubmittedValue() {
		return submittedValue;
	}
	public void setSubmittedValue(String submittedValue) {
		this.submittedValue = submittedValue;
	}
	
	
	
	
}
