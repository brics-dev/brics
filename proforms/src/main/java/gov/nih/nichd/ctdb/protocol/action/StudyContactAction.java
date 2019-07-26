package gov.nih.nichd.ctdb.protocol.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.LookupSessionKeys;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.contacts.domain.ExternalContact;
import gov.nih.nichd.ctdb.contacts.manager.ExternalContactManager;
import gov.nih.nichd.ctdb.protocol.common.ProtocolConstants;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.domain.Address;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;

public class StudyContactAction extends BaseAction {

	private static final long serialVersionUID = 6622214183499789872L;
	private static final Logger logger = Logger.getLogger(StudyContactAction.class);

    private static final String CONTACTACTION_MESSAGES_KEY = "ContactAction_ActionMessages";
    
	private int id = -1;
	private int studyId = Integer.MIN_VALUE;
	private String name = "";
	private String address1 = "";
	private String address2 = "";
	private String city = "";
	private int state = 0;
	private String zipCode = "";
	private int country = 0;
	private int studySite = 0;
	private String organization = "";
	private int instituteId = 11; // default to NICHD
	private String phone1 = "";
	private String phone2 = "";
	private int contactType = 0;
	private String emailAddress = "";
	private int addressId = Integer.MIN_VALUE;
	private String idsToDelete = "";

	private void setupPage() throws CtdbException {
		
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_CONTACTS);
		
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		this.setStudyId(p.getId());
		
		LookupManager luMan = new LookupManager();
		ExternalContactManager eMan = new ExternalContactManager();
		
