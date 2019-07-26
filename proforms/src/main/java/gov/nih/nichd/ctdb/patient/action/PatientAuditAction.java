package gov.nih.nichd.ctdb.patient.action;

import java.util.List;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;

/**
 * The Struts Action class responsable for displaying the patient
 * trail for the nichd ctdb
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientAuditAction extends BaseAction {

	private static final long serialVersionUID = -2819426942348759899L;

    public String execute() throws Exception {
    	
        try {
        	String idStr = request.getParameter(CtdbConstants.PATIENT_ID_REQUEST_ATTR);
            if (Utils.isBlank(idStr)) {
                throw new CtdbException("Invalid Id: " + idStr + " passed to Subject Audit Action");
            } else {
               
                PatientManager pm = new PatientManager();
                List<Patient> patientVersions = pm.getPatientVersions(idStr);
                request.setAttribute("patientVersions", patientVersions);
                request.setAttribute("patientChanges", pm.getPatientChanges(idStr));
            }
        } catch (CtdbException ce) {
        	ce.printStackTrace();
        	addActionError(ce.getMessage());
            return StrutsConstants.FAILURE;
        }
        
        return  SUCCESS;
    }
}