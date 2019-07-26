package gov.nih.nichd.ctdb.protocol.action;

import java.util.List;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;

/**
 * The Struts Action class responsable for displaying the protocol
 * audit trail for the nichd ctdb
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ProtocolAuditAction extends BaseAction
{
    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return ActionForward which will indicate to Struts which view to display upon completion of this Action
     * @exception Exception if any exception occurs during processing
     */
    public String execute() throws Exception {
    	String forward = SUCCESS;
        try
        {
            if(request.getParameter(CtdbConstants.ID_REQUEST_ATTR) == null ||
                    request.getParameter(CtdbConstants.ID_REQUEST_ATTR).equals(""))
            {
                throw new Exception("Invalid Id: " + request.getParameter(CtdbConstants.ID_REQUEST_ATTR)
                                    + " passed to Patient Audit Action");
            }
            else
            {
                ProtocolManager pm = new ProtocolManager();
                List<Protocol> versions = pm.getProtocolVersions(Integer.parseInt(request.getParameter(CtdbConstants.ID_REQUEST_ATTR)));
                request.setAttribute("versions", versions);
            }
        }
        catch(CtdbException ce)
        {
        		return StrutsConstants.FAILURE;
        	
        }
        
        return forward;
    }
}