		request.setAttribute(LookupSessionKeys.KEY_INSTITUTE, luMan.getLookups(LookupType.INSTITUTE));
		request.setAttribute(LookupSessionKeys.KEY_STATE, luMan.getLookups(LookupType.STATE));
		request.setAttribute(LookupSessionKeys.KEY_CONTACT_TYPES, luMan.getLookups(LookupType.CONTACT_TYPES));
		request.setAttribute(LookupSessionKeys.KEY_COUNTRY, luMan.getLookups(LookupType.COUNTRY));
		request.setAttribute(ProtocolConstants.CONTACTS_LIST, eMan.getExternalContacts(p.getId()));
		request.setAttribute(ProtocolConstants.STUDY_SITE_LIST, p.getStudySites());
	}
	
	public String execute() throws CtdbException {
		String strutsResult = BaseAction.SUCCESS;
		
		this.retrieveActionMessages(StudyContactAction.CONTACTACTION_MESSAGES_KEY);
		
		this.setupPage();
		
		try {
			// Edit contact
			if ( id > 0 ) {
				ExternalContactManager eMan = new ExternalContactManager();
				ExternalContact contact = eMan.getExternalContact(id);
				Address a = contact.getAddress();
				
				// Set contact info
				this.setName(contact.getName());
				this.setStudySite(contact.getStudySiteId());
				this.setOrganization(contact.getOrganization());
				this.setInstituteId(contact.getInstitute().getId());
				this.setPhone1(contact.getPhone1());
				this.setPhone2(contact.getPhone2());
				this.setContactType(contact.getContactType().getId());
				this.setEmailAddress(contact.getEmailAddress());
				this.setStudyId(contact.getProtocolId());
				
				// Setting address info
				this.setAddressId(a.getId());
				this.setAddress1(a.getAddressOne());
				this.setAddress2(a.getAddressTwo());
				this.setCity(a.getCity());
				this.setState(a.getState().getId());
				this.setZipCode(a.getZipCode());
				this.setCountry(a.getCountry().getId());
			}
		}
		catch (CtdbException ce) {
			logger.error("A database error occurred while getting contact information.", ce);
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE_RETRY, 
					new String[]{getText("study.contacts.message.general")}));
		}
		
		return strutsResult;
	}


	public String saveContact() throws Exception {
		String strutsResult = BaseAction.SUCCESS;
		
		if ( !validateForm() ) {
			this.setupPage();
			return StrutsConstants.EXCEPTION;
		}
		
		User u = getUser();
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		ExternalContact contact = new ExternalContact();
		
		// Setting external contact info
		contact.setName(getName());
		contact.setStudySiteId(getStudySite());
		contact.setOrganization(getOrganization());
		contact.setInstitute(new CtdbLookup(getInstituteId()));
		contact.setPhone1(getPhone1());
		contact.setPhone2(getPhone2());
		contact.setContactType(new CtdbLookup(getContactType()));
		contact.setEmailAddress(getEmailAddress());
		contact.setProtocolId(getStudyId());
		contact.setUpdatedBy(u.getId());
		contact.setProtocolId(p.getId());
		
		// Setting address info
		Address a = new Address();
		a.setId(getAddressId());
		a.setAddressOne(getAddress1());
		a.setAddressTwo(getAddress2());
		a.setCity(getCity());
		a.setState(new CtdbLookup(getState()));
		a.setZipCode(getZipCode());
		a.setCountry(new CtdbLookup(getCountry()));
		a.setUpdatedBy(u.getId());
		contact.setAddress(a);
		
		try {
			ExternalContactManager eMan = new ExternalContactManager();
			
			// Update contact
			if ( id > 0 ) {
				contact.setId(id);
				eMan.updateExternalContact(contact);
				addActionMessage(getText(StrutsConstants.SUCCESS_EDIT_KEY, 
						new String[]{getText("study.contacts.message.part1", new String[]{name})}));
			}
			// Create contact
			else {
				contact.setCreatedBy(u.getId());
				a.setCreatedBy(u.getId());
				eMan.createExternalContact(contact);
				addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, 
						new String[]{getText("study.contacts.message.part1", new String[]{name})}));
			}
			
			session.put(StudyContactAction.CONTACTACTION_MESSAGES_KEY, this.getActionMessages());

		}
		catch ( DuplicateObjectException doe ) {
			logger.error(name + " is already in the system.", doe);
			addActionError(getText(StrutsConstants.ERROR_DUPLICATE, 
					new String[]{getText("study.contacts.message.part1", new String[]{name})}));
		}
		catch ( CtdbException ce ) {
			logger.error("A database error ocurred while saving" + name + "'s contact information.", ce);
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE, 
					new String[]{getText("study.contacts.message.part1", new String[]{name})}));
		}

		if ( hasActionErrors() ) {
			this.setupPage();
			strutsResult = StrutsConstants.EXCEPTION;
		}
		
		return strutsResult;
	}


	public String deleteContact() {
		List<Integer> delIdList = Utils.convertStrToIntArray(getIdsToDelete());
		try {
			ExternalContactManager eMan = new ExternalContactManager();
			
			List<String> contactsDeleted = new ArrayList<String>();
			List<String> failedDeletion = new ArrayList<String>();
			eMan.deleteExternalContacts(delIdList, contactsDeleted, failedDeletion);
			
			// Create any success messages
			if (!contactsDeleted.isEmpty()) {
				String strList = Utils.convertListToString(contactsDeleted);
				addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY, Arrays.asList("contact information for " + strList)));
			}
			
			// Create any error messages
			if (!failedDeletion.isEmpty()) {
				String strList = Utils.convertListToString(failedDeletion);
				addActionError("The contact information for " + strList + "could not be deleted. Please try again.");
			}
			
			this.setupPage();
			
		}
		catch (NumberFormatException nfe) {
			nfe.printStackTrace();
            return StrutsConstants.FAILURE;
		}
		catch (CtdbException ce) {
			ce.printStackTrace();
			addActionError("Could not connect to the database. Please try again later.");
		}

		if (this.hasActionErrors()) {
            return StrutsConstants.FAILURE;
		} else {
			return SUCCESS;
		}
	}

	
	/**
	 * Performs validation checks on the form data.
	 */
	private boolean validateForm() {

		this.clearErrorsAndMessages();
		
		// Check the name field
		if (!Utils.isBlank(getName())) {
			if (getName().length() > 255) {
				addFieldError(getText("study.contacts.name.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
						new String[]{getText("study.contacts.name.display"), "255"}));
			}
		} else {
			addFieldError(getText("study.contacts.name.display"), getText(StrutsConstants.ERROR_FIELD_REQUIRED, 
					new String[]{getText("study.contacts.name.display"), "is"}));
		}

		// Check the organization field
		if (!Utils.isBlank(getOrganization()) && getOrganization().length() > 255) {
			addFieldError(getText("study.contacts.organization.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.contacts.organization.display"), "255"}));
		}
		
		// Check the address line one field
		if (!Utils.isBlank(getAddress1()) && getAddress1().length() > 210) {
			addFieldError(getText("study.contacts.address1.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.contacts.address1.display"), "210"}));
		}
		
		// Check the address line two field
		if (!Utils.isBlank(getAddress2()) && getAddress2().length() > 210) {
			addFieldError(getText("study.contacts.address2.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.contacts.address2.display"), "210"}));
		}
		
		// Check the phone line one field
		if (!Utils.isBlank(getPhone1()) && getPhone1().length() > 25) {
			addFieldError(getText("study.contacts.phone1.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.contacts.phone1.display"), "25"}));
		}
		
		// Check the phone line two field
		if (!Utils.isBlank(getPhone2()) && getPhone2().length() > 25) {
			addFieldError(getText("study.contacts.phone2.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.contacts.phone2.display"), "25"}));
		}
		
		// Check the city field
		if (!Utils.isBlank(getCity()) && getCity().length() > 210) {
			addFieldError(getText("study.contacts.city.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.contacts.city.display"), "210"}));
		}
		
		// Check the zip code field
		if (!Utils.isBlank(getZipCode()) && getZipCode().length() > 25) {
			addFieldError(getText("study.contacts.zipCode.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.contacts.zipCode.display"), "25"}));
		}
		
		// Check the email field
		if (!Utils.isBlank(getEmailAddress())) {
			if (getEmailAddress().length() > 255) {
				addFieldError(getText("study.contacts.email.display"), getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("study.contacts.email.display"), "255"}));
			} else if (!getEmailAddress().matches("^\\S+@\\S+\\.\\S+$")) {
				addFieldError(getText("study.contacts.email.display"), getText(StrutsConstants.ERROR_FIELD_INVALID, 
					Arrays.asList(getText("study.contacts.email.display"))));
			}
		}
		
		return !hasFieldErrors();
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public int getStudyId() {
		return studyId;
	}
	public void setStudyId(int studyId) {
		this.studyId = studyId;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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

	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}

	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public int getCountry() {
		return country;
	}
	public void setCountry(int country) {
		this.country = country;
	}

	public int getStudySite() {
		return studySite;
	}
	public void setStudySite(int studySite) {
		this.studySite = studySite;
	}

	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public int getInstituteId() {
		return instituteId;
	}
	public void setInstituteId(int instituteId) {
		this.instituteId = instituteId;
	}

	public String getPhone1() {
		return phone1;
	}
	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}

	public String getPhone2() {
		return phone2;
	}
	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}

	public int getContactType() {
		return contactType;
	}
	public void setContactType(int contactType) {
		this.contactType = contactType;
	}

	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public int getAddressId() {
		return addressId;
	}
	public void setAddressId(int addressId) {
		this.addressId = addressId;
	}
	
	public String getIdsToDelete() {
		return idsToDelete;
	}
	public void setIdsToDelete(String idsToDelete) {
		this.idsToDelete = idsToDelete;
	}

}
