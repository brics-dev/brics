package gov.nih.tbi.dictionary.ws;

import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.RestAuthenticationProvider;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.CategoryList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.ClassificationList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.DataStructureList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.DiseaseList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.DomainList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.KeywordList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.PopulationList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.SemanticFormStructureList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.StringWrapper;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.SubDomainList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.SubgroupList;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.UserFileWrapper;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

public class RestDictionaryProvider extends RestAuthenticationProvider {

	/***************************************************************************************************/

	private static Logger logger = Logger.getLogger(RestAccountProvider.class);
	protected final String restServiceUrl = "portal/ws/ddt/dictionary";
	protected final String accRestServiceUrl = "portal/ws/account/account/";

	/***************************************************************************************************/

	/**
	 * A constructor for a rest provider in which the serverLocation provided is the domain of the web service call
	 * being made. In the case that any path information other than a domain (such as /portal), that information is
	 * stripped.
	 * 
	 * A proxyTicket provided can only be used one time. If RestAccountProvider attempts to use the proxy ticket
	 * multiple times, an exception is thrown. If a null or blank string is provided, then public calls may be made.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @throws MalformedURLException
	 */
	public RestDictionaryProvider(String serverLocation, String proxyTicket) {

		super(serverLocation, proxyTicket);
		// TODO Auto-generated constructor stub
	}
	
	public SemanticFormStructureList getPublishedDataStructures() throws UnsupportedEncodingException {
		ticketValid();
		String url = serverUrl + restServiceUrl  + "/FormStructure/Published/listAll?page=1&incDEs=false&pageSize=10000&ascending=false&sort=shortName&filterId=2";
		url += "?" + getTicketProperty();
 		WebClient client = WebClient.create(url);
 		SemanticFormStructureList structures = client.accept("text/xml").get(SemanticFormStructureList.class);
		cleanup();
		return structures;
	}
	
	public SemanticFormStructureList getPublishedAndAwaitingFS() throws UnsupportedEncodingException {
		ticketValid();
		String url = serverUrl + restServiceUrl  + "/FormStructure/byStatus?status=Published,Awaiting%20Publication";
		url += "&" + getTicketProperty();
 		WebClient client = WebClient.create(url);
 		SemanticFormStructureList structures = client.accept("text/xml").get(SemanticFormStructureList.class);
		cleanup();
		return structures;
	}
	
	
	//unauthenticated because it is used by the public site
	public SemanticFormStructureList getLatestFormStructureByIdSet(Set<Long> dataStructureIds) throws UnsupportedEncodingException {
		String url = serverUrl + restServiceUrl  + "/FormStructure/public/details";
 		WebClient client = WebClient.create(url);
 		client.query("formStructureList", dataStructureIds);
 		SemanticFormStructureList structures = client.accept("text/xml").get(SemanticFormStructureList.class);
		return structures;
	}

	/**
	 * Returns a DataStructure using it's corresponding id number
	 * 
	 * @param dataStructureId
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public FormStructure getDataStructureDetailsById(Long dataStructureId) throws UnsupportedEncodingException {

		ticketValid();
		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "/FormStructure/dataLoader/" + dataStructureId + "?"
						+ getTicketProperty());
		FormStructure dataStructure = client.accept("text/xml").get(FormStructure.class);
		cleanup();
		return dataStructure;
	}

	public FormStructure getLatestDataStructureByName(String name) throws UnsupportedEncodingException {

		ticketValid();
		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "/FormStructure/latest/" + name + "?"
						+ getTicketProperty());
		FormStructure dataStructure = client.accept("text/xml").get(FormStructure.class);
		cleanup();
		return dataStructure;
	}

	/**
	 * Returns a DataStructure using it's corresponding id number
	 * 
	 * @param dataStructureId
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public FormStructure getFormStructureFirstVersion(String shortName) throws UnsupportedEncodingException {

		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/FormStructure/First/" + shortName);
		// +"?" + getTicketProperty()
		FormStructure dataStructure = client.accept("text/xml").get(FormStructure.class);
		cleanup();
		return dataStructure;
	}

	/**
	 * Returns a list dataStructures after passing a list of AbstractData as xml.
	 * 
	 * @param dataStructureList
	 * @return
	 */
	public List<FormStructure> getDataStructuresDetails(List<FormStructure> dataStructureList) {

		ticketValid();
		cleanup();
		return null;
	}

