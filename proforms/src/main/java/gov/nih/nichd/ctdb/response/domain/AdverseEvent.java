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

	private String aeStartDate;  //Date
    private String aeEndDate;
    private String reasonForEdit;
    
    private String formCompleteddate;

 
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
    

	public String getFormCompleteddate() {
		return formCompleteddate;
	}

	public void setFormCompleteddate(String formCompleteddate) {
		this.formCompleteddate = formCompleteddate;
	}

}
