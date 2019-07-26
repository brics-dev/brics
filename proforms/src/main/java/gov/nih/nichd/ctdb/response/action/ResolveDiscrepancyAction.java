package gov.nih.nichd.ctdb.response.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.response.common.InputHandler;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.security.domain.User;


/**
 * The Struts Action class responsable for displaying the forms with discrepancies
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ResolveDiscrepancyAction extends BaseAction {
	
	private static final long serialVersionUID = -8605500465918634575L;
	private static final Logger logger = Logger.getLogger(ResolveDiscrepancyAction.class);
	
	public static final String ACTION_MESSAGES_KEY = "ResolveDiscrepancyAction_ActionMessages";
	public static final String ACTION_ERRORS_KEY = "ResolveDiscrepancyAction_ActionErrors";
	
	private String id;
	private boolean closePopUp = false;
	
	public String viewResolvedForm() throws Exception {
		
		/*int aformId = -1;
		
		if (!Utils.isBlank(id)) {
			aformId = Integer.parseInt(id);
		}
		
		AdministeredForm aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
		ResponseManager rm = new ResponseManager();
		
		try {
			AdministeredForm dForm = null;
			
			//get the up-to-date AdministeredForm object with discrepant responses only.
			dForm = rm.getAdministeredForm(aformId, true);
			
			session.put("discrepancyForm", dForm);
			request.setAttribute("responseList", dForm.getResponses());
			session.put("adminformid", Integer.toString(dForm.getId()));
		}
		catch (ObjectNotFoundException onfe) {
			addActionError(getText(StrutsConstants.ERROR_NOTFOUND, new String[]{
				"Administerd form " + aform.getForm().getName() + " "}));
			session.put(ResolveDiscrepancyAction.ACTION_ERRORS_KEY, getActionErrors());
			logger.error("Could not find the admin form.", onfe);
			
			return StrutsConstants.EXCEPTION;
		}
		catch (CtdbException ce) {
			logger.error("Database error while viewing resolved form.", ce);
			
			return StrutsConstants.EXCEPTION;
		}*/
		
		return SUCCESS;
	}
	
	
	public String saveDiscrepancy() throws Exception {
		
        AdministeredForm aForm = (AdministeredForm) session.get("discrepancyForm");
		User user = getUser();
        ResponseManager rm = new ResponseManager();

        try {
            List<String> qidList = new ArrayList<String>();
            List<String> errorList = InputHandler.getFinalAnswers(aForm, request, qidList);
            	
            if (!errorList.isEmpty()) {
            	for (String errorMsg : errorList) {
            		addActionError(errorMsg);
            	}
            		
            	List<Response> expandedList = this.expandList(aForm.getResponses(), qidList);
            	aForm.setResponses(expandedList);
            	request.setAttribute("responseList", expandedList);
        		
            	return StrutsConstants.EXCEPTION;
            }
            	
            rm.saveDiscrepancyProgress(aForm, user);
            	
            if (!aForm.getDiscrepancyFlag()) {
            	addActionMessage("The discrepancies in administered form " +
            			aForm.getForm().getName() + " have been completely resolved.");
            	session.put(ResolveDiscrepancyAction.ACTION_MESSAGES_KEY, getActionMessages());
            } else {
            	request.setAttribute("responseList", aForm.getResponses());
            	addActionMessage(getText(StrutsConstants.SUCCESS_EDIT_KEY, 
            			new String[]{aForm.getForm().getName()}));
            }
        }
        catch (ObjectNotFoundException onfe) {
            addActionError(getText(StrutsConstants.ERROR_NOTFOUND, new String[]{
            		"Administerd form " + aForm.getForm().getName() + " "}));
            logger.error("Could not find the admin form.", onfe);
            
            return StrutsConstants.EXCEPTION;
            
        }
        catch (CtdbException ce) {
        	addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, new String[] {aForm.getForm().getName()}));
        	logger.error("Database error while resolving a discrpancy.", ce);
        	
            return StrutsConstants.EXCEPTION;
        }
        
        closePopUp = true;

        return SUCCESS;
    }
	
	
    /**
     * Expand a list of responses which has another list
     *			of responses in it into a single list of responses.
     *
     * @param responses the list of responses to be expanded
     * @param qidList the list of question ids used in response list expansion.
     * @return The expanded list of responses
     */
    private List<Response> expandList(List<Response> responses, List<String> qidList) {
        
    	//First to remove those q ids that are already in the responses list. 
    	//This happens when save errors occur more than once.
		for (Response response : responses) {
			String id = Integer.toString(response.getQuestion().getId());
			if (qidList.contains(id)) {
        		qidList.remove(id);
			}        
		}		
		
		if (qidList.isEmpty()) {
            return responses;
        }
				
    	List<Response> returnList = new ArrayList<Response>();
    	Set<Integer> addQIdList = new HashSet<Integer>();
    	
		for (Response response : responses) {
			returnList.add(response);
			addQIdList.add(response.getQuestion().getId());
			
			List<Response> childList = response.getVersionResponses();
			if (childList.isEmpty()) {
				continue;
				
			} else {
				for (String qid : qidList) {
					for (Response r : childList) {
						
						if (r.getQuestion().getId() == Integer.parseInt(qid) &&
							!(addQIdList.contains(r.getQuestion().getId()))) {
							r.setIsFlag(true);
							returnList.add(r);
						}
					}
				}
			}
		}
        
		return returnList;
	}
    
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}


	/**
	 * @return the closePopUp
	 */
	public boolean isClosePopUp() {
		return closePopUp;
	}


	/**
	 * @param closePopUp the closePopUp to set
	 */
	public void setClosePopUp(boolean closePopUp) {
		this.closePopUp = closePopUp;
	}
	
}
