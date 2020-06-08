package gov.nih.nichd.ctdb.protocol.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CasProxyTicketException;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.security.domain.Privilege;
import gov.nih.nichd.ctdb.security.domain.User;

public class ConfigureEformsAction extends BaseAction {
	private static final Logger logger = Logger.getLogger(ConfigureEformsAction.class);
	private static final long serialVersionUID = -1208803885883409753L;
	
	private int eformId;
	private String hiddenSectionsQuestionsPVsIdsJSON;
	private String jsonString;
	private String action = "";
	private String hasEditPriv;
	private String calcRuleQuestionsJSON;
	private String calcRuleDependentQuestionsJSON;
	private String skipRuleQuestionsJSON;
	private String skipRuleDependentQuestionsJSON;
	
	/**
	 * execute method
	 */
	public String execute() {
		if(action.equals("saveHiddenElements")) {
			saveHiddenElements();
			return StrutsConstants.SUCCESS;
		}else {
			buildLeftNav(LeftNavController.LEFTNAV_EFORMS_CONFIGURE);
			FormDataStructureUtility fsUtil = new FormDataStructureUtility();
			FormManager formMgr = new FormManager();
			ProtocolManager protoMgr = new ProtocolManager();
			try {
				String shortName = formMgr.getEFormShortNameByEFormId(eformId);
				Form form = fsUtil.getEformFromBrics(request, shortName);
				Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
				ArrayList<String> psrHiddenElements = protoMgr.getPSRHiddenElements(p.getId(), eformId);
				JSONArray hiddenSectionsQuestionsPVsIdsJSONArray = new JSONArray(psrHiddenElements);
				hiddenSectionsQuestionsPVsIdsJSON = hiddenSectionsQuestionsPVsIdsJSONArray.toString();
				List<Section> orderedSectionList = form.getOrderedSectionList();
				Set<String> calcRuleQuestions = form.getCalcRuleQuestions();
				JSONArray calcRuleQuestionsJSONArray = new JSONArray(calcRuleQuestions);
				calcRuleQuestionsJSON = calcRuleQuestionsJSONArray.toString();
				
				Set<String> calcRuleDependentQuestions = form.getCalcRuleDependentQuestions();
				JSONArray calcRuleDependentQuestionsJSONArray = new JSONArray(calcRuleDependentQuestions);
				calcRuleDependentQuestionsJSON = calcRuleDependentQuestionsJSONArray.toString();
				
				
				Set<String> skipRuleQuestions = form.getSkipRuleQuestions();
				JSONArray skipRuleQuestionsJSONArray = new JSONArray(skipRuleQuestions);
				skipRuleQuestionsJSON = skipRuleQuestionsJSONArray.toString();
				
				Set<String> skipRuleDependentQuestions = form.getSkipRuleDependentQuestions();
				JSONArray skipRuleDependentQuestionsJSONArray = new JSONArray(skipRuleDependentQuestions);
				skipRuleDependentQuestionsJSON = skipRuleDependentQuestionsJSONArray.toString();
				
				

				//for promis forms, only allow main and form administration sections to be configurable
				if(form.isCAT()) {
					Iterator<Section> orderedSectionListIterator = orderedSectionList.iterator();
					while(orderedSectionListIterator.hasNext()) {
						Section s = orderedSectionListIterator.next();
						String sectionName = s.getName();
						if(!sectionName.equalsIgnoreCase(CtdbConstants.PROMIS_MAIN_SECTION) && !sectionName.equalsIgnoreCase(CtdbConstants.PROMIS_FA_SECTION)) {
							orderedSectionListIterator.remove();
						}	
					}
				}else {
					//remove child repeatables
					Iterator<Section> orderedSectionListIterator = orderedSectionList.iterator();
					while(orderedSectionListIterator.hasNext()) {
						Section s = orderedSectionListIterator.next();
						if(s.isRepeatable() && s.getRepeatedSectionParent() != -1) {
							orderedSectionListIterator.remove();
						}
					}
				}

				request.setAttribute(CtdbConstants.EFORM_CALC_QUESTIONS_LIST, calcRuleQuestions);
				request.setAttribute(CtdbConstants.EFORM_CALC_DEPENDENT_QUESTIONS_LIST, calcRuleDependentQuestions);
				request.setAttribute(CtdbConstants.EFORM_SKIP_QUESTIONS_LIST, skipRuleQuestions);
				request.setAttribute(CtdbConstants.EFORM_SKIP_DEPENDENT_QUESTIONS_LIST, skipRuleDependentQuestions);
				request.setAttribute(CtdbConstants.EFORM_SECTIONLIST, orderedSectionList);
				request.setAttribute(CtdbConstants.EFORM_NAME, form.getShortName());
				User user = getUser();
				Privilege privilegeToCheck = new Privilege();
		        privilegeToCheck.setCode("editConfigureEform");
		        if(user.isSysAdmin()) {
		        	hasEditPriv = "true";
		        } else {
		        	hasEditPriv = String.valueOf(user.hasPrivilege(privilegeToCheck));
		        }
				return StrutsConstants.SUCCESS;
			} catch (ObjectNotFoundException e) {
				logger.error("Error in retrieving form", e);
				addActionError("Error retrieving eform");
				return StrutsConstants.SUCCESS;
				
			} catch (CtdbException e) {
				logger.error("Error in retrieving eform", e);
				addActionError("Error retrieving eform");
				return StrutsConstants.SUCCESS;
			} catch (WebApplicationException e) {
				logger.error("Error in retrieving eform", e);
				addActionError("Error retrieving eform");
				return StrutsConstants.SUCCESS;
			} catch (CasProxyTicketException e) {
				logger.error("Error in retrieving eform", e);
				addActionError("Error retrieving eform");
				return StrutsConstants.SUCCESS;
			}
		}
		
		
		

	}
	
	
	/**
	 * save hidden elements
	 */
	private void saveHiddenElements() {
		ArrayList<String> hiddenElementsArray = new ArrayList<String>();
		setJsonString("");
		User user = getUser();
		
		try {
			JSONArray hiddenElementsJSONArray = new JSONArray(getHiddenSectionsQuestionsPVsIdsJSON());
			for (int i = 0; i < hiddenElementsJSONArray.length(); i++) {
				hiddenElementsArray.add(hiddenElementsJSONArray.getString(i));
		    }
			ProtocolManager protoMgr = new ProtocolManager();
			Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);

			protoMgr.updatePSRHiddenElements(p.getId(),getEformId(),hiddenElementsArray,user);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CtdbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

	}
		