	/*
	 * returns a list of form structures that
	 */
	public List<FormStructure> getAttachedDataStructure(String deName, String deVersion)
			throws UnsupportedEncodingException {
		ticketValid();
		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "/DataElement/LinkedFormStructures/" + deName + "/"
						+ deVersion + "?" + getTicketProperty());
		DataStructureList dsl = client.accept("text/xml").get(DataStructureList.class);
		cleanup();
		return dsl.getList();
	}

	public FormStructure getDataStructureDetailsByShortName(String shortName, String version)
			throws UnsupportedEncodingException {

		ticketValid();
		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "/FormStructure/" + shortName + "/" + version + "?"
						+ getTicketProperty());
		FormStructure dataStructure = client.accept("text/xml").get(FormStructure.class);
		cleanup();
		return dataStructure;
	}

	/*******************************************************
	 * 
	 * 
	 * Elements
	 * 
	 * @throws UnsupportedEncodingException
	 * 
	 * @throws MalformedURLException
	 * 
	 * 
	 *******************************************************/

	public MapElement getMapElementById(Long mapElementId) throws UnsupportedEncodingException {

		ticketValid();
		WebClient client =
				WebClient
						.create(serverUrl + restServiceUrl + "/MapElement/" + mapElementId + "?" + getTicketProperty());
		MapElement mapElement = client.accept("text/xml").get(MapElement.class);
		cleanup();
		return mapElement;
	}

	public DataElement getDataElementById(Long dataElementId) throws UnsupportedEncodingException {

		ticketValid();
		logger.debug("public ws URL that gets a DE " + serverUrl + restServiceUrl + "/DataElement/" + dataElementId
				+ "?" + getTicketProperty());
		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "/DataElement/" + dataElementId + "?"
						+ getTicketProperty());
		DataElement dataElement = client.accept("text/xml").get(DataElement.class);
		cleanup();
		return dataElement;
	}

	public DataElement getDataElementByName(String name) throws UnsupportedEncodingException {

		ticketValid();
		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "/DataElement/name/" + name + "?" + getTicketProperty());
		DataElement dataElement = client.accept("application/xml").get(DataElement.class);
		cleanup();
		return dataElement;
	}

	public List<FormStructure> getDataStructureDetailsByIds(List<Long> dsIdList) throws UnsupportedEncodingException {

		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/FormStructure/details");
		for (Long dsId : dsIdList) {
			client.query("dsId", dsId);
		}
		String ac = client.getCurrentURI().toString();
		WebClient.create(ac + "&" + getTicketProperty());
		DataStructureList dsl = client.accept("text/xml").get(DataStructureList.class);
		cleanup();
		return dsl.getList();
	}

	public List<String> getFormStructureNamesByIds(List<Long> dsIdList) throws UnsupportedEncodingException {

		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/FormStructure/names");
		for (Long dsId : dsIdList) {
			client.query("dsId", dsId);
		}
		String ac = client.getCurrentURI().toString();
		WebClient.create(ac + "&" + getTicketProperty());
		DataStructureList dsl = client.accept("text/xml").get(DataStructureList.class);

		List<String> nameList = new ArrayList<String>();

		for (FormStructure form : dsl.getList()) {
			nameList.add(form.getShortName());
		}

		cleanup();
		return nameList;
	}

	public Integer searchDataElementsCount(String diseaseSelection, Boolean generalSearch, String domainSelection,
			String subDomainSelection, String populationSelection, String subgroupSelection,
			String classificationSelection, Long filterId, Category category, String searchKey, String exactMatch,
			Integer page, Integer pageSize, Boolean ascending, String sort, String searchLocations)
			throws UnsupportedEncodingException {

		// TODO: Change to use string builder and URL encoder
		ticketValid();

		StringBuilder builder = new StringBuilder();

		// List of variables with their respective values
		String[] variables =
				{"disease", "general", "domain", "subDomain", "population", "subGroup", "classification", "filterId",
						"elementType", "searchKey", "exactMatch", "page", "pageSize", "ascending", "sort",
						"searchLocations"};
		String[] parameters =
				{diseaseSelection, (generalSearch != null) ? generalSearch.toString() : null, domainSelection,
						subDomainSelection, populationSelection, subgroupSelection, classificationSelection,
						(filterId != null) ? filterId.toString() : null,
						(category != null && category.getId() != null) ? category.getId().toString() : null, searchKey,
						exactMatch, (page != null) ? page.toString() : null,
						(pageSize != null) ? pageSize.toString() : null,
						(ascending != null) ? ascending.toString() : null, sort, searchLocations};

		builder.append(serverUrl).append(restServiceUrl).append("/DataElement/count")
				.append(buildParameters(variables, parameters, getTicketProperty()));

		String webServiceUrl = builder.toString();

		logger.debug(builder.toString());

		WebClient client = WebClient.create(webServiceUrl);
		Integer count = client.accept("text/xml").get(Integer.class);
		cleanup();
		return count;
	}


