package gov.nih.tbi.account.model;

public enum AccountEmailReportFrequency {
	DAILY("Daily"),
	MONDAY("Monday"),
	TUESDAY("Tuesday"),
	WEDNESDAY("Wednesday"),
	THURSDAY("Thursday"),
	FRIDAY("Friday"),
	MONTHLY("Monthly");
	
	private String name;
	
	AccountEmailReportFrequency(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
