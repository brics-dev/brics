package gov.nih.tbi.commons.portal;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.result.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.portal.PortalUtils;

/**
 * This action class serves as a dummy server request to keep the session alive before timeout.
 * Please note this is shared by both portal and dictionary. For dictionary, this call will extend
 * both portal and dictionary server sessions.
 *  
 * @author jim3
 * 
 */
public class KeepAliveAction extends ActionSupport {
	@Autowired
	ModulesConstants modulesConstants;

	private static final long serialVersionUID = -2543642857913300299L;
	static Logger logger = Logger.getLogger(KeepAliveAction.class);

	public StreamResult keepAlive() {
		// extend application session
		ServletActionContext.getRequest().getSession();

		// extend CAS session doing WS call by getting proxy ticket
		String url = modulesConstants.getModulesAccountURL();
		String proxyTicket = PortalUtils.getProxyTicket(url);
		if (proxyTicket == null) {
			logger.error("The user's portal session has expired");
			return new StreamResult(new ByteArrayInputStream((ERROR).getBytes()));
		}

		RestAccountProvider restProvider = new RestAccountProvider(url, proxyTicket);
		try {
			restProvider.keepSessionAlive();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		// If it's in dictionary, request a new proxy ticket to extend dictionary CAS session as well
		String namespace = ServletActionContext.getActionMapping().getNamespace().substring(1);
		if (PortalConstants.NAMESPACE_DICTIONARY.equals(namespace) || 
				PortalConstants.NAMESPACE_DICTIONARYADMIN.equals(namespace)) {
			
			String dictProxyTicket = PortalUtils.getProxyTicket(modulesConstants.getModulesDDTURL());
			if (dictProxyTicket == null) {
				logger.error("The user's dictionary session has expired");
				return new StreamResult(new ByteArrayInputStream((ERROR).getBytes()));
			}
		}
		
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}
}
