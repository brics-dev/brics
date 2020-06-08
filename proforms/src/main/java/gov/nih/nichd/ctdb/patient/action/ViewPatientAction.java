package gov.nih.nichd.ctdb.patient.action;

import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.patient.common.PatientAssembler;
import gov.nih.nichd.ctdb.patient.common.PatientVisitResultControl;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.form.PatientForm;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.site.manager.SiteManager;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;

/**
 * Created by IntelliJ IDEA.
 * User: 511343
 * Date: Mar 17, 2004
 * Time: 2:36:31 PM
 * To change this template use Options | File Templates.
 */
public class ViewPatientAction extends BaseAction {
	private static final long serialVersionUID = 6277912670029312424L;
	private static final Logger log = Logger.getLogger(ViewPatientAction.class);
	
    private String patientId = null;
	private PatientForm patientForm = new PatientForm();
    private String associatedWithStudy = "true";

    public String execute() throws Exception {
		buildLeftNav(LeftNavController.LEFTNAV_SUBJECTS_MYSUBJECTS);
		
        Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        int protocolId = protocol.getId();
		
		boolean protocolClosed = ((Boolean) session.get(CtdbConstants.PROTOCOL_CLOSED_SESSION_KEY)).booleanValue();
		if (protocolClosed) {
			disableLinksForProtocolCloseout(LeftNavController.LEFTNAV_SUBJECTS_MYSUBJECTS,
					new int[] {LeftNavController.LEFTNAV_SUBJECTS_ADD, LeftNavController.LEFTNAV_SUBJECTS_VISITS});
		}

        String expandedDisplayOption = request.getParameter("sectionDisplay");
        if (expandedDisplayOption != null){
        	patientForm.setSectionDisplay(expandedDisplayOption);
        }
        
        try {
            PatientManager pm = new PatientManager();
            ResponseManager rm = new ResponseManager();
            
       
			Patient patient = pm.getPatient(patientId, protocolId);
            pm.updatePatientDisplayValues(patient);
            patientForm.setId(patient.getId());
            patientForm.setCurrentProtocolId(protocolId + "");
            
            PatientAssembler.domainToForm(patient, patientForm);
            setExtraDisplayInfo(patient);
            
            for (Protocol pp : patient.getProtocols()) {
            	if (pp.getId() == protocolId) {
                    patientForm.setActive(String.valueOf(pp.isActive()));
                    break;
            	}
            }
            
            request.setAttribute(CtdbConstants.PATIENT_ATTACHMENTS_SESSION_KEY, patient.getAttachments());
            request.setAttribute("_completedForms", rm.getCompletedPatientForms(patient.getId(), protocolId));
            
            // get list of patient visit dates
    		PatientVisitResultControl pvrc = new PatientVisitResultControl();
    		pvrc.setPatientId(patient.getId());
    		pvrc.setProtocolId(protocolId);
            request.setAttribute(CtdbConstants.PATIENT_VISIT_DATE_LIST_KEY, pm.getPatientVisits(pvrc,protocolId));
            session.put(CtdbConstants.PATIENT_ID_REQUEST_ATTR, patientId);
        } catch (Exception e) {
        	log.error("Error", e);
        	return StrutsConstants.FAILURE;
        }

		return SUCCESS;
    }


    private CtdbLookup getCtdbLookup(int id, List<CtdbLookup> CtdbLookupList) {
    	if (CtdbLookupList != null && !CtdbLookupList.isEmpty()) {
    		for (CtdbLookup cl : CtdbLookupList) {
    			if (id == cl.getId()) {
    				return cl;
    			}
    		}
    	}
    	
    	return new CtdbLookup();
    }
    

    private void setExtraDisplayInfo(Patient p) {
       	if (p == null) {
       		return;
       	}
       	
       	try {
           	LookupManager lookup = new LookupManager();
           	
           	// set country names
			List<CtdbLookup> lookupList = lookup.getLookups(LookupType.COUNTRY);
			CtdbLookup cl = getCtdbLookup(patientForm.getBirthCountryId(), lookupList);
			patientForm.setDisplayBirthCountry(cl.getLongName());

			int id = 0;
			if (!Utils.isBlank(patientForm.getCountry())) {
				id = Integer.parseInt(patientForm.getCountry());
			}
			patientForm.setDisplayHomeCountry(getCtdbLookup(id, lookupList).getLongName());

            // set state names
			lookupList = lookup.getLookups(LookupType.STATE);
			id = 0;
			if (!Utils.isBlank(patientForm.getState())) {
				id = Integer.parseInt(patientForm.getState());
			}
			patientForm.setDisplayHomeState(getCtdbLookup(id, lookupList).getLongName());
			
			id = 0;
				
			// set patient Site name
			if (patientForm.getSiteId() != Integer.MIN_VALUE) {
				SiteManager sm = new SiteManager();
		        patientForm.setDisplaySiteName(sm.getSite(patientForm.getSiteId()).getName());
			} else {
				patientForm.setDisplaySiteName("N.A.");
			}
		} catch (CtdbException e) {
			log.error("Could not set extra display info.", e);
		}
    }


	public PatientForm getPatientForm() {
		return patientForm;
	}
	public void setPatientForm(PatientForm patientForm) {
		this.patientForm = patientForm;
	}



	public String getAssociatedWithStudy() {
		return associatedWithStudy;
	}
	public void setAssociatedWithStudy(String associatedWithStudy) {
		this.associatedWithStudy = associatedWithStudy;
	}


	public String getPatientId() {
		return patientId;
	}


	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}



}