/*
 	public List<DataElement> searchDataElements(String diseaseSelection, Boolean generalSearch, String domainSelection,
			String subDomainSelection, String populationSelection, String subgroupSelection,
			String classificationSelection, Long filterId, Category category, String searchKey, String exactMatch,
			Integer page, Integer pageSize, Boolean ascending, String sort, String searchLocations)
			throws UnsupportedEncodingException {

		// TODO: Change to use string builder and URL encoder
		ticketValid();

		StringBuilder builder = new StringBuilder();

		// List of variables with their respective values
		String[] variables =
				{"disease", "general", "domain", "subDomain", "population", "subGroup", "classification", "filterId",
						"elementType", "searchKey", "exactMatch", "page", "pageSize", "ascending", "sort",
						"searchLocations"};
		String[] parameters =
				{diseaseSelection, (generalSearch != null) ? generalSearch.toString() : null, domainSelection,
						subDomainSelection, populationSelection, subgroupSelection, classificationSelection,
						(filterId != null) ? filterId.toString() : null,
						(category != null && category.getId() != null) ? category.getId().toString() : null, searchKey,
						exactMatch, (page != null) ? page.toString() : null,
						(pageSize != null) ? pageSize.toString() : null,
						(ascending != null) ? ascending.toString() : null, sort, searchLocations};

		for (String parameter : parameters) {
			logger.debug("Here is a list of parameters being passed: " + parameter);
		}

		builder.append(serverUrl).append(restServiceUrl).append("/DataElement/list")
				.append(buildParameters(variables, parameters, getTicketProperty()));

		// need to put this back into debug mode. needed it for testing.
		logger.debug("This is the string being sent : " + builder.toString());

		String webServiceUrl = builder.toString();
		WebClient client = WebClient.create(webServiceUrl);
		DataElementList dsl = client.accept("text/xml").get(DataElementList.class);
		cleanup();
		logger.debug("public list of DEs: " + dsl.getList().size());
		return dsl.getList();
	}
*/
	
	
	public List<Domain> getDomainList() throws UnsupportedEncodingException {

		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/Domain?" + getTicketProperty());
		DomainList dl = client.accept("text/xml").get(DomainList.class);
		cleanup();
		return dl.getList();
	}

	public List<Disease> getDiseaseList() throws UnsupportedEncodingException {

		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/Disease?" + getTicketProperty());
		DiseaseList dl = client.accept("text/xml").get(DiseaseList.class);
		cleanup();
		return dl.getList();
	}

	public List<Subgroup> getSubgroupList() throws UnsupportedEncodingException {

		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/Subgroups?" + getTicketProperty());
		SubgroupList sgl = client.accept("text/xml").get(SubgroupList.class);
		cleanup();
		return sgl.getList();
	}

	public List<Population> getPopulationList() throws UnsupportedEncodingException {

		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/Population?" + getTicketProperty());
		PopulationList pl = client.accept("text/xml").get(PopulationList.class);
		cleanup();
		return pl.getList();
	}

	public List<Category> getCategoryList() throws UnsupportedEncodingException {

		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/Category?" + getTicketProperty());
		CategoryList cl = client.accept("text/xml").get(CategoryList.class);
		cleanup();
		return cl.getList();
	}

	public String getDiseasePrefix(Long diseaseId) throws UnsupportedEncodingException {

		ticketValid();
		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "/Disease/" + diseaseId + "?" + getTicketProperty());
		String stringPrefix = client.accept("text/xml").get(String.class);
		cleanup();
		return stringPrefix;
	}

	public List<Classification> getClassificationList(Disease disease, boolean isAdmin) throws MalformedURLException,
			UnsupportedEncodingException {

		ticketValid();
		logger.debug(serverUrl + restServiceUrl + "/Classification/" + disease.getId() + "?isAdmin=" + isAdmin + "&"
				+ getTicketProperty());
		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "/Classification/" + disease.getId() + "?isAdmin="
						+ isAdmin + "&" + getTicketProperty());
		ClassificationList cl = client.accept("text/xml").get(ClassificationList.class);
		cleanup();
		return cl.getList();
	}

	public List<Subgroup> getSubgroupsByDisease(Disease disease) throws MalformedURLException,
			UnsupportedEncodingException {

		ticketValid();
		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "/Subgroups/" + disease.getId() + "?"
						+ getTicketProperty());
		SubgroupList sgl = client.accept("text/xml").get(SubgroupList.class);
		cleanup();
		return sgl.getList();
	}

	public List<Domain> getDomainsByDisease(Disease disease) throws MalformedURLException, UnsupportedEncodingException {

		ticketValid();
		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "/Domain/" + disease.getId() + "?" + getTicketProperty());
		DomainList dl = client.accept("text/xml").get(DomainList.class);
		cleanup();
		return dl.getList();
	}

	/**
	 * Query Parameters Domain={Domain}&Diesease={Disease} This provider does not append a ticket to the call because
	 * the data is public and the service does not verify the user.
	 * 
	 * @param domain
	 * @param disease
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */

	public List<SubDomain> getSubDomainsList(Domain domain, Disease disease) throws MalformedURLException,
			UnsupportedEncodingException {

		ticketValid();
		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "/SubDomain/list?domainId=" + domain.getId()
						+ "&diseaseId=" + disease.getId());
		SubDomainList sdl = client.accept("text/xml").get(SubDomainList.class);
		cleanup();
		return sdl.getList();

	}

	public Set<Keyword> getKeywords(Long dataElementId) throws UnsupportedEncodingException {

		ticketValid();

		WebClient client;

		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {
			client =
					WebClient.create(serverUrl + restServiceUrl + "/DataElement/" + dataElementId + "/getKeywords?"
							+ getTicketProperty());
		} else {
			client = WebClient.create(serverUrl + restServiceUrl + "/DataElement/" + dataElementId + "/getKeywords");
		}

		cleanup();

		return client.accept("text/xml").get(KeywordList.class).getList();
	}

	/**
	 * 
	 * TODO Once the Repository Rest Web Service is created, this method should be put in there.
	 * 
	 * @param fileId
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String getDocumentName(Long fileId) throws UnsupportedEncodingException {

		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/Repository/Documents/getName/" + fileId);
		String documentName = client.accept("text/xml").get(StringWrapper.class).getStr();
		cleanup();
		return documentName;

	}

	/**
	 * 
	 * TODO Once the Repository Rest Web Service is created, this method should be put in there.
	 * 
	 * @param fileId
	 * @return
	 */
	public UserFile getDocument(Long fileId) {

		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/Repository/Documents/" + fileId);
		UserFile userFile = client.accept("text/xml").get(UserFileWrapper.class).getUserFile();
		cleanup();
		return userFile;

	}
}
