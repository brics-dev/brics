package gov.nih.nichd.ctdb.security.domain;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.ws.HashMethods;

/**
 * User DomainObject for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class User extends CtdbDomainObject implements Comparable<User> {
	private static final long serialVersionUID = 7190176992656888418L;

	private long bricsUserId;
	private String username;
	private String password;
	private String firstName;
	private String middleName;
	private String lastName;
	private String office;
	private String phoneNumber;
	private String email;
	private boolean sysAdmin = false;
	private boolean passwordExpired = false;
	private List<Role> roleList;
	private Map<Integer, Role> roleMap;  // map from protocol ID to role
	boolean useTreeView;
	protected String displayName;
	private boolean isStaff;
	private Map<Integer, Integer> protocolSiteMap;
	private int instituteId = Integer.MIN_VALUE;
	private boolean editPasswords = false;
	private boolean refreshCtss = false;
	private boolean vbrAdmin = false;
	private boolean createStudy = false;
	private String ctdbLookupStringForDisplay;

	/**
	 * Default Constructor for the User Domain Object
	 */
	public User() {
		// default constructor
	}

	public User(int _id) {
		this.setId(_id);
		// default constructor
	}

	public User(int _id, String username) {
		this.setId(_id);
		this.setUsername(username);
		// default constructor
	}

	/**
	 * Gets the user's username
	 *
	 * @return String username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the user's username
	 *
	 * @param username The username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the user's password
	 *
	 * @return String password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the user's password
	 *
	 * @param password The password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Sets the user's password using the BRICS encoded password
	 * 
	 * @param password
	 * @throws UnsupportedEncodingException
	 */
	public void setPassword(byte[] password) throws UnsupportedEncodingException {
		this.password = HashMethods.convertFromByte(password);
	}

	/**
	 * Gets the user's first name
	 *
	 * @return String First Name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Sets the user's first name
	 *
	 * @param firstName The first name
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Gets the user's middle name
	 *
	 * @return String Middle Name
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * Sets the user's middle name
	 *
	 * @param middleName The middle name
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * Gets the user's last name
	 *
	 * @return String Last Name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Sets the user's last name
	 *
	 * @param lastName The last name
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Gets the user's email address
	 *
	 * @return String The user's office
	 */
	public String getOffice() {
		return office;
	}

	/**
	 * Sets the user's office
	 *
	 * @param office The users's office
	 */
	public void setOffice(String office) {
		this.office = office;
	}

	/**
	 * Gets the user's phone number
	 *
	 * @return String Phone Number
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * Sets the user's phone number
	 *
	 * @param phoneNumber The user's phone number
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * Gets the user's email address
	 *
	 * @return String Email Address
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the user's email address
	 *
	 * @param email The email address
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets the user's sys admin status
	 *
	 * @return boolean True if the user is a sys admin, False if the user is not a sys admin
	 */
	public boolean isSysAdmin() {
		return sysAdmin;
	}

	/**
	 * Sets the user's sys admin status. True the user is a sys admin, false the user is not a sys admin.
	 *
	 * @param sysAdmin The sysAdmin status
	 */
	public void setSysAdmin(boolean sysAdmin) {
		this.sysAdmin = sysAdmin;
	}

	/**
	 * Gets the user's password expiration status.
	 *
	 * @return boolean True if the user is a sys admin, False if the user is not a sys admin
	 */
	public boolean isPasswordExpired() {
		return passwordExpired;
	}

	/**
	 * Sets the user's password expired flag. By setting this flag to true, the user will be prompted on the next login
	 * to change their password.
	 *
	 * @param passwordExpired The expiration status of the user's password
	 */
	public void setPasswordExpired(boolean passwordExpired) {
		this.passwordExpired = passwordExpired;
	}

	/**
	 * Gets the user's roles in the system
	 *
	 * @return List User's roles
	 */
	public List<Role> getRoleList() {
		return roleList;
	}

	/**
	 * Sets the role list for the user
	 *
	 * @param roleList Role list for the user
	 */
	public void setRoleList(List<Role> roleList) {
		this.roleList = roleList;
	}

	public int getInstituteId() {
		return instituteId;
	}

	public void setInstituteId(int instituteId) {
		this.instituteId = instituteId;
	}


	public Map<Integer, Integer> getProtocolSiteMap() {
		return protocolSiteMap;
	}

	public void setProtocolSiteMap(Map<Integer, Integer> protocolSiteMap) {
		this.protocolSiteMap = protocolSiteMap;
	}

	public boolean isRefreshCtss() {
		return refreshCtss;
	}

	public void setRefreshCtss(boolean refreshCtss) {
		this.refreshCtss = refreshCtss;
	}

	public boolean isVbrAdmin() {
		return vbrAdmin;
	}

	public void setVbrAdmin(boolean vbrAdmin) {
		this.vbrAdmin = vbrAdmin;
	}


	/**
	 * Gets the user's full name in the format &lt;last name&gt;, &lt;first name&gt;
	 *
	 * @return &lt;last name&gt;, &lt;first name&gt;
	 */
	public String getFullName() {
		String result = "";
		if (this.lastName != null) {
			result += this.lastName + ", ";
		}
		if (this.firstName != null) {
			result += this.firstName;
		}
		return result;
	}

	/**
	 * Determines if the user has a privilege. The privilege will be checked against the database code.
	 *
	 * @param privilege The privilege to check for
	 * @return boolean true if the user has the privilege; false if the user does not have the privilege
	 */
	public boolean hasPrivilege(Privilege privilege) {
		boolean hasPriv = sysAdmin;

		if (privilege.getCode().trim().equalsIgnoreCase("validuser")) {
			return true;
		}

		if (roleList != null) {
			for (Role role : roleList) {
				for (Privilege priv : role.getPrivList()) {
					if (priv.getCode().equalsIgnoreCase(privilege.getCode())) {
						hasPriv = true;
						break;
					}
				}

				if (hasPriv) {
					break;
				}
			}
		}

		return hasPriv;
	}

	/**
	 * Determines if the user has a privilege for a particular protocol.
	 *
	 * @param privilege The privilege to check for
	 * @return boolean true if the user has the privilege; false if the user does not have the privilege
	 */
	public boolean hasPrivilege(Privilege privilege, int protocolId) {
		boolean hasPriv = this.sysAdmin;
		Integer key = new Integer(protocolId);
		Role role = roleMap.get(key);

		if (role != null) {
			for (Privilege priv : role.getPrivList()) {
				if (priv.getCode().equalsIgnoreCase(privilege.getCode())) {
					hasPriv = true;
					break;
				}
			}
		}

		return hasPriv;
	}

	/**
	 * Determines if the user has a privilege for a particular protocol.
	 *
	 * @param privileges The privileges to check for
	 * @return boolean true if the user has the privilege for the form; false if the user does not have the privilege
	 *         for the form;
	 */
	public boolean hasPrivilege(String[] privileges, Object o) {
		boolean hasPriv = this.sysAdmin;
		Integer key = null;
		int protocolId = -1;
		int formStatus = 0;
		int checkOutBy = 0;
		Role role = null;
		boolean securityAccess = true;

		if (o == null) {
			return true;
		}
		if (o instanceof Protocol) {
			Protocol p = (Protocol) o;
			protocolId = p.getId();
		}
		if (o instanceof Form) {
			Form form = (Form) o;
			formStatus = form.getStatus().getId();
			checkOutBy = form.getCheckOutBy();
			securityAccess = form.getSecurityAccess();
			if (!securityAccess) {
				return false;
			}
			protocolId = form.getProtocolId();
		} else if (o instanceof Interval) {
			Interval interval = (Interval) o;
			protocolId = interval.getProtocolId();
		} else if (o instanceof Patient) {

		}
		key = new Integer(protocolId);
		role = roleMap.get(key);

		if (role != null) {
			for (Privilege priv : role.getPrivList()) {
				for (int idx = 0; idx < privileges.length && !hasPriv; idx++) {
					Privilege privilege = new Privilege();
					privilege.setCode(privileges[idx]);

					if (priv.getCode().equalsIgnoreCase(privilege.getCode())) {
						hasPriv = true;
						break;
					}
				}

				if (hasPriv) {
					break;
				}
			}
		}

		if (hasPriv && (o instanceof Form)) {
			if ((formStatus == FormConstants.STATUS_CHECKEDOUT && checkOutBy == 0)
					|| (formStatus == FormConstants.STATUS_INPROGRESS)) {
				hasPriv = false;
			}
		}

		/**
		 * currently this new method of checking access priviledges doesn't take into account the possibility of event
		 * based forms, so all security is handled by default permission checking
		 */


		if (protocolId == -1) {
			hasPriv = true;
		}

		return hasPriv;
	}

	/**
	 * Determines if the user has any of the passed in privileges. The privilege Strings passed to this method represent
	 * privilege codes in the database.
	 *
	 * @param privileges String representations of privilege codes to check for.
	 * @return boolean true if the user has the privilege; false if the user does not have the privilege
	 */
	public boolean hasAnyPrivilege(String[] privileges) {
		boolean hasPriv = this.sysAdmin;

		for (int idx = 0; idx < privileges.length && !hasPriv; idx++) {
			Privilege priv = new Privilege();
			priv.setCode(privileges[idx]);
			hasPriv = this.hasPrivilege(priv);
		}

		return hasPriv;
	}

	/**
	 * Determines if hte user has any of the passed in privileges for the given protocol. The privileges Strings passed
	 * to this method represent privilege codes in the database
	 * 
	 * @param privileges String representations of the privilege codes to check for
	 * @param protocolId the protocol to compare against
	 * @return boolean true if the user has any of the privileges; otherwise false
	 */
	public boolean hasAnyPrivilege(String[] privileges, int protocolId) {
		boolean hasPriv = this.sysAdmin;

		for (int i = 0; i < privileges.length; i++) {
			Privilege priv = new Privilege();
			priv.setCode(privileges[i]);
			hasPriv = this.hasPrivilege(priv, protocolId);

			// I don't like the syntax in the hasAnyPrivilege above, so I'll use this
			// it makes this break condition much more obvious
			if (hasPriv)
				break;
		}
		return hasPriv;
	}

	/**
	 * Set Role Map. Role map maps from protocol ID to the role that the user has with that protocol.
	 * 
	 * @param roleMap role map to set
	 */

	public void setRoleMap(Map<Integer, Role> roleMap) {
		this.roleMap = roleMap;
	}

	/**
	 * Get Role Map.
	 * 
	 * @return the role map for this user. Role map returned is a map from protocol ID to the role that the user has
	 *         with that protocol.
	 */

	public Map<Integer, Role> getRoleMap() {
		return roleMap;
	}

	public boolean isUseTreeView() {
		return useTreeView;
	}

	public void setUseTreeView(boolean useTreeView) {
		this.useTreeView = useTreeView;
	}


	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public boolean isStaff() {
		return isStaff;
	}

	public void setStaff(boolean staff) {
		isStaff = staff;
	}


	public boolean isEditPasswords() {
		return editPasswords;
	}

	public void setEditPasswords(boolean editPasswords) {
		this.editPasswords = editPasswords;
	}

	/**
	 * Compares this User to another User object. If the Object is not a User, it throws a ClassCastException (as Users
	 * are comparable only to other Users).
	 *
	 * @param compare - The User object to be compared
	 * @return The value 0 if the argument is a user equal to this user; A value less than 0 if the argument is a user
	 *         greater than this user; A value greater than 0 if the argument is a user less than this user.
	 * @throws ClassCastException Thrown if the argument is not a User
	 */
	public int compareTo(User compare) throws ClassCastException {
		int compareValue = 0;

		if (compare.getLastName().compareTo(this.lastName) == 0) {
			// last names the same, compare first names
			if (compare.getFirstName().compareTo(this.firstName) == 0) {
				// first names the same
				compareValue = 0;
			} else if (compare.getFirstName().compareTo(this.firstName) < 0) {
				// first name less than current name
				compareValue = 1;
			} else if (compare.getFirstName().compareTo(this.firstName) > 0) {
				// first name greater than current name
				compareValue = -1;
			}
		} else if (compare.getLastName().compareTo(this.lastName) < 0) {
			// last name less than current name
			compareValue = 1;
		} else if (compare.getLastName().compareTo(this.lastName) > 0) {
			// last name greater than current name
			compareValue = -1;
		}

		return compareValue;
	}

	/**
	 * This method allows the transformation of a User into an XML Document. If no implementation is available at this
	 * time, an UnsupportedOperationException will be thrown.
	 *
	 * @return XML Document
	 * @throws TransformationException is thrown if there is an error during the XML tranformation
	 * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
	 */
	public Document toXML() throws TransformationException, UnsupportedOperationException {
		throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in User.");
	}

	public String getCtdbLookupStringForDisplay() {
		return ctdbLookupStringForDisplay;
	}

	public void setCtdbLookupStringForDisplay(String ctdbLookupStringForDisplay) {
		this.ctdbLookupStringForDisplay = ctdbLookupStringForDisplay;
	}

	/**
	 * Translates a BRICS Account to a local User object for use throughout the system
	 * 
	 * @param account a BRICS Account object which holds user information
	 * @return a User object version of the user
	 * @throws UnsupportedEncodingException
	 */
	public static User userFromBricsAccount(Account account) throws UnsupportedEncodingException {
		User user = new User();

		user.setBricsUserId(account.getId());
		user.setFirstName(account.getUser().getFirstName());
		user.setLastName(account.getUser().getLastName());
		user.setUsername(account.getUserName());
		user.setEmail(account.getUser().getEmail());
		user.setDisplayName(account.getDisplayName());
		user.setCreatedBy(0);
		user.setUpdatedBy(0);
		user.setStaff(false);
		user.setActive(true);
		user.setPassword(HashMethods.convertFromByte(account.getPassword()));
		user.setInstituteId(22);
		user.setSysAdmin(false);

		// check to see if user is proforms admin
		for (AccountRole role : account.getAccountRoleList()) {
			if (role.getRoleType() != null) {
				if (role.getRoleType().equals(RoleType.ROLE_PROFORMS_ADMIN)
						&& role.getRoleStatus().equals(RoleStatus.ACTIVE)) {
					user.setSysAdmin(true);
				}

				// Set the create study privilege
				if (role.getRoleType().equals(RoleType.ROLE_STUDY) && role.getRoleStatus().equals(RoleStatus.ACTIVE)) {
					user.setCreateStudy(true);
				}
			}
		}
		// get the institution if it exists

		return user;
	}

	/**
	 * Checks for the BRICS "ROLE_STUDY" for the current user and updates the associated flag
	 * 
	 * @param account - The BRICS user account that holds all user information from the account web service
	 * @param user - The ProFoRMS user account
	 */
	public static void setCreateStudyPrivilege(Account account, User user) {
		user.setCreateStudy(false);

		for (AccountRole role : account.getAccountRoleList()) {
			// Set the create study privilege
			if (role.getRoleType().equals(RoleType.ROLE_STUDY) && role.getRoleStatus().equals(RoleStatus.ACTIVE)) {
				user.setCreateStudy(true);
				break;
			}
		}
	}

	/**
	 * @return the createStudy
	 */
	public boolean isCreateStudy() {
		return createStudy;
	}

	/**
	 * @param createStudy the createStudy to set
	 */
	public void setCreateStudy(boolean createStudy) {
		this.createStudy = createStudy;
	}

	/**
	 * @return the bricsUserId
	 */
	public long getBricsUserId() {
		return bricsUserId;
	}

	/**
	 * @param bricsUserId the bricsUserId to set
	 */
	public void setBricsUserId(long bricsUserId) {
		this.bricsUserId = bricsUserId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (bricsUserId ^ (bricsUserId >>> 32));
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + instituteId;
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((middleName == null) ? 0 : middleName.hashCode());
		result = prime * result + ((office == null) ? 0 : office.hashCode());
		result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		result = prime * result + ((roleList == null) ? 0 : roleList.hashCode());
		result = prime * result + ((roleMap == null) ? 0 : roleMap.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (bricsUserId != other.bricsUserId)
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (instituteId != other.instituteId)
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (middleName == null) {
			if (other.middleName != null)
				return false;
		} else if (!middleName.equals(other.middleName))
			return false;
		if (office == null) {
			if (other.office != null)
				return false;
		} else if (!office.equals(other.office))
			return false;
		if (phoneNumber == null) {
			if (other.phoneNumber != null)
				return false;
		} else if (!phoneNumber.equals(other.phoneNumber))
			return false;
		if (roleList == null) {
			if (other.roleList != null)
				return false;
		} else if (!roleList.equals(other.roleList))
			return false;
		if (roleMap == null) {
			if (other.roleMap != null)
				return false;
		} else if (!roleMap.equals(other.roleMap))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
}
