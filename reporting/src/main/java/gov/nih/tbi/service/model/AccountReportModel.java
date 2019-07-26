package gov.nih.tbi.service.model;

public class AccountReportModel {

	
	private String userName;
	
	private String firstName;
	
	private String lastName;
	
	private String email;
	
	private String affiliatedInstitution;
	
	private String applicationDate;
	
	private String lastUpdatedDate;
	
	private String accountStatus;
	
	private String accountRole;

	
	
	
	public AccountReportModel(String userName, String firstName, String lastName, String email,
			String affiliatedInstitution, String applicationDate, String lastUpdatedDate, String accountStatus,
			String accountRole) {
		super();
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.affiliatedInstitution = affiliatedInstitution;
		this.applicationDate = applicationDate;
		this.lastUpdatedDate = lastUpdatedDate;
		this.accountStatus = accountStatus;
		this.accountRole = accountRole;
	}

	public AccountReportModel() {
		// TODO Auto-generated constructor stub
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAffiliatedInstitution() {
		return affiliatedInstitution;
	}

	public void setAffiliatedInstitution(String affiliatedInstitution) {
		this.affiliatedInstitution = affiliatedInstitution;
	}

	public String getApplicationDate() {
		return applicationDate;
	}

	public void setApplicationDate(String applicationDate) {
		this.applicationDate = applicationDate;
	}

	public String getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(String lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public String getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(String accountStatus) {
		this.accountStatus = accountStatus;
	}

	public String getAccountRole() {
		return accountRole;
	}

	public void setAccountRole(String accountRole) {
		this.accountRole = accountRole;
	}
	
	
	
	
	
	
}
