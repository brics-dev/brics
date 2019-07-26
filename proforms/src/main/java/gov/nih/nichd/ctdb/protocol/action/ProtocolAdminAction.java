package gov.nih.nichd.ctdb.protocol.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;

/**
 * Developed by CIT.
 * @author Shashi Rudrappa
 * Date: May 18, 2012
 * @version 1.0
 */
public class ProtocolAdminAction extends BaseAction {
	private static final long serialVersionUID = -3908357025359496344L;
	private static final Logger logger = Logger.getLogger(ProtocolAdminAction.class);
	
	/**
	 * Struts Constant used to set/get ActionMessages for this action from session.
	 * Used for the redirect to the main listing page for this functionality.
	 */
	public static final String ACTION_MESSAGES_KEY = "ProtocolAdminAction_ActionMessages";
	
	public String deleteProtocols() throws Exception {
        Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		String sessionProtocolNumber = "";
		if (protocol != null) {
			sessionProtocolNumber = protocol.getProtocolNumber();
		}

		User user = getUser();
		String selectedProtocolIdsStr = request.getParameter(CtdbConstants.ID_REQUEST_ATTR);

		try {
			ProtocolManager protoMan = new ProtocolManager();
			List<Integer> selectedProtocolIds = Utils.convertStrToIntArray(selectedProtocolIdsStr);
			List<String> notDeletedProtocols = new ArrayList<String>();
			List<String> deletedProtocols = protoMan.softDeleteProtocol(selectedProtocolIds, 
					user, notDeletedProtocols);

			for (String protocolNumber : notDeletedProtocols) {
				addActionMessage("Could not delete protocol \"" + protocolNumber + 
						"\". Please contact the System Admin or Study PI to delete the study.");
			}
					
			if (deletedProtocols != null && !deletedProtocols.isEmpty()) {
				String deleteProtocols = "";
					
				// Create a list of study names that have been deleted for the success message.
				for (String deletedProcolNumber : deletedProtocols) {
					if (!deleteProtocols.isEmpty()) {
						deleteProtocols += ", ";
					}
					deleteProtocols += deletedProcolNumber;
						
					// Check if the currently selected study is being deleted, and 
					// reset the overview dropdown it is
					if (deletedProcolNumber.equalsIgnoreCase(sessionProtocolNumber)) {
						session.remove(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
					}
				}
					
				addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY, new String[]{
						"Protocol(s) " + deleteProtocols}));
			}
				
			// Refreshing overview dropdown
			List<Protocol> newProtocolList = SecuritySessionUtil.refreshUserProtocols(user, request);
			request.setAttribute("protocolList", newProtocolList);
			session.put(ACTION_MESSAGES_KEY, getActionMessages());
			
		} catch (Exception e) {
			logger.error("Couldn't delete protocol(s): " + selectedProtocolIdsStr, e);
			return StrutsConstants.FAILURE;
		}
		
		return SUCCESS;
	}
	
	
	public String protocolAudit() throws Exception {
		
		String selectedProtocolId = request.getParameter(CtdbConstants.ID_REQUEST_ATTR);
		
        try {
            if (Utils.isBlank(selectedProtocolId)) {
                throw new Exception("Invalid Id passed to Patient Audit Action");
            } else {
                ProtocolManager pm = new ProtocolManager();
                List<Protocol> versions = pm.getProtocolVersions(Integer.parseInt(selectedProtocolId));
                request.setAttribute("versions", versions);
            }
		}catch (CtdbException ce) {
			return StrutsConstants.FAILURE;
        }

        return SUCCESS;
    }	
}
