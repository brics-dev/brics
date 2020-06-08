package gov.nih.nichd.ctdb.response.action;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.PaginationData;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.response.common.ResponseConstants;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.response.tag.DataEntrySummaryIdtDecorator;
import gov.nih.nichd.ctdb.response.util.DataCollectionUtils;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.idt.ws.IdtColumnDescriptor;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.IdtRequest;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

/**
 * The Struts Action class responsable for for displaying the forms that are in
 * the progress of data entry by one user.
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class MyCollectionsAction extends BaseAction {

	private static final long serialVersionUID = 8972653714077265284L;
	private static final Logger logger = Logger.getLogger(MyCollectionsAction.class);
	
	public static final String ACTION_MESSAGES_KEY = "DataEntryInProgressAction_ActionMessages";
	//Subject identifier
	private String subjectId;
	private String guid;
	private String mrn;

	// patient fields
	private String formName;
	private String intervalName;
	private String visitDatePreColl;
	private String assignedToUserOne;
	private String assignedToUserTwo;
	private String pVisitDate;

	// non patient fields
	private String npFormName;
	private String npVisitDate;
	private String dataEntryStatus;


	// search type
	private String searchFormType = "patientPVDiv";
	private String selectedFormId;
	private int isStatus;
	private CtdbLookup status;
	private CtdbLookup xformtype;

	private String reassignableAformsJSON;
	
    private String searchSubmitted = "NO";
    
    private String token;
	private String key;
	private String sort;
	private Boolean ascending;
	
    private List<Integer> siteIds = new ArrayList<Integer>();

	public String execute() throws Exception {
		try{
			
			buildLeftNav(LeftNavController.LEFTNAV_COLLECT_COLLECTIONS);
			
			User user = getUser();
			Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			if (p == null) {
				return StrutsConstants.SELECTPROTOCOL;
			}
			int protocolId = p.getId();				
			session.remove("editUser");
			session.remove("editAssignmentList");
			session.remove("editAssignment2 List");
			session.remove("eaUserMap");
			session.remove("eaUser2Map");
			session.remove("selectedId");
			
			this.retrieveActionMessages(MyCollectionsAction.ACTION_MESSAGES_KEY);
			this.retrieveActionMessages(EditAssignmentAction.ACTION_MESSAGES_KEY);
			this.retrieveActionMessages(DataCollectionAction.ACTION_MESSAGES_KEY);
			this.retrieveActionErrors(DataCollectionAction.ACTION_ERRORS_KEY);
			
			try {
				ResponseManager rm = new ResponseManager();
				ProtocolManager pm = new ProtocolManager();
				List<AdministeredForm> aformList = new ArrayList<AdministeredForm>();
				Map<String, String> searchOptions = new HashMap<String, String>();
				int userId = user.getId();
								
				String selectedId = request.getParameter("bsFormId");
				if(selectedId != null) {
					session.put("selectedId", selectedId);
				}
				
				// put subject id in searchOptioins from session to filter based on subject
				if(session.get(StrutsConstants.SUBJECT_COLLECTING_DATA)!=null) {
					searchOptions.put(StrutsConstants.SUBJECT_COLLECTING_DATA,
						String.valueOf(session.get(StrutsConstants.SUBJECT_COLLECTING_DATA)));
				}

				if (!Utils.isBlank(getDataEntryStatus())) {
					searchOptions.put(CtdbConstants.SEARCH_STATUS_MyCollection, this.getDataEntryStatus());
				}
				if (!Utils.isBlank(getGuid())) {
					searchOptions.put(CtdbConstants.SUBJECT_GUID_DISPLAY, this.getGuid());
				}
				if (!Utils.isBlank(getMrn())) {
					searchOptions.put(CtdbConstants.SUBJECT_MRN_DISPLAY, this.getMrn());
				}
				if (!Utils.isBlank(getSubjectId())) {
					searchOptions.put(CtdbConstants.PATIENTID_DISPLAY, this.getSubjectId());
				}
				  				
				if(!isUserSiteCheckNeeded()) {
					aformList = rm.myCollectionsListBySiteIds(protocolId, userId, searchOptions,null, null,null);
				}else {
					siteIds = getUserAssignedSites();
					aformList = rm.myCollectionsListBySiteIds(protocolId, userId, searchOptions,null, null,siteIds);
				}
				
				session.put(ResponseConstants.LIST_ADMINISTEREDFORM, aformList);
				session.put(CtdbConstants.VISIT_TYPE_OPTIONS, DataCollectionUtils.getVisitTypeMap(pm.getIntervals(p.getId())));
	
			}
			catch (CtdbException ce) {
				return StrutsConstants.FAILURE;
			}
		} 
		catch(RuntimeException e) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request)+"something went wrong in this action DataEntryInProgressAction", e);
			addActionError("Something went wrong in data collection process please retry your collection again.");
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			return StrutsConstants.DATA_COLLECTION;
		}
		catch(Exception e) {
			logger.error(DataCollectionUtils.getUserIdSessionIdString(request)+"something went wrong in this action DataEntryInProgressAction", e);
			addActionError("Something went wrong in data collection process please retry your collection again.");
			session.put(DataCollectionAction.ACTION_ERRORS_KEY, getActionErrors());
			return StrutsConstants.DATA_COLLECTION;
		}
		//remove session variable to track subject collecting data
		session.remove(StrutsConstants.SUBJECT_COLLECTING_DATA);
		return SUCCESS;
	}
	
	  public String getMyCollection() throws Exception {
		  
		    IdtInterface idt;
		    try {
		    	idt = new Struts2IdtInterface();
		    	IdtRequest idtRequest = idt.getRequest();
				User user = getUser();
				Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
				int subjectDisplayType = p.getPatientDisplayType();
				if (p == null) {
					return StrutsConstants.SELECTPROTOCOL;
				}
				int protocolId = p.getId();				    	
				ResponseManager rm = new ResponseManager();
				ProtocolManager pm = new ProtocolManager();
				List<AdministeredForm> aformList = new ArrayList<AdministeredForm>();
				Map<String, String> searchOptions = new HashMap<String, String>();
				int userId = user.getId();
				// put subject id in searchOptioins from session to filter based on subject
				if(session.get(StrutsConstants.SUBJECT_COLLECTING_DATA)!=null) {
					searchOptions.put(StrutsConstants.SUBJECT_COLLECTING_DATA,
						String.valueOf(session.get(StrutsConstants.SUBJECT_COLLECTING_DATA)));
				}

				if (!Utils.isBlank(getDataEntryStatus())) {
					searchOptions.put(CtdbConstants.SEARCH_STATUS_MyCollection, this.getDataEntryStatus());
				}
				String selectedId = (String) session.get("selectedId");
 				if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_GUID) {
					if (selectedId != null && !Utils.isBlank(selectedId)) {
						this.setGuid(selectedId);
					}
					if (!Utils.isBlank(getGuid())) {
						searchOptions.put(CtdbConstants.SUBJECT_GUID_DISPLAY, this.getGuid());
					}
				}
				if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_MRN) {
					if (selectedId != null && !Utils.isBlank(selectedId)) {
						this.setMrn(selectedId);
					}
					if (!Utils.isBlank(getMrn())) {
						searchOptions.put(CtdbConstants.SUBJECT_MRN_DISPLAY, this.getMrn());
					}
				}
				if (subjectDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {
					if (selectedId != null && !Utils.isBlank(selectedId)) {
						this.setSubjectId(selectedId);
					}
					if (!Utils.isBlank(getSubjectId())) {
						searchOptions.put(CtdbConstants.PATIENTID_DISPLAY, this.getSubjectId());
					}
				}
				if (!Utils.isBlank(getVisitDatePreColl())) {
					searchOptions.put(CtdbConstants.SUBJECT_VISIT_DATE, this.getVisitDatePreColl());
				}
				if (!Utils.isBlank(getIntervalName())) {
					searchOptions.put(CtdbConstants.SUBJECT_INTERVAL_NAME, this.getIntervalName());
				}
				if (!Utils.isBlank(getFormName())) {
					searchOptions.put(CtdbConstants.DATA_COLLECTION_EFORMS_NAME, this.getFormName());
				}
				
				
				updateAccessRecordOrder(idtRequest.getOrderColumn());
				updateAccessRecordSearch(idtRequest);
					
				PaginationData pageData = new PaginationData();
				pageData.setPage((idtRequest.getStart() / idtRequest.getLength()) + 1);
				pageData.setPageSize(idtRequest.getLength());
				pageData.setAscending(ascending);
				pageData.setSort(sort);				
				
				if(!isUserSiteCheckNeeded()) {
					aformList = rm.myCollectionsListBySiteIds(protocolId, userId, searchOptions, pageData, key,null); 
				}else {
					siteIds = getUserAssignedSites();
					aformList = rm.myCollectionsListBySiteIds(protocolId, userId, searchOptions, pageData, key,siteIds);
				}
				
				/*getting the search value from data table search box and the values of the selected columns 
				 * from idt search drop checkboxes. And filtering data from the data table search input and selected columns.
				 */
				List<IdtColumnDescriptor> columns = new ArrayList<IdtColumnDescriptor>(idtRequest.getColumnDescriptions());
				// pre-filter out columns we don't want to search
				Iterator<IdtColumnDescriptor> columnsIterator = columns.iterator();
				while (columnsIterator.hasNext()) {
					IdtColumnDescriptor column = columnsIterator.next();
					if (!column.isSearchable()) {
						columnsIterator.remove();
					}
				}
			
				List<AdministeredForm> displayAFormList = new ArrayList<AdministeredForm>();
				// CRIT-11445: using eform title from dictionary in form name
				List<String> shortNameList = new ArrayList<String>();
				for (AdministeredForm af : aformList) {
					Form form = af.getForm();
					shortNameList.add(form.getShortName());
				}
				FormDataStructureUtility fsUtil = new FormDataStructureUtility();
				List<BasicEform> basicEforms = fsUtil.getBasicEforms(request, shortNameList);
				String searchVal = idtRequest.getSearchVal();
				
				if (columns.size() != 0 && !searchVal.isEmpty()) {
					for(AdministeredForm af : aformList) {
						boolean isInList = false;
						for(IdtColumnDescriptor column : columns){
							String checkedColumn = column.getName();
							switch (checkedColumn) {
							case "guid":
								if (af.getPatient().getGuid().toLowerCase().contains(searchVal)) isInList = true;
								break;
							case "subjectid":
								if (af.getPatient().getSubjectId().toLowerCase().contains(searchVal)) isInList = true;
								break;
							case "mrn":
								if (af.getPatient().getMrn().toLowerCase().contains(searchVal)) isInList = true;
								break;
							case "pVisitDate":
								if (af.getpVisitDate().toLowerCase().contains(searchVal)) isInList = true;
								break;
							case "intervalname":
								if (af.getInterval().getName().toLowerCase().contains(searchVal)) isInList = true;
								break;
							case "formName":
								String afShortName = af.getForm().getShortName();
								// CRIT-11445: using eform title from dictionary in form name
								for (BasicEform beForm : basicEforms) {
									String efTitle = beForm.getTitle();
									if (beForm.getShortName().equals(afShortName)
											&& efTitle.toLowerCase().contains(searchVal)) {
										af.getForm().setName(efTitle);
										isInList = true;
										break;
									}
								}
								break;
							case "coll_status":
								if (af.getEntryOneStatus().toLowerCase().contains(searchVal)) isInList = true;
								break;
							case "firstname":
								if (af.getUser1LastNameFirstNameDisplay().toLowerCase().contains(searchVal)) isInList = true;
								break;
							case "finallockdate":
								String finalLockDateString = "";							
								Date finalLockDate = af.getFinalLockDate();									
								if(finalLockDate != null) {
									SimpleDateFormat dateFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.datetimeformat"));
									finalLockDateString = dateFormat.format(finalLockDate);
								}
								if (finalLockDateString.toLowerCase().contains(searchVal)) isInList = true;
								break;
							}
							if(isInList && !displayAFormList.contains(af)){
								// CRIT-11445: using eform title from dictionary in form name
								String afShortName = af.getForm().getShortName();
								for (BasicEform beForm : basicEforms) {
									String efTitle = beForm.getTitle();
									if (beForm.getShortName().equals(afShortName)) {
										af.getForm().setName(efTitle);
										break;
									}
								}
								displayAFormList.add(af);
								isInList = false;
							}
						}
					}
				} else if (searchVal.isEmpty()) {
					// CRIT-11445: using eform title from dictionary in form name
					for (AdministeredForm af : aformList) {
						String afShortName = af.getForm().getShortName();
						for (BasicEform beForm : basicEforms) {
							String efTitle = beForm.getTitle();
							if (beForm.getShortName().equals(afShortName)) {
								af.getForm().setName(efTitle);
								break;
							}
						}
					}
					displayAFormList.addAll(aformList);
				}

				
				int countFilteredRecord = rm.countMyCollectionsList(protocolId, userId, searchOptions, key);
				int countTotalRecord = rm.countMyCollectionsList(protocolId, userId, new HashMap<String, String>(), null);

					
			    idt.setTotalRecordCount(countTotalRecord);
			    idt.setFilteredRecordCount(countFilteredRecord);
			    
			    ArrayList<AdministeredForm> outputAr = new ArrayList<AdministeredForm>(displayAFormList);
			    idt.setList(outputAr);
			    idt.decorate(new DataEntrySummaryIdtDecorator());
			    idt.output();

		    } catch (Exception e) {
		      e.printStackTrace();
		    }
		    return null;
		}
	  
		private void updateAccessRecordSearch(IdtRequest request) {
			key = null;
			String searchVal = request.getSearchVal();
			if (searchVal != null && !searchVal.equals("")) {
				key = searchVal;
			}
		}
		
		private void updateAccessRecordOrder(IdtColumnDescriptor orderColumn) {
			
			// these are intentionally NOT mapped 1:1
			sort = null;
			if (orderColumn != null) {
				sort = orderColumn.getData();
				if (sort.equals("id")) {
					sort = "administeredformid";
				}
				
				ascending = orderColumn.getOrderDirection().equals(IdtRequest.ORDER_ASCENDING);
			}
		}
	
    public String deleteDataEntry() throws Exception {
		
		//remove visit date in session scope
		if (session.get("currentVisitDate") != null) {
			session.remove("currentVisitDate");
		}
		
	
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		if (p == null) {
			return StrutsConstants.SELECTPROTOCOL;
		}
		
		User user = getUser();
		ResponseManager rm = new ResponseManager();
		FormManager fman = new FormManager();
		
		int dataEntryFlag = 0;
		String deFlag = request.getParameter("dataentryflag");
							
		String admFormId = request.getParameter(CtdbConstants.ID_REQUEST_ATTR);
		String[] aformIdsArr = admFormId.split(",");
		List<String> formNamesList = new ArrayList<String>();
		
		AdministeredForm admForm = null;
		StringBuilder formNameSB = new StringBuilder();
		
		for (int i = 0; i < aformIdsArr.length; i++) {
			int aformId = Integer.parseInt(aformIdsArr[i]);
			admForm = rm.getAdministeredFormForUnadmin(aformId);
			
			if (!Utils.isBlank(deFlag)) {
				dataEntryFlag = Integer.parseInt(deFlag);
			} else {
				dataEntryFlag = rm.getDataEntryFlag(admForm.getId(), user.getId());
			}
			
			//This will allow only unique name in success message deleted list
			
			//String formName = fman.getFormNameP(p.getId(), aformId);
			String formName = fman.getFormNameForEFormId(p.getId(), admForm.getForm().getId());
			if (!formNamesList.contains(formName)) {
				formNamesList.add(formName);
				formNameSB.append(formName).append(", ");
			}
			
			logger.info(user.getUsername() + " is deleting the adminitered form " + admForm.getId());
			rm.unadministerForm(admForm, dataEntryFlag);
		}
			
		//This one liner removes the last comma from the string list of form names added by yogi
		String formNames = formNameSB.toString().substring(0, formNameSB.length() - 2);
		if (dataEntryFlag != 0) {
			addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY, new String[]{
					"Data Entry for Administered Form(s) "+ formNames}));
		} else {
			addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY, new String[]{
					"Data Entry for Administered Form "+ formName}));
		}
			
		if ("1".equals(request.getParameter("cancelFlag"))) {
			// if you click the cancel button in start mode clear the message 
			this.clearErrorsAndMessages();
			return StrutsConstants.CANCEL;
		} 
			
		session.put("searchFormType","patientPVDiv");
		session.put(ACTION_MESSAGES_KEY, getActionMessages());
		return SUCCESS;
	}
    
    
    
    
    /**
     * Method to delete PSR entry 
     * @return
     * @throws Exception
     */
    
	public String deleteDataEntryPSR() throws Exception {
		try {
			HttpServletResponse response = ServletActionContext.getResponse();
			PrintWriter out = response.getWriter();

			ResponseManager rm = new ResponseManager();
			int dataEntryFlag = 1;
			int admFormId = Integer.parseInt(request.getParameter(CtdbConstants.AFORM_ID_REQUEST_ATTR));

			AdministeredForm admForm = null;

			admForm = rm.getAdministeredFormForUnadmin(admFormId);

			rm.unadministerForm(admForm, dataEntryFlag);

			token = request.getParameter("token");

			out.print("success");

			out.flush();

		} catch (Exception e) {
			this.addActionError("Something went wrong in deleteDataEntryPSR please see stack trace");
			e.printStackTrace();
		}
		return null;
	}
    

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}



	
	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getIntervalName() {
		return intervalName;
	}

	public void setIntervalName(String intervalName) {
		this.intervalName = intervalName;
	}



	public String getVisitDatePreColl() {
		return visitDatePreColl;
	}

	public void setVisitDatePreColl(String visitDatePreColl) {
		this.visitDatePreColl = visitDatePreColl;
	}

	public String getAssignedToUserOne() {
		return assignedToUserOne;
	}

	public void setAssignedToUserOne(String assignedToUserOne) {
		this.assignedToUserOne = assignedToUserOne;
	}

	public String getAssignedToUserTwo() {
		return assignedToUserTwo;
	}

	public void setAssignedToUserTwo(String assignedToUserTwo) {
		this.assignedToUserTwo = assignedToUserTwo;
	}

	public String getNpFormName() {
		return npFormName;
	}

	public void setNpFormName(String npFormName) {
		this.npFormName = npFormName;
	}

	public String getNpVisitDate() {
		return npVisitDate;
	}

	public void setNpVisitDate(String npVisitDate) {
		this.npVisitDate = npVisitDate;
	}

	public String getDataEntryStatus() {
		return dataEntryStatus;
	}

	public void setDataEntryStatus(String dataEntryStatus) {
		this.dataEntryStatus = dataEntryStatus;
	}



	public String getSearchFormType() {
		return searchFormType;
	}

	public void setSearchFormType(String searchFormType) {
		this.searchFormType = searchFormType;
	}

	public String getSelectedFormId() {
		return selectedFormId;
	}

	public void setSelectedFormId(String selectedFormId) {
		this.selectedFormId = selectedFormId;
	}

	public int getIsStatus() {
		return isStatus;
	}

	public void setIsStatus(int isStatus) {
		this.isStatus = isStatus;
	}

	public CtdbLookup getStatus() {
		return status;
	}

	public void setStatus(CtdbLookup status) {
		this.status = status;
	}

	public CtdbLookup getXformtype() {
		return xformtype;
	}

	public void setXformtype(CtdbLookup xformtype) {
		this.xformtype = xformtype;
	}



	public String getReassignableAformsJSON() {
		return reassignableAformsJSON;
	}

	public void setReassignableAformsJSON(String reassignableAformsJSON) {
		this.reassignableAformsJSON = reassignableAformsJSON;
	}
	
	public String getSearchSubmitted() {
		return searchSubmitted;
	}

	public void setSearchSubmitted(String searchSubmitted) {
		this.searchSubmitted = searchSubmitted;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getMrn() {
		return mrn;
	}

	public void setMrn(String mrn) {
		this.mrn = mrn;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}
	
	public Boolean getAscending() {
		return ascending;
	}

	public void setAscending(Boolean ascending) {
		this.ascending = ascending;
	}
}
