package gov.nih.tbi.commons.portal;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.portal.PortalUtils;

/**
 * This action class serves as a dummy server request to keep the session alive before timeout.
 * 
 * @author jim3
 * 
 */
public class KeepAliveAction extends ActionSupport {
	@Autowired
	ModulesConstants modulesConstants;

	private static final long serialVersionUID = -2543642857913300299L;
	static Logger logger = Logger.getLogger(KeepAliveAction.class);

	public String execute() {
		// extend application session
		ServletActionContext.getRequest().getSession();
		
		if (logger.isDebugEnabled()) {
			logger.debug("KeepAliveAction->execute->Id:\t" + ServletActionContext.getRequest().getSession().getId());
		}

		// extend CAS session doing WS call by getting proxy ticket
		String url = modulesConstants.getModulesAccountURL();


		RestAccountProvider restProvider = new RestAccountProvider(url, PortalUtils.getProxyTicket(url));
		try {
			restProvider.keepSessionAlive();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// extend CAS session
		// String dictionaryUrl = modulesConstants.getModulesDDTURL(ServiceConstants.DEFAULT_PROVIDER);
		// RestDictionaryProvider restProvider =
		// new RestDictionaryProvider(dictionaryUrl, PortalUtils.getProxyTicket(dictionaryUrl));
		// try {
		// logger.info("WS diseses list size:\t"+restProvider.getDiseaseList().size());
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return SUCCESS;
	}
}