	public String getHiddenSectionsQuestionsPVsIdsJSON() {
		return hiddenSectionsQuestionsPVsIdsJSON;
	}

	public void setHiddenSectionsQuestionsPVsIdsJSON(String hiddenSectionsQuestionsPVsIdsJSON) {
		this.hiddenSectionsQuestionsPVsIdsJSON = hiddenSectionsQuestionsPVsIdsJSON;
	}

	public int getEformId() {
		return eformId;
	}


	public void setEformId(int eformId) {
		this.eformId = eformId;
	}



	public String getJsonString() {
		return jsonString;
	}



	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}



	public String getAction() {
		return action;
	}



	public void setAction(String action) {
		this.action = action;
	}



	public String getHasEditPriv() {
		return hasEditPriv;
	}



	public void setHasEditPriv(String hasEditPriv) {
		this.hasEditPriv = hasEditPriv;
	}


	public String getCalcRuleQuestionsJSON() {
		return calcRuleQuestionsJSON;
	}


	public void setCalcRuleQuestionsJSON(String calcRuleQuestionsJSON) {
		this.calcRuleQuestionsJSON = calcRuleQuestionsJSON;
	}


	public String getCalcRuleDependentQuestionsJSON() {
		return calcRuleDependentQuestionsJSON;
	}


	public void setCalcRuleDependentQuestionsJSON(String calcRuleDependentQuestionsJSON) {
		this.calcRuleDependentQuestionsJSON = calcRuleDependentQuestionsJSON;
	}


	public String getSkipRuleQuestionsJSON() {
		return skipRuleQuestionsJSON;
	}


	public void setSkipRuleQuestionsJSON(String skipRuleQuestionsJSON) {
		this.skipRuleQuestionsJSON = skipRuleQuestionsJSON;
	}


	public String getSkipRuleDependentQuestionsJSON() {
		return skipRuleDependentQuestionsJSON;
	}


	public void setSkipRuleDependentQuestionsJSON(String skipRuleDependentQuestionsJSON) {
		this.skipRuleDependentQuestionsJSON = skipRuleDependentQuestionsJSON;
	}


	

}
