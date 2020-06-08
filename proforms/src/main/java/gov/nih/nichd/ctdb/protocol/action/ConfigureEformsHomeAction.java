package gov.nih.nichd.ctdb.protocol.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.protocol.tag.ConfigureEformHomeIdtDecorator;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;


public class ConfigureEformsHomeAction extends BaseAction {

	private static final Logger logger = Logger.getLogger(ConfigureEformsHomeAction.class);

	
	
	/**
	 * execute method
	 */
	public String execute() {
		buildLeftNav(LeftNavController.LEFTNAV_EFORMS_CONFIGURE);

		
		return BaseAction.SUCCESS;
	}
	
	
	/**
	 * Gets the list of eforms associated to visit types to load in datatable
	 * 
	 * @return
	 */
	public String getEformConfigureList() {
		
		Locale userLocale = request.getLocale();
		
		ProtocolManager protoMgr = new ProtocolManager();
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		List<BasicEform> eformList = new ArrayList<BasicEform>();
		
		try {
			eformList = protoMgr.getEformsForAllVisitTypes(p.getId());
		} catch (CtdbException e) {
			logger.error("A database error occured while getting a list of eforms.", e);
			addActionError(getText(StrutsConstants.ERROR_DATABASE_GET,
					new String[] {getText("protocol.eform.configure.title.display").toLowerCase(userLocale)}));
		}
		
		try {
			IdtInterface idt = new Struts2IdtInterface();			
			ArrayList<BasicEform> outputList = new ArrayList<BasicEform>(eformList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ConfigureEformHomeIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		
		return null;
	}

}
