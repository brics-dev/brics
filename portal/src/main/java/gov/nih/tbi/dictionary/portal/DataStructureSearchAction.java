package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.FormStructureStandardization;
import gov.nih.tbi.dictionary.model.SessionDataStructureSearchCriteria;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.idt.ws.IdtColumnDescriptor;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.IdtRequest;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;
import gov.nih.tbi.taglib.datatableDecorators.AccessReportIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.DataStructureSearchIdtDecorator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;

public class DataStructureSearchAction extends BaseDictionaryAction {

	private static final long serialVersionUID = 3541164796943743957L;

	public static final String DATA_STRUCTURE_SEARCH_CRITERIA_KEY = "dataStructureSearchCriteriaKey";

	private String searchKey;
	private String exactMatch;
	private int ownerId;

	private String selectedRequiredOptions;
	private String selectedStatusOptions;
	private String selectedStandardizationOptions;
	private String selectedFormTypeOptions;
	private String selectedDiseaseOptions;
	private String selectedCopyrightOptions;

	private Map<String, List<String>> facets;
	private Map<String, List<String>> sessionFacets;
	private Map<String, List<String>> selectedFacets;

	private List<SemanticFormStructure> dataStructureList;

	private SessionDataStructureSearchCriteria sessionCriteria;
	
	private List<Disease> diseaseOptions;

	// These are variables passed from datatable
	
	private String sort;
	private Boolean ascending;

	// Request sequence number sent by DataTable, same value must be returned in response
	private Integer sEcho;
	private Integer draw;

	// Number of records that should be shown in table
	private Integer iDisplayLength;

	// First record that should be shown(used for paging)
	private Integer iDisplayStart;

	// Index of the column that is used for sorting
	private Integer iSortCol_0;

	// Sorting direction "asc" or "desc"
	private String sSortDir_0;

	private Integer numSearchResults;


	private static final Map<Integer, FormStructureFacet> fsDataTableDisplayMap;
	static {
		fsDataTableDisplayMap = new HashMap<Integer, FormStructureFacet>();
		fsDataTableDisplayMap.put(0, FormStructureFacet.TITLE);
		fsDataTableDisplayMap.put(1, FormStructureFacet.SHORT_NAME);
		fsDataTableDisplayMap.put(2, FormStructureFacet.STATUS);
		fsDataTableDisplayMap.put(3, FormStructureFacet.MODIFIED_DATE);
	}

	public String execute() {

		if (getSession().getAttribute(DATA_STRUCTURE_SEARCH_CRITERIA_KEY) != null) {
			sessionCriteria =
					(SessionDataStructureSearchCriteria) getSession().getAttribute(DATA_STRUCTURE_SEARCH_CRITERIA_KEY);

			this.setSearchKey(sessionCriteria.getSearchKey());
			this.setOwnerId(sessionCriteria.getOwnerId());

		} else {
			sessionCriteria = new SessionDataStructureSearchCriteria();
		}

		return null;
	}


