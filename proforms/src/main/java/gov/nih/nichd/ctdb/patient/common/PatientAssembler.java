package gov.nih.nichd.ctdb.patient.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nih.nichd.ctdb.common.AssemblerException;
import gov.nih.nichd.ctdb.common.CtdbAssembler;
import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientExtraInfo;
import gov.nih.nichd.ctdb.patient.domain.PatientProtocol;
import gov.nih.nichd.ctdb.patient.domain.Phone;
import gov.nih.nichd.ctdb.patient.domain.PhoneType;
import gov.nih.nichd.ctdb.patient.form.PatientForm;
import gov.nih.nichd.ctdb.util.domain.Address;


/**
 * Transforms the Patient domain object into the PatientForm object and the
 * PatientForm object into the Patient domain object.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientAssembler extends CtdbAssembler
{
    /**
     * Assembles a Patient domain object out of a PatientForm
     *
     * @param patientForm The PatientForm object
     * @return The Patient domain object
     */
    private static Patient assemblePatient(PatientForm patientForm) {
        Patient patient = new Patient();
        // get phone numbers
        Map<PhoneType, Phone> phoneNumbers = new HashMap<PhoneType, Phone>();

        Phone homePhone = new Phone();
        homePhone.setNumber(patientForm.getHomePhone());
        phoneNumbers.put(PhoneType.HOME, homePhone);
        Phone workPhone = new Phone();
        workPhone.setNumber(patientForm.getWorkPhone());
        phoneNumbers.put(PhoneType.WORK, workPhone);
        Phone mobilePhone = new Phone();
        mobilePhone.setNumber(patientForm.getMobilePhone());
        phoneNumbers.put(PhoneType.MOBILE, mobilePhone);
        patient.setPhoneNumbers(phoneNumbers);
        
        // set Address
        Address address = new Address();
        address.setId(patientForm.getAddressId());
        address.setAddressOne(patientForm.getAddress1());
        address.setAddressTwo(patientForm.getAddress2());
        address.setCity(patientForm.getCity());
        CtdbLookup state = new CtdbLookup();
        state.setId(Integer.parseInt(patientForm.getState()));
        address.setState(state);
        
        // set up country
        CtdbLookup country = new CtdbLookup();
        country.setId (Integer.parseInt (patientForm.getCountry()));
        address.setCountry (country);
        address.setZipCode(patientForm.getZip());
        patient.setHomeAddress(address);

        patient.setFirstName(patientForm.getFirstName());
        patient.setLastName(patientForm.getLastName());
        patient.setMiddleName(patientForm.getMiddleName());
        patient.setSubjectId(patientForm.getSubjectId());
        patient.setMrn(patientForm.getMrn());
        patient.setEmail(patientForm.getEmail());

        List<PatientProtocol> protocols = new ArrayList<PatientProtocol>();

        String protocolId = patientForm.getCurrentProtocolId();
        if ( !Utils.isBlank(protocolId) && Integer.parseInt(protocolId) > 0 ) {
            // associate to protocol is checked, get protocol information
            PatientProtocol protocol = new PatientProtocol();
            protocol.setId(Integer.parseInt(protocolId));
            protocol.setRecruited(patientForm.isRecruited());
            protocol.setBiorepositoryId(patientForm.getBiorepositoryId());
            protocol.setSubjectNumber(patientForm.getSubjectNumber());
            protocol.setEnrollmentDate(patientForm.getEnrollmentDate());
            protocol.setAssociated(Boolean.valueOf(patientForm.getAssociated2Protocol()).booleanValue());
            protocol.setActive(Boolean.valueOf(patientForm.getActive()).booleanValue());
            protocol.setSiteId(patientForm.getSiteId());
            protocol.setCompletionDate(patientForm.getCompletionDate());
            protocol.setFutureStudy(patientForm.getFutureStudy());
            protocol.setValidated(patientForm.isValidated());
            protocol.setSubjectId(patientForm.getSubjectId());
            protocols.add(protocol);
        }
        
        patient.setProtocols(protocols);

        // set id
        patient.setId(patientForm.getId());
        patient.setDateOfBirth (patientForm.getDateOfBirth());
        patient.setExtraInfo (generatePatientExtraInfo (patientForm));
        patient.getExtraInfo().setBirthCity(patientForm.getBirthCity());
        patient.getExtraInfo().setBirthCountry(new CtdbLookup(patientForm.getBirthCountryId()));
        patient.setGuid(patientForm.getGuid() );
        return patient;

    }

    /**
     * Assembles a PatientForm object out of a Patient Domain Object
     *
     * @param patient The Patient domain object
     * @param form The Patient form object
     */
    public static void assemblePatient(Patient patient, PatientForm form)
    {
        form.setAddress1(patient.getHomeAddress().getAddressOne());
        form.setAddress2(patient.getHomeAddress().getAddressTwo());
        form.setCity(patient.getHomeAddress().getCity());
        form.setState(Integer.toString(patient.getHomeAddress().getState().getId()));
        form.setZip(patient.getHomeAddress().getZipCode());
        form.setAddressId(patient.getHomeAddress().getId());
        form.setCountry(Integer.toString (patient.getHomeAddress().getCountry().getId()));
        
        Phone phone = (Phone) patient.getPhoneNumbers().get(PhoneType.HOME);
        form.setHomePhone(phone.getNumber());
        phone = (Phone) patient.getPhoneNumbers().get(PhoneType.WORK);
        form.setWorkPhone(phone.getNumber());
        phone = (Phone) patient.getPhoneNumbers().get(PhoneType.MOBILE);
        form.setMobilePhone(phone.getNumber());

        form.setFirstName(patient.getFirstName());
        form.setLastName(patient.getLastName());
        form.setMiddleName(patient.getMiddleName());
        //form.setRecordNumber(patient.getNihRecordNumber());
        form.setMrn(patient.getMrn());
        form.setEmail(patient.getEmail());

        Boolean protocolDetected = false;
        
        for( PatientProtocol protocol : patient.getProtocols() ) {
            if (protocol.getId() == Integer.parseInt(form.getCurrentProtocolId())) {
            	protocolDetected = true;
                form.setAssociated2Protocol(new Boolean(protocol.isAssociated()).toString());
                form.setSubjectNumber(protocol.getSubjectNumber());
                form.setEnrollmentDate(protocol.getEnrollmentDate());
                form.setSiteId(protocol.getSiteId());
                form.setCompletionDate(protocol.getCompletionDate());
                form.setActive(new Boolean(protocol.isActive()).toString() );
                form.setBiorepositoryId(protocol.getBiorepositoryId());
                form.setFutureStudy(protocol.isFutureStudy());
                form.setValidated(protocol.isValidated());
                form.setRecruited(protocol.isRecruited());
                form.setSubjectId(protocol.getSubjectId());
            }
        }
        
        // patient was not associated with current protocol yet
        if( !protocolDetected ) { 
            form.setAssociated2Protocol("true");
            form.setActive("true");
        }
        	
        form.setId(patient.getId());
        form.setDateOfBirth (patient.getDateOfBirth());
        populateFormPatientExtraInfo (patient, form);
        form.setBirthCity(patient.getExtraInfo().getBirthCity());
        form.setBirthCountryId(patient.getExtraInfo().getBirthCountry().getId());
        form.setGuid(notNull( patient.getGuid()));
        form.setAttachment(patient.getAttachment());
    }

    /**
     * Transforms a Patient Domain Object to a PatientForm object
     *
     * @param domain The Patient object to transform to the PatientForm object
     * @throws AssemblerException Thrown if any error occurs while transforming the Patient domain object to the PatientForm object
     */
    public static void domainToForm(CtdbDomainObject domain, CtdbForm patientForm) throws AssemblerException
    {
        try
        {
            PatientAssembler.assemblePatient((Patient) domain, (PatientForm) patientForm);
        }
        catch(Exception e)
        {
            throw new AssemblerException("Unable to assemble patient form object: " + e.getMessage(), e);
        }
    }

    /**
     * Transforms a PatientForm object to a Patient Domain Object
     *
     * @param form The PatientForm object to transform to the Patient Domain Object
     * @return The Patient Domain Object
     * @throws AssemblerException Thrown if any error occurs while transforming the PatientForm object to the Patient Domain Object
     */
    public static CtdbDomainObject formToDomain(CtdbForm form) throws AssemblerException
    {
        try
        {
            return PatientAssembler.assemblePatient((PatientForm) form);
        }
        catch(Exception e)
        {
            throw new AssemblerException("Exception assembling patient domain object: " + e.getMessage(), e);
        }
    }

    
    /** generatePatientExtraInfo generates a patientExtraInfo object from a patient form
     * @param form the patient form
     * @return  the patientExtraInfo obj
     */    
    private static PatientExtraInfo generatePatientExtraInfo (PatientForm form) {
    	
        PatientExtraInfo pei = new PatientExtraInfo();
        pei.setSex (form.getSex());
        return pei;
    }
    
    /** populates the patient extra info in a paitientForm form bean from a domain obj
     * @param patient  the patient domain objecxt
     * @param form  the form  bean
     */    
    private static void populateFormPatientExtraInfo (Patient patient, PatientForm form) {

        PatientExtraInfo pei = patient.getExtraInfo();
        form.setSex (notNull (pei.getSex()));
        form.setDisplayBirthCountry(notNull(pei.getBirthCountryName()));
    }
    
    private static String notNull (String o)
    {
        if (o == null)
        {
            return "";
        }
        else
        {
            return o;
        }
    }
    
}
