package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.model.BaseDictionaryFacet;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.DataStructureList;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.FormStructureStandardization;
import gov.nih.tbi.dictionary.model.SessionDataElementSearchCriteria;
import gov.nih.tbi.dictionary.model.StringFacet;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.idt.ws.IdtColumnDescriptor;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.IdtRequest;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.taglib.datatableDecorators.DataElementSearchIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.DataStructureSearchIdtDecorator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.logout.LogoutFilter;


public class DataElementSearchAction extends BaseDataElementSearchAction {

	private static final long serialVersionUID = 3541164796943743957L;

	static Logger logger = Logger.getLogger(DataElementSearchAction.class);

	@Autowired
	DataElementDao deDao;

	private static final String DATA_ELEMENT_SEARCH_CRITERIA_KEY = "dataElementSearchCriteriaKey";

	// Active values are currently being selected/viewed on the domain leaf. Only one disease at a time but
	// multiple domains in a disease possible.
	private String activeDisease;

	// This will be used to pass the diseases selected from the base page to the popup.
	private List<String> selectedDiseasesFromBase;

	private List<Disease> diseaseOptions;
	private Map<String, String> domainOptions;
	private Map<String, String> subDomainOptions;
	// Key is subgroup and List is all classifications valid for that subgroup
	private Map<String, List<ClassificationElement>> classificationOptions;

	// This is grouped subDomain options for display purpose, key:
	// disease.domain, value: Sorted list of subDomainName
	private Map<String, List<String>> groupedSubDomains;

	// This is to flag if we are creating a data element
	private String createDataElement;

	private List<SemanticDataElement> dataElementList;

	private Set<Long> accessIdList;

	private SessionDataElementSearchCriteria sessionCriteria;

	private Integer numSearchResults;

	// These are variables passed from datatable

	// Request sequence number sent by DataTable, same value must be returned in response
	private String sort;
	private Boolean ascending;
	private Integer sEcho;

	// Number of records that should be shown in table
	private Integer iDisplayLength;

	// First record that should be shown(used for paging)
	private Integer iDisplayStart;

	// Index of the column that is used for sorting
	private Integer iSortCol_0;

	// Sorting direction "asc" or "desc"
	private String sSortDir_0;

	private static final Map<Integer, FacetType> deMappedDataTableDisplayMap;
	static {
		deMappedDataTableDisplayMap = new HashMap<Integer, FacetType>();
		deMappedDataTableDisplayMap.put(0, FacetType.TITLE);
		deMappedDataTableDisplayMap.put(1, FacetType.TITLE);
		deMappedDataTableDisplayMap.put(2, FacetType.PERMISSIONS);
		deMappedDataTableDisplayMap.put(3, FacetType.CATEGORY);
		deMappedDataTableDisplayMap.put(6, FacetType.CREATED_DATE);
		deMappedDataTableDisplayMap.put(4, FacetType.MODIFIED_DATE);
		deMappedDataTableDisplayMap.put(5, FacetType.STATUS);
	};

	private static final Map<Integer, FacetType> deDataTableDisplayMap;
	static {
		deDataTableDisplayMap = new HashMap<Integer, FacetType>();
		deDataTableDisplayMap.put(0, FacetType.TITLE);
		deDataTableDisplayMap.put(1, FacetType.PERMISSIONS);
		deDataTableDisplayMap.put(2, FacetType.CATEGORY);
		deDataTableDisplayMap.put(5, FacetType.CREATED_DATE);
		deDataTableDisplayMap.put(3, FacetType.MODIFIED_DATE);
		deDataTableDisplayMap.put(4, FacetType.STATUS);
	}

	public String execute() {

		if (getSession().getAttribute(DATA_ELEMENT_SEARCH_CRITERIA_KEY) != null) {
			sessionCriteria =
					(SessionDataElementSearchCriteria) getSession().getAttribute(DATA_ELEMENT_SEARCH_CRITERIA_KEY);

			this.setSearchKey(sessionCriteria.getSearchKey());
		} else {
			sessionCriteria = new SessionDataElementSearchCriteria();
		}

		return null;
	}

