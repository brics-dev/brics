package gov.nih.nichd.ctdb.response.domain;

import java.util.Date;

/**
 * Created by wangla.
 * Date: Apr 25, 2019
 * For View Queries.
 */
public class ViewAuditorComment {

	private int administeredformId;
    private String eformName;
	private String count; //#auditcomments
	
	private String guId;
	private String mrn;
	private String subjectId;
	private String subject;
	private int questionId;
	private int sectionId;
	private Date editDate;
	private String questionText;
	private String auditStatus;
	private boolean isCat;
	 
 
    public int getAdministeredformId() {
        return administeredformId;
    }
    public void setAdministeredformId(int administeredformId) {
        this.administeredformId = administeredformId;
    }
    public String getEformName() {
		return eformName;
	}
	public void setEformName(String eformName) {
		this.eformName = eformName;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}

	/* subject */
	public String getGuId() {
		return guId;
	}
	public void setGuId(String guId) {
		this.guId = guId;
	}
	public String getMrn() {
		return mrn;
	}
	public void setMrn(String mrn) {
		this.mrn = mrn;
	}
	public String getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}	
    public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
    public int getQuestionId() {
        return questionId;
    }
    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }
	public int getSectionId() {
		return sectionId;
	}
	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}
    public Date getEditDate() {
        return editDate;
    }
    public void setEditDate(Date editDate) {
        this.editDate = editDate;
    }
    public String getQuestionText() {
        return questionText;
    }
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
	public String getAuditStatus() {
		return auditStatus;
	}
	public void setAuditStatus(String auditStatus) {
		this.auditStatus = auditStatus;
	}
	public boolean getIsCat() {
		return isCat;
	}
	public void setIsCat(boolean isCat) {
		this.isCat = isCat;
	}
    
}
