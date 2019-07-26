package gov.nih.nichd.ctdb.response.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jan 4, 2006
 * Time: 7:06:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class EditAnswerDisplay {

    private String username;
    private Date editDate;
    private int questionId;
    private String questionType;
    private String questionName;
    private String questionText;
	private List<String> previousAnswer = new ArrayList<String>();
	private List<String> editedAnswer = new ArrayList<String>();
    private String reasonForEdit;
    private int administeredformId;
    private int sectionId;
    private String sectionName;
    private String dataElementName;
    private String prevAnswer;
    private String editAnswer; 

    public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public String getDataElementName() {
		return dataElementName;
	}

	public void setDataElementName(String dataElementName) {
		this.dataElementName = dataElementName;
	}

	public int getSectionId() {
		return sectionId;
	}

	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}

	public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getEditDate() {
        return editDate;
    }

    public void setEditDate(Date editDate) {
        this.editDate = editDate;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
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

	public List<String> getPreviousAnswer() {
        return previousAnswer;
    }

	public void setPreviousAnswer(List<String> previousAnswer) {
        this.previousAnswer = previousAnswer;
    }

	public List<String> getEditedAnswer() {
        return editedAnswer;
    }

	public void setEditedAnswer(List<String> editedAnswer) {
        this.editedAnswer = editedAnswer;
    }

    public String getReasonForEdit() {
        return reasonForEdit;
    }

    public void setReasonForEdit(String reasonForEdit) {
        this.reasonForEdit = reasonForEdit;
    }

    public int getAdministeredformId() {
        return administeredformId;
    }

    public void setAdministeredformId(int administeredformId) {
        this.administeredformId = administeredformId;
    }

	/**
	 * @return the prevAnswer
	 */
	public String getPrevAnswer() {
		return prevAnswer;
	}

	/**
	 * @param prevAnswer the prevAnswer to set
	 */
	public void setPrevAnswer(String prevAnswer) {
		this.prevAnswer = prevAnswer;
	}

	/**
	 * @return the editAnswer
	 */
	public String getEditAnswer() {
		return editAnswer;
	}

	/**
	 * @param editAnswer the editAnswer to set
	 */
	public void setEditAnswer(String editAnswer) {
		this.editAnswer = editAnswer;
	}
    
}