	public String search() throws IOException, UserPermissionException {
		
		DictionarySearchFacets facets = buildFacets();
		
		List<String> searchLocations = parseSelectedOptions(getDataElementLocations());
		Map<FacetType, Set<String>> searchKeywords = buildSearchKeywordsMap(searchLocations);

		PaginationData pageData = this.getPaginationData();
		
		if(!getInPublicNamespace()){
			searchDataELementPortalSearch(facets,searchLocations,searchKeywords,pageData);
		} else {
			searchDataElementPublicSite(facets, searchKeywords, pageData);
		}
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		try {
			out.print(this.buildSearchResultJson());
		} catch (DateParseException e) {
			e.printStackTrace();
			logger.error("Error occured while parsing a date for Json");
		}

		out.flush();

		return null;
	}
	
  public String searchList() throws IOException, UserPermissionException {
		List<String> searchLocations = parseSelectedOptions(getDataElementLocations());
		Map<FacetType, Set<String>> searchKeywords = buildSearchKeywordsMap(searchLocations);
	  	IdtInterface idt;
	  	try {
	  		
	  		idt = new Struts2IdtInterface();
	    	IdtRequest request = idt.getRequest();

			DictionarySearchFacets facets = buildFacets();
	
		    updateAccessRecordOrder(request.getOrderColumn());
		    
			PaginationData pageData = new PaginationData();
			pageData.setPage((request.getStart() / request.getLength()) + 1);
			pageData.setPageSize(request.getLength());
			pageData.setAscending(ascending);
			pageData.setSort(sort);
			
			if(!getInPublicNamespace()){
				searchDataELementPortalSearch(facets,searchLocations,searchKeywords,pageData);
			} else {
				searchDataElementPublicSite(facets, searchKeywords, pageData);
			}			
			
		    idt.setTotalRecordCount(dataElementTotalCount());
		    idt.setFilteredRecordCount(numSearchResults);
			ArrayList<SemanticDataElement> outputAr = new ArrayList<SemanticDataElement>(dataElementList);
			idt.setList(outputAr);
			idt.decorate(new DataElementSearchIdtDecorator());
			idt.output();		
	  	}catch (Exception e) {
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
	
	private int dataElementTotalCount() {
		boolean onlyOwned = (getOwnerId() == 1);

		return dictionaryService.semanticDataElementSearchCount(new DictionarySearchFacets(), new HashMap<FacetType, Set<String>>(), false, onlyOwned);
		
	}
		
	
	private void searchDataELementPortalSearch(DictionarySearchFacets facets,List<String> searchLocations, Map<FacetType, Set<String>> searchKeywords, PaginationData pageData){
		boolean onlyOwned = (getOwnerId() == 1);
		Account account = this.getAccount();
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		//The function DictionaryService.semanticDataElementSearch  makes 2 calls to the account web
		// service
		String[] twoProxyTickets =PortalUtils.getMultipleProxyTickets(accountUrl, 2);

		dataElementList = dictionaryService.semanticDataElementSearch(account, facets, searchKeywords, Boolean.parseBoolean(exactMatch), onlyOwned, pageData, twoProxyTickets);
		numSearchResults = pageData.getNumSearchResults();
		this.saveSessionSearchCriteria(facets, searchLocations);
	}
	
	
	//need to pass parameters in to the site
	// need to validate i didn't break dis shizzle with the public site
	//
	private void searchDataElementPublicSite(DictionarySearchFacets facets, Map<FacetType,Set<String>> searchKeywords, PaginationData pageData) throws UserPermissionException{
		if (getInPublicNamespace() && getSelectedStatuses().contains(DataElementStatus.DRAFT.getName())) {
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}
		dataElementList = dictionaryService.semanticDataElementSearchNoPermissions(facets,searchKeywords,pageData, Boolean.parseBoolean(exactMatch));
		numSearchResults = pageData.getNumSearchResults();
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
		if ((this.getMapped() != null && this.getMapped().equals("true")) || getInAdmin()) {
			pageData.setSort(deMappedDataTableDisplayMap.get(iSortCol_0).getName());
		} else {
			pageData.setSort(deDataTableDisplayMap.get(iSortCol_0).getName());
		}

		return pageData;
	}

	private String buildSearchResultJson() throws DateParseException {

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"sEcho\"").append(": ").append(sEcho).append(",");
		sb.append("\"iTotalRecords\"").append(": ").append(numSearchResults).append(",");
		sb.append("\"iTotalDisplayRecords\"").append(": ").append(numSearchResults).append(",");
		sb.append("\"aaData\"").append(": ").append("[");

		if (dataElementList != null && !dataElementList.isEmpty()) {
			int i = 0;
			for (SemanticDataElement de : dataElementList) {
				
				String title = StringEscapeUtils.escapeJava(de.getTitle());
				String desc = StringEscapeUtils.escapeJava(de.getDescription());
				
				if (i > 0) {
					sb.append(", ");
				}
				sb.append("[");
				if (this.getMapped().equals("true") || getInAdmin()) {
					sb.append("\"").append(de.getName()).append("\",");
				}
				sb.append("\"")
						.append(de.getName())
						.append("|")
						.append(title)
						.append("|")
						.append(desc != null ? desc : PortalConstants.EMPTY_STRING)
						.append("\",");
				sb.append("\"").append(de.getName()).append("\",");

				// None of these fields should ever be null. This is only put in for early RDF graph development.
				if (de.getCategory() != null) {
					String categoryType = (de.getCategory().getName().equals("Unique Data Element")) ? "UDE" : "CDE";
					sb.append("\"").append(categoryType).append("\",");
				} else {
					sb.append("\"").append("UNKNOWN").append("\",");
				}

				// r.s. this needs to changed to getModifiedDate
				if (de.getModifiedDate() != null) {
					sb.append("\"").append(BRICSTimeDateUtil.formatDate(de.getModifiedDate())).append("\",");
				} else {
					sb.append("\"").append("UNKNOWN").append("\",");
				}
				if (de.getStatus() != null) {
					sb.append("\"").append(de.getStatus().getName()).append("\"");
				} else {
					sb.append("\"").append("UNKNOWN").append("\"");
				}
				sb.append("]");
				i++;
			}
		}

		sb.append("]").append("}");

		String returnValue = sb.toString();
		return returnValue;
	}

	public String list() throws MalformedURLException {

		getSessionDataStructure().clear();
		return PortalConstants.ACTION_LIST;
	}

	private void saveSessionSearchCriteria(DictionarySearchFacets facets, List<String> searchLocations) {

		sessionCriteria = new SessionDataElementSearchCriteria();

		sessionCriteria.setSearchKey(searchKey);
		sessionCriteria.setSearchLocations(searchLocations);
		sessionCriteria.setOwnerId(getOwnerId());
		sessionCriteria.setModifiedDate(getModifiedDate());

		BaseDictionaryFacet statusFacet = facets.getByType(FacetType.STATUS);
		BaseDictionaryFacet elementTypesFacet = facets.getByType(FacetType.CATEGORY);
		BaseDictionaryFacet populationFacet = facets.getByType(FacetType.POPULATION);

		if (elementTypesFacet != null && elementTypesFacet instanceof StringFacet) {
			sessionCriteria.setStatuses(((StringFacet) elementTypesFacet).getValues());
		}

		if (populationFacet != null && populationFacet instanceof StringFacet) {
			sessionCriteria.setElementTypes(((StringFacet) populationFacet).getValues());
		}

		if (statusFacet != null && statusFacet instanceof StringFacet) {
			sessionCriteria.setPopulations(((StringFacet) statusFacet).getValues());
		}

		sessionCriteria.setDiseases(this.parseSelectedOptions(getSelectedDiseases()));
		sessionCriteria.setDomains(this.parseSelectedOptions(getSelectedDomains()));
		sessionCriteria.setSubdomains(this.parseSelectedOptions(getSelectedSubdomains()));
		sessionCriteria.setClassifications(this.parseSelectedOptions(getSelectedClassifications()));

		getSession().setAttribute(DATA_ELEMENT_SEARCH_CRITERIA_KEY, sessionCriteria);
	}

	public String updateDisease() {

		if (selectedDiseasesFromBase == null && getSelectedDiseases() != null) {
			selectedDiseasesFromBase = this.parseSelectedOptions(getSelectedDiseases());
		}
		return "disease";
	}

	/**
	 * Given the currently selected disease, this function will set the value of classificationOptions to contain all
	 * the possible classifications for the activeDisease. The results will be different based on if the user is an
	 * admin.
	 * 
	 * @return A string that will load the classification list jsp
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public String updateClassifications() throws MalformedURLException, UnsupportedEncodingException {

		classificationOptions = new HashMap<String, List<ClassificationElement>>();
		if (activeDisease != null && !activeDisease.isEmpty()) {
			Disease disease = staticManager.getDiseaseByName(activeDisease);
			List<Subgroup> subgroups = staticManager.getSubgroupsByDisease(disease);
			List<Classification> classifications = staticManager.getClassificationList(disease, true);
			for (Subgroup s : subgroups) {
				List<ClassificationElement> oneSubgroup = new ArrayList<ClassificationElement>();
				for (Classification c : classifications) {
					oneSubgroup.add(new ClassificationElement(disease, c, s));
				}
				classificationOptions.put(s.getSubgroupName(), oneSubgroup);
			}
		}

		return "classification";
	}

	public String updateDomain() throws MalformedURLException, UnsupportedEncodingException {

		domainOptions = new HashMap<String, String>();

		if (getSelectedDiseases() != null && !getSelectedDiseases().isEmpty()) {
			List<String> diseaseList = this.parseSelectedOptions(getSelectedDiseases());

			for (String diseaseSelection : diseaseList) {

				Disease disease = staticManager.getDiseaseByName(diseaseSelection);
				List<Domain> domainList = dictionaryManager.getDomainsByDisease(disease);

				for (Domain domain : domainList) {
					domainOptions.put(domain.getName(), disease.getName() + "." + domain.getName());
				}
			}
		}

		return "domain";
	}

	public String updateSubDomain() throws MalformedURLException, UnsupportedEncodingException {

		subDomainOptions = new HashMap<String, String>();
		groupedSubDomains = new HashMap<String, List<String>>();

		if (getSelectedDiseases() != null && !getSelectedDiseases().isEmpty() && getSelectedDiseases() != null
				&& !getSelectedDiseases().isEmpty()) {

			List<Disease> diseaseSelections = new ArrayList<Disease>();
			List<Domain> domainSelections = new ArrayList<Domain>();

			if (getSelectedDiseases().equals("all")) {
				diseaseSelections.addAll(this.getDiseaseOptions());
			} else {
				List<String> diseaseNames = this.parseSelectedOptions(getSelectedDiseases());
				for (String diseaseName : diseaseNames) {
					Disease disease = staticManager.getDiseaseByName(diseaseName);
					diseaseSelections.add(disease);
				}
			}

			if (getSelectedDomains().equals("all")) {
				for (String domainName : this.getDomainOptions().keySet()) {
					Domain domain = staticManager.getDomainByName(domainName);
					domainSelections.add(domain);
				}
			} else {
				// selectedDomains is in the format of
				// diseaseName1.domainName1,diseaseName1.domainName2,...
				List<String> concatNames = this.parseSelectedOptions(getSelectedDomains());
				for (String concatName : concatNames) {
					String domainName = concatName.substring(concatName.indexOf(".") + 1);
					Domain domain = staticManager.getDomainByName(domainName);
					domainSelections.add(domain);
				}
			}

			for (Disease disease : diseaseSelections) {
				for (Domain domain : domainSelections) {
					String prefix = disease.getName() + "." + domain.getName() + ".";
					List<SubDomain> subDomainList = staticManager.getSubDomainsList(domain, disease);

					List<String> subDomainNames = new ArrayList<String>();
					for (SubDomain subDomain : subDomainList) {
						subDomainOptions.put(subDomain.getName(), prefix + subDomain.getName());
						subDomainNames.add(subDomain.getName());
					}

					Collections.sort(subDomainNames);
					groupedSubDomains.put(prefix, subDomainNames);
				}
			}
		}

		return "subDomain";
	}

	/**
	 * Retrieves a list of Categories for the the search page
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<Category> getCategoryOptions() throws MalformedURLException, UnsupportedEncodingException {

		return staticManager.getCategoryList();
	}

	public List<Disease> getDiseaseOptions() throws MalformedURLException, UnsupportedEncodingException {

		if (diseaseOptions == null) {
			diseaseOptions = staticManager.getDiseaseList();
		}
		return diseaseOptions;
	}

	public Map<String, List<ClassificationElement>> getClassificationOptions() {

		return classificationOptions;

	}

	public List<Population> getPopulationOptions() throws MalformedURLException, UnsupportedEncodingException {

		return staticManager.getPopulationList();
	}

	/**
	 * This method returns a list of all requiredTypes for populating form select options.
	 * 
	 * @return List<RequiredType>
	 */
	public List<RequiredType> getRequiredTypes() {

		List<RequiredType> requiredTypeList = new ArrayList<RequiredType>();
		requiredTypeList.add(RequiredType.REQUIRED);
		requiredTypeList.add(RequiredType.RECOMMENDED);
		requiredTypeList.add(RequiredType.OPTIONAL);

		return requiredTypeList;
	}

	// Key: domainName, Value: diseaseName.domainName
	public Map<String, String> getDomainOptions() throws MalformedURLException, UnsupportedEncodingException {

		if (domainOptions == null) {
			this.updateDomain();
		}
		return domainOptions;
	}

	// Key: subDomainName, Value: diseaseName.domainName.subDomainName
	public Map<String, String> getSubDomainOptions() throws MalformedURLException, UnsupportedEncodingException {

		return subDomainOptions;
	}

	public Map<String, List<String>> getGroupedSubDomains() {

		return groupedSubDomains;
	}

	/**
	 * Returns a list of Data Elements Ids the user has access to
	 * 
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public Set<Long> getAccessIdList() throws MalformedURLException, UnsupportedEncodingException {

		if (accessIdList == null) {
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider =
					new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			accessIdList =
					restProvider.listUserAccess(getAccount().getId(), EntityType.DATA_ELEMENT, PermissionType.READ,
							false);
		}

		return accessIdList;
	}

	public void setAccessIdList(Set<Long> accessIdList) {

		this.accessIdList = accessIdList;
	}

	public List<String> getSelectedDiseasesFromBase() {

		return selectedDiseasesFromBase;
	}

	public SessionDataElementSearchCriteria getSessionCriteria() {

		return sessionCriteria;
	}

	public void setSessionCriteria(SessionDataElementSearchCriteria sessionCriteria) {

		this.sessionCriteria = sessionCriteria;
	}

	public void setActiveDisease(String activeDisease) {

		this.activeDisease = activeDisease;
	}

	public String getActiveDisease() {

		return activeDisease;
	}

	public Integer getsEcho() {

		return sEcho;
	}

	public void setsEcho(Integer sEcho) {

		this.sEcho = sEcho;
	}

	public Integer getiDisplayLength() {

		return iDisplayLength;
	}

	public void setiDisplayLength(Integer iDisplayLength) {

		this.iDisplayLength = iDisplayLength;
	}

	public Integer getiDisplayStart() {

		return iDisplayStart;
	}

	public void setiDisplayStart(Integer iDisplayStart) {

		this.iDisplayStart = iDisplayStart;
	}

	public Integer getiSortCol_0() {

		return iSortCol_0;
	}

	public void setiSortCol_0(Integer iSortCol_0) {

		this.iSortCol_0 = iSortCol_0;
	}

	public String getsSortDir_0() {

		return sSortDir_0;
	}

	public void setsSortDir_0(String sSortDir_0) {

		this.sSortDir_0 = sSortDir_0;
	}

	public String getCreateDataElement() {

		return createDataElement;
	}

	public void setCreateDataElement(String createDataElement) {

		this.createDataElement = createDataElement;
	}

	public String wsTest() {

		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		if (searchKey.contains("-dd-")) {
			accountUrl = modulesConstants.getModulesDDTURL(getDiseaseId());
		}
		String proxyTicket = PortalUtils.getProxyTicket(accountUrl);
		String url = searchKey + proxyTicket;
		logger.info("ticket: " + proxyTicket);
		logger.info("url: " + url);
		logger.info("acccount:" + getAccount().getUserName());
		logger.info("diseaseID: " + getDiseaseId());

		WebClient client = WebClient.create(url);

		DataStructureList basicDataStructureList = client.accept(MediaType.TEXT_XML).get(DataStructureList.class);

		return null;
	}

	public String loadIFrame() {
		getResponse().setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
		return PortalConstants.ACTION_IFRAME;
	}

	/**
	 * Because the public search page is outside a namespace, we need to enter the dictionary namespace with an ajax
	 * call
	 * 
	 * @return
	 */
	public String loadPublicSearch() {
		getResponse().setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
		return PortalConstants.ACTION_LOAD;
	}

	public Integer getNumSearchResults() {

		return numSearchResults;
	}

	public void setNumSearchResults(Integer numSearchResults) {

		this.numSearchResults = numSearchResults;
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
	
	public String getActionName() {
	    return PortalConstants.ACTION_DE_EVENT_LOG_DOC;
	}
	
	
	

}
