package gov.nih.nichd.ctdb.protocol.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.protocol.common.ProtocolConstants;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.protocol.tag.IntervalHomeDecorator;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class IntervalHomeAction extends BaseAction {

	private static final long serialVersionUID = 8901037744500590984L;
	private static final Logger logger = Logger.getLogger(IntervalHomeAction.class);
	private String intervalStrList = CtdbConstants.EMPTY_JSON_ARRAY_STR;

	private String idsToDelete = null;
	
	public static final String INTERVALORDER_MESSAGES_KEY = "IntervalOrder_ActionMessages";
	public static final String INTERVALORDER_ERROR_KEY = "IntervalOrder_ActionErrors";


	public String execute() {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_INTERVAL);
		
        retrieveActionMessages(IntervalAction.INTERVALACTION_MESSAGES_KEY);
        retrieveActionErrors(IntervalAction.INTERVALACTION_ERROR_KEY);
        session.remove(FormConstants.PUBLISHED_EFORM_MAP);
		
		return BaseAction.SUCCESS;
	}


	public String deleteVisitType() {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_INTERVAL);
		
		Locale userLocale = request.getLocale();
		
		try {
			ProtocolManager protoMgr = new ProtocolManager();
			List<Integer> delIdList = new ArrayList<Integer>();
			
			if (!Utils.isBlank(this.getIdsToDelete())) {
				delIdList = Utils.convertStrToIntArray(getIdsToDelete());
			}
			
			// Setup the failed deleted visit type hash map
			Map<String, List<String>> failedDeleted = new HashMap<String, List<String>>();
			failedDeleted.put("database_error", new ArrayList<String>());
			failedDeleted.put("collect_data_association", new ArrayList<String>());
			
			List<String> deletedVisitTypes = new ArrayList<String>(delIdList.size());
			Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			protoMgr.deleteVisitTypes(delIdList, deletedVisitTypes, failedDeleted);
			
			// Create the success message if applicable
			String strList = "";
			if (!deletedVisitTypes.isEmpty()) {
				strList = Utils.convertListToString(deletedVisitTypes);
				
				// Create the success message for the deletion
				if (deletedVisitTypes.size() > 1) {
					addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_MULTI_KEY, 
							new String[]{"The " + strList + " visit types"}));
				} else {
					addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY, new String[]{strList + " visit type"}));
				}
			}
			
			// Create an error message if needed
			List<String> databaseErrors = failedDeleted.get("database_error");
			List<String> assocErrors = failedDeleted.get("collect_data_association");
			
			// Check for any deletion errors
			if (!databaseErrors.isEmpty()) {
				strList = Utils.convertListToString(databaseErrors);
				if (databaseErrors.size() > 1) {
					addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, new String[]{strList + " visit types"}));
				} else {
					addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, new String[]{strList + " visit type"}));
				}
			}
			
			// Check for visit types being associated with a data collection
			if (!assocErrors.isEmpty()) {
				strList = Utils.convertListToString(assocErrors);
				if (assocErrors.size() > 1) {
					addActionError(getText(StrutsConstants.ERROR_VISIT_TYPE_DATA_COLL_DELETE_MULTI, new String[]{strList}));
				} else {
					addActionError(getText(StrutsConstants.ERROR_VISIT_TYPE_DATA_COLL_DELETE, new String[]{strList}));
				}
			}
			

			List<Interval> intervalList = protoMgr.getIntervals(p.getId());
			request.setAttribute(ProtocolConstants.STUDYINTERVAL_LIST, intervalList);
			
		}
		catch (CtdbException ce) {
			logger.error("A database error occurred while deleting the selected visit type(s).", ce);
        	addActionError(getText(StrutsConstants.ERROR_DELETE, 
        			new String[]{getText("interval.list.title.display").toLowerCase(userLocale)}));
            return StrutsConstants.FAILURE;
		} 
		
		return SUCCESS;
	}
	
	/*
	 * Populate the existing Visit Types based on the orderval property.
	 */
	public String orderVisitType() {
		Locale userLocale = request.getLocale();
        retrieveActionMessages(INTERVALORDER_MESSAGES_KEY);
        retrieveActionErrors(INTERVALORDER_ERROR_KEY);

        try {
			buildLeftNav(LeftNavController.LEFTNAV_STUDY_ORDER_INTERVAL);

			ProtocolManager protoMgr = new ProtocolManager();
			Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			List<Interval> intervalList = protoMgr.getIntervalsOrderByOrderval(p.getId());
			request.setAttribute(ProtocolConstants.STUDYINTERVAL_LIST, intervalList);
		}
		catch (CtdbException ce) {
			logger.error("A database error occured while getting a list of visit types.", ce);
        	addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, 
        	new String[]{getText("interval.list.title.display").toLowerCase(userLocale)}));
            return StrutsConstants.FAILURE;
		} 

		return SUCCESS;
	}
	
	/*
	 * Re-order the visit types based on the order selected.
	 */
	public String saveVisitTypeOrder() {
		ProtocolManager protoMan = new ProtocolManager();

		try {
			/*
			 * IntervalStrList contains the interval ids in the order desired by the user.
			 */
			String intervalStrList = this.getIntervalStrList(); 
			if ( !Utils.isBlank(intervalStrList) && !intervalStrList.equals(CtdbConstants.EMPTY_JSON_ARRAY_STR) ) {
				JSONArray input = new JSONArray(intervalStrList);
				String[] intervals = new String[input.length()];
				
				//Convert to a string array.
				for ( int i = 0; i < input.length(); i++ ) {
					intervals[i] = input.getString(i);
				}
				
				//Update Interval order values based on the new order.
				protoMan.updateIntervalOrder(intervals);
				
				addActionMessage("The visit type order has been updated successfully.");
	            session.put(INTERVALORDER_MESSAGES_KEY, this.getActionMessages());
			}
		}
		catch (CtdbException ce) {
			logger.error("Database error occurred while updating visit type order.", ce);
        	addActionError("Database error occurred while updating visit type order.");
		}
		catch (JSONException je) {
			logger.error("Could not construct JSON object.", je);
			addActionError("Could not construct JSON object.");
		}
		
		// Check for any errors
		if ( hasActionErrors() ) {
			session.put(INTERVALORDER_ERROR_KEY, getActionErrors());
			return StrutsConstants.EXCEPTION;
		}

		return SUCCESS;
	}
	
	// url: http://fitbir-portal-local.cit.nih.gov:8082/ibis/getVisitTypeList.action
	public String getVisitTypeList() {
		Locale userLocale = request.getLocale();
		
		ProtocolManager protoMgr = new ProtocolManager();
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		List<Interval> intervalList = new ArrayList<Interval>();
		
		try {
			intervalList = protoMgr.getIntervals(p.getId());
		} catch (CtdbException e) {
			logger.error("A database error occured while getting a list of visit types.", e);
        	addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, 
        	new String[]{getText("interval.list.title.display").toLowerCase(userLocale)}));
		}
		
		try {
			IdtInterface idt = new Struts2IdtInterface();			
			ArrayList<Interval> outputList = new ArrayList<Interval>(intervalList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new IntervalHomeDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
		
	public String getIdsToDelete() {
		return idsToDelete;
	}

	public void setIdsToDelete(String idsToDelete) {
		this.idsToDelete = idsToDelete;
	}
		
	public String getIntervalStrList() {
		return intervalStrList;
	}

	public void setIntervalStrList(String intervalStrList) {
		this.intervalStrList = intervalStrList;
	}
}
