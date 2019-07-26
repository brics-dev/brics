package gov.nih.nichd.ctdb.form.domain;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class QuestionSectionExportImport {
	private int questionId;
	private int questionVersion;
	private int questionType;
	private int sectionId;
	private int questionOrder;
	private int questionOrderCol=1;
	private boolean suppressFlag;
	private int questionAttributeId;
	private QuestionAttributesExportImport qaei;

	@XmlElementWrapper(name="skipedQuestionList")
	private List <String> QuestionsToSkip;

	public List<String> getQuestionsToSkip() {
		return QuestionsToSkip;
	}

	public void setQuestionsToSkip(List<String> questionsToSkip) {
		QuestionsToSkip = questionsToSkip;
	}

	public QuestionAttributesExportImport getQaei() {
		return qaei;
	}

	public void setQaei(QuestionAttributesExportImport qaei) {
		this.qaei = qaei;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public int getQuestionType() {
		return questionType;
	}

	public void setQuestionType(int questionType) {
		this.questionType = questionType;
	}

	public int getQuestionVersion() {
		return questionVersion;
	}

	public void setQuestionVersion(int questionVersion) {
		this.questionVersion = questionVersion;
	}

	public int getSectionId() {
		return sectionId;
	}

	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}

	public int getQuestionOrder() {
		return questionOrder;
	}

	public void setQuestionOrder(int questionOrder) {
		this.questionOrder = questionOrder;
	}

	public boolean isSuppressFlag() {
		return suppressFlag;
	}

	public void setSuppressFlag(boolean suppressFlag) {
		this.suppressFlag = suppressFlag;
	}

	public int getQuestionAttributeId() {
		return questionAttributeId;
	}

	// @XmlElement(name = "QuestionAttributes")
	public void setQuestionAttributeId(int questionAttributeId) {
		this.questionAttributeId = questionAttributeId;
	}

	public void setQuestionOrderCol(int questionOrderCol) {
		this.questionOrderCol = questionOrderCol;
	}
	public int getQuestionOrderCol() {
		return questionOrderCol;
	}

}
