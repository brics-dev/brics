package gov.nih.tbi.account.model.hibernate;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.account.model.AccountType;
import gov.nih.tbi.account.model.EmailReportType;
import gov.nih.tbi.account.model.PermissionAuthority;
import gov.nih.tbi.account.model.SignatureType;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.Country;
import gov.nih.tbi.commons.model.hibernate.State;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.commons.util.ValUtil;

/**
 * Model for storing accounts
 * 
 * @author Francis Chen
 */
@Entity
@Table(name = "ACCOUNT")
@XmlRootElement(name = "account")
@XmlAccessorType(XmlAccessType.FIELD)
public class Account implements Serializable, PermissionAuthority {

	private static final long serialVersionUID = -288792190452003023L;
	/**********************************************************************/
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCOUNT_SEQ")
	@SequenceGenerator(name = "ACCOUNT_SEQ", sequenceName = "ACCOUNT_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "USER_NAME")
	private String userName;

	@Column(name = "PASSWORD")
	private byte[] password;

	@OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "USER_ID")
	private User user;

	@Column(name = "AFFILIATED_INSTITUTION")
	private String affiliatedInstitution;

	@Column(name = "ERA_COMMONS_ID")
	private String eraId;

	@Column(name = "ADDRESS_1")
	private String address1;

	@Column(name = "ADDRESS_2")
	private String address2;

	@Column(name = "CITY")
	private String city;