	private Map<FormStructureFacet, Set<String>> buildSearchFacets() throws IOException {
		
		Map<FormStructureFacet, Set<String>> selectedFacets = new HashMap<FormStructureFacet, Set<String>>();

		if (!StringUtils.isBlank(selectedStatusOptions) && !selectedStatusOptions.equals("all")) {
			Set<String> statusStringSet = this.parseSelectedOptions(selectedStatusOptions);
			Set<String> statusSet = new HashSet<String>();
			for (String statusString : statusStringSet) {
				StatusType s = StatusType.statusOf(Long.parseLong(statusString));
				statusSet.add(s.getType());
			}

			if (!statusSet.isEmpty()) {
				selectedFacets.put(FormStructureFacet.STATUS, statusSet);
			}
		}
		
		if (!StringUtils.isBlank(selectedStandardizationOptions) && !selectedStandardizationOptions.equals("all")) {
			Set<String> standardizationStringSet = this.parseSelectedOptions(selectedStandardizationOptions);
			Set<String> standardizationSet = new HashSet<String>();
			for (String standardizationString : standardizationStringSet) {
				FormStructureStandardization s = FormStructureStandardization.getByName(standardizationString);
				String newName = RDFConstants.FS_STANDARDIZATION_URI + s.name();
				standardizationSet.add(newName);
			}

			if (!standardizationSet.isEmpty()) {
				selectedFacets.put(FormStructureFacet.STANDARDIZATION, standardizationSet);
			}
		}

		if (!StringUtils.isBlank(selectedFormTypeOptions) && !selectedFormTypeOptions.equals("all")) {
			Set<String> formTypeSet = this.parseSelectedOptions(selectedFormTypeOptions);
			if (!formTypeSet.isEmpty()) {
				selectedFacets.put(FormStructureFacet.SUBMISSION_TYPE, formTypeSet);
			}
		}

		if (!StringUtils.isBlank(selectedDiseaseOptions) && !selectedDiseaseOptions.equals("all")) {
			Set<String> diseaseStringSet = this.parseSelectedOptions(selectedDiseaseOptions);
			Set<String> diseaseSet = new HashSet<String>();
			List<Disease> diseases = getDiseaseOptions();
			Map<String, String> diseaseMap = new HashMap<String, String>();

			for (Disease disease : diseases) {
				diseaseMap.put(disease.getId().toString(), disease.getName());
			}

			for (String diseaseString : diseaseStringSet) {
				diseaseSet.add(diseaseMap.get(diseaseString));
			}

			if (!diseaseSet.isEmpty()) {
				selectedFacets.put(FormStructureFacet.DISEASE, diseaseSet);
			}
		}
		
		
		
		if (!StringUtils.isBlank(selectedRequiredOptions) && !selectedRequiredOptions.equals("all")) {
			Set<String> requiredStringSet = this.parseSelectedOptions(selectedRequiredOptions);
			if (!requiredStringSet.isEmpty()) {
				selectedFacets.put(FormStructureFacet.REQUIRED, requiredStringSet);
			}
		}
		
		if (!StringUtils.isBlank(selectedFormTypeOptions) && !selectedFormTypeOptions.equals("all")) {
			Set<String> formTypeSet = this.parseSelectedOptions(selectedFormTypeOptions);
			if (!formTypeSet.isEmpty()) {
				selectedFacets.put(FormStructureFacet.SUBMISSION_TYPE, formTypeSet);
			}
		}

		if (!StringUtils.isBlank(selectedCopyrightOptions) && !selectedCopyrightOptions.equals("all")) {
			Set<String> copyRightSet = this.parseSelectedOptions(selectedCopyrightOptions);
			if (!copyRightSet.isEmpty()) {
				selectedFacets.put(FormStructureFacet.IS_COPYRIGHTED, copyRightSet);
			}
		}

		return selectedFacets;
	}
	
