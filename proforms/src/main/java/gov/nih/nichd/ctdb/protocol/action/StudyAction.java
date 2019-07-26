package gov.nih.nichd.ctdb.protocol.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.result.StreamResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.nih.nichd.ctdb.common.AssemblerException;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ErrorMessage;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.drugDevice.domain.DrugDevice;
import gov.nih.nichd.ctdb.drugDevice.manager.DrugDeviceManager;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientRole;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.patient.manager.PatientRoleManager;
import gov.nih.nichd.ctdb.protocol.common.ProtocolAssembler;
import gov.nih.nichd.ctdb.protocol.common.ProtocolConstants;
import gov.nih.nichd.ctdb.protocol.domain.BricsStudy;
import gov.nih.nichd.ctdb.protocol.domain.ClinicalLocation;
import gov.nih.nichd.ctdb.protocol.domain.MilesStone;
import gov.nih.nichd.ctdb.protocol.domain.PointOfContact;
import gov.nih.nichd.ctdb.protocol.domain.Procedure;
import gov.nih.nichd.ctdb.protocol.domain.ProcedureType;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.form.ProtocolForm;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.protocol.tag.BricsStudyIdtDecorator;
import gov.nih.nichd.ctdb.protocol.tag.ProtocolClinicalLocationIdtDecorator;
import gov.nih.nichd.ctdb.protocol.tag.ProtocolMilesStoneIdtDecorator;
import gov.nih.nichd.ctdb.protocol.tag.ProtocolPointOfContactIdtDecorator;
import gov.nih.nichd.ctdb.protocol.tag.ProtocolProcedureIdtDecorator;
import gov.nih.nichd.ctdb.security.domain.Privilege;
import gov.nih.nichd.ctdb.security.domain.Role;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.site.domain.Site;
import gov.nih.nichd.ctdb.site.manager.SiteManager;
import gov.nih.nichd.ctdb.util.common.BricsRepoWsClient;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.nichd.ctdb.util.domain.Address;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class StudyAction extends BaseAction {
	private static final long serialVersionUID = -7125851687510799171L;
	private static final Logger logger = Logger.getLogger(StudyAction.class);
	private static final String SUBJECT_LABEL_CONFIGURATION = "subjectlabelconfiguration";
	
	private ProtocolForm protoForm = new ProtocolForm(); 
	private String studyId;
	private String selectedBricsStudyId;
	private String selectedSiteId;
	private String selectedDrugDeviceId;
	private String enableEsignatureYesno;
	private String reasonForEsignatureYesno;
	private String jsonString = "{}";
	private String editOrigName = "";
	private String protoProceduresStr = CtdbConstants.EMPTY_JSON_ARRAY_STR; 
	private String editOrigJsonStr = "{}";

	public String showStudy() throws JSONException {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_DETAILS);
        retrieveActionMessages(StrutsConstants.SUCCESS_ADD_KEY);
        retrieveActionMessages(StrutsConstants.SUCCESS_EDIT_KEY);
		
		clearStudySession();
		ProtocolManager protoMan = new ProtocolManager();
		Protocol study = null;
		
		try {
			resetUserInfo();
			
			int protocolId = 0;
			
			if (Utils.isBlank(getStudyId())) {
				logger.error("No protocol ID was found in the query parameters.");
				
				return StrutsConstants.FAILURE;
			}
			else {
				protocolId = Integer.parseInt(getStudyId());
			}
			
			study = protoMan.getProtocol(protocolId);
			session.put(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY, study);
			ProtocolAssembler.domainToForm(study, protoForm);
			protoForm.setAllowPii(SysPropUtil.getProperty(CtdbConstants.CAN_USE_PII_KEY).equals("0"));
			
			session.put(CtdbConstants.SITE_HASH_MAP, createSiteMap(study.getStudySites()));
			session.put(CtdbConstants.DRUG_DEVICE_HASH_MAP, createDrugDeviceMap(study.getDrugDeviceList()));
			session.put(CtdbConstants.CURRENT_PROTOCOL_STUDY_SITES_SESSION_KEY, study.getStudySites());
							
			LookupManager lookUp = new LookupManager();
			session.put("xinstitutes", lookUp.getLookups(LookupType.INSTITUTE));
			session.put("xProtocolStatus", lookUp.getLookups(LookupType.PROTOCOL_STATUS));
			session.put("xuserlist", lookUp.getLookups(LookupType.USER_NAME));
			session.put("__SiteHome_states", lookUp.getLookups(LookupType.STATE));
			session.put("__SiteHome_countries", lookUp.getLookups(LookupType.COUNTRY));
			session.put("_availiableDefaults", lookUp.getLookups(LookupType.PROTOCOL_DEFAULTS));
			session.put("_btrisAccess", lookUp.getLookups(LookupType.BTRIS_ACCESS));
			
			if (protoForm.isAddedFromDashboard()) {
				protoForm.setAddedFromDashboard(false);
			}

			User user = getUser(); 

			request.getSession().setAttribute("subjectlabelconfiguration", new Boolean(false));
			
			List<Role> roles = protoMan.getProtocolRoles(user.getId(), protocolId);
			
			for(Role role : roles) {
				List<Privilege> priv = role.getPrivList();
				
				for(Privilege priveleges : priv) {
					if(priveleges.getCode().equalsIgnoreCase(SUBJECT_LABEL_CONFIGURATION)) {
						request.getSession().setAttribute("subjectlabelconfiguration", new Boolean(true));
					}
				}	
			}

			//loading all point of contact list for protocol
			List<ClinicalLocation> sessionClinicLocList = protoMan.getProtocolClinicalLocs(protocolId);
			session.put("protoClinicalLocationList", sessionClinicLocList);
			
			List<Procedure> allProcedureList = protoMan.getAllProcdeureList();
			JSONArray procedureArray = ProtocolAssembler.procedureListTOJSONArray(allProcedureList);
			protoProceduresStr = procedureArray.toString();
			
			//loading all procedure list for protocol
			List<Procedure> sessionProcList = protoMan.getProtocolProcdeure(protocolId);
			session.put("protoProcedureList", sessionProcList);
			
			//loading all point of contact list for protocol
			List<PointOfContact> sessionPOCList = protoMan.getProtocolPointOfContact(protocolId);
			session.put("protoPointOfContactList", sessionPOCList);
			
			List<MilesStone> sessionMilesStoneList = protoMan.getProtocolMilesStone(protocolId);
			session.put("protoMilesStoneList", sessionMilesStoneList);
		}
		catch (AssemblerException ae) {
			logger.error("Couldn't convert the protocol POJO to the protocol form object.", ae);
			addActionError(getText(StrutsConstants.ERROR_DATA_GET,
					new String[] {study.getProtocolNumber()}));
		}
		catch (CtdbException ce) {
			logger.error("Database error occurred while preparing the protocol for display.", ce);
			addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, 
					new String[] {getText("app.label.lcase.protocol").toLowerCase(request.getLocale())}));
		}
		
		JSONArray selectedSiteIdsArray = new JSONArray();
		
		for ( Site s : study.getStudySites() ) {
			selectedSiteIdsArray.put(s.getStudySiteId());
		}
		
		protoForm.setSelectedSiteIds(selectedSiteIdsArray.toString());
		
		return SUCCESS;
	}
	
	// url: http://fitbir-portal-local.cit.nih.gov:8080/proforms/getBricsStudy.action
	public String getBricsStudy() throws IOException {
		ProtocolManager protoMan = new ProtocolManager();
		Protocol proto = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		try {
			@SuppressWarnings("unchecked")
			Hashtable<String, BricsStudy> bStudyTable = (Hashtable<String, BricsStudy>) session.get(ProtocolConstants.BRICS_STUDY_HASHTABLE);
			List<BricsStudy> bricsStudyList = null;
			
			// If the BRICS study hashtable is not in session, then query the need info from the repository.
			if ( bStudyTable != null ) {
				bricsStudyList = new ArrayList<BricsStudy>(bStudyTable.values());
				protoMan.sortBricsStudyList(proto, bricsStudyList);
			}
			else {
				bricsStudyList = getDataRepositoryList(protoMan, proto);
			}
			
			IdtInterface idt = new Struts2IdtInterface();
			//showStudy();
			ArrayList<BricsStudy> outputList = new ArrayList<BricsStudy>(bricsStudyList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new BricsStudyIdtDecorator());
			idt.output();
		}
		catch (InvalidColumnException e) {
			logger.error("invalid column: " + e.getMessage(), e);
			ServletActionContext.getResponse().sendError(500, e.getLocalizedMessage());
		}
		catch (Exception e) {
			logger.error("Error occurred while getting the list of Repository studies.", e);
			ServletActionContext.getResponse().sendError(500, e.getLocalizedMessage());
		}
		
		
		return null;
	}
	
	// url: http://fitbir-portal-local.cit.nih.gov:8080/proforms/createBricsStudy.action
	public String createBricsStudy() throws IOException {
		try {
			ProtocolManager protoMan = new ProtocolManager();
			List<BricsStudy> bricsStudyList = getDataRepositoryList(protoMan, null);
			
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<BricsStudy> outputList = new ArrayList<BricsStudy>(bricsStudyList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new BricsStudyIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e.getMessage(), e);
			ServletActionContext.getResponse().sendError(500, e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Error occurred while getting the list of Repository studies.", e);
			ServletActionContext.getResponse().sendError(500, e.getLocalizedMessage());
		}
		
		return null;
	}
	
	/**
	 * Builds a list of BRICS study objects from queries to the repository web service's RESTfull APIs. The list 
	 * will first be sorted by study title. If the current study is linked to a BRICS study, that list entry will be
	 * moved to the beginning of the list. Finally, any list entries that are linked to other ProFoRMS studies will 
	 * be moved to the end of the list, and will be marked to be disabled in the interface.
	 * 
	 * @param manager - A reference to the Protocol Manager object to be used for list manipulation operations.
	 * @param study - A reference to the current study.
	 * @return	A listing of BRICS study objects to be used in the Data Repository Study display table.
	 * @throws CtdbException There was a database error while looking up the other linked ProFoRMS studies in the database.
	 * @throws ParserConfigurationException	There was an error while parsing the XML returned by the repository web service.
	 * @throws SAXException	There was an error while parsing the XML returned by the repository web service.
	 * @throws IOException	There was an error while parsing the XML returned by the repository web service.
	 */
	private List<BricsStudy> getDataRepositoryList(ProtocolManager manager, Protocol protocol) 
			throws CtdbException, ParserConfigurationException, SAXException, IOException {
		
		BricsRepoWsClient client = new BricsRepoWsClient();
		User user = getUser();
		Hashtable<String, BricsStudy> bricsStudyTable = client.getBricsStudiesForUser(user, request, protocol);
		List<BricsStudy> bricsStudyList = new ArrayList<BricsStudy>(bricsStudyTable.values());
		
		
		//remove any requested brics studies from list
		Iterator iter = bricsStudyList.iterator();
		while(iter.hasNext()) {
			BricsStudy bs = (BricsStudy)iter.next();
			if(bs.getStudyStatus().equalsIgnoreCase(StudyStatus.REQUESTED.getName())) {
				iter.remove();
			}	
		}
		
		
		session.put(ProtocolConstants.BRICS_STUDY_HASHTABLE, bricsStudyTable);
		
		// Sort the listing and mark any BRICS studies that are linked to other ProFoRMS studies.
		manager.sortBricsStudyList(protocol, bricsStudyList);
		
		return bricsStudyList;
	}
	
	public String createProtocol() {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_DETAILS, new int[]{3, 8, 11, 15, 16, 17, 18, 19, 20, 34, 36, 46});
		protoForm.setAllowPii(SysPropUtil.getProperty(CtdbConstants.CAN_USE_PII_KEY).equals("0"));
//		session.remove(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		if (protoForm.isAddedFromDashboard()) {
			protoForm.setAddedFromDashboard(false);
		}

		clearStudySession();

		// Get or reset the list of available BRICS studies available to the user.
		try {
			LookupManager lookUp = new LookupManager();
			
			session.put("xinstitutes", lookUp.getLookups(LookupType.INSTITUTE));
			session.put("xProtocolStatus", lookUp.getLookups(LookupType.PROTOCOL_STATUS));
			session.put("xuserlist", lookUp.getLookups(LookupType.USER_NAME));
			session.put("__SiteHome_states", lookUp.getLookups(LookupType.STATE));
			session.put("__SiteHome_countries", lookUp.getLookups(LookupType.COUNTRY));
			session.put("_availiableDefaults", lookUp.getLookups(LookupType.PROTOCOL_DEFAULTS));
			session.put("_btrisAccess", lookUp.getLookups(LookupType.BTRIS_ACCESS));
			
			ProtocolManager protoMan = new ProtocolManager();
			List<Procedure> allProcedureList = protoMan.getAllProcdeureList();
			JSONArray procedureArray = ProtocolAssembler.procedureListTOJSONArray(allProcedureList);
			protoProceduresStr = procedureArray.toString();
		}
		catch ( Exception e ) {
			String errMsg = getText(StrutsConstants.ERROR_BRICS_STUDY_LIST_GET);
			logger.error(errMsg, e);
			addActionError(errMsg);
		}
		
		return SUCCESS;
	}
	
	public String saveProtocol() {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_DETAILS, new int[]{3, 8, 11, 15, 17, 18, 19, 20, 34, 36, 46});
		
		SiteManager sMan = new SiteManager();
		
		try {
			User user = getUser();
			List<Site> studySites = new ArrayList<Site>();
			List<String> selectedBricsStudySiteIds = new ArrayList<String>();
			
			if (!Utils.isBlank(protoForm.getSelectedSites())) {
				JSONArray sitesJSONArr = new JSONArray(protoForm.getSelectedSites());
				
				if(sitesJSONArr != null  && sitesJSONArr.length() > 0) {
					for ( int i = 0; i < sitesJSONArr.length(); i++ ) {
						JSONObject obj = sitesJSONArr.getJSONObject(i);
						String bricsStudySiteId =  obj.getString("bricsStudySiteId");
						selectedBricsStudySiteIds.add(bricsStudySiteId);
						String siteName = obj.getString("siteName");
						String address1 = obj.getString("address1"); 
						String address2 = obj.getString("address2"); 
						String city = obj.getString("city");
						int stateId = Integer.MIN_VALUE;
						
						if (!Utils.isBlank(obj.getString("stateId")) && Utils.isNumeric(obj.getString("stateId"))) {
							stateId = obj.getInt("stateId");
						}
						
						String stateName = obj.getString("stateName");
						int countryId = Integer.MIN_VALUE;
						
						if(!Utils.isBlank(obj.getString("countryId")) && Utils.isNumeric(obj.getString("countryId"))) {
							countryId = obj.getInt("countryId");
						}
						
						String countryName = obj.getString("countryName");  
						String zipCode = obj.getString("zipCode");
						boolean isPrimary = obj.getBoolean("isPrimary");
						Address address = new Address();
						
						address.setAddressOne(address1);
						address.setAddressTwo(address2);
						address.setCity(city);
						address.setCreatedBy(user.getId());
						address.setUpdatedBy(user.getId());
						
						CtdbLookup state = new CtdbLookup();
						state.setId(stateId);
						state.setFullName(stateName);
						address.setState(state);
						CtdbLookup country = new CtdbLookup();
						
						country.setId(countryId);
						country.setFullName(countryName);
						address.setCountry(country);
						address.setZipCode(zipCode);
						Site site = new Site();
						site.setBricsStudySiteId(bricsStudySiteId);
						site.setName(siteName);
						site.setAddress(address);
						site.setCreatedBy(user.getId());
						site.setUpdatedBy(user.getId());
						site.getSitePI().setId(user.getId());
						site.setPrimarySite(isPrimary); 
						studySites.add(site);
						boolean isValid = false;
						
						if (countryId > 0) {
							isValid = sMan.isCountryIDAndCountryNameValid(countryId, countryName);
							
							if (!isValid) {
								addActionError(getText(StrutsConstants.ERROR_SITE_ADDRESS));
								return StrutsConstants.EXCEPTION;
							}
						}
						
						if (stateId > 0) {
							isValid = sMan.isStateIdAndStateNameValid(stateId, stateName);
							
							if (!isValid) {
								addActionError(getText(StrutsConstants.ERROR_SITE_ADDRESS));
								return StrutsConstants.EXCEPTION;
							}
						}
					}	
				}
			}
			
			JSONArray selectedSiteIdArray = new JSONArray(selectedBricsStudySiteIds);
			protoForm.setSelectedSiteIds(selectedSiteIdArray.toString());
			
			List<ClinicalLocation> clinicLocToSaveList = (List<ClinicalLocation>) session.get("protoClinicalLocationList");
			protoForm.setProtoClinicLocList(clinicLocToSaveList);
			
			List<MilesStone> milesStoneToSaveList = (List<MilesStone>) session.get("protoMilesStoneList");
			protoForm.setProtoMilesStoneList(milesStoneToSaveList);
			
			ProtocolManager protoMan = new ProtocolManager();
			List<Procedure> procToSaveList = (List<Procedure>) session.get("protoProcedureList");
			//save newly added procedure
			if (procToSaveList != null) {
				for (Procedure proc : procToSaveList) {
					if (proc.getIsNew()) {
						int newProcId = protoMan.createProcedure(proc);
						proc.setId(newProcId);
					}
				}
			}
			protoForm.setProtoProcedureList(procToSaveList);
			
			List<PointOfContact> pocToSaveList = (List<PointOfContact>) session.get("protoPointOfContactList");
			protoForm.setProtoPOCList(pocToSaveList);
			
			if ( !this.validateStudy() ) {
				return StrutsConstants.EXCEPTION;
			}
			
			Protocol protocol = ProtocolAssembler.formToDomain(protoForm);
			
			protocol.setStudySites(studySites);
			protocol.setSelectedBricsStudySiteIds(selectedBricsStudySiteIds);
			protocol.setUpdatedBy(user.getId());
			protocol.setCreatedBy(user.getId());
			protocol.setCreatedByUsername(user.getUsername());
			protocol.setSiteHashMap(new HashMap<String, Site>());
			protocol.setDrugDeviceHashMap(new HashMap<String, DrugDevice>());
			
			if (protocol.getId() <= 0) {
				protocol = protoMan.createProtocol(protocol);
				PatientRoleManager prm = new PatientRoleManager();
				prm.createPatientRole(new PatientRole(protocol.getId(), "N/A"));
				session.put("new_protocol_id", protocol.getId());
				protoForm.setId(protocol.getId()); 
				addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, new String[]{"protocol \"" + protocol.getName() + "\""}));
				session.put(StrutsConstants.SUCCESS_ADD_KEY, this.getActionMessages());	            
			}
			else {
				protoMan.updateProtocol(protocol);
				addActionMessage(getText(StrutsConstants.SUCCESS_EDIT_KEY, new String[]{"protocol \"" + protocol.getName() + "\""}));
				session.put(StrutsConstants.SUCCESS_EDIT_KEY, this.getActionMessages());
			}
			
			this.setStudyId(String.valueOf(protocol.getId()));
			// refresh the Overview drop down
			SecuritySessionUtil.refreshUserProtocols(user, request);
			session.put(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY, protocol);
			buildLeftNav(LeftNavController.LEFTNAV_STUDY_DETAILS);
		}
		catch (AssemblerException e) {
			logger.error("Couldn't construct a Protocol object from the user's data.", e);
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE,
					new String[] {getText("app.label.lcase.protocol").toLowerCase(request.getLocale())}));
			return StrutsConstants.EXCEPTION;
		}
		catch (DuplicateObjectException e) {
			String errMsg = getText(StrutsConstants.ERROR_DUPLICATE, new String[]{
					getText("study.add.number.display") + " " + protoForm.getProtocolNumber()});
			logger.error("There is already an existing protocol for " + protoForm.getProtocolNumber(), e);
			addActionError(errMsg);
			return StrutsConstants.EXCEPTION;
		}
		catch (CtdbException e) {
			logger.error("Database error occurred while saving the protocol.", e);
			addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, 
					new String[] {getText("app.label.lcase.protocol").toLowerCase(request.getLocale())}));
			return StrutsConstants.EXCEPTION;
		}
		catch (Exception e) {
			logger.error("An error occured while saving the protocol.", e);
			addActionError(getText(StrutsConstants.ERROR_DATA_SAVE,
					new String[] {getText("app.label.lcase.protocol").toLowerCase(request.getLocale())}));
			return StrutsConstants.EXCEPTION;
		}
		
		return SUCCESS;
	}
	
	public String getSiteJSONData() {
		String strutsResult = BaseAction.SUCCESS;
		String siteId = request.getParameter("id");
		@SuppressWarnings("unchecked")
		Map<String, Site> siteMap = (Map<String, Site>) session.get(CtdbConstants.SITE_HASH_MAP);
		
		// Check if the site map was in the session.
		if ( siteMap == null ) {
			logger.error("Could not get the site map form the session.");
			return BaseAction.ERROR;
		}
		
		Site s = siteMap.get(siteId);
		
		// Check if a site object was returned from the site map.
		if ( s == null ) {
			logger.error("Could not retreive the site, " + siteId + ", from the site map.");
			return BaseAction.ERROR;
		}
		
		JSONObject siteJson = new JSONObject();
		
		try {
			// Construct site JSON object.
			siteJson.put("id", s.getId());
			siteJson.put("isPrimarySite", s.isPrimarySite());
			siteJson.put("name", s.getName());
			siteJson.put("studySiteId", s.getStudySiteId());
			siteJson.put("description", s.getDescription());
			siteJson.put("sitePiId", s.getSitePI().getId());
			siteJson.put("siteURL", s.getSiteURL());
			
			// Set address data.
			Address address = s.getAddress();
			
			siteJson.put("addressId", address.getId());
			siteJson.put("addressOne", address.getAddressOne());
			siteJson.put("addressTwo", address.getAddressTwo());
			siteJson.put("city", address.getCity());
			siteJson.put("stateId", address.getState().getId());
			siteJson.put("zipCode", address.getZipCode());
			siteJson.put("countryId", address.getCountry().getId());
			siteJson.put("phoneNumber", s.getPhoneNumber());
			
			// Set JSON string.
			jsonString = siteJson.toString();
			
			// Verify generated JSON string
			if ( jsonString == null ) {
				logger.error("Could not generate JSON string for site " + siteId);
				strutsResult = BaseAction.ERROR;
			}
		}
		catch ( JSONException jsone ) {
			logger.error("Could not construct site JSON object for site " + siteId, jsone);
			strutsResult = BaseAction.ERROR;
		}
		
		return strutsResult;
	}

	public String processSite() throws Exception {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_DETAILS);
		protoForm.setAllowPii(SysPropUtil.getProperty(CtdbConstants.CAN_USE_PII_KEY).equals("0"));
		@SuppressWarnings("unchecked")
		Map<String, Site> siteMap = (Map<String, Site>) session.get(CtdbConstants.SITE_HASH_MAP);
		
		// Check if the site map was in the session.
		if ( siteMap == null ) {
			logger.error("Could not get site map from session. Abort site processing.");
			return StrutsConstants.FAILURE;
		}
		
		String actionFlag = protoForm.getProtocolSiteActionFlag();
		
		if (actionFlag.equalsIgnoreCase("add_site_form") || actionFlag.equalsIgnoreCase("edit_site_form")) {
			if (!validateSite(siteMap)) {
				return StrutsConstants.EXCEPTION;
			}
			
			this.addOrUpdateSite(siteMap, actionFlag);
		}
		else if (actionFlag.equalsIgnoreCase("delete_site_form")) {
			@SuppressWarnings("unchecked")
			List<Site> studySites = (List<Site>) session.get(CtdbConstants.CURRENT_PROTOCOL_STUDY_SITES_SESSION_KEY);
			List<String> deletedSiteNames = null;
			
			if ( studySites == null ) {
				studySites = new ArrayList<Site>();
			}
			
			deletedSiteNames = this.deleteSite(siteMap, studySites);
			
			// If there are no errors, add in the success message.
			if ( !hasActionErrors() ) {
				if ( deletedSiteNames.size() == 1 ) {
					addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY, 
							new String[]{ Utils.convertListToString(deletedSiteNames) }));
				}
				else {
					addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_MULTI_KEY, 
							new String[]{ Utils.convertListToString(deletedSiteNames) }));
				}
			}
		}
						
		List<Site> siteListForSession = new ArrayList<Site>(siteMap.values());
		
		Collections.sort(siteListForSession, new Comparator<Site>() {
			public int compare(Site s1, Site s2) {
				return s1.getName().compareTo(s2.getName());
			}
		});
		
		session.put(CtdbConstants.CURRENT_PROTOCOL_STUDY_SITES_SESSION_KEY, siteListForSession);
		protoForm.setSiteHashMap(siteMap);
		
		return SUCCESS;
	}
	
	public String getDrugDeviceJSONData() {
		String strutsResult = BaseAction.SUCCESS;
		String deviceId = request.getParameter("id");
		@SuppressWarnings("unchecked")
		Map<String, DrugDevice>	drugDeviceMap = (Map<String, DrugDevice>) session.get(CtdbConstants.DRUG_DEVICE_HASH_MAP);
		
		// Check if the drug device map is in the session.
		if ( drugDeviceMap == null ) {
			logger.error("The drug device map was not in the session.");
			return BaseAction.ERROR;
		}
		
		DrugDevice dd = drugDeviceMap.get(deviceId);
		
		// Check if the drug device was in the map.
		if ( dd == null ) {
			logger.error("Could not find the drug device, " + deviceId + ", in the drug device map.");
			return BaseAction.ERROR;
		}
		
		// Construct drug device JSON object
		JSONObject ddJSON = new JSONObject();
		
		try {
			ddJSON.put("id", dd.getId());
			ddJSON.put("fdaInd", dd.getFdaInd());
			ddJSON.put("sponsor", dd.getSponsor());
			
			// Build JSON string, and verify it.
			jsonString = ddJSON.toString();
			
			if ( jsonString == null ) {
				logger.error("Could not build JSON string for drug device " + deviceId);
				strutsResult = BaseAction.ERROR;
			}
		}
		catch ( JSONException je ) {
			logger.error("Could not construct a JSON object for drug device " + deviceId, je);
			strutsResult = BaseAction.ERROR;
		}
		
		return strutsResult;
	}
	
	@SuppressWarnings("unchecked")
	public String processDrugDevice() throws Exception {
		buildLeftNav(LeftNavController.LEFTNAV_STUDY_DETAILS);
		protoForm.setAllowPii(SysPropUtil.getProperty(CtdbConstants.CAN_USE_PII_KEY).equals("0"));
		
		Map<String, DrugDevice>	drugDeviceHashMap = new HashMap<String, DrugDevice>();
		
		if (session.get(CtdbConstants.DRUG_DEVICE_HASH_MAP) != null) {
			drugDeviceHashMap = (Map<String, DrugDevice>) session.get(CtdbConstants.DRUG_DEVICE_HASH_MAP);
		}
		
		String drugDeviceActionFlag = protoForm.getProtocolDrugDeviceActionFlag();
		
		if (!(drugDeviceActionFlag.equalsIgnoreCase("delete_DrugDevice_form")) && !this.validateDrugDevice(drugDeviceHashMap)) {
			return StrutsConstants.EXCEPTION;
		}
		
		this.createUpdateDrugDeviceHashMap(drugDeviceHashMap);
		
		List<DrugDevice> drugDeviceListForSession = new ArrayList<DrugDevice>(drugDeviceHashMap.values());
		
		Collections.sort(drugDeviceListForSession, new Comparator<DrugDevice>() {
			public int compare(DrugDevice d1, DrugDevice d2) {
				return d1.getFdaInd().compareTo(d2.getFdaInd());
			}
		});

		protoForm.setDrugDeviceHashMap(drugDeviceHashMap);
		
		return SUCCESS;
	}
	
	@SuppressWarnings("unchecked")
	private void addOrUpdateSite(Map<String, Site> siteHashMap, String siteActionFlag) {
		Site site = protoForm.getSite();
		site.setProtocolId(Integer.parseInt(getStudyId()));
				
		// User Name
		List<CtdbLookup> userListDropDown = (List<CtdbLookup>) session.get("xuserlist");
		CtdbLookup userCl = getCtdbLookup(site.getSitePI().getId(), userListDropDown);
		site.getSitePI().setCtdbLookupStringForDisplay(userCl.getLongName());
				
		// State Name
		List<CtdbLookup> stateListDropDown = (List<CtdbLookup>) session.get("__SiteHome_states");
		CtdbLookup stateCl = getCtdbLookup(site.getAddress().getState().getId(), stateListDropDown);
		site.getAddress().setCtdbLookupStringForStateDisplay(stateCl.getLongName());
				
		// Country Name
		List<CtdbLookup> countryListDropDown = (List<CtdbLookup>) session.get("__SiteHome_countries");
		CtdbLookup countryCl = getCtdbLookup(site.getAddress().getCountry().getId(), countryListDropDown);
		site.getAddress().setCtdbLookupStringForCountryDisplay(countryCl.getLongName());
			
		User user = getUser();
		site.updateUpdatedByInfo(user);
		site.getAddress().updateUpdatedByInfo(user);
		
		if (siteActionFlag.equalsIgnoreCase("edit_site_form")) {
			site.setId(Integer.parseInt(this.getSelectedSiteId()));
			site.getAddress().setId(protoForm.getSelectedAddressId());
			protoForm.setProtocolSiteActionFlag("add_site_form");
		}
		else {
			logger.info("Adding a site ++++++++++++++++++++++++++");
			site.setId(getNegativeUniqueId()); 
			site.setCreatedBy(user.getId());
			site.getAddress().updateCreatedByInfo(user);
		}
			
		if (site.getId() > 0) {
			site.setSiteActionFlag("edit_site");
		}
		else {
			site.setSiteActionFlag("add_site");
		}
		
		siteHashMap.put(Integer.toString(site.getId()), site);
	}

	private List<String> deleteSite(Map<String, Site> siteHashMap, List<Site> studySites) throws CtdbException {
		logger.info("Deleting the site------------------------" + studySites.size());
		SiteManager sMan = new SiteManager();
		List<String> removedSites = new ArrayList<String>(siteHashMap.size());
		
		try {
			List<Integer> selectedSiteIds = Utils.convertStrToIntArray(protoForm.getSelectedSiteIds());
			
			if (studySites != null) {
				List<Site> siteListToBeDeleted = new ArrayList<Site>();
				
				for (Site s : studySites) {
					for (int siteId : selectedSiteIds) {
						if ( siteId == s.getId() ) {
							if ( s.getId() > 0 ) {
								siteListToBeDeleted.add(s);
							}
							
							siteHashMap.remove(Integer.toString(siteId)); // remove the site & and old site object
							removedSites.add(s.getName());
						}
					}
				}
						
				List<ErrorMessage> errorList = new ArrayList<ErrorMessage>();
				sMan.deleteSites(siteListToBeDeleted, errorList);
				
				if (!errorList.isEmpty()) {
					List<String> siteNames = new ArrayList<String>();
					for (ErrorMessage errorMsg : errorList) {
						logger.error(errorMsg.getMessage(), errorMsg.getException());
						siteNames.add(errorMsg.getObjName());
						
						for (Site s : studySites) {
							if (s.getId() == errorMsg.getObjId()) {
								siteHashMap.put(Integer.toString(s.getId()), s);
								break;
							}
						}
					}
					
					this.addActionError(getText(StrutsConstants.ERROR_DELETE, 
							new String[]{Utils.convertListToString(siteNames) + " site(s)"}));
				}
			}
		}
		catch (Exception e) {
			String errMsg = "An error occured while deleting the selected sites. Please try again.";
			logger.error(errMsg, e);
			addActionError(errMsg);
		}
		
		session.put(CtdbConstants.SITE_HASH_MAP, siteHashMap);
		
		return removedSites != null ? removedSites : new ArrayList<String>();
	}
		
	private Map<String, Site> createSiteMap(List<Site> sites) {
		Map<String, Site> siteHashMap = new HashMap<String, Site>();
		
		if (sites != null) {
			for (Site s : sites) {
				siteHashMap.put(String.valueOf(s.getId()), s);
			}
		}
		
		return siteHashMap;
	}
	
	private boolean isDuplicateSite(Map<String, Site> siteHashMap) {
		boolean isDuplicate = false;
		String siteName = protoForm.getSite().getName();
		String siteActionFlag = protoForm.getProtocolSiteActionFlag();
		
		if (!Utils.isBlank(siteActionFlag) && !siteHashMap.isEmpty()) {
			if (siteActionFlag.equalsIgnoreCase("add_site_form")) {
				for (Site s : siteHashMap.values()) {
					if (s.getName().equalsIgnoreCase(siteName.trim())){
						isDuplicate = true;
						break;
					}
				}
			} else if (siteActionFlag.equalsIgnoreCase("edit_site_form")){
				int siteId = Integer.parseInt(getSelectedSiteId());
				for (Site s : siteHashMap.values()) {
					if (s.getId() != siteId && s.getName().equalsIgnoreCase(siteName.trim())){
						isDuplicate = true;
					}
				}
			}
		}
		return isDuplicate;
	}
	
	private void createUpdateDrugDeviceHashMap(Map<String, DrugDevice> drugDeviceHashMap) throws CtdbException {
		
		String drugDeviceActionFlag = protoForm.getProtocolDrugDeviceActionFlag();
		DrugDevice drugDevice = protoForm.getDrugDevice();
		
		if (!Utils.isBlank(drugDeviceActionFlag)) {
			if (drugDeviceActionFlag.equalsIgnoreCase("add_DrugDevice_form") || drugDeviceActionFlag.equalsIgnoreCase("edit_DrugDevice_form")) {
				drugDevice.setProtocolId(Integer.parseInt(getStudyId()));
				
				User user = getUser();
				drugDevice.updateUpdatedByInfo(user);
				
				if (drugDeviceActionFlag.equalsIgnoreCase("edit_DrugDevice_form")) {
					drugDevice.setId(Integer.parseInt(getSelectedDrugDeviceId()));
					protoForm.setProtocolDrugDeviceActionFlag("add_DrugDevice_form");
				}
				else {
					drugDevice.setId(getNegativeUniqueId()); 
					drugDevice.setCreatedBy(user.getId());
				}
				
				if (drugDevice.getId() > 0) {
					drugDevice.setDrugDeviceActionFlag("edit_drugDevice");
				}
				else {
					drugDevice.setDrugDeviceActionFlag("add_drugDevice");
				}
				
				drugDeviceHashMap.put(String.valueOf(drugDevice.getId()), drugDevice);

			}
			else if ( drugDeviceActionFlag.equalsIgnoreCase("delete_DrugDevice_form") ) {
				logger.info("Deleting the DrugDevice------------------------" + drugDeviceHashMap.size());
				DrugDeviceManager dMan = new DrugDeviceManager();
				
				List<DrugDevice> drugDevicesToBeDeleted = new ArrayList<DrugDevice>();
				List<String> deletedDevices = new ArrayList<String>();
				
				try {
					List<Integer> selectedDrugDeviceIds = Utils.convertStrToIntArray(protoForm.getSelectedDrugDeviceIds());
					
					for ( Iterator<DrugDevice> it = drugDeviceHashMap.values().iterator(); it.hasNext(); ) {
						DrugDevice d = it.next();
						
						for ( Integer idToDelete : selectedDrugDeviceIds ) {
							if ( idToDelete.intValue() == d.getId() ) {
								// Check if the drug device needs to be deleted from the database later.
								if ( d.getId() > 0 ) {
									drugDevicesToBeDeleted.add(d);
								}
								
								// Remove the drug device from the map.
								it.remove();
								deletedDevices.add(d.getFdaInd());
								break;
							}
						}
						
						dMan.deleteDrugDevices(drugDevicesToBeDeleted);
					}
					
					// Create success message
					if ( deletedDevices.size() == 1 ) {
						addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY, 
							new String[]{ Utils.convertListToString(deletedDevices) }));
					}
					else {
						addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_MULTI_KEY, 
							new String[]{ Utils.convertListToString(deletedDevices) }));
					}
				}
				catch (CtdbException ce) {
					String errMsg = "An error occured while trying to delete a drug device from the database.";
					logger.error(errMsg, ce);
					addActionError(errMsg);
				}
				catch (Exception e) {
					String errMsg = "An error occured while processing the selected drug devices. Please try again.";
					logger.error(errMsg, e);
					addActionError(errMsg);
				}
			}
		}
	
		session.put(CtdbConstants.DRUG_DEVICE_HASH_MAP, drugDeviceHashMap);		
	}

	private Map<String, DrugDevice> createDrugDeviceMap(List<DrugDevice> drugDevices) {
		Map<String, DrugDevice> drugDeviceHashMap = new HashMap<String, DrugDevice>();
		
		if (drugDevices != null) {
			for (DrugDevice d : drugDevices) {
				drugDeviceHashMap.put(String.valueOf(d.getId()), d);
			}
		}
		
		return drugDeviceHashMap;
	}

	private boolean isDuplicateDrugDevice(Map<String, DrugDevice> drugDeviceHashMap) {
		
		String fdaInd = protoForm.getDrugDevice().getFdaInd().trim();
		String drugDeviceActionFlag = protoForm.getProtocolDrugDeviceActionFlag();
		
		if (!Utils.isBlank(drugDeviceActionFlag) && !drugDeviceHashMap.isEmpty()) {
			if (drugDeviceActionFlag.equalsIgnoreCase("add_DrugDevice_form")) {
				for (DrugDevice d : drugDeviceHashMap.values()) {
					if (d.getFdaInd().equalsIgnoreCase(fdaInd)){
						return true;
					}
				}
			} else if (drugDeviceActionFlag.equalsIgnoreCase("edit_DrugDevice_form")) {
				int drugDeviceId = Integer.parseInt(getSelectedDrugDeviceId());
				for (DrugDevice d : drugDeviceHashMap.values()) {
					if (d.getId() != drugDeviceId && d.getFdaInd().equalsIgnoreCase(fdaInd)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

	private void resetUserInfo() throws CtdbException {
		session.remove(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		User u = getUser();
		SecurityManager sm = new SecurityManager();
		u.setRoleList(sm.getUserRoles(u.getId()));
		u.setRoleMap(sm.getRoleMap(u.getId()));
	}
	
	/**
	 * Finds the CtdbLookup object by ID, if not found return null.
	 * 
	 * @param id - The CtdbLookup ID to search for
	 * @param CtdbLookupList - The list to search over
	 * @return	The found CtdbLookup object or a default object if not found
	 */
	private CtdbLookup getCtdbLookup(int id, List<CtdbLookup> CtdbLookupList) {
		
		if (CtdbLookupList != null && !CtdbLookupList.isEmpty()) {
			for (CtdbLookup cl : CtdbLookupList) {
				if (id == cl.getId()) {
					return cl;
				}
			}
		}
		return null;
	}
	
	
	public void areAnyPatientsWithBlankSubjectId() throws CtdbException, IOException {
		boolean hasBlankSubjectIds = false;
		
		PatientManager pm = new PatientManager();
		List<Patient> patients = pm.getMinPatientsByProtocolId(Long.valueOf(studyId));
		for (Patient patient : patients) {
			if (patient.getSubjectId() == null || patient.getSubjectId().equals("")) {
				hasBlankSubjectIds =  true;
				break;
			}
		}
		
		HttpServletResponse response = ServletActionContext.getResponse();
		PrintWriter out = response.getWriter();
		out.print(hasBlankSubjectIds);
		out.flush();
		
		return;
	}
	

	
	private boolean validateStudy() throws CtdbException {
		this.clearErrorsAndMessages();
		String studyNumber = protoForm.getProtocolNumber();
		String studyName = protoForm.getName();
		String reasonForEsignature = protoForm.getReasonForEsignature();
		boolean enableEsignature = protoForm.isEnableEsignature();
		String bricsStudyId = protoForm.getBricsStudyId();
		String studyTypeName = protoForm.getStudyType();
		Integer patientDisplayTypeId = protoForm.getPatientDisplayType();

		if (Utils.isBlank(bricsStudyId)) {
			addFieldError(getText("study.data.repository.study"), getText(StrutsConstants.ERROR_FIELD_REQUIRED,
					new String[]{getText("study.data.repository.study"), "is"}));
		}
		
		if (Utils.isBlank(studyTypeName)) {
			addFieldError(getText("study.studyType.display"), getText(StrutsConstants.ERROR_FIELD_REQUIRED,
					new String[]{getText("study.studyType.display"), "is"}));
		}
		
		if (patientDisplayTypeId == 0) {
			addFieldError(getText("patient.label.display"), getText(StrutsConstants.ERROR_FIELD_REQUIRED,
					new String[]{getText("patient.label.display"), "is"}));
		}
		
		if (Utils.isBlank(studyName)) {
			addFieldError(getText("study.add.name.display"), getText(StrutsConstants.ERROR_FIELD_REQUIRED,
					new String[]{getText("study.add.name.display"), "is"}));
		} else if (studyName.length() > 400) {
			addFieldError(getText("study.add.name.display"), getText(StrutsConstants.ERROR_MAX_LENGTH,
					new String[]{getText("study.add.name.display"), "400"}));
		}
		
		if (Utils.isBlank(studyNumber)) {
			addFieldError(getText("study.add.number.display"), getText(StrutsConstants.ERROR_FIELD_REQUIRED,
					new String[]{getText("study.add.number.display"), "is"}));
		} else if (studyNumber.length() > 50) {
			addFieldError(getText("study.add.number.display"), getText(StrutsConstants.ERROR_MAX_LENGTH,
					new String[]{getText("study.add.number.display"), "50"}));
		}
		
		if (!enableEsignature && Utils.isBlank(reasonForEsignature)) {
			addFieldError(getText("protocol.add.reasonForEsignature.display"), getText(StrutsConstants.ERROR_FIELD_REQUIRED,
					new String[]{getText("protocol.add.reasonForEsignature.display"), "is"}));
		} else if (!enableEsignature && (reasonForEsignature.length() > 4000)) {
			addFieldError(getText("protocol.add.reasonForEsignature.display"), getText(StrutsConstants.ERROR_MAX_LENGTH,
					new String[]{getText("protocol.add.reasonForEsignature.display"), "4000"}));
		}
		
		if (protoForm.getOrganization().length() > 150) {
			addFieldError(getText("protocol.add.institute.display"), getText(StrutsConstants.ERROR_MAX_LENGTH,
					new String[]{getText("protocol.add.institute.display"), "150"}));
		}
		if (Boolean.valueOf(SysPropUtil.getProperty("display.protocol.clinicalPoint"))) {
			List<ClinicalLocation> clinicLocToValidList = protoForm.getProtoClinicLocList();
			if (clinicLocToValidList == null || clinicLocToValidList.size() == 0) {
				addFieldError(getText("protocol.add.clinicalLocation.display"),
						getText(StrutsConstants.ERROR_FIELD_REQUIRED,
								new String[] {getText("protocol.add.clinicalLocation.display"), "is"}));
			}

			List<Procedure> procToValidList = protoForm.getProtoProcedureList();
			if (procToValidList == null || procToValidList.size() == 0) {
				addFieldError(getText("protocol.add.procedure.display"), getText(StrutsConstants.ERROR_FIELD_REQUIRED,
						new String[] {getText("protocol.add.procedure.display"), "is"}));
			}

			List<PointOfContact> pocToValidList = protoForm.getProtoPOCList();
			if (pocToValidList == null || pocToValidList.size() == 0) {
				addFieldError(getText("protocol.add.pointOfContact.display"),
						getText(StrutsConstants.ERROR_FIELD_REQUIRED,
								new String[] {getText("protocol.add.pointOfContact.display"), "is"}));
			}
		}
		return !hasFieldErrors();
	}
	
	private boolean validateSite(Map<String, Site> siteHashMap) {
		this.clearErrorsAndMessages();
		
		String siteName = protoForm.getSite().getName();
		if (Utils.isBlank(siteName)) {
			addFieldError(getText("protocol.siteName"), getText(StrutsConstants.ERROR_SITE_NAME_REQUIRED));
		} else if (this.isDuplicateSite(siteHashMap)) {
			addFieldError(getText("protocol.siteName"), getText(StrutsConstants.ERROR_SITE_NAME_DUPLICATE));
		}
		
		if (protoForm.getSite().getSitePI().getId() < 0) {
			addFieldError(getText("protocol.sitePI"), getText(StrutsConstants.ERROR_SITE_PI_REQUIRED));
		}
		
		String siteUrl = protoForm.getSite().getSiteURL();
		if (!Utils.isBlank(siteUrl)) {
			try {
				URL url = new URL(siteUrl);
				URLConnection conn = url.openConnection();
				conn.connect();
			} catch (MalformedURLException e) {
				addFieldError(getText("protocol.siteURL"), getText(StrutsConstants.ERROR_URL_INVALID,
						new String[]{getText("protocol.siteURL")}));
			} catch (Exception e) {
				addFieldError(getText("protocol.siteURL"), getText(StrutsConstants.ERROR_FIELD_INVALID,
						new String[]{getText("protocol.siteURL")}));
			}
			
			if (siteUrl.length() > 255) {
				addFieldError(getText("protocol.siteURL"), getText(StrutsConstants.ERROR_MAX_LENGTH,
						new String[]{getText("protocol.siteURL"), "255"}));
			}
		}

		return !hasFieldErrors();
	}
	
	private boolean validateDrugDevice(Map<String, DrugDevice> drugDeviceHashMap) {
		String fdaInd = protoForm.getDrugDevice().getFdaInd();
		
		if (Utils.isBlank(fdaInd)) {
			addFieldError("FDA IND/IDE", getText(StrutsConstants.ERROR_FDA_IND_REQUIRED));
			return false;
		}
		else if (isDuplicateDrugDevice(drugDeviceHashMap)) {
			addFieldError("FDA IND/IDE", getText(StrutsConstants.ERROR_FDA_IND_DUPLICATE));
			return false;
		}
		
		return true;
	}
	
	private void clearStudySession() {
		session.remove(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		session.remove("protoClinicalLocationList");
		session.remove("protoProcedureList");
		session.remove("protoPointOfContactList");
		session.remove("protoMilesStoneList");
	}
	
	public String getProtoClinicalLocationDTList() throws CtdbException {
		List<ClinicalLocation> protoClinicLocList = new ArrayList<ClinicalLocation>();

		List<ClinicalLocation> sessionClinicLocList = (List<ClinicalLocation>) session.get("protoClinicalLocationList");	
		if (sessionClinicLocList != null) {
			protoClinicLocList.addAll(sessionClinicLocList);
		}
		
		try {
			IdtInterface idt = new Struts2IdtInterface();			
			ArrayList<ClinicalLocation> outputList = new ArrayList<ClinicalLocation>(protoClinicLocList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ProtocolClinicalLocationIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	public StreamResult addProtoClinicalLocation() throws CtdbException {
		List<ClinicalLocation> sessionClinicLocList = (List<ClinicalLocation>) session.get("protoClinicalLocationList");
		if (sessionClinicLocList == null) {
			sessionClinicLocList = new ArrayList<ClinicalLocation>();
		}
		
		String jsonStr = this.getJsonString();
		ClinicalLocation clinicLocFromJson = convertJsonStrToClinicLocObj(jsonStr);
		sessionClinicLocList.add(clinicLocFromJson);
		session.put("protoClinicalLocationList", sessionClinicLocList);
		
		return new StreamResult (new ByteArrayInputStream(BaseAction.SUCCESS.getBytes()));		
	}

	public StreamResult editProtoClinicalLocation() throws CtdbException {
		List<ClinicalLocation> sessionClinicLocList = (List<ClinicalLocation>) session.get("protoClinicalLocationList");
		if (sessionClinicLocList == null) {
			sessionClinicLocList = new ArrayList<ClinicalLocation>();
		}
		String editOrigJson = this.getEditOrigJsonStr();
		ClinicalLocation cLOrig = convertJsonStrToClinicLocObj(editOrigJson);
		String jsonStr = this.getJsonString();
		ClinicalLocation cLFromJson = convertJsonStrToClinicLocObj(jsonStr);
	
		for(ClinicalLocation cl : sessionClinicLocList){
			if(cl.equals(cLOrig)) {
				cl.setName(cLFromJson.getName());
				cl.setAddress(cLFromJson.getAddress());
				cl.setStatus(cLFromJson.getStatus());
			}
		}
		session.put("protoClinicalLocationList", sessionClinicLocList);
		
		return new StreamResult (new ByteArrayInputStream(BaseAction.SUCCESS.getBytes()));		
	}
	
	public StreamResult deleteProtoClinicalLocation() throws CtdbException {
		List<ClinicalLocation> clToRemoveList = new ArrayList<ClinicalLocation>();
		
		String jsonStr = this.getJsonString();
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(jsonStr);
		JsonArray jsonArray = element.getAsJsonArray();
		
		if(jsonArray != null && jsonArray.size() > 0){
			for(int i = 0; i < jsonArray.size(); i++) {
				JsonObject jsonObj = jsonArray.get(i).getAsJsonObject(); 
				ClinicalLocation clToRemove = convertJsonStrToClinicLocObj(jsonObj.toString());
				clToRemoveList.add(clToRemove);
			}
		}
		
		List<ClinicalLocation> sessionClinicLocList = (List<ClinicalLocation>) session.get("protoClinicalLocationList");
		if (sessionClinicLocList != null && sessionClinicLocList.size() >= clToRemoveList.size()) {
			sessionClinicLocList.removeAll(clToRemoveList);
		} else {
			return new StreamResult (new ByteArrayInputStream(StrutsConstants.EXCEPTION.getBytes()));
		}
		
		session.put("protoClinicalLocationList", sessionClinicLocList);
		return new StreamResult (new ByteArrayInputStream(BaseAction.SUCCESS.getBytes()));
	}
	
	public Address convertJsonToAddressObj(JsonObject addressJsonObj){
		Address address = new Address();
		String addressIdStr = addressJsonObj.get("id").toString().replaceAll("^\"|\"$", "");
		if(!addressIdStr.equals("") &&
				Long.valueOf(addressIdStr) > 0) {
			address.setId(Integer.parseInt(addressIdStr));
		}
		address.setAddressOne(addressJsonObj.get("addressOne").toString().replaceAll("^\"|\"$", ""));
		address.setAddressTwo(addressJsonObj.get("addressTwo").toString().replaceAll("^\"|\"$", ""));
		address.setCity(addressJsonObj.get("city").toString().replaceAll("^\"|\"$", ""));
		
		Integer stateId = Integer.parseInt(addressJsonObj.get("state").toString().replaceAll("^\"|\"$", ""));
		List<CtdbLookup> stateList = (List<CtdbLookup>) session.get("__SiteHome_states");
		CtdbLookup state = getCtdbLookup(stateId, stateList); 
        address.setState(state);
        
        Integer countryId = Integer.parseInt(addressJsonObj.get("country").toString().replaceAll("^\"|\"$", ""));
        List<CtdbLookup> countryList = (List<CtdbLookup>) session.get("__SiteHome_countries");
		CtdbLookup country = getCtdbLookup(countryId, countryList); 
        address.setCountry(country);
        
        address.setZipCode(addressJsonObj.get("zipCode").toString().replaceAll("^\"|\"$", ""));	
        
		User user = getUser();
		address.setCreatedBy(user.getId());
		address.setUpdatedBy(user.getId());
        
        return address;
	}

	public ClinicalLocation convertJsonStrToClinicLocObj(String jsonStr){
		ClinicalLocation cl = new ClinicalLocation();
		JsonParser jsonParser = new JsonParser();
		JsonObject clJsonObj = jsonParser.parse(jsonStr).getAsJsonObject();
		String clIdStr = clJsonObj.get("id").toString().replaceAll("^\"|\"$", "");
		if(!clIdStr.equals("") &&
				Long.valueOf(clIdStr) > 0) {
			cl.setId(Integer.parseInt(clIdStr));
		}
		cl.setName(clJsonObj.get("name").toString().replaceAll("^\"|\"$", ""));
		cl.setStatus(clJsonObj.get("status").toString().replaceAll("^\"|\"$", ""));
		
		
		JsonObject addressJsonObj = clJsonObj.get("address").getAsJsonObject();
		Address address = convertJsonToAddressObj(addressJsonObj);
		cl.setAddress(address);
        
		return cl;
	}
		
	public String getProtoProcedureDTList() throws CtdbException {
		List<Procedure> protoProcedureList = new ArrayList<Procedure>();

		//creating a protocol, get procedure list from session adding from method addProtoLocation()
		List<Procedure> sessionProcList = (List<Procedure>) session.get("protoProcedureList");	
		if (sessionProcList != null) {
			protoProcedureList.addAll(sessionProcList);
		}
		
		try {
			IdtInterface idt = new Struts2IdtInterface();			
			ArrayList<Procedure> outputList = new ArrayList<Procedure>(protoProcedureList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ProtocolProcedureIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	public StreamResult addProtoProcedure() throws CtdbException {
		List<Procedure> sessionProcList = (List<Procedure>) session.get("protoProcedureList");
		if (sessionProcList == null) {
			sessionProcList = new ArrayList<Procedure>();
		}
		
		String jsonStr = this.getJsonString();
		List<Procedure> procArrFromJson = convertJsonStrToProcObj(jsonStr);
		sessionProcList.addAll(procArrFromJson);
		session.put("protoProcedureList", sessionProcList);
		
		return new StreamResult (new ByteArrayInputStream(BaseAction.SUCCESS.getBytes()));
	}
	
	public StreamResult deleteProtoProcedure() throws CtdbException {
		
		String jsonStr = this.getJsonString();
		List<Procedure> procToRemoveList = convertJsonStrToProcObj(jsonStr);
		
		List<Procedure> sessionLocList = (List<Procedure>) session.get("protoProcedureList");
		if (sessionLocList != null && sessionLocList.size() >= procToRemoveList.size()) {
			sessionLocList.removeAll(procToRemoveList);
		} else {
			return new StreamResult (new ByteArrayInputStream(StrutsConstants.EXCEPTION.getBytes()));
		}
		
		session.put("protoProcedureList", sessionLocList);
		return new StreamResult (new ByteArrayInputStream(BaseAction.SUCCESS.getBytes()));
	}
	
	public List<Procedure> convertJsonStrToProcObj(String jsonStr){
		List<Procedure> rtnLocArr = new ArrayList<Procedure>();
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(jsonStr);
		JsonArray procJsonArr = element.getAsJsonArray();
		
		if (procJsonArr != null && procJsonArr.size() > 0) {
			for (int i = 0; i < procJsonArr.size(); i++) {
				JsonObject procJsonObj = procJsonArr.get(i).getAsJsonObject();
				Procedure proc = new Procedure();
				String procIdStr = procJsonObj.get("id").toString().replaceAll("^\"|\"$", "");
				if(!procIdStr.equals("") && 
						Long.valueOf(procIdStr) > 0) {
					proc.setId(Integer.parseInt(procIdStr));
				}
				proc.setName(procJsonObj.get("name").toString().replaceAll("^\"|\"$", ""));
				ProcedureType procType = ProcedureType.getByName(procJsonObj.get("procedureTypeName").toString().replaceAll("^\"|\"$", ""));
				proc.setProcedureType(procType);
				proc.setIsNew(Boolean.valueOf(procJsonObj.get("isNew").toString().replaceAll("^\"|\"$", "")));
				
				rtnLocArr.add(proc);
			}
		}
		return rtnLocArr;
	}
	
	public String getProtoPointOfContactDTList() throws CtdbException {
		List<PointOfContact> protoPOCList = new ArrayList<PointOfContact>();

		//creating a protocol, get procedure list from session adding from method addProtoLocation()
		List<PointOfContact> sessionPOCList = (List<PointOfContact>) session.get("protoPointOfContactList");	
		if (sessionPOCList != null) {
			protoPOCList.addAll(sessionPOCList);
		}
		
		try {
			IdtInterface idt = new Struts2IdtInterface();			
			ArrayList<PointOfContact> outputList = new ArrayList<PointOfContact>(protoPOCList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ProtocolPointOfContactIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	public StreamResult addProtoPointOfContact() throws CtdbException {
		List<PointOfContact> sessionPOCList = (List<PointOfContact>) session.get("protoPointOfContactList");
		if (sessionPOCList == null) {
			sessionPOCList = new ArrayList<PointOfContact>();
		}
		
		String jsonStr = this.getJsonString();
		PointOfContact pocFromJson = convertJsonStrToPOCObj(jsonStr);
		sessionPOCList.add(pocFromJson);
		session.put("protoPointOfContactList", sessionPOCList);
		
		return new StreamResult (new ByteArrayInputStream(BaseAction.SUCCESS.getBytes()));		
	}

	public StreamResult editProtoPOintOfContact() throws CtdbException {
		List<PointOfContact> sessionPOCList = (List<PointOfContact>) session.get("protoPointOfContactList");
		if (sessionPOCList == null) {
			sessionPOCList = new ArrayList<PointOfContact>();
		}
		
		String jsonStr = this.getJsonString();
		PointOfContact pocFromJson = convertJsonStrToPOCObj(jsonStr);
		String editOrigJson = this.getEditOrigJsonStr();
		PointOfContact pocOrig = convertJsonStrToPOCObj(editOrigJson);
	
		for(PointOfContact poc : sessionPOCList){
			if(poc.equals(pocOrig)){
				poc.setFirstName(pocFromJson.getFirstName());
				poc.setMiddleName(pocFromJson.getMiddleName());
				poc.setLastName(pocFromJson.getLastName());
				poc.setPhone(pocFromJson.getPhone());
				poc.setEmail(pocFromJson.getEmail());
				poc.setAddress(pocFromJson.getAddress());
				poc.setPosition(pocFromJson.getPosition());
				poc.setStatus(pocFromJson.getStatus());
			}
		}
		session.put("protoPointOfContactList", sessionPOCList);
		
		return new StreamResult (new ByteArrayInputStream(BaseAction.SUCCESS.getBytes()));		
	}
	
	public StreamResult deleteProtoPointOfContact() throws CtdbException {
		List<PointOfContact> pocToRemoveList = new ArrayList<PointOfContact>();
		
		String jsonStr = this.getJsonString();
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(jsonStr);
		JsonArray jsonArray = element.getAsJsonArray();
		
		if(jsonArray != null && jsonArray.size() > 0){
			for(int i = 0; i < jsonArray.size(); i++) {
				JsonObject jsonObj = jsonArray.get(i).getAsJsonObject(); 
				PointOfContact pocToRemove = convertJsonStrToPOCObj(jsonObj.toString());
				pocToRemoveList.add(pocToRemove);
			}
		}
		
		List<PointOfContact> sessionPOCList = (List<PointOfContact>) session.get("protoPointOfContactList");
		if (sessionPOCList != null && sessionPOCList.size() >= pocToRemoveList.size()) {
			sessionPOCList.removeAll(pocToRemoveList);
		} else {
			return new StreamResult (new ByteArrayInputStream(StrutsConstants.EXCEPTION.getBytes()));
		}
		
		session.put("protoPointOfContactList", sessionPOCList);
		return new StreamResult (new ByteArrayInputStream(BaseAction.SUCCESS.getBytes()));
	}

	public PointOfContact convertJsonStrToPOCObj(String jsonStr){
		PointOfContact poc = new PointOfContact();
		JsonParser jsonParser = new JsonParser();
		JsonObject pocJsonObj = jsonParser.parse(jsonStr).getAsJsonObject();
		String pocIdStr = pocJsonObj.get("id").toString().replaceAll("^\"|\"$", "");
		if(!pocIdStr.equals("") &&
				Long.valueOf(pocIdStr) > 0) {
			poc.setId(Integer.parseInt(pocIdStr));
		}
		poc.setFirstName(pocJsonObj.get("firstName").toString().replaceAll("^\"|\"$", ""));
		poc.setMiddleName(pocJsonObj.get("middleName").toString().replaceAll("^\"|\"$", ""));
		poc.setLastName(pocJsonObj.get("lastName").toString().replaceAll("^\"|\"$", ""));
		poc.setPosition(pocJsonObj.get("position").toString().replaceAll("^\"|\"$", ""));
		poc.setPhone(pocJsonObj.get("phone").toString().replaceAll("^\"|\"$", ""));
		poc.setEmail(pocJsonObj.get("email").toString().replaceAll("^\"|\"$", ""));
		poc.setStatus(pocJsonObj.get("status").toString().replaceAll("^\"|\"$", ""));
		
		JsonObject addressJsonObj = pocJsonObj.get("address").getAsJsonObject();
		Address address = convertJsonToAddressObj(addressJsonObj);
        poc.setAddress(address);
		return poc;
	}
	
	public ProtocolForm getProtoForm() {
		return protoForm;
	}
	public void setProtoForm(ProtocolForm protoForm) {
		this.protoForm = protoForm;
	}

	public String getStudyId() {
		return studyId;
	}
	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}

	public String getSelectedBricsStudyId() {
		return selectedBricsStudyId;
	}
	public void setSelectedBricsStudyId(String selectedBricsStudyId) {
		this.selectedBricsStudyId = selectedBricsStudyId;
	}

	public String getSelectedSiteId() {
		return selectedSiteId;
	}
	public void setSelectedSiteId(String selectedSiteId) {
		this.selectedSiteId = selectedSiteId;
	}

	public String getSelectedDrugDeviceId() {
		return selectedDrugDeviceId;
	}
	
	public void setSelectedDrugDeviceId(String selectedDrugDeviceId) {
		this.selectedDrugDeviceId = selectedDrugDeviceId;
	}
	
	public String getEnableEsignatureYesno() {
		return enableEsignatureYesno;
	}
	public void setEnableEsignatureYesno(String enableEsignatureYesno) {
		this.enableEsignatureYesno = enableEsignatureYesno;
	}
	

	public String getReasonForEsignatureYesno() {
		return reasonForEsignatureYesno;
	}
	public void setReasonForEsignatureYesno(String reasonForEsignatureYesno) {
		this.reasonForEsignatureYesno = reasonForEsignatureYesno;
	}

	/**
	 * @return the jsonString
	 */
	public String getJsonString() {
		return jsonString;
	}

	/**
	 * @param jsonString the jsonString to set
	 */
	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
	
	/**
	 * @return the editOrigName
	 */
	public String getEditOrigName() {
		return editOrigName;
	}

	/**
	 * @param editOrigName the editOrigName to set
	 */
	public void setEditOrigName(String editOrigName) {
		this.editOrigName = editOrigName;
	}
	
	public String getProtoProceduresStr() {
		return protoProceduresStr;
	}
	
	public void setProtoProceduresStr(String protoProceduresStr){
		this.protoProceduresStr = protoProceduresStr;
	}
	
	public String getEditOrigJsonStr() {
		return editOrigJsonStr;
	}

	public void setEditOrigJsonStr(String editOrigJsonStr) {
		this.editOrigJsonStr = editOrigJsonStr;
	}
	
	public List<ProcedureType> getProcedureTypeList() {
		List<ProcedureType> procTypeNameList = Arrays.asList(ProcedureType.values());
		List<ProcedureType> procedureTypeList = new ArrayList<ProcedureType>();

		for(ProcedureType procTypeName : procTypeNameList){
			ProcedureType procedureType = ProcedureType.getByName(procTypeName.getName());
			procedureTypeList.add(procedureType);			
		}

		return procedureTypeList;
	}
	
	public String getProtoMilesStoneDTList() throws CtdbException {
		List<MilesStone> protoMilesStoneList = new ArrayList<MilesStone>();

		List<MilesStone> sessionMilesStoneList = (List<MilesStone>) session.get("protoMilesStoneList");	
		if (sessionMilesStoneList != null) {
			protoMilesStoneList.addAll(sessionMilesStoneList);
		}
		
		try {
			IdtInterface idt = new Struts2IdtInterface();			
			ArrayList<MilesStone> outputList = new ArrayList<MilesStone>(protoMilesStoneList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ProtocolMilesStoneIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: ", e);
		}
		
		return null;
	}
	
	public StreamResult addProtoMilesStone() throws CtdbException {
		List<MilesStone> sessionMilesStoneList = (List<MilesStone>) session.get("protoMilesStoneList");

		if (sessionMilesStoneList == null) {
			sessionMilesStoneList = new ArrayList<MilesStone>();
		}

		String jsonStr = this.getJsonString();
		MilesStone mStoneFromJson = convertJsonStrToMilesStoneObj(jsonStr);
		sessionMilesStoneList.add(mStoneFromJson);
		session.put("protoMilesStoneList", sessionMilesStoneList);

		return new StreamResult (new ByteArrayInputStream(BaseAction.SUCCESS.getBytes()));		
	}

	public StreamResult editProtoMilesStone() throws CtdbException {
		List<MilesStone> sessionMilesStoneList = (List<MilesStone>) session.get("protoMilesStoneList");

		if (sessionMilesStoneList == null) {
			sessionMilesStoneList = new ArrayList<MilesStone>();
		}

		String jsonStr = this.getJsonString();
		String editOrigName = this.getEditOrigName();
		MilesStone mSFromJson = convertJsonStrToMilesStoneObj(jsonStr);
	
		for(MilesStone ms : sessionMilesStoneList){
			if(ms.getName().equals(editOrigName)) {
				ms.setName(mSFromJson.getName());
				ms.setMilesStoneDate(mSFromJson.getMilesStoneDate());
			}
		}
		session.put("protoMilesStoneList", sessionMilesStoneList);
		
		return new StreamResult (new ByteArrayInputStream(BaseAction.SUCCESS.getBytes()));		
	}
	
	public StreamResult deleteProtoMilesStone() throws CtdbException {
		List<MilesStone> msToRemoveList = new ArrayList<MilesStone>();
		String jsonStr = this.getJsonString();
		JsonParser jsonParser = new JsonParser();
		JsonElement element = jsonParser.parse(jsonStr);
		JsonArray jsonArray = element.getAsJsonArray();
		
		if(jsonArray != null && jsonArray.size() > 0){
			for(int i = 0; i < jsonArray.size(); i++) {
				JsonObject jsonObj = jsonArray.get(i).getAsJsonObject(); 
				MilesStone msToRemove = convertJsonStrToMilesStoneObj(jsonObj.toString());
				msToRemoveList.add(msToRemove);
			}
		}
		
		List<MilesStone> sessionMilesStoneList = (List<MilesStone>) session.get("protoMilesStoneList");
		if (sessionMilesStoneList != null && sessionMilesStoneList.size() >= msToRemoveList.size()) {
			sessionMilesStoneList.removeAll(msToRemoveList);
		} else {
			return new StreamResult (new ByteArrayInputStream(StrutsConstants.EXCEPTION.getBytes()));
		}
		
		session.put("protoMilesStoneList", sessionMilesStoneList);
		
		return new StreamResult (new ByteArrayInputStream(BaseAction.SUCCESS.getBytes()));
	}
	
	public MilesStone convertJsonStrToMilesStoneObj(String jsonStr){
		MilesStone ms = new MilesStone();
		JsonParser jsonParser = new JsonParser();
		JsonObject msJsonObj = jsonParser.parse(jsonStr).getAsJsonObject();
		String msIdStr = msJsonObj.get("id").toString().replaceAll("^\"|\"$", "");
		
		if (!msIdStr.equals("") &&
				Long.valueOf(msIdStr) > 0) {
			ms.setId(Integer.parseInt(msIdStr));
		}
		
		ms.setName(msJsonObj.get("name").toString().replaceAll("^\"|\"$", ""));
		String dateStr = msJsonObj.get("milesStoneDate").getAsString();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date msDate = null;
		
		try {
			msDate = sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ms.setMilesStoneDate(new Timestamp(msDate.getTime()));
        
		return ms;
	}
}
