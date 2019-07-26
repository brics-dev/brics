package gov.nih.tbi.dictionary.model.hibernate.eform;

public enum QuestionAnswerDataType {
	
	STRING(1l,"String", "^\\w+$"),
	NUMERIC(2l,"Numeric", "^-?[0-9]*\\.?[0-9]*$"),
	DATE(3l,"Date", "yyyy-MM-dd"),
	DATETIME(4l,"Date-Time", "yyyy-MM-dd HH:mm");
	
	private long id;
	private String answerDataType;
	private String answerValidationRule;
	
	QuestionAnswerDataType(long id, String answerDataType, String answerValidationRule) {
		this.id = id;
		this.answerDataType = answerDataType;
		this.answerValidationRule = answerValidationRule;
	}
	
	public String getAnswerDataType() {
		return answerDataType;
	}

	public String getAnswerValidationRule() {
		return answerValidationRule;
	}
	
	public long getId(){
		return this.id;
	}
	
	public static QuestionAnswerDataType questionAnswerDataTypeById(long id) {
        for (QuestionAnswerDataType questionAnswerDataType : QuestionAnswerDataType.values()){
            if (questionAnswerDataType.getId() == id){
                return questionAnswerDataType;
            }
        }
        return null;
    }
}
