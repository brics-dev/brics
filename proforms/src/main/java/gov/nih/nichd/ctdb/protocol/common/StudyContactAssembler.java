package gov.nih.nichd.ctdb.protocol.common;

import gov.nih.nichd.ctdb.common.CtdbAssembler;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.contacts.domain.ExternalContact;
import gov.nih.nichd.ctdb.protocol.form.StudyContactForm;
import gov.nih.nichd.ctdb.util.domain.Address;

public class StudyContactAssembler extends CtdbAssembler
{
	public static void formToDomain(StudyContactForm form, ExternalContact contact)
	{
		Address a = new Address();
		
		contact.setId(form.getId());
		contact.setName(form.getName());
		contact.setStudySiteId(form.getStudySite());
		contact.setOrganization(form.getOrganization());
		contact.setInstitute(new CtdbLookup(form.getInstituteId()));
		contact.setPhone1(form.getPhone1());
		contact.setPhone2(form.getPhone2());
		contact.setContactType(new CtdbLookup(form.getContactType()));
		contact.setEmailAddress(form.getEmailAddress());
		contact.setProtocolId(form.getStudyId());
		
		// Setting address info
		a.setId(form.getAddressId());
		a.setAddressOne(form.getAddress1());
		a.setAddressTwo(form.getAddress2());
		a.setCity(form.getCity());
		a.setState(new CtdbLookup(form.getState()));
		a.setZipCode(form.getZipCode());
		a.setCountry(new CtdbLookup(form.getCountry()));
		contact.setAddress(a);
	}
	
	public static void domainToForm(ExternalContact contact, StudyContactForm form)
	{
		Address a = contact.getAddress();
		
		form.setId(contact.getId());
		form.setName(contact.getName());
		form.setStudySite(contact.getStudySiteId());
		form.setOrganization(contact.getOrganization());
		form.setInstituteId(contact.getInstitute().getId());
		form.setPhone1(contact.getPhone1());
		form.setPhone2(contact.getPhone2());
		form.setContactType(contact.getContactType().getId());
		form.setEmailAddress(contact.getEmailAddress());
		form.setStudyId(contact.getProtocolId());
		
		// Setting address info
		form.setAddressId(a.getId());
		form.setAddress1(a.getAddressOne());
		form.setAddress2(a.getAddressTwo());
		form.setCity(a.getCity());
		form.setState(a.getState().getId());
		form.setZipCode(a.getZipCode());
		form.setCountry(a.getCountry().getId());
	}
}
