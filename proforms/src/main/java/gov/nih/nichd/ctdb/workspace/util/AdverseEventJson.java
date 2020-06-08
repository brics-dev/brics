package gov.nih.nichd.ctdb.workspace.util;

import org.json.JSONObject;

import gov.nih.nichd.ctdb.response.domain.AdverseEvent;


public class AdverseEventJson extends JSONObject {
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
	    
	    private String formCompleteddate;
	
	
	public static AdverseEventJson fromAdverseEvent(AdverseEvent ae) {
		AdverseEventJson aejson = new AdverseEventJson();
		aejson.setAdministeredformId(ae.getAdministeredformId());
		aejson.setSubject(ae.getSubject());
		aejson.setFormCompleteddate(ae.getFormCompleteddate());
		/* subject */
		aejson.setGuId(ae.getGuId());
		aejson.setmRN(ae.getmRN());
		aejson.setnRN(ae.getnRN());
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
			
			/* subject */
			obj.put("guId", this.getGuId());
			obj.put("mRN",  this.getmRN());
			obj.put("nRN",  this.getnRN());
			return obj.toString();
		}
		catch(Exception e) {
			return "{}";
		}
	}
}
