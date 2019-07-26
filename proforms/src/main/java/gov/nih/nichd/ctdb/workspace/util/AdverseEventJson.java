package gov.nih.nichd.ctdb.workspace.util;

import org.json.JSONObject;

import gov.nih.nichd.ctdb.response.domain.AdverseEvent;


public class AdverseEventJson extends JSONObject {
	   private int administeredformId;
	    private String dataElementName;
	    private String subject;


		private String aeStartDate;  //Date
	    private String aeEndDate;
	    private String reasonForEdit;
	    
	    private String formCompleteddate;
	
	
	public static AdverseEventJson fromAdverseEvent(AdverseEvent ae) {
		AdverseEventJson aejson = new AdverseEventJson();
		aejson.setAdministeredformId(ae.getAdministeredformId());
		aejson.setSubject(ae.getSubject());
		aejson.setFormCompleteddate(ae.getFormCompleteddate());
		return aejson;
	}

 
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
	
	public String toString() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("administeredformId", this.getAdministeredformId());
			obj.put("subject", this.getSubject());
			obj.put("formCompleteddate", this.getFormCompleteddate());
			return obj.toString();
		}
		catch(Exception e) {
			return "{}";
		}
	}
}
