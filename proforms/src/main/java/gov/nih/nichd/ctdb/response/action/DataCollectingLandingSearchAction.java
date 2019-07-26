package gov.nih.nichd.ctdb.response.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.form.DataCollectionLandingForm;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.response.tag.DataCollectionSearchIdtDecorator;
import gov.nih.nichd.ctdb.response.util.DataCollectionUtils;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class DataCollectingLandingSearchAction extends BaseAction {
	
	private static final long serialVersionUID = 6467972513287534241L;
	
	private DataCollectionLandingForm dataForm = new DataCollectionLandingForm();
    private String searchSubmitted = "NO";
	private String formName;

	public String getFormName() {
		return formName;
	}


	public void setFormName(String formName) {
		this.formName = formName;
	}


	public String execute() throws Exception {
		
        buildLeftNav(LeftNavController.LEFTNAV_COLLECT_COLLECT);
				
		final Logger logger = Logger.getLogger(DataCollectingLandingSearchAction.class.getName());
		
		
		try{
		logger.info(DataCollectionUtils.getUserIdSessionIdString(request)+"DataCollectingLandingSearchAction----------------->");
		
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		if (p == null) {
			return StrutsConstants.SELECTPROTOCOL;
		}

		ResponseManager rm = new ResponseManager();
		Map<String, String> searchOptions = new HashMap<String, String>();
		
		
		//remove variables from session when coming to this action 
		if (session.get("currentVisitDate") != null) {
			session.remove("currentVisitDate");
		}
		
		this.retrieveActionMessages(DataCollectionAction.ACTION_MESSAGES_KEY);
		this.retrieveActionErrors(DataCollectionAction.ACTION_ERRORS_KEY);
		int protocolId = p.getId();
		
		
	
		List<DataCollectionLandingForm> dataFormList = rm.getCollectDataSearchResult(dataForm ,protocolId,searchOptions);

	
		
	
		List pvDataList = rm.getPatientViewDataForLandingPage(dataForm, protocolId, searchOptions);
		
		String searchFormType = dataForm.getSearchFormType();
		
		if (searchFormType.equals("patientFormsData")) {
			if (!StringUtils.isEmpty(dataForm.getPatientFormViewStatus())) {
				searchOptions.put(CtdbConstants.DATA_COLLECTION_FORM_STATUS, dataForm.getPatientFormViewStatus());
			}

			if (!StringUtils.isEmpty(dataForm.getFormName())) {
				searchOptions.put(CtdbConstants.DATA_COLLECTION_EFORMS_NAME, dataForm.getFormName());
			}

			if (!StringUtils.isEmpty(dataForm.getFormLastUpdatedDate())) {
				searchOptions.put(CtdbConstants.DATA_COLLECTION_FORM_LAST_UPDATED, dataForm.getFormLastUpdatedDate());
			}

			dataFormList = rm.getCollectDataSearchResult(dataForm, protocolId,searchOptions);

		}  
		
		if (searchFormType.equals("patientData")) {
			if (dataForm.getPvVisitDate() != null) {
				
			}
			if (dataForm.getPatientLastName() != null) {
				
			}
			if (dataForm.getPatientFirstName() != null) {
				
			}
			if (dataForm.getSubjectId() != null && !dataForm.getSubjectId().trim().equals("0")) {
			
			}
			if (dataForm.getMrn()!= null && !dataForm.getMrn().trim().equals("0")) {
			
			}
			if (dataForm.getPvGuid()!= null && !dataForm.getPvGuid().trim().equals("0")) {
			
			}
			
			if (!StringUtils.isEmpty(dataForm.getPvVisitDate())) {
				searchOptions.put(CtdbConstants.DATA_COLLECTION_VISIT_DATE, dataForm.getPvVisitDate());
			}
			if (!StringUtils.isEmpty(dataForm.getPvGuid())) {
				searchOptions.put(CtdbConstants.DATA_COLLECTION_SUBJECT_GUID, dataForm.getPvGuid());
			}
			pvDataList = rm.getPatientViewDataForLandingPage(dataForm, protocolId, searchOptions);

		}

		List<CtdbLookup> formStatus = (List<CtdbLookup>) session.get(FormConstants.FORMSEARCHSTATUS);

		if (formStatus == null) {
			formStatus = new ArrayList<CtdbLookup>();
			formStatus.add(0, new CtdbLookup(0, "All"));
			formStatus.add(1, new CtdbLookup(1, "Active"));
			formStatus.add(2, new CtdbLookup(2, "In Progress"));
			session.put(FormConstants.FORMSEARCHSTATUS, formStatus);
		}
		
		session.put("DataFormList", dataFormList);
		session.put("PVDataList", pvDataList);
		
		//request.setAttribute("aForms", npForms);
		request.setAttribute("searchFormType", dataForm.getSearchFormType());
		}catch(CtdbException e){
			logger.log(Level.SEVERE, DataCollectionUtils.getUserIdSessionIdString(request)+"something went wrong in this action DataCollectionLandingSearchAction", e);
			addActionError("Something went wrong in data collection process please retry your collection again.");
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			return StrutsConstants.DATA_COLLECTION;
		}catch(RuntimeException e){
			logger.log(Level.SEVERE, DataCollectionUtils.getUserIdSessionIdString(request)+"something went wrong in this action DataCollectionLandingSearchAction", e);
			addActionError("Something went wrong in data collection process please retry your collection again.");
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			return StrutsConstants.DATA_COLLECTION;
		}
		catch(Exception e){
			logger.log(Level.SEVERE, DataCollectionUtils.getUserIdSessionIdString(request)+"something went wrong in this action DataCollectionLandingSearchAction", e);
			addActionError("Something went wrong in data collection process please retry your collection again.");
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			return StrutsConstants.DATA_COLLECTION;
		}
		
		return SUCCESS;
	}

	
	// http://fitbir-portal-local.cit.nih.gov:8080/proforms/response/dataCollectingLandingSearch!getDataCollectionPVList.action
	public String getDataCollectionPVList() {
		
		try {
			List pvDataList = (List) session.get("PVDataList");
			IdtInterface idt = new Struts2IdtInterface();
			idt.setList((ArrayList)pvDataList);
			idt.setTotalRecordCount(pvDataList.size());
			idt.setFilteredRecordCount(pvDataList.size());
			idt.decorate(new DataCollectionSearchIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			//logger.log("Failed get PV list.", e);
			e.printStackTrace();
			return StrutsConstants.FAILURE;
		}
		return null;
	}
	
	
	// http://fitbir-portal-local.cit.nih.gov:8080/proforms/response/dataCollectingLandingSearch!getDataCollectionDataFormList.action
		public String getDataCollectionDataFormList() {
			try {
				List<DataCollectionLandingForm> dataFormList = (List<DataCollectionLandingForm>)session.get("DataFormList");
				IdtInterface idt = new Struts2IdtInterface();
				idt.setList((ArrayList)dataFormList);
				idt.setTotalRecordCount(dataFormList.size());
				idt.setFilteredRecordCount(dataFormList.size());
				idt.decorate(new DataCollectionSearchIdtDecorator());
				idt.output();
			} catch (InvalidColumnException e) {
				//logger.error("Failed get data form list.", e);
				e.printStackTrace();
				return StrutsConstants.FAILURE;
			}
			return null;
		}
	public DataCollectionLandingForm getDataForm() {
		return dataForm;
	}

	public void setDataForm(DataCollectionLandingForm dataForm) {
		this.dataForm = dataForm;
	}


	public String getSearchSubmitted() {
		return searchSubmitted;
	}


	public void setSearchSubmitted(String searchSubmitted) {
		this.searchSubmitted = searchSubmitted;
	}


}
