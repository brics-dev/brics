package gov.nih.tbi.guid.model;

import java.util.List;

public class GuidSearchResult {

	private String guid;

	private String type;

	private String serverShortName;
	
	private String organization;

	private String fullName;

	private String dateCreated;

	private List<String> linked;

	private Boolean detailsFlag;

	private String cohort;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getServerShortName() {
		return serverShortName;
	}

	public void setServerShortName(String serverShortName) {
		this.serverShortName = serverShortName;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public List<String> getLinked() {
		return linked;
	}

	public void setLinked(List<String> linked) {
		this.linked = linked;
	}

	public Boolean getDetailsFlag() {
		return detailsFlag;
	}

	public void setDetailsFlag(Boolean detailsFlag) {
		this.detailsFlag = detailsFlag;
	}

	public String getCohort() {
		return cohort;
	}

	public void setCohort(String cohort) {
		this.cohort = cohort;
	}

}

