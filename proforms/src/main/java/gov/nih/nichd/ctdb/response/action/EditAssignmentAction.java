package gov.nih.nichd.ctdb.response.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.EditAssignment;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.security.domain.User;

/**
 * EditAssignmentAction is the Struts action class for Re_Assign Data Entry
 * user. This class works with the <code>display</code> tag library.
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class EditAssignmentAction extends BaseAction {

	private static final long serialVersionUID = 783079288573329848L;
	
	/**
	 * Struts Constant used to set/get ActionErrors for this action from
	 * session. Used for the redirect to the main listing page for this
	 * functionality.
	 */
	public static final String ACTION_MESSAGES_KEY = "EditAssignmentAction_ActionMessages";
	public static final String ACTION_ERRORS_KEY = "EditAssignmentAction_ActionErrors";

	private String reassignableAformsJSON;
	private int dataEntryNumber;
	private int userId = Integer.MIN_VALUE;
	private int user2Id = Integer.MIN_VALUE;

	public String execute() throws Exception {
		buildLeftNav(LeftNavController.LEFTNAV_COLLECT_COLLECTIONS);

		ResponseManager rm = new ResponseManager();
		FormManager fm = new FormManager();
		
		Protocol currProtocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);

		// if it is true, then all double data aforms wanting to be reassigned have entry 2 started...
		// this will be false if there is at least 1 single entry in collection or if at least 
		// one double data collection has not entry 2 started
		//boolean entry2Started = true; 
		

		int singleDoubleEntry = CtdbConstants.SINGLE_ENTRY_FORM;   
		
		try {
			Map<String, String> userMap = rm.getProtocolUsers(currProtocol.getId());
			Map<String, String> user2Map = new LinkedHashMap<String, String>();
				
			user2Map.putAll(userMap);

			//this is the list of collections that have been selected for reassignment
			String aformidJSONString = getReassignableAformsJSON();
			JSONArray aformidJSONArr = new JSONArray(aformidJSONString);
			
			List<EditAssignment> editAssignmentList = new ArrayList<EditAssignment>();
			List<EditAssignment> editAssignment2List = new ArrayList<EditAssignment>();
			List<AdministeredForm> aformList = new ArrayList<AdministeredForm>();
				
			for (int k=0; k<aformidJSONArr.length(); k++) {
				String aformidString = aformidJSONArr.getString(k);

				String[] splits = aformidString.split("::");
				int aformId = Integer.parseInt(splits[0]);
				String subj = splits[1];
				String vDate = splits[2];
				String interv = splits[3];
				String fName = splits[4];
				String u1 = splits[5];
				String s1 = splits[6];
				String u2 = splits[7];
				String s2 = splits[8];
					
				int formId = rm.getFormId(aformId);
				int currProtocolId = currProtocol.getId();
				String formName = fm.getFormNameForEFormId(currProtocolId, formId);
					
				EditAssignment editAssignment = rm.getEditAssignment(aformId, 1);
				editAssignmentList.add(editAssignment);
	

				AdministeredForm aform = new AdministeredForm();
				Patient p = new Patient();
				p.setLabel(subj);
				aform.setPatient(p);
					
				Interval interval = new Interval();
				interval.setName(interv);
				aform.setInterval(interval);
				aform.setpVisitDate(vDate);
					
				Form f = new Form();
				f.setId(formId);
				f.setName(formName);
				aform.setForm(f);
				aform.setUser1LastNameFirstNameDisplay(u1);
				aform.setEntryOneStatus(s1);
				aform.setUser2LastNameFirstNameDisplay(u2);
				aform.setEntryTwoStatus(s2);
				aformList.add(aform);
					
				//filter the user lists to avoid potential duplicates
				Iterator<Entry<String, String>> u1Iter = userMap.entrySet().iterator();
				while (u1Iter.hasNext()) {
					Entry<String, String> entry = u1Iter.next();
					if (u2.equals(entry.getValue())) {
						u1Iter.remove();
						break;
					}
				}
					
				Iterator<Entry<String, String>> u2Iter = user2Map.entrySet().iterator();
				while (u2Iter.hasNext()) {
					Entry<String, String> entry = u2Iter.next();
					if (u1.equals(entry.getValue())) {
						u2Iter.remove();
						break;
					}
				}
			}
				
			request.setAttribute("eaAformList", aformList); ///change!!!
			session.put("eaUserMap", userMap);
			session.put("eaUser2Map", user2Map);
			session.put("editAssignmentList", editAssignmentList);
			session.put("editAssignment2List", editAssignment2List);
				
			request.setAttribute("entry2Started", String.valueOf(false));
			request.setAttribute("singleDoubleEntry", String.valueOf(singleDoubleEntry));

		} catch (DuplicateObjectException doe) {
			addActionError(getText(StrutsConstants.ERROR_DUPLICATE_DATAENTRY_USER));
			return StrutsConstants.FAILURE;
		} catch (CtdbException ce) {
			return StrutsConstants.FAILURE;
		}
		return SUCCESS;
	}

	
	public String saveAssignment() throws Exception {
		
		User user = getUser();

		int dataEntryNewUser;
		Map<String, String> eaUserMap; 
		List<EditAssignment> editAssignmentList;
		
		if (dataEntryNumber == 1) {
			dataEntryNewUser = getUserId();
			eaUserMap = (Map<String, String>) session.get("eaUserMap");
			editAssignmentList = (List<EditAssignment>) session.get("editAssignmentList");
		} else {
			dataEntryNewUser = getUser2Id();
			eaUserMap = (Map<String, String>) session.get("eaUser2Map");
			editAssignmentList = (List<EditAssignment>) session.get("editAssignment2List");
		}
			
		String strNewUserName = "";
		if (eaUserMap.containsKey(String.valueOf(dataEntryNewUser))) {
			strNewUserName = eaUserMap.get(String.valueOf(dataEntryNewUser));
		}
		
		try {
			ResponseManager rm = new ResponseManager();
			for (EditAssignment ea : editAssignmentList) {
				ea.setPreviousBy(ea.getCurrentBy());
				ea.setCurrentBy(dataEntryNewUser);
				ea.setAssignedBy(user.getId());
				ea.setAssignedByName(user.getUsername());
				ea.setDataEntryFlag(dataEntryNumber);

				rm.saveDataEntryReAssign(ea);
				addActionMessage(getText(StrutsConstants.SUCCESS_REASSIGN_KEY, 
						new String[]{strNewUserName + ""}));
			}

			session.put(ACTION_MESSAGES_KEY, getActionMessages());
			session.remove("editAssignmentList");
			session.remove("editAssignment2List");
			session.remove("eaUserMap");
			session.remove("eaUser2Map");

		} catch (DuplicateObjectException doe) {
			addActionError(getText(StrutsConstants.ERROR_DUPLICATE_DATAENTRY_USER));
			return StrutsConstants.FAILURE;
		} catch (CtdbException ce) {
			return StrutsConstants.FAILURE;
		}

		return SUCCESS;
	}
	
	public String getReassignableAformsJSON() {
		return reassignableAformsJSON;
	}

	public void setReassignableAformsJSON(String reassignableAformsJSON) {
		this.reassignableAformsJSON = reassignableAformsJSON;
	}

	public int getDataEntryNumber() {
		return dataEntryNumber;
	}

	public void setDataEntryNumber(int dataEntryNumber) {
		this.dataEntryNumber = dataEntryNumber;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getUser2Id() {
		return user2Id;
	}

	public void setUser2Id(int user2Id) {
		this.user2Id = user2Id;
	}

}