	@OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "STATE_ID")
	private State state;

	@Column(name = "POSTAL_CODE")
	private String postalCode;

	@OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "COUNTRY_ID")
	private Country country;

	@Column(name = "PHONE")
	private String phone;

	@Column(name = "INTEREST_IN_TBI")
	private String interestInTbi;

	@Column(name = "RECOVERY_DATE")
	private Date recoveryDate;

	@Column(name = "IS_ACTIVE")
	private Boolean isActive;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "account", targetEntity = AccountRole.class, orphanRemoval = true)
	private Set<AccountRole> accountRoleList;

	@XmlTransient
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE, mappedBy = "account", targetEntity = PermissionGroupMember.class, orphanRemoval = true)
	private Set<PermissionGroupMember> permissionGroupMemberList;

	@XmlTransient
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "account", targetEntity = AccountHistory.class, orphanRemoval = true)
	private Set<AccountHistory> accountHistory;
	
	@XmlTransient
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "account", targetEntity = AccountAdministrativeNote.class, orphanRemoval = true)
	private Set<AccountAdministrativeNote> accountAdministrativeNotes;
	
	@XmlTransient
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "account", targetEntity = AccountEmailReportSetting.class, orphanRemoval = true)
	private Set<AccountEmailReportSetting> accountEmailReportSettings;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "account", targetEntity = ElectronicSignature.class, orphanRemoval = true)
	private Set<ElectronicSignature> electronicSignatures;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "account", targetEntity = TwoFactorAuthentication.class, orphanRemoval = true)
	private Set<TwoFactorAuthentication> twoFactorAuthentications;
	
	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "account", targetEntity = AccountReportingLog.class, orphanRemoval = true)
	private Set<AccountReportingLog> accountReportingLog;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "ACCOUNT_STATUS_ID")
	private AccountStatus accountStatus;

	@Column(name = "ADMIN_NOTE")
	private String adminNote;

	@Column(name = "APPLICATION_DATE")
	private Date applicationDate;

	@Column(name = "SUCCESSFUL_LOGIN_DATE")
	private Date lastSuccessfulLogin;

	@Column(name = "ACCOUNT_UNLOCK_DATE")
	private Date unlockDate;

	@Column(name = "PASSWORD_EXPIRATION_DATE")
	private Date passwordExpirationDate;

	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdatedDate;

	@Column(name = "HIDE_ACCESS_RECORDS")
	private Boolean hideAccessRecords;

	@Column(name = "IS_LOCKED")
	private Boolean isLocked;

	@Column(name = "LOCKED_UNTIL")
	private Date lockedUntil;

	@Column(name = "FAILED_INTERVAL_START")
	private Date failedIntervalStart;

	@Column(name = "FAILED_CONSECUTIVE_COUNT")
	private Integer failedConsecutiveCount;

	@Column(name = "FAILED_INTERVAL_COUNT")
	private Integer failedIntervalCount;

	@Column(name = "SALT")
	private String salt;
	
	@Column(name="REQUEST_SUBMIT_DATE")
	private Date requestSubmitDate;
	
	@Column(name="INITIAL_REQUEST_DATE")
	private Date initialRequestDate;

	@Transient
	private String diseaseKey = "";
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "ACCOUNT_REPORTING_TYPE_ID")
	private AccountType accountReportingType;

	public Account() {
		user = new User();
		accountRoleList = new HashSet<AccountRole>();
		permissionGroupMemberList = new HashSet<PermissionGroupMember>();
		electronicSignatures = new HashSet<ElectronicSignature>();
		twoFactorAuthentications = new HashSet<TwoFactorAuthentication>();
	}
	
	public Account(Long id, String userName, User user, AccountStatus accountStatus, String affiliatedInstitution,
			Date requestSubmitDate, Date lastUpdatedDate, Date applicationDate) {
		this.id = id;
		this.userName = userName;
		this.user = user;
		this.accountStatus = accountStatus;
		this.affiliatedInstitution = affiliatedInstitution;
		this.requestSubmitDate = requestSubmitDate;
		this.lastUpdatedDate = lastUpdatedDate;
		this.applicationDate = applicationDate;
	}

	public Long getId() {

		return id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public String getUserName() {

		return userName;
	}

	public void setUserName(String userName) {

		this.userName = userName;
	}

	public byte[] getPassword() {

		return password;
	}

	public void setPassword(byte[] password) {

		this.password = password;
	}

	public User getUser() {

		return user;
	}

	public void setUser(User user) {

		this.user = user;
	}

	public Long getUserId() {

		if (user != null) {
			return user.getId();
		} else {
			return null;
		}
	}

	public String getAffiliatedInstitution() {

		return affiliatedInstitution;
	}

	public void setAffiliatedInstitution(String affiliatedInstitution) {

		this.affiliatedInstitution = affiliatedInstitution;
	}

	public String getEraId() {

		return eraId;
	}

	public void setEraId(String eraId) {

		this.eraId = eraId;
	}

	public String getAddress1() {

		return address1;
	}

	public void setAddress1(String address1) {

		this.address1 = address1;
	}

	public String getAddress2() {

		return address2;
	}

	public void setAddress2(String address2) {

		this.address2 = address2;
	}

	public String getCity() {

		return city;
	}

	public void setCity(String city) {

		this.city = city;
	}

	public State getState() {

		return state;
	}

	public void setState(State state) {

		this.state = state;
	}

	public String getPostalCode() {

		return postalCode;
	}

	public void setPostalCode(String postalCode) {

		this.postalCode = postalCode;
	}

	public Country getCountry() {

		return country;
	}

	public void setCountry(Country country) {

		this.country = country;
	}

	public String getPhone() {

		return phone;
	}

	public void setPhone(String phone) {

		this.phone = phone;
	}

	public String getInterestInTbi() {

		return interestInTbi;
	}

	public void setInterestInTbi(String interestInTbi) {

		this.interestInTbi = interestInTbi;
	}

	public Date getLastSuccessfulLogin() {

		return lastSuccessfulLogin;
	}

	public void setLastSuccessfulLogin(Date lastSuccessfulLogin) {

		this.lastSuccessfulLogin = lastSuccessfulLogin;
	}

	public Date getUnlockDate() {

		return unlockDate;
	}

	public void setUnlockDate(Date unlockDate) {

		this.unlockDate = unlockDate;
	}

	public Date getPasswordExpirationDate() {

		return passwordExpirationDate;
	}

	public void setPasswordExpirationDate(Date passwordExpirationDate) {

		this.passwordExpirationDate = passwordExpirationDate;
	}

	public AccountStatus getAccountStatus() {

		return accountStatus;
	}

	public void setAccountStatus(AccountStatus accountStatus) {

		this.accountStatus = accountStatus;
	}

	/**
	 * @return the recoveryDate
	 */
	public Date getRecoveryDate() {

		return recoveryDate;
	}

	/**
	 * @param recoveryDate the recoveryDate to set
	 */
	public void setRecoveryDate(Date recoveryDate) {

		this.recoveryDate = recoveryDate;
	}

	/**
	 * Searches for an account role by its role type.
	 * 
	 * @param target - The role type to search for.
	 * @return The found AccountRole object or null if none can be found.
	 */
	public AccountRole getAccountByRoleType(RoleType target) {
		AccountRole foundRole = null;

		// Search the account role list for the given role type ID.
		for (AccountRole role : accountRoleList) {
			if (role.getRoleType() == target) {
				foundRole = role;
				break;
			}
		}

		return foundRole;
	}

	/**
	 * Creates a list of AccountRole objects ordered by their role type title. The list will be based off of the
	 * "accountRoleList" set.
	 * 
	 * @return An ordered listing of account roles by their role type tile.
	 */
	public List<AccountRole> getOrderedAccountRoles() {
		List<AccountRole> roleList = new ArrayList<AccountRole>(accountRoleList);

		// Order the list by role type title name.
		Collections.sort(roleList, new Comparator<AccountRole>() {
			@Override
			public int compare(AccountRole o1, AccountRole o2) {
				return o1.getRoleType().getTitle().compareTo(o2.getRoleType().getTitle());
			}
		});

		return roleList;
	}

	public Set<AccountRole> getAccountRoleList() {
		return accountRoleList;
	}

	public void setAccountRoleList(Set<AccountRole> accountRoleList) {

		if (this.accountRoleList == null) {
			this.accountRoleList = new HashSet<AccountRole>();
		}

		this.accountRoleList.clear();

		if (accountRoleList != null) {
			this.accountRoleList.addAll(accountRoleList);
		}
	}

	public Boolean getIsActive() {

		if (isActive != null) {
			return isActive;
		}

		return false;
	}

	public void setIsActive(Boolean isActive) {

		this.isActive = isActive;
	}

	public Boolean isAdmin() {
		Set<AccountRole> roles = getAccountRoleList();
		for (AccountRole role : roles) {
			if (role.getRoleType() == RoleType.ROLE_ADMIN && role.getIsActive()) {
				return true;

			}
		}
		return false;
	}
	
	public Boolean isRepositoryAdmin() {
		Set<AccountRole> roles = getAccountRoleList();
		for(AccountRole role : roles) {
			if(role.getIsActive() && (role.getRoleType() == RoleType.ROLE_REPOSITORY_ADMIN || role.getRoleType() == RoleType.ROLE_ADMIN)) {
				return true;
			}
		}
		return false;
	}
	
	public Boolean isAccountReviewer(){
		Set<AccountRole> roles = getAccountRoleList();
		for (AccountRole role : roles) {
			if (role.getIsActive() && role.getRoleType() == RoleType.ROLE_ACCOUNT_REVIEWER) {
				return true;

			}
		}


		return false;
		
	}

	public Set<PermissionGroupMember> getPermissionGroupMemberList() {

		return permissionGroupMemberList;
	}

	public void setPermissionGroupMemberList(Set<PermissionGroupMember> permissionGroupMemberListUpdate) {
		permissionGroupMemberList.clear();

		if (!ValUtil.isCollectionEmpty(permissionGroupMemberListUpdate)) {
			permissionGroupMemberList.addAll(permissionGroupMemberListUpdate);
		}
	}

	/**
	 * Convenience method for adding PermissionGroupMember objects to this account's member set. The bidirectional
	 * relationship between the given member and this account will also be set. If there is an existing member that is
	 * equal to the given member, then nothing will be added to the list.
	 * 
	 * @param newMember - A new permission group member to be added to this account's member set.
	 * @return True if and only if the given new member was added to this account's member set.
	 */
	public boolean addPermissionGroupMember(PermissionGroupMember newMember) {
		boolean added = false;

		if (newMember != null) {
			newMember.setAccount(this);

			// Using the getter method to ensure the member set is retrieved with JPA/Hibernate.
			added = getPermissionGroupMemberList().add(newMember);
		}

		return added;
	}

	public String getAdminNote() {

		return adminNote;
	}

	public void setAdminNote(String adminNote) {

		this.adminNote = adminNote;
	}

	public Date getApplicationDate() {

		return applicationDate;
	}

	public void setApplicationDate(Date applicationDate) {

		this.applicationDate = applicationDate;
	}

	public String getApplicationString() {

		if (applicationDate == null) {
			return "-";
		}
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");

		return df.format(applicationDate);
	}

	/**
	 * Overrides display name for use in permission pages
	 */
	public String getDisplayName() {
		return BRICSStringUtils.capitalizeFirstCharacter(user.getLastName().trim()) + ", "
				+ BRICSStringUtils.capitalizeFirstCharacter(user.getFirstName().trim());
	}

	/**
	 * Overrides display key for use in permission pages
	 */
	public String getDisplayKey() {

		return "ACCOUNT:" + getDiseaseKey() + ";" + getId();
	}

	@Override
	public String getDiseaseKey() {

		return diseaseKey;
	}

	@Override
	public void setDiseaseKey(String diseaseKey) {

		this.diseaseKey = diseaseKey;
	}

	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(Date lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public Boolean getHideAccessRecords() {
		return hideAccessRecords;
	}

	public void setHideAccessRecords(Boolean hideAccessRecords) {
		this.hideAccessRecords = hideAccessRecords;
	}

	public Boolean getIsLocked() {
		return isLocked;
	}

	public void setIsLocked(Boolean isLocked) {
		this.isLocked = isLocked;
	}

	public Date getLockedUntil() {
		return lockedUntil;
	}

	public void setLockedUntil(Date lockedUntil) {
		this.lockedUntil = lockedUntil;
	}

	public Date getFailedIntervalStart() {
		return failedIntervalStart;
	}

	public void setFailedIntervalStart(Date failedIntervalStart) {
		this.failedIntervalStart = failedIntervalStart;
	}

	public Integer getFailedConsecutiveCount() {
		return failedConsecutiveCount;
	}

	public void setFailedConsecutiveCount(Integer failedConsecutiveCount) {
		this.failedConsecutiveCount = failedConsecutiveCount;
	}

	public Integer getFailedIntervalCount() {
		return failedIntervalCount;
	}

	public void setFailedIntervalCount(Integer failedIntervalCount) {
		this.failedIntervalCount = failedIntervalCount;
	}

	public boolean getIsCurrentLocked() {
		if (isLocked != null && isLocked) {
			return true;
		} else if (getIsTemporarilyLocked()) {
			return true;
		} else {
			return false;
		}
	}

	public Set<AccountHistory> getAccountHistory() {
		return accountHistory;
	}

	public void setAccountHistory(Set<AccountHistory> accountHistory) {
		this.accountHistory = accountHistory;
	}

	public void addAccountHistory(AccountHistory accountHistory) {
		if (this.accountHistory == null) {
			this.accountHistory = new HashSet<AccountHistory>();
		}

		this.accountHistory.add(accountHistory);
	}

	public boolean getIsTemporarilyLocked() {
		return (lockedUntil != null && lockedUntil.after(new Date()));
	}

	public String getSalt() {
		if (salt == null) {
			return "";
		}
		return salt;
	}

    
    public String getDisplayAccountRoleList() {
    	StringBuffer displayAccountRoleList = new StringBuffer("");
    	
    	List<AccountRole> roleList = new ArrayList<>(getAccountRoleList());
    	
    	// Sort the role list by status name.
    	Collections.sort(roleList, new Comparator<AccountRole>() {

			@Override
			public int compare(AccountRole o1, AccountRole o2) {
				return o1.getRoleStatus().getName().compareTo(o2.getRoleStatus().getName());
			}
    		
    	});
    	
    	Iterator<AccountRole> roles = roleList.iterator();
		while (roles.hasNext()) {
			AccountRole role = roles.next();
			displayAccountRoleList.append(role.getRoleType().getName() + "(" + role.getRoleStatus().getName() + ")");
			if (roles.hasNext()) {
				displayAccountRoleList.append(", ");
			}
		}
		return displayAccountRoleList.toString();
    }

	public void setSalt(String salt) {

		this.salt = salt;
	}

	public AccountHistory getLatestAccountHistory() {

		if (this.accountHistory != null && !this.accountHistory.isEmpty()) {

			// comparing account history object based on createdDate
			Comparator<AccountHistory> comparator = Comparator.comparing(AccountHistory::getCreatedDate);

			return accountHistory.stream().max(comparator).get();
		}

		return null;
	}

	public Set<AccountEmailReportSetting> getAccountEmailReportSettings() {
		return accountEmailReportSettings;
	}

	public void setAccountEmailReportSettings(Set<AccountEmailReportSetting> accountEmailReportSettings) {
		this.accountEmailReportSettings = accountEmailReportSettings;
	}

	/**
	 * This method will add the new email report setting and ensure that the current user only has one setting per
	 * report type. Not adhering to this rule will violate DB constraint.
	 * 
	 * @param accountEmailReportSetting
	 */
	public void updateAccountEmailReportSetting(AccountEmailReportSetting accountEmailReportSetting) {
		if (this.accountEmailReportSettings == null) {
			this.accountEmailReportSettings = new HashSet<AccountEmailReportSetting>();
			this.accountEmailReportSettings.add(accountEmailReportSetting);
		} else {
			for (AccountEmailReportSetting currentEmailSetting : accountEmailReportSettings) {
				// if the email report already exists, just update the frequency setting
				if (currentEmailSetting.getReportType() == accountEmailReportSetting.getReportType()) {
					currentEmailSetting.setFrequency(accountEmailReportSetting.getFrequency());
					currentEmailSetting.setAccountStatuses(accountEmailReportSetting.getAccountStatuses());
					return; // we've updated the frequency, nothing else to do after this.
				}
			}

			// if the setting does not exist, just add the new setting
			this.accountEmailReportSettings.add(accountEmailReportSetting);
		}
	}
	
	/**
	 * This method will remove the email report setting when the user selects none from email setting lightbox
	 * 
	 * @param emailReportType
	 */
	public void removeAccountEmailReportSetting(EmailReportType emailReportType) {
		
		if (this.accountEmailReportSettings != null) {
			
			for (AccountEmailReportSetting currentEmailSetting : accountEmailReportSettings) {
				if (currentEmailSetting.getReportType() == emailReportType) {
					accountEmailReportSettings.remove(currentEmailSetting);
					return;
				}
			}
		}
	}

	public Set<AccountRole> getRolesWithStatus(List<RoleStatus> statuses) {


		Set<AccountRole> accountRoleList = this.getAccountRoleList();
		Set<AccountRole> accountRolesReturnList = new HashSet<AccountRole>();

		for (AccountRole accountRole : accountRoleList) {
			if (statuses.contains(accountRole.getRoleStatus())) {
				accountRolesReturnList.add(accountRole);
			}
		}

		return accountRolesReturnList;
	}
	
	/**
	 * Returns the soonest occurring expiration date of the account roles in ISO string date format.
	 * @return
	 */
	public String getExpirationDate() {
		Set<AccountRole> expiringAccountRoles = getRolesWithStatus(Arrays.asList(RoleStatus.EXPIRED,RoleStatus.EXPIRING_SOON));

		if (!expiringAccountRoles.isEmpty()) {

			// we are interested in only the max date(the most recent one)
			// so we are mapping only the expiration date to a stream and get the max value
			Date expirationDate =
					expiringAccountRoles.stream().filter(item -> item.getExpirationDate()!=null).map(item -> item.getExpirationDate()).min(Date::compareTo).orElse(null);

			return BRICSTimeDateUtil.formatDate(expirationDate);

		} else {
			return ModelConstants.EMPTY_STRING;
		}
	}
	
	public Date getRequestSubmitDate() {
		return requestSubmitDate;
	}

	public void setRequestSubmitDate(Date requestSubmitDate) {
		this.requestSubmitDate = requestSubmitDate;
	}

	public Date getInitialRequestDate() {
		return initialRequestDate;
	}

	public void setInitialRequestDate(Date initialRequestDate) {
		this.initialRequestDate = initialRequestDate;
	}

	public Set<AccountAdministrativeNote> getAccountAdministrativeNotes() {
		return accountAdministrativeNotes;
	}

	public void setAccountAdministrativeNotes(Set<AccountAdministrativeNote> accountAdministrativeNotes) {
		this.accountAdministrativeNotes = accountAdministrativeNotes;
	}
	
	public void addAccountAdministrativeNote(AccountAdministrativeNote accountAdministrativeNote){
		if(this.accountAdministrativeNotes == null){
			this.accountAdministrativeNotes = new HashSet<AccountAdministrativeNote>();
		}
		
		this.accountAdministrativeNotes.add(accountAdministrativeNote);
	}

	public Set<ElectronicSignature> getElectronicSignatures() {
		return electronicSignatures;
	}

	public void setElectronicSignatures(Set<ElectronicSignature> electronicSignatures) {
		this.electronicSignatures = electronicSignatures;
	}

	public Set<TwoFactorAuthentication> getTwoFactorAuthentications() {
		return twoFactorAuthentications;
	}

	public void setTwoFactorAuthentications(Set<TwoFactorAuthentication> twoFactorAuthentications) {
		this.twoFactorAuthentications = twoFactorAuthentications;
	}
	
	public void addElectronicSignature(ElectronicSignature signature) {
		if (electronicSignatures == null) {
			electronicSignatures = new HashSet<ElectronicSignature>();
		}
		electronicSignatures.add(signature);
	}
	
	
	public boolean hasDucESignature() {
		if (electronicSignatures != null) {
			for (ElectronicSignature es : electronicSignatures) {
				if (es.getSignatureType() == SignatureType.PDBP_DMR_DUC) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasBricsESignature() {
		if (electronicSignatures != null) {
			for (ElectronicSignature es : electronicSignatures) {
				if (es.getSignatureType() == SignatureType.BRICS_ELECTRONIC_SIGNATURE
						&& es.getSignatureFile() != null) {
					return true;
				}
			}
		}
		return false;
	}
	
	public ElectronicSignature getBricsESignature() {
		if (electronicSignatures != null) {
			for (ElectronicSignature es : electronicSignatures) {
				if (es.getSignatureType() == SignatureType.BRICS_ELECTRONIC_SIGNATURE) {
					return es;
				}
			}
		}
		return null;
	}
	
	public void addTwoFactorAuthentication(TwoFactorAuthentication twoFa) {
		if (twoFactorAuthentications == null) {
			twoFactorAuthentications = new HashSet<TwoFactorAuthentication>();
		}
		twoFactorAuthentications.add(twoFa);
	}

	public boolean hasTwoFactorAuthentication() {
		if (twoFactorAuthentications != null) {
			for (TwoFactorAuthentication tfa : twoFactorAuthentications) {
				if (tfa != null) {
					return true;
				}
			}
		}
		return false;
	}
	
	public TwoFactorAuthentication getTwoFactorAuthentication() {
		if (twoFactorAuthentications != null) {
			for (TwoFactorAuthentication tfa : twoFactorAuthentications) {
				if (tfa != null) {
					return tfa;
				}
			}
		}
		return null;
	}	
	
	public AccountType getAccountReportingType() {
		return accountReportingType;
	}

	public void setAccountReportingType(AccountType accountReportingType) {
		this.accountReportingType = accountReportingType;
	}

	public Set<AccountReportingLog> getAccountReportingLog() {
		return accountReportingLog;
	}

	public void setAccountReportingLog(Set<AccountReportingLog> accountReportingLog) {
		this.accountReportingLog = accountReportingLog;
	}
	
	public void addAccountReportingLog(AccountReportingLog log) {
		if (accountReportingLog == null) {
			accountReportingLog = new HashSet<AccountReportingLog>();
		}
		accountReportingLog.add(log);
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
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
		Account other = (Account) obj;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Account [id=" + id.toString() + ", username=" + userName + ", user=" + user.toString()
				+ ", accountStatus=" + accountStatus.toString();
	}

}
