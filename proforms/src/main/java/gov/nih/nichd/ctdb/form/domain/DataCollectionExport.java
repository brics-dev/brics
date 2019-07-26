package gov.nih.nichd.ctdb.form.domain;

public class DataCollectionExport {
	
	private int order;  //this represents order of every question on form
	private int sectionId;
	private int questionId;
	private String groupName;
	private String dataElementName;
	private String answer;
	private String submitAnswer;
	private String sectionName;
	private String questionName;
	private String questionText;
	private String columnLabel;
	private int repeatedSectionParent;
	private boolean isRepeatable;
	private int questionOrder; //this represent order of every question just in its section
	


	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public int getSectionId() {
		return sectionId;
	}
	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}
	public int getQuestionId() {
		return questionId;
	}
	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getDataElementName() {
		return dataElementName;
	}
	public void setDataElementName(String dataElementName) {
		this.dataElementName = dataElementName;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public String getSubmitAnswer() {
		return submitAnswer;
	}
	public void setSubmitAnswer(String submitAnswer) {
		this.submitAnswer = submitAnswer;
	}
	public String getSectionName() {
		return sectionName;
	}
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}
	public String getQuestionName() {
		return questionName;
	}
	public void setQuestionName(String questionName) {
		this.questionName = questionName;
	}
	public String getQuestionText() {
		return questionText;
	}
	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}
	public String getColumnLabel() {
		return columnLabel;
	}
	public void setColumnLabel(String columnLabel) {
		this.columnLabel = columnLabel;
	}
	public int getRepeatedSectionParent() {
		return repeatedSectionParent;
	}
	public void setRepeatedSectionParent(int repeatedSectionParent) {
		this.repeatedSectionParent = repeatedSectionParent;
	}
	public boolean isRepeatable() {
		return isRepeatable;
	}
	public void setRepeatable(boolean isRepeatable) {
		this.isRepeatable = isRepeatable;
	}
	public int getQuestionOrder() {
		return questionOrder;
	}
	public void setQuestionOrder(int questionOrder) {
		this.questionOrder = questionOrder;
	}
	
	

}
