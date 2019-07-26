package gov.nih.nichd.ctdb.response.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.importdata.thread.JsonDataSendingThread;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.response.form.ImportHL7DataForm;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * Created by Ching Heng Lin.
 * Date: Dec 11, 2012
 */
public class SendHL7DataByFormAction extends BaseAction {
	
	private static final long serialVersionUID = 7500693890780603827L;
	
	private ImportHL7DataForm importForm = new ImportHL7DataForm();
	
	public String execute()throws Exception {
        buildLeftNav(LeftNavController.LEFTNAV_COLLECT_COLLECT);
        
        Protocol currProtocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		User user = getUser();
        
        ProtocolManager protoMan = new ProtocolManager();
        FormManager fm = new FormManager();
        
        if (importForm.getAction() == null) {
        	//get form name
            int formId = Integer.parseInt(request.getParameter("formId"));
            Form form = fm.getForm(formId, true);
            String formName = form.getName();
            request.setAttribute("formName", formName);
            
	        // get all intervals for protocol
			List<Interval> intervals = protoMan.getIntervals(currProtocol.getId());
	        Map<String, String> intervalOptions = new HashMap<String, String>();
	        for (Interval interv : intervals) {
				intervalOptions.put(Integer.toString(interv.getId()), interv.getName());
			}
			request.setAttribute("intervalOptions", intervalOptions);
			
			Map<String, String> dataSourceList = new HashMap<String, String>();
			for (int i=1; i<3; i++) { // just for demo, those two data source are the same
				dataSourceList.put("BRITS HL7 Web Service-"+i, SysPropUtil.getProperty("brits.hl7.web.service"));
			}
			request.setAttribute("dataSourceURLOptions", dataSourceList);
						
			importForm.setTheFormId(formId);
			importForm.setAction(StrutsConstants.CONTINUE);
			return StrutsConstants.FORM;
			
        } else if (importForm.getAction().equalsIgnoreCase(StrutsConstants.CONTINUE)) {
        	importForm.setProtocol(currProtocol);
        	importForm.setUser(user);
        	JsonDataSendingThread jsonDataSendingThread = new JsonDataSendingThread(importForm);
        	jsonDataSendingThread.start();
        	
        	return SUCCESS;
        	
        } else {
            return StrutsConstants.FAILURE;
        }
   
    }
}
