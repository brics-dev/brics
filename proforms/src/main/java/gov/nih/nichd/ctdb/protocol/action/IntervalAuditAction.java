package gov.nih.nichd.ctdb.protocol.action;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;

import java.util.List;

/**
 * The Struts Action class responsable for displaying the interval
 * audit trail for the nichd ctdb
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class IntervalAuditAction extends BaseAction {

	private static final long serialVersionUID = 4017978582360640711L;

	private String id;
	
	public String execute() throws Exception {

		try {
            if (Utils.isBlank(getId())) {
                addActionError("Invalid Id passed to Interval Audit Action.");
                return StrutsConstants.FAILURE;
            } else {
                ProtocolManager pm = new ProtocolManager();
                List<Interval> versions = pm.getIntervalVersions(Integer.parseInt(id));
                request.setAttribute("versions", versions);
            }
        } catch (CtdbException ce) {
           ce.printStackTrace();
        	return StrutsConstants.FAILURE;
        }
		
        return SUCCESS;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
