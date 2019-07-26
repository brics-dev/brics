package gov.nih.nichd.ctdb.security.action;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CasProxyTicketException;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.manager.DataSubmissionManager;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.response.util.DataCollectionUtils;
import gov.nih.nichd.ctdb.security.domain.User;

public class AdminDataSubmissionAction extends BaseAction {
	private static final long serialVersionUID = -4076407741359160809L;
	private static final Logger logger = Logger.getLogger(AdminDataSubmissionAction.class);
	
	public String adminFormId;
	
	/**
	 * TODO: need to get the data submission dao and submit the data
	 * 			make sure that the data is correct when pushed into the datasubmission table
	 * 			this might need a new method to look at the answers
	 * 
	 * 		need to bundle in the front end. there needs to be a submit and a text box
	 * 
	 * 		need to convert the function to make sure you can upload multiple ids at once
	 * 			you should make sure that the protocols are stored in a map to prevent unnecessary dao calls
	 * 			you should also make sure multiple admin form ids aren't submitted in one transaction
	 * 
	 * 		if there is time after testing you can create a bulk upload capability. maybe in groups of 10
	 * 
	 * 
	 */

	public String execute() throws Exception {
		logger.info("AdminDataSubmissionAction-----------------> begin");

		User user = getUser();

		// Ensure that the current user is system admin.
		if ((user == null) || !user.isSysAdmin()) {
			addActionError("The user (" + user.getUsername() + ") is not a system admin.");
			return StrutsConstants.FORBIDDEN;
		}
		this.buildLeftNav(LeftNavController.LEFTNAV_ADMIN_URLS, new int[]{3, 8, 11, 15});
		return SUCCESS;
	}

	public String setCollectionForMirth() {
		logger.info("AdminDataSubmissionAction-----------------> setCollectionForMirth");

		User user = getUser();

		// only sys admin should be able to do this
		if ((user == null) || !user.isSysAdmin()) {
			addActionError("The user (" + user.getUsername() + ") is not a system admin.");
			return StrutsConstants.FORBIDDEN;
		}

		if (adminFormId == null) {
			logger.error("There weren't any administered form ids to process");;
			addActionError("Form Id is not fdefined");
			return StrutsConstants.FAILURE;
		} 
		
		if(adminFormId == null || adminFormId.isEmpty()){
			this.buildLeftNav(LeftNavController.LEFTNAV_ADMIN_URLS, new int[]{3, 8, 11, 15});
			adminFormId = null;
			return SUCCESS;
		}
		String[] idList = adminFormId.split(",");
		
		FormManager fm = new FormManager();
		ProtocolManager pm = new ProtocolManager();
		DataSubmissionManager dsm = new DataSubmissionManager();
		
		for(String adminFormId :  idList){
			logger.error("AdminDataSubmissionAction-----------------> processing " + adminFormId);
			int administeredFormId = Integer.parseInt(adminFormId.trim());
			
			try{
				String[] idResponse = fm.getEFormShortNameAndProtocolIdByAFormId(administeredFormId);
				
				String shortName = idResponse[0];
				int protocolId = Integer.parseInt(idResponse[1]);
				
				Protocol protocol = pm.getProtocol(protocolId);
				
				AdministeredForm aForm =  retrieveAdministeredFormWithResponses(shortName, administeredFormId, user);
				aForm.getForm().setProtocol(protocol);
				aForm.getForm().setProtocolId(protocolId);
				
				dsm.submitDataForMirth(aForm,protocol);
			} catch (Exception e){
				logger.error("We weren't able to build and submit the collection from the form and eform.", e);
				addActionError("There was an error processing the collection. Check the logs and last submission for more details");
				return StrutsConstants.FAILURE;
			}
			logger.error("AdminDataSubmissionAction-----------------> finished processing " + adminFormId);
		}
		
		this.buildLeftNav(LeftNavController.LEFTNAV_ADMIN_URLS, new int[]{3, 8, 11, 15});
		adminFormId = null;
		return SUCCESS;
	}

	private AdministeredForm retrieveAdministeredFormWithResponses(String shortName, int administeredFormId, User user) 
			throws CtdbException, WebApplicationException, CasProxyTicketException  {
		ResponseManager rm = new ResponseManager();
		AdministeredForm aform = null;
		
		aform = rm.getAdministeredFormForUnadmin(administeredFormId);

		int eformid = aform.getEformid();
		FormDataStructureUtility fsUtil = new FormDataStructureUtility();
		Form form = fsUtil.getEformFromBrics(request, shortName);
		form.setId(eformid);
		aform.setForm(form);
		aform.setResponses(rm.getResponsesLocked(aform.getId()));
		DataCollectionUtils.completeAform(aform);

		return aform;

	}

	public String getAdminFormId() {
		return this.adminFormId;
	}

	public void setAdminFormId(String adminFormId) {
		this.adminFormId = adminFormId;
	}

}
