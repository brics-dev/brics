package gov.nih.nichd.ctdb.response.action;

import java.util.Map;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.common.ResponseConstants;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.DataEntryHeader;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;


/**
 * ViewAssignmentAction is the Struts action class for viewing the history
 *		of data entry history.
 * This class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ViewAssignmentAction extends BaseAction {

    private static final long serialVersionUID = 6961898234705696958L;

	public String execute() throws Exception {

        String formIdStr = request.getParameter(CtdbConstants.ID_REQUEST_ATTR);
        try {

            if (Utils.isBlank(formIdStr)) {
                throw new Exception("Invalid Id passed to View Data Entry Re-Assignment Action");
                
            } else {
            	int formId = Integer.parseInt(formIdStr);
            	ResponseManager rm = new ResponseManager();
            	
                request.setAttribute("dataentryassignmentlist", rm.getAssignArchives(formId));

                AdministeredForm admForm = rm.getAdministeredFormForViewAudit(formId);

                @SuppressWarnings("unchecked")
				Map<String, String> recordsMap = (Map<String, String>) session.get("recordsMap");
                @SuppressWarnings("unchecked")
				Map<String, String> patientLabelMap = (Map<String, String>) session.get("patientLabelMap");

                Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
                
                DataEntryHeader dataEntryHeader = new DataEntryHeader();
                dataEntryHeader.setFormDisplay(admForm.getForm().getName());
                dataEntryHeader.setDateDisplay(admForm.getVisitDateStringMMddyyyy());
                dataEntryHeader.setScheduledVisitDateDisplay(admForm.getScheduledVisitDateStringyyyyMMddHHmm());

                
                if (protocol.isUsePatientName()) {
                    dataEntryHeader.setPatientDisplay(patientLabelMap.get(formIdStr));
                } else {
                    dataEntryHeader.setPatientDisplay(recordsMap.get(formIdStr));
                }
                
                session.put(ResponseConstants.DATAENTRYHEADER_SESSION_KEY, dataEntryHeader);
            }
        } catch (CtdbException ce) {
            return StrutsConstants.FAILURE;
        }
        
        return SUCCESS;
    }

}
