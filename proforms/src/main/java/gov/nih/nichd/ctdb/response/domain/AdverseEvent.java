package gov.nih.nichd.ctdb.response.domain;

/**
 * Created by wangla.
 * Date: Feb 22, 2019
 * For Adverse Event Log on Workspace.
 */
public class AdverseEvent {

    private int administeredformId;
    private String dataElementName;
    private String subject;
    /* subject */
	private String guId;
	private String mRN;
	private String nRN;

	private String aeStartDate;  //Date
    private String aeEndDate;
    private String reasonForEdit;
    private String ansUpddate;
    
    private String formCompleteddate;
    
	private String visitdate;
    private String startdate;
    private String enddate;

 
    public int getAdministeredformId() {
        return administeredformId;
    }

    public void setAdministeredformId(int administeredformId) {
        this.administeredformId = administeredformId;
    }
    
	public String getDataElementName() {
		return dataElementName;
	}

	public void setDataElementName(String dataElementName) {
		this.dataElementName = dataElementName;
	}

	public String getSubject() {
        return subject;
    }

	public void setSubject(String subject) {
		this.subject = subject;
	}
	/* subject */
	public String getGuId() {
		return guId;
	}
	public void setGuId(String guId) {
		this.guId = guId;
	}
	public String getmRN() {
		return mRN;
	}
	public void setmRN(String mRN) {
		this.mRN = mRN;
	}
	public String getnRN() {
		return nRN;
	}
	public void setnRN(String nRN) {
		this.nRN = nRN;
	}	
	
    public String getAeStartDate() {
		return aeStartDate;
	}

	public void setAeStartDate(String aeStartDate) {
		this.aeStartDate = aeStartDate;
	}

	public String getAeEndDate() {
		return aeEndDate;
	}

	public void setAeEndDate(String aeEndDate) {
		this.aeEndDate = aeEndDate;
	}
    public String getReasonForEdit() {
        return reasonForEdit;
    }

    public void setReasonForEdit(String reasonForEdit) {
        this.reasonForEdit = reasonForEdit;
    }
    
    public String getAnsUpddate() {
		return ansUpddate;
	}
	public void setAnsUpddate(String ansUpddate) {
		this.ansUpddate = ansUpddate;
	}
	
	public String getFormCompleteddate() {
		return formCompleteddate;
	}

	public void setFormCompleteddate(String formCompleteddate) {
		this.formCompleteddate = formCompleteddate;
	}

	public String getVisitdate() {
		return visitdate;
	}

	public void setVisitdate(String visitdate) {
		this.visitdate = visitdate;
	}

	public String getStartdate() {
		return startdate;
	}

	public void setStartdate(String startdate) {
		this.startdate = startdate;
	}

	public String getEnddate() {
		return enddate;
	}

	public void setEnddate(String enddate) {
		this.enddate = enddate;
	}

}
