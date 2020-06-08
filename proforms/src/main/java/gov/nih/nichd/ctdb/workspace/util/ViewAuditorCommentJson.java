package gov.nih.nichd.ctdb.workspace.util;

import org.json.JSONObject;

import gov.nih.nichd.ctdb.response.domain.ViewAuditorComment;


public class ViewAuditorCommentJson extends JSONObject {
	
	private int administeredformId;
    private String eformName;
	private String count; //#auditcomments
	
	/* subject */
	private String guId;
	private String mrn;
	private String subjectId;
	private String subject;
	
	public static ViewAuditorCommentJson fromViewQuery(ViewAuditorComment vq) {
		ViewAuditorCommentJson vqjson = new ViewAuditorCommentJson();
		vqjson.setAdministeredformId(vq.getAdministeredformId());
		vqjson.setEformName(vq.getEformName());
		vqjson.setCount(vq.getCount());
		
		/* subject */
		vqjson.setGuId(vq.getGuId());
		vqjson.setMrn(vq.getMrn());
		vqjson.setSubjectId(vq.getSubjectId());
		vqjson.setSubject(vq.getSubject());
		
		return vqjson;
	}

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
	
	public String toString() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("administeredformId", this.getAdministeredformId());
			obj.put("eformName", this.getEformName());
			obj.put("count", this.getCount());
			
			/* subject */
			obj.put("guId", this.getGuId());
			obj.put("mrn", this.getMrn());
			obj.put("subjectId", this.getSubjectId());
			obj.put("subject", this.getSubject());
			
			return obj.toString();
		}
		catch(Exception e) {
			return "{}";
		}
	}
}