	public String search() throws IOException {

		Set<String> searchTerms = new HashSet<String>();
		if (searchKey != null && !searchKey.isEmpty()) {
			searchTerms.add(searchKey);
		}

		boolean onlyOwned = false;
		Account account = null;
		String accountUrl = null;

		if (getInPublicNamespace()) {
			// public site: by default force search for published, awaiting publication and shared Draft
			if (StringUtils.isBlank(selectedStatusOptions) || selectedStatusOptions.equals("all")) {
				selectedStatusOptions = StatusType.PUBLISHED.getId() + "," + StatusType.SHARED_DRAFT.getId() + "," + StatusType.AWAITING_PUBLICATION.getId();
			}
			
			//public site: by default force search for Standard NINDS and Standard not NINDS
			//CM-735: Do NOT display the data for Standard Modified/Appendix/Unique FS on the Public Site
			if (StringUtils.isBlank(selectedStandardizationOptions)) {
				selectedStandardizationOptions = FormStructureStandardization.NINDSSTANDARD.getName() + "," + FormStructureStandardization.NINDSNOTSTANDARD.getName();
			}

			account = this.getAnonymousAccount();
			accountUrl = modulesConstants.getModulesAccountURL();

		} else {
			account = this.getAccount();
			accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			onlyOwned = (ownerId == 0);
		}

		Map<FormStructureFacet, Set<String>> selectedFacets = this.buildSearchFacets();

		PaginationData pageData = this.getPaginationData();

		try {
			String proxyTicket = PortalUtils.getProxyTicket(accountUrl);

			dataStructureList =
					dictionaryService.semanticFormStructureSearch(account, selectedFacets, searchTerms,
							Boolean.parseBoolean(exactMatch), onlyOwned, pageData, proxyTicket);

			numSearchResults = pageData.getNumSearchResults();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.saveSessionSearchCriteria(selectedFacets, searchTerms);

		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(this.buildSearchResultJson());
		out.flush();

		return null;
	}
	
	  public String searchList() throws IOException {
		    
		    Set<String> searchTerms = new HashSet<String>();
		    if (searchKey != null && !searchKey.isEmpty()) {
		      searchTerms.add(searchKey);
		    }

		    boolean onlyOwned = false;
		    Account account = null;
		    String accountUrl = null;
		    IdtInterface idt;
		    try {
		    	idt = new Struts2IdtInterface();
		    	IdtRequest request = idt.getRequest();
			    if (getInPublicNamespace()) {
			      // public site: by default force search for published, awaiting publication and shared Draft
			      if (StringUtils.isBlank(selectedStatusOptions) || selectedStatusOptions.equals("all")) {
			        selectedStatusOptions = StatusType.PUBLISHED.getId() + "," + StatusType.SHARED_DRAFT.getId() + "," + StatusType.AWAITING_PUBLICATION.getId();
			      }
			      
			      //public site: by default force search for Standard NINDS and Standard not NINDS
			      //CM-735: Do NOT display the data for Standard Modified/Appendix/Unique FS on the Public Site
			      if (StringUtils.isBlank(selectedStandardizationOptions)) {
			        selectedStandardizationOptions = FormStructureStandardization.NINDSSTANDARD.getName() + "," + FormStructureStandardization.NINDSNOTSTANDARD.getName();
			      }
	
			      account = this.getAnonymousAccount();
			      accountUrl = modulesConstants.getModulesAccountURL();
	
			    } else {
			      account = this.getAccount();
			      accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			      onlyOwned = (ownerId == 0);
			    }
	
			    Map<FormStructureFacet, Set<String>> selectedFacets = this.buildSearchFacets();
			    
			    updateAccessRecordOrder(request.getOrderColumn());
			    
				PaginationData pageData = new PaginationData();
				pageData.setPage((request.getStart() / request.getLength()) + 1);
				pageData.setPageSize(request.getLength());
				pageData.setAscending(ascending);
				pageData.setSort(sort);
				 	   
			      String proxyTicket = PortalUtils.getProxyTicket(accountUrl);
			      dataStructureList =
							dictionaryService.semanticFormStructureSearch(account, selectedFacets, searchTerms,
									Boolean.parseBoolean(exactMatch), onlyOwned, pageData, proxyTicket);
	
			      numSearchResults = pageData.getNumSearchResults();
			      idt.setTotalRecordCount(numSearchResults);
			      idt.setFilteredRecordCount(numSearchResults);
			    
			    this.saveSessionSearchCriteria(selectedFacets, searchTerms);
			    ArrayList<SemanticFormStructure> outputAr = new ArrayList<SemanticFormStructure>(dataStructureList);
			    idt.setList(outputAr);
			    idt.decorate(new DataStructureSearchIdtDecorator());
			    idt.output();

		    } catch (Exception e) {
		      e.printStackTrace();
		    }
		    return null;
		  }
	
	private void updateAccessRecordOrder(IdtColumnDescriptor orderColumn) {
		
		// these are intentionally NOT mapped 1:1
		sort = null;
		if (orderColumn != null) {
			sort = orderColumn.getData();
			
			ascending = orderColumn.getOrderDirection().equals(IdtRequest.ORDER_ASCENDING);
		}
	}

	private PaginationData getPaginationData() {

		if (sEcho == null) {
			sEcho = 1;
		}
		if (iDisplayStart == null) {
			iDisplayStart = 0;
		}
		if (iDisplayLength == null) {
			iDisplayLength = 10;
		}
		if (iSortCol_0 == null) {
			iSortCol_0 = 1;
		}

		PaginationData pageData = new PaginationData();
		pageData.setPage((iDisplayStart / iDisplayLength) + 1);
		pageData.setPageSize(iDisplayLength);
		pageData.setAscending("asc".equals(sSortDir_0));
		pageData.setSort(fsDataTableDisplayMap.get(iSortCol_0).getName());

		return pageData;
	}

	private String buildSearchResultJson() {

		if (sEcho == null) {
			sEcho = 1;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"sEcho\"").append(": ").append(sEcho).append(",");
		sb.append("\"iTotalRecords\"").append(": ").append(numSearchResults).append(",");
		sb.append("\"iTotalDisplayRecords\"").append(": ").append(numSearchResults).append(",");
		sb.append("\"aaData\"").append(": ").append("[");

		if (dataStructureList != null && !dataStructureList.isEmpty()) {
			int i = 0;
			for (SemanticFormStructure fs : dataStructureList) {
				
				String title = StringEscapeUtils.escapeJava(fs.getTitle());
				String desc = StringEscapeUtils.escapeJava(fs.getDescription());
				
				if (i > 0) {
					sb.append(", ");
				}
				sb.append("[");
				sb.append("\"")
						.append(fs.getShortName())
						.append("|")
						.append(title)
						.append("|")
						.append(desc != null ? desc : PortalConstants.EMPTY_STRING);
				sb.append("\",");
				sb.append("\"").append(fs.getShortName()).append("\",");
				sb.append("\"").append(fs.getStatus().getType()).append("\",");

				if (fs.getModifiedDate() != null)
					sb.append("\"").append(new SimpleDateFormat("yyyy-MM-dd").format(fs.getModifiedDate()))
							.append("\"");
				else
					sb.append("\"").append("UNKNOWN").append("\"");

				sb.append("]");
				i++;
			}
		}

		sb.append("]").append("}");

		return sb.toString();
	}

	public String list() throws MalformedURLException {

		getSessionDataStructure().clear();
		return PortalConstants.ACTION_LIST;
	}

	private Set<String> parseSelectedOptions(String options) {

		Set<String> optionSet = new HashSet<String>();
		if (options != null && !options.isEmpty()) {
			String[] optionArr = options.split(",");
			optionSet.addAll(Arrays.asList(optionArr));
		}

		return optionSet;
	}

	private void saveSessionSearchCriteria(Map<FormStructureFacet, Set<String>> selectedFacets, Set<String> searchTerms) {

		sessionCriteria = new SessionDataStructureSearchCriteria();
		sessionCriteria.setSearchTerms(searchTerms);
		sessionCriteria.setOwnerId(ownerId);
		sessionCriteria.setFormTypes(selectedFacets.get(FormStructureFacet.SUBMISSION_TYPE));
		sessionCriteria.setStatuses(selectedFacets.get(FormStructureFacet.STATUS));
		sessionCriteria.setStandardizations(selectedFacets.get(FormStructureFacet.STANDARDIZATION));
		sessionCriteria.setRequiredForm(selectedRequiredOptions);
		sessionCriteria.setDiseases(selectedFacets.get(FormStructureFacet.DISEASE));
		sessionCriteria.setIsCopyrighted(selectedCopyrightOptions);

		getSession().setAttribute(DATA_STRUCTURE_SEARCH_CRITERIA_KEY, sessionCriteria);
	}

	public String getSearchKey() {

		return searchKey;
	}

	public void setSearchKey(String searchKey) {

		this.searchKey = searchKey;
	}

	public String getExactMatch() {

		return exactMatch;
	}

	public void setExactMatch(String exactMatch) {

		this.exactMatch = exactMatch;
	}

	public int getOwnerId() {

		return ownerId;
	}

	public void setOwnerId(int ownerId) {

		this.ownerId = ownerId;
	}

	public String getSelectedStatusOptions() {

		return selectedStatusOptions;
	}
	
	public void setSelectedStatusOptions(String selectedStatusOptions) {

		this.selectedStatusOptions = selectedStatusOptions;
	}
	
	
	public String getSelectedStandardizationOptions() {

		return selectedStandardizationOptions;
	}
	
	public void setSelectedStandardizationOptions(String selectedStandardizationOptions) {

		this.selectedStandardizationOptions = selectedStandardizationOptions;
	}

	public String getSelectedFormTypeOptions() {

		return selectedFormTypeOptions;
	}

	public void setSelectedFormTypeOptions(String selectedFormTypeOptions) {

		this.selectedFormTypeOptions = selectedFormTypeOptions;
	}

	public List<Disease> getDiseaseOptions() throws MalformedURLException, UnsupportedEncodingException {

		if (diseaseOptions == null) {
			diseaseOptions = staticManager.getDiseaseList();
		}
		return diseaseOptions;
	}

	public String getSelectedDiseaseOptions() {

		return selectedDiseaseOptions;
	}

	public void setSelectedDiseaseOptions(String selectedDiseaseOptions) {

		this.selectedDiseaseOptions = selectedDiseaseOptions;
	}

	public Map<String, List<String>> getFacets() {

		return facets;
	}

	public void setFacets(Map<String, List<String>> facets) {

		this.facets = facets;
	}

	public Map<String, List<String>> getSessionFacets() {

		return sessionFacets;
	}

	public void setSessionFacets(Map<String, List<String>> sessionFacets) {

		this.sessionFacets = sessionFacets;
	}

	public Map<String, List<String>> getSelectedFacets() {

		return selectedFacets;
	}

	public void setSelectedFacets(Map<String, List<String>> selectedFacets) {

		this.selectedFacets = selectedFacets;
	}

	public List<SemanticFormStructure> getDataStructureList() {

		return dataStructureList;
	}

	public void setDataStructureList(List<SemanticFormStructure> dataStructureList) {

		this.dataStructureList = dataStructureList;
	}

	public SessionDataStructureSearchCriteria getSessionCriteria() {

		return sessionCriteria;
	}

	public void setSessionCriteria(SessionDataStructureSearchCriteria sessionCriteria) {

		this.sessionCriteria = sessionCriteria;
	}

	public Integer getsEcho() {

		return sEcho;
	}

	public void setsEcho(Integer sEcho) {

		this.sEcho = sEcho;
	}

	public int getiDisplayLength() {

		return iDisplayLength;
	}

	public void setiDisplayLength(int iDisplayLength) {

		this.iDisplayLength = iDisplayLength;
	}

	public int getiDisplayStart() {

		return iDisplayStart;
	}

	public void setiDisplayStart(int iDisplayStart) {

		this.iDisplayStart = iDisplayStart;
	}

	public int getiSortCol_0() {

		return iSortCol_0;
	}

	public void setiSortCol_0(int iSortCol_0) {

		this.iSortCol_0 = iSortCol_0;
	}

	public String getsSortDir_0() {

		return sSortDir_0;
	}

	public void setsSortDir_0(String sSortDir_0) {

		this.sSortDir_0 = sSortDir_0;
	}

	/**
	 * Filter action that can only return published data structures. For the public site.
	 * 
	 * @return String
	 * @throws IOException
	 */
	public String searchPublished() throws IOException {

		publicArea = true;
		return "searchPublished";
	}

	public String loadIFrame() {

		return PortalConstants.ACTION_IFRAME;
	}
		
	public List<Disease> getMajorDiseaseOptions() throws MalformedURLException,	UnsupportedEncodingException { 
		getDiseaseOptions();
		List<Disease> majorDiseaseList = new ArrayList<Disease>();
		for(Disease disease : diseaseOptions){
			if(disease.getIsMajor()){
				majorDiseaseList.add(disease);
			} 
		}
		return majorDiseaseList;
}

	public List<Disease> getMinorDiseaseOptions() throws MalformedURLException,	UnsupportedEncodingException {
		getDiseaseOptions();
		List<Disease> minorDiseaseList = new ArrayList<Disease>();
		for(Disease disease : diseaseOptions){
			if(!disease.getIsMajor()){
				minorDiseaseList.add(disease);
			}
		}
		return minorDiseaseList;
	}


	public String getSelectedRequiredOptions() {
		return selectedRequiredOptions;
	}


	public void setSelectedRequiredOptions(String selectedRequiredOptions) {
		this.selectedRequiredOptions = selectedRequiredOptions;
	}
	
	public String getOrgNameURI() {
    	return RDFConstants.FS_REQUIRED_URI + getOrgName();
    }
	
	public String getSelectedCopyRightOptions() {

		return selectedCopyrightOptions;
	}

	public void setSelectedCopyRightOptions(String selectedCopyrightOptions) {

		this.selectedCopyrightOptions = selectedCopyrightOptions;
	}
}
