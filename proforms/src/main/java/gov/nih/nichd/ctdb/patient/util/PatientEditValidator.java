package gov.nih.nichd.ctdb.patient.util;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientProtocol;
import gov.nih.nichd.ctdb.patient.domain.Phone;
import gov.nih.nichd.ctdb.patient.domain.PhoneType;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.site.manager.SiteManager;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Oct 6, 2010
 * Time: 2:47:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatientEditValidator {

    private static boolean doesEqual (String s1, String s2) {
        boolean blankNulls1 = s1 == null || s1.trim().equals("");
        boolean  blankNulls2 = s2 == null || s2.trim().equals("");

        if (blankNulls1 && blankNulls2) { return true;}
        if (s1 != null && s2 != null) {
            return s1.equals(s2);
        }
        if (s1 == null && s2 != null) {
            return false;
        }
        return s1 != null;
    }
    
    public static PatientChangeTracker validateEditWithoutReasons(Patient oldP, Patient newP, HttpServletRequest request) throws CtdbException {

        PatientChangeTracker pct = new PatientChangeTracker();
        pct.setVersionNumber(oldP.getVersion().getVersionNumber());
        String change1 = "";
        String change2 = "";
        
		@SuppressWarnings("unchecked")
		List<CtdbLookup> countryOptions = (List<CtdbLookup>) request.getSession().getAttribute("countryOptions");

		if (countryOptions != null && !countryOptions.isEmpty()) {
			for (CtdbLookup lookup : countryOptions) {
				if (lookup.getId() == newP.getHomeAddress().getCountry().getId()) {
					newP.getHomeAddress().setCountry(lookup);
				}
				if (lookup.getId() == newP.getExtraInfo().getBirthCountry().getId()) {
					newP.getExtraInfo().setBirthCountry(lookup);
				}
			}
		}
		if (!doesEqual(oldP.getMrn(), newP.getMrn())) {
        	pct.getChangedFields().put("MRN", new String [] {oldP.getMrn(), newP.getMrn()});
        }

		if (!doesEqual(oldP.getSubjectId(), newP.getSubjectId())) {
        	pct.getChangedFields().put("Subject ID", new String [] {oldP.getSubjectId(), newP.getSubjectId()});
        }

		if (oldP.getExtraInfo().getBirthCountry().getId() != newP.getExtraInfo().getBirthCountry().getId()) {
			pct.getChangedFields().put("Birth Country", new String [] {oldP.getExtraInfo().getBirthCountry().getShortName(), newP.getExtraInfo().getBirthCountry().getShortName()});
        }
		if (!doesEqual(oldP.getExtraInfo().getBirthCity(), newP.getExtraInfo().getBirthCity())) {
			pct.getChangedFields().put("Birth City", new String [] {oldP.getExtraInfo().getBirthCity(), newP.getExtraInfo().getBirthCity()});
        }
        if (!doesEqual(oldP.getFirstName(), newP.getFirstName()) ) {
            pct.getChangedFields().put("First Name", new String [] {oldP.getFirstName(), newP.getFirstName()} );
        }

        if (!doesEqual(oldP.getLastName(), newP.getLastName())) {
            pct.getChangedFields().put("Last Name", new String [] {oldP.getLastName(), newP.getLastName()});
        }

        if (! doesEqual(oldP.getMiddleName(), newP.getMiddleName())) {
            pct.getChangedFields().put("Middle Name", new String [] {oldP.getMiddleName(), newP.getMiddleName()});
        }

        if (! doesEqual (oldP.getEmail(), newP.getEmail())) {
            pct.getChangedFields().put("Email", new String [] {oldP.getEmail(), newP.getEmail()});
        }

        if (! doesEqual(oldP.getDateOfBirth(), newP.getDateOfBirth())) {
            pct.getChangedFields().put("Date Of Birth", new String [] {oldP.getDateOfBirth(), newP.getDateOfBirth()});
        }

        if (!doesEqual (((Phone) oldP.getPhoneNumbers().get(PhoneType.HOME)).getNumber()
                , ((Phone) newP.getPhoneNumbers().get(PhoneType.HOME)).getNumber())) {
            pct.getChangedFields().put("Home Phone", new String [] { ((Phone) oldP.getPhoneNumbers().get(PhoneType.HOME)).getNumber(), 
            	((Phone) newP.getPhoneNumbers().get(PhoneType.HOME)).getNumber()} );
        }
        
        if (!doesEqual(((Phone) oldP.getPhoneNumbers().get(PhoneType.WORK)).getNumber()
                , ((Phone) newP.getPhoneNumbers().get(PhoneType.WORK)).getNumber())) {

        	pct.getChangedFields().put("Work Phone", new String [] {((Phone) oldP.getPhoneNumbers().get(PhoneType.WORK)).getNumber(),
            		((Phone) newP.getPhoneNumbers().get(PhoneType.WORK)).getNumber() } );
        }
        
        if (!doesEqual (((Phone) oldP.getPhoneNumbers().get(PhoneType.MOBILE)).getNumber()
                , ((Phone) newP.getPhoneNumbers().get(PhoneType.MOBILE)).getNumber())) {
        	pct.getChangedFields().put("Mobile Phone", new String [] {((Phone) oldP.getPhoneNumbers().get(PhoneType.MOBILE)).getNumber(),
            		((Phone) newP.getPhoneNumbers().get(PhoneType.MOBILE)).getNumber() } );
        }

        if (! doesEqual(oldP.getHomeAddress().getAddressOne(), newP.getHomeAddress().getAddressOne())) {
            pct.getChangedFields().put("Home Address 1", new String [] {oldP.getHomeAddress().getAddressOne(), newP.getHomeAddress().getAddressOne()});
        }

        if (! doesEqual(oldP.getHomeAddress().getAddressTwo(), newP.getHomeAddress().getAddressTwo())) {
            pct.getChangedFields().put("Home Address 2", new String [] {oldP.getHomeAddress().getAddressTwo(), newP.getHomeAddress().getAddressTwo()});
        }

        if (! doesEqual(oldP.getHomeAddress().getCity(), newP.getHomeAddress().getCity())) {
            pct.getChangedFields().put("Home Address: City", new String [] {oldP.getHomeAddress().getCity(), newP.getHomeAddress().getCity()});
        }

        if (oldP.getHomeAddress().getState().getId() != newP.getHomeAddress().getState().getId()) {
            @SuppressWarnings("unchecked")
			List<CtdbLookup> states = (List<CtdbLookup>)request.getSession().getAttribute("xstates");
        	if (states != null && !states.isEmpty()) {
        		for (CtdbLookup lookup : states) {
        			if (lookup.getId() == newP.getHomeAddress().getState().getId()) {
        				newP.getHomeAddress().setState(lookup);
        			}
        		}
        	}

        	change1 = oldP.getHomeAddress().getState().getShortName();
        	change2 = newP.getHomeAddress().getState().getShortName();
            pct.getChangedFields().put("Home Address: State", new String [] { change1, change2 });
        }

        if (! doesEqual(oldP.getHomeAddress().getZipCode(), newP.getHomeAddress().getZipCode())) {
            pct.getChangedFields().put("Home Address: Zip", new String [] {oldP.getHomeAddress().getZipCode(), newP.getHomeAddress().getZipCode()});
        }

        if (oldP.getHomeAddress().getCountry().getId() != newP.getHomeAddress().getCountry().getId()) {
        	
			if (countryOptions != null && !countryOptions.isEmpty()) {
				for (CtdbLookup lookup : countryOptions) {
					if (lookup.getId() == newP.getHomeAddress().getCountry().getId()) {
						newP.getHomeAddress().setCountry(lookup);
					}
				}
			}
			
        	change1 = oldP.getHomeAddress().getCountry().getShortName();
        	change2 = newP.getHomeAddress().getCountry().getShortName();
            pct.getChangedFields().put("Home Address: Country", new String [] { change1, change2 });
        }

        if (!doesEqual(oldP.getGuid(), newP.getGuid())) {
            pct.getChangedFields().put("GUID", new String [] {oldP.getGuid(),newP.getGuid()});
        }

        // get the protocol From Old patient
        List<PatientProtocol> protocols = oldP.getProtocols();
        List<PatientProtocol> protocolsNew = newP.getProtocols();
        
        if(protocols.isEmpty() && !protocolsNew.isEmpty()){
       		pct.getChangedFields().put("Association to study", new String [] {"false", "true" });
        }
        else if(!protocols.isEmpty() && protocolsNew.isEmpty()){
      		pct.getChangedFields().put("Association to study", new String [] {"true", "false" });
        }
        else if (!protocols.isEmpty() && !protocolsNew.isEmpty()) {
        	int pCount = newP.getProtocols().size();
        	go2: for (int i = 0; i < pCount; i++) {
        		PatientProtocol p = (PatientProtocol)protocols.get(i);
        		if ( p.getId() != ((Protocol)newP.getProtocols().get(0)).getId()) {
        			continue go2;
        		}
        		PatientProtocol pp = (PatientProtocol) newP.getProtocols().get(0);

        		// skip the tracking when the current patient is disassociated from current protocol 
        		if(!pp.isAssociated()){
        			if(p.isAssociated() != pp.isAssociated()){
        				pct.getChangedFields().put("Association to study", new String [] {Boolean.toString(p.isAssociated()), Boolean.toString(pp.isAssociated()) });
        			}
        			break go2;
        		}            	

        		if (!doesEqual(p.getSubjectNumber(), pp.getSubjectNumber())) {
        			pct.getChangedFields().put("Subject Number", new String [] {p.getSubjectNumber(), pp.getSubjectNumber()});
        		}

        		if (!doesEqual (p.getEnrollmentDate(), pp.getEnrollmentDate())) {
        			pct.getChangedFields().put("Enrollment Date", new String [] {p.getEnrollmentDate(), pp.getEnrollmentDate()});
        		}

        		if (!doesEqual (p.getCompletionDate(), pp.getCompletionDate())) {
        			pct.getChangedFields().put("Completion Date", new String [] {p.getCompletionDate(), pp.getCompletionDate()});
        		}

        		if (p.getSiteId() != pp.getSiteId()) {
        			pct.getChangedFields().put("Site ID", new String [] {Integer.toString(p.getSiteId()), Integer.toString( pp.getSiteId()) });
        		}

        		if (p.getStatus() != null  && pp.getStatus()!= null){
        			if (p.getStatus().getId()  != pp.getId() ) {
        				pct.getChangedFields().put("Study Status", new String [] {p.getStatus().getShortName(), pp.getStatus().getShortName()});
        			}
        		}
        		
        		// New patient-protocol attributes to be included for edit tracking
        		if (p.getGroupId() != pp.getGroupId() && !(p.getGroupId()==0 && pp.getGroupId()==Integer.MIN_VALUE)) {
        			pct.getChangedFields().put("Subject Group ID", new String [] {
        					(p.getGroupId() >0 ) ? Integer.toString(p.getGroupId()) : "", 
        							(pp.getGroupId() >0 ) ? Integer.toString(pp.getGroupId()) : ""}); 
        		}
        		
        		if (!doesEqual(p.getBiorepositoryId(), pp.getBiorepositoryId())) {
        			pct.getChangedFields().put("Biorepository ID", new String [] {p.getBiorepositoryId(),pp.getBiorepositoryId() });
        		}
        		
        		if (p.isFutureStudy() != pp.isFutureStudy() ) {
        			pct.getChangedFields().put("Consent to Future Study", new String [] {Boolean.toString(p.isFutureStudy()), Boolean.toString(pp.isFutureStudy()) });
        		}
        		
        		if (p.isRecruited() != pp.isRecruited() ) {
        			pct.getChangedFields().put("Recruit Status", new String [] {Boolean.toString(p.isRecruited()), Boolean.toString(pp.isRecruited()) });
        		}
        		
        		if (p.isValidated() != pp.isValidated() ) {
        			pct.getChangedFields().put("Validation Status", new String [] {Boolean.toString(p.isValidated()), Boolean.toString(pp.isValidated()) });
        		}
        		
        		if (p.isActive() != pp.isActive() ) {
        			pct.getChangedFields().put("Active Status", new String [] {Boolean.toString(p.isActive()), Boolean.toString(pp.isActive()) });
        		}
        		
        		break go2;
        	}
        }
        
        return pct;

    }
    public static PatientChangeTracker validateEdit(Patient oldP, Patient newP, HttpServletRequest request) throws CtdbException {
    	
        PatientChangeTracker pct = new PatientChangeTracker();
        String[] changes = null; // changes[0] == old value and changes[1] == new value
        
        if ( !oldP.getSubjectId().equals(newP.getSubjectId()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getSubjectId();
        	changes[1] = newP.getSubjectId();
            pct.getChangedFields().put("nihrecordnumber", changes);
            
            if ( (request.getParameter("recordNumber_reason") != null)
                    && !request.getParameter("recordNumber_reason").trim().equals("") ) {
                pct.getReasons().put("nihrecordnumber", request.getParameter("recordNumber_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the Patient Id");
            }
        }
        
        if ( !oldP.getFirstName().equals(newP.getFirstName()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getFirstName();
        	changes[1] = newP.getFirstName();
            pct.getChangedFields().put("First Name", changes);
            
            if ( (request.getParameter("firstName_reason") != null)
                    && !request.getParameter("firstName_reason").trim().equals("") ) {
                pct.getReasons().put("First Name", request.getParameter("firstName_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the First Name");
            }
        }
        
        if ( !oldP.getLastName().equals(newP.getLastName()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getLastName();
        	changes[1] = newP.getLastName();
            pct.getChangedFields().put("Last Name", changes);
            
            if ( (request.getParameter("lastName_reason") != null)
                    && !request.getParameter("lastName_reason").trim().equals("") ) {
                pct.getReasons().put("Last Name", request.getParameter("lastName_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the Last Name");
            }
        }
        
        if ( !doesEqual(oldP.getMiddleName(), newP.getMiddleName()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getMiddleName();
        	changes[1] = newP.getMiddleName();
            pct.getChangedFields().put("Middle Name", changes);
            
            if ( (request.getParameter("middleName_reason") != null)
                    && !request.getParameter("middleName_reason").trim().equals("") ) {
                pct.getReasons().put("Middle Name", request.getParameter("middleName_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the Middle Name");
            }
        }
        
        if ( !doesEqual (oldP.getEmail(), newP.getEmail()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getEmail();
        	changes[1] = newP.getEmail();
            pct.getChangedFields().put("email", changes);
            
            if ( (request.getParameter("email_reason") != null)
                    && !request.getParameter("email_reason").trim().equals("") ) {
                pct.getReasons().put("email", request.getParameter("email_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the email");
            }
        }
        
        if ( !doesEqual(oldP.getDateOfBirth(), newP.getDateOfBirth()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getDateOfBirth();
        	changes[1] = newP.getDateOfBirth();
            pct.getChangedFields().put("Date Of Birth", changes);
            
            if ( (request.getParameter("dateOfBirth_reason") != null)
                    && !request.getParameter("dateOfBirth_reason").trim().equals("") ) {
                pct.getReasons().put("Date Of Birth", request.getParameter("dateOfBirth_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the Date Of Birth");
            }
        }
        
        if ( !doesEqual(oldP.getPhoneNumbers().get(PhoneType.HOME).getNumber(), newP.getPhoneNumbers().get(PhoneType.HOME).getNumber()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getPhoneNumbers().get(PhoneType.HOME).getNumber();
        	changes[1] = newP.getPhoneNumbers().get(PhoneType.HOME).getNumber();
            pct.getChangedFields().put("Home Phone", changes);
            
            if ( (request.getParameter("homePhone_reason") != null) 
            		&& !request.getParameter("homePhone_reason").trim().equals("") ) {
                pct.getReasons().put("Home Phone", request.getParameter("homePhone_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the Home Phone");
            }
        }
        
        if ( !doesEqual(oldP.getPhoneNumbers().get(PhoneType.WORK).getNumber(), newP.getPhoneNumbers().get(PhoneType.WORK).getNumber()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getPhoneNumbers().get(PhoneType.WORK).getNumber();
        	changes[1] = newP.getPhoneNumbers().get(PhoneType.WORK).getNumber();
            pct.getChangedFields().put("Work Phone", changes);
            
            if ( (request.getParameter("workPhone_reason") != null)
                    && !request.getParameter("workPhone_reason").trim().equals("") ) {
                pct.getReasons().put("Work Phone", request.getParameter("workPhone_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the Work Phone");
            }
        }
        
        if ( !doesEqual(oldP.getPhoneNumbers().get(PhoneType.MOBILE).getNumber(), newP.getPhoneNumbers().get(PhoneType.MOBILE).getNumber()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getPhoneNumbers().get(PhoneType.MOBILE).getNumber();
        	changes[1] = newP.getPhoneNumbers().get(PhoneType.MOBILE).getNumber();
            pct.getChangedFields().put("Mobile Phone", changes);
            
            if ( (request.getParameter("mobilePhone_reason") != null)
                    && !request.getParameter("mobilePhone_reason").trim().equals("") ) {
                pct.getReasons().put("Mobile Phone", request.getParameter("mobilePhone_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the Mobile Phone");
            }
        }
        
        if ( !doesEqual(oldP.getHomeAddress().getAddressOne(), newP.getHomeAddress().getAddressOne()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getHomeAddress().getAddressOne();
        	changes[1] = newP.getHomeAddress().getAddressOne();
            pct.getChangedFields().put("address1", changes);
            
            if ( (request.getParameter("address1_reason") != null)
                    && !request.getParameter("address1_reason").trim().equals("") ) {
                pct.getReasons().put("address1", request.getParameter("address1_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the address 1");
            }
        }
        
        if ( !doesEqual(oldP.getHomeAddress().getAddressTwo(), newP.getHomeAddress().getAddressTwo()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getHomeAddress().getAddressTwo();
        	changes[1] = newP.getHomeAddress().getAddressTwo();
            pct.getChangedFields().put("address2", changes);
            
            if ( (request.getParameter("address2_reason") != null)
                    && !request.getParameter("address2_reason").trim().equals("") ) {
                pct.getReasons().put("address2", request.getParameter("address2_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the address 2");
            }
        }
        
        if ( !doesEqual(oldP.getHomeAddress().getCity(), newP.getHomeAddress().getCity()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getHomeAddress().getCity();
        	changes[1] = newP.getHomeAddress().getCity();
            pct.getChangedFields().put("city", changes);
            
            if ( (request.getParameter("city_reason") != null)
                    && !request.getParameter("city_reason").trim().equals("") ) {
                pct.getReasons().put("city", request.getParameter("city_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the city");
            }
        }
        
        if ( !(oldP.getHomeAddress().getState().getId() == newP.getHomeAddress().getState().getId()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getHomeAddress().getState().getShortName();
        	changes[1] = newP.getHomeAddress().getState().getShortName();
            pct.getChangedFields().put("state", changes);
            
            if ( (request.getParameter("state_reason") != null)
                    && !request.getParameter("state_reason").trim().equals("") ) {
                pct.getReasons().put("state", request.getParameter("state_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the state");
            }
        }
        
        if ( !doesEqual(oldP.getHomeAddress().getZipCode(), newP.getHomeAddress().getZipCode()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getHomeAddress().getZipCode();
        	changes[1] = newP.getHomeAddress().getZipCode();
            pct.getChangedFields().put("zip", changes);
            
            if ( (request.getParameter("zip_reason") != null)
                    && !request.getParameter("zip_reason").trim().equals("") ) {
                pct.getReasons().put("zip", request.getParameter("zip_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the zip");
            }
        }
        
        if ( !(oldP.getHomeAddress().getCountry().getId() == newP.getHomeAddress().getCountry().getId()) ) {
        	changes = new String[2];
        	changes[0] = oldP.getHomeAddress().getCountry().getShortName();
        	changes[1] = newP.getHomeAddress().getCountry().getShortName();
            pct.getChangedFields().put("country", changes);
            
            if ( (request.getParameter("country_reason") != null)
                    && !request.getParameter("country_reason").trim().equals("") ) {
                pct.getReasons().put("country", request.getParameter("country_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the country");
            }
        }
        
        // Compare the new protocol with the old one
        SiteManager sMan = new SiteManager();
        PatientProtocol oldProto = null;
        PatientProtocol newProto = newP.getProtocols().get(0);
        
        // Get the protocol from the old patient
        for ( PatientProtocol p : oldP.getProtocols() )
        {
        	if ( p.getId() == newProto.getId() )
        	{
        		oldProto = p;
        		break;
        	}
        }
        
        // Continue with the protocol validation
        if ( !doesEqual(oldProto.getSubjectNumber(), newProto.getSubjectNumber()) ) {
        	changes = new String[2];
        	changes[0] = oldProto.getSubjectNumber();
        	changes[1] = newProto.getSubjectNumber();
        	pct.getChangedFields().put("Subject Number", changes);
        	
            if ( (request.getParameter("subjectNumber_reason") != null)
                    && !request.getParameter("subjectNumber_reason").trim().equals("") ) {
                pct.getReasons().put("Subject Number", request.getParameter("subjectNumber_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the  subject Number");
            }
        }
        
        if ( !doesEqual(oldProto.getEnrollmentDate(), newProto.getEnrollmentDate()) ) {
        	changes = new String[2];
        	changes[0] = oldProto.getEnrollmentDate();
        	changes[1] = newProto.getEnrollmentDate();
            pct.getChangedFields().put("Enrollment Date", changes);
            
            if ( (request.getParameter("enrollmentDate_reason") != null)
                    && !request.getParameter("enrollmentDate_reason").trim().equals("") ) {
                pct.getReasons().put("Enrollmen tDate", request.getParameter("enrollmentDate_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the  enrollment Date");
            }
        }
        
        if ( !doesEqual( oldProto.getCompletionDate(), newProto.getCompletionDate()) ) {
        	changes = new String[2];
        	changes[0] = oldProto.getCompletionDate();
        	changes[1] = newProto.getCompletionDate();
            pct.getChangedFields().put("Completion Date", changes);
            
            if ( (request.getParameter("completionDate_reason") != null)
                    && !request.getParameter("completionDate_reason").trim().equals("") ) {
                pct.getReasons().put("Completion Date", request.getParameter("completionDate_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the  completion Date");
            }
        }
        
        if ( oldProto.getSiteId() != newProto.getSiteId() ) {
        	changes = new String[2];
        	changes[0] = sMan.getSite(oldProto.getSiteId()).getName();
        	changes[1] = sMan.getSite(newProto.getSiteId()).getName();
            pct.getChangedFields().put("siteId", changes);
            
            if ( (request.getParameter("siteId_reason") != null)
                    && !request.getParameter("siteId_reason").trim().equals("") ) {
                pct.getReasons().put("siteId", request.getParameter("siteId_reason"));
            } else {
                pct.getErrors().add("A reason is required to change the  Patient Site");
            }
        }
        
        if ( (oldProto.getStatus() != null) && (newProto.getStatus() != null) ) {
        	if ( oldProto.getStatus().getId() != newProto.getStatus().getId() ) {
        		changes = new String[2];
        		changes[0] = oldProto.getStatus().getShortName();
        		changes[1] = newProto.getStatus().getShortName();
                pct.getChangedFields().put("active", changes);
                
                if ( (request.getParameter("active_reason") != null)
                        && !request.getParameter("active_reason").trim().equals("") ) {
                    pct.getReasons().put("active", request.getParameter("active_reason"));
                } else {
                    pct.getErrors().add("A reason is required to change the  patient status");
                }
            }
        }

        return pct;
    }
}
