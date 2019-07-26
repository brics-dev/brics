package gov.nih.tbi.account.model;

public enum EmailReportType {
	ACCOUNT_REQUEST("Account Request"),
	ACCOUNT_RENEWAL("Account Renewal");
	
	private String name;
	
	EmailReportType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
