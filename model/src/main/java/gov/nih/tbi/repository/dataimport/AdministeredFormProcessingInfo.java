package gov.nih.tbi.repository.dataimport;

import java.util.Map;

public class AdministeredFormProcessingInfo {
	
	private String dataStructure;
	private String studyId;
	private String adminFormId;
	private String finalLockDate;
		
	public AdministeredFormProcessingInfo(String dataStructure, String study, String adminFormId,
			String finalLockDate) {
		super();
		this.dataStructure = dataStructure;
		this.studyId = study;
		this.adminFormId = adminFormId;
		this.finalLockDate = finalLockDate;
	}
	
	public AdministeredFormProcessingInfo(Map<String, Object> result) {
		super();
		this.dataStructure = (String) result.get("data_structure_name");
		this.studyId = (String) result.get("brics_studyid");
		this.adminFormId = (String) result.get("administeredformid").toString();
		this.finalLockDate = (String) result.get("finallockdate").toString();
	}
	public String getDataStructure() {
		return dataStructure;
	}
	public void setDataStructure(String dataStructure) {
		this.dataStructure = dataStructure;
	}
	public String getStudyId() {
		return studyId;
	}
	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}
	public String getAdminFormId() {
		return adminFormId;
	}

	public void setAdminFormId(String adminFormId) {
		this.adminFormId = adminFormId;
	}

	public String getFinalLockDate() {
		return finalLockDate;
	}

	public void setFinalLockDate(String finalLockDate) {
		this.finalLockDate = finalLockDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((adminFormId == null) ? 0 : adminFormId.hashCode());
		result = prime * result + ((dataStructure == null) ? 0 : dataStructure.hashCode());
		result = prime * result + ((finalLockDate == null) ? 0 : finalLockDate.hashCode());
		result = prime * result + ((studyId == null) ? 0 : studyId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdministeredFormProcessingInfo other = (AdministeredFormProcessingInfo) obj;
		if (adminFormId == null) {
			if (other.adminFormId != null)
				return false;
		} else if (!adminFormId.equals(other.adminFormId))
			return false;
		if (dataStructure == null) {
			if (other.dataStructure != null)
				return false;
		} else if (!dataStructure.equals(other.dataStructure))
			return false;
		if (finalLockDate == null) {
			if (other.finalLockDate != null)
				return false;
		} else if (!finalLockDate.equals(other.finalLockDate))
			return false;
		if (studyId == null) {
			if (other.studyId != null)
				return false;
		} else if (!studyId.equals(other.studyId))
			return false;
		return true;
	}

	
	
	

}
