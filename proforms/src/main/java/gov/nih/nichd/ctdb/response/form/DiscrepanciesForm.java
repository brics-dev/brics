package gov.nih.nichd.ctdb.response.form;
import gov.nih.nichd.ctdb.common.CtdbForm;

import java.util.Date;
import java.util.List;
/**
 * create by sunny 2012/05/51
 */
public class DiscrepanciesForm  extends CtdbForm{



	private static final long serialVersionUID = 1L;
	String formName;
    String interval="";
	String nihRecordNumber="";
	int administeredformid=0;
	private String guId;
	//Added by yogi to get subject mode in PII Mode
	private String subjectName;

	String resolvedDate=null;
	String resolveUserName="";

	int formId=0;
	
	private List questionList = null;    
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	
	public String getInterval() {
		return interval;
	}
	public void setInterval(String interval) {
		this.interval = interval;
	}   
	public String getNihRecordNumber() {
		return nihRecordNumber;
	}
	public void setNihRecordNumber(String nihRecordNumber) {
		this.nihRecordNumber = nihRecordNumber;
	}
	public int getAdministeredformid() {
		return administeredformid;
	}
	public void setAdministeredformid(int administeredformid) {
		this.administeredformid = administeredformid;
	}
    public List getQuestionList() {
		return questionList;
	}
	public void setQuestionList(List questionList) {
		this.questionList = questionList;
	}	
	public String getResolvedDate() {
		return resolvedDate;
	}
	public void setResolvedDate(String resolvedDate) {
		this.resolvedDate = resolvedDate;
	}
	public String getResolveUserName() {
		return resolveUserName;
	}
	public void setResolveUserName(String resolveUserName) {
		this.resolveUserName = resolveUserName;
	}	
	public int getFormId() {
		return formId;
	}
	public void setFormId(int formId) {
		this.formId = formId;
	}
	public String getGuId() {
		return guId;
	}
	public void setGuId(String guId) {
		this.guId = guId;
	}
	public String getSubjectName() {
		return subjectName;
	}
	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}	


}
