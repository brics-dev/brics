
package gov.nih.tbi.dictionary.service;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.commons.service.BaseManager;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.dictionary.service.rulesengine.model.RulesEngineException;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author mvalei
 * 
 */
public interface DictionaryServiceInterface extends BaseManager
{

    // SEARCH FUNCTIONS
    /**
     * Service level form structure search. Only returns semantic results. This function contains duplicate code from
     * compositeFormStructureSearch.
     * 
     * @param: account - The account performing the search. A null value means anonymous user
     * @param: selectedFacets - a mapping of facets to all the selected values to search on for that facet
     * @param: searchKeywords - A map of search locations (description, title etc) to a list of terms to search for in
     *         that field.
     * @param: onlyOwned - a value of true means that we only want to return entities in which the given account is the
     *         "owner". False or null mean all entities in which the user has read access.
     * @param: pageData - information on pagination. Should be passed to the DAO
     * @param: proxyTicket -
     */
    public List<SemanticFormStructure> semanticFormStructureSearch(Account account,
            Map<FormStructureFacet, Set<String>> selectedFacets, Set<String> searchTerms, 
            Boolean exactMatch, Boolean onlyOwned, PaginationData pageData, String proxyTicket);

    /**
     * Service level form structure search. Returns composite form structure results. This function contains duplicate
     * code from semanticFormStructureSearch.
     * 
     * @param: account - The account performing the search. A null value means anonymous user
     * @param: selectedFacets - a mapping of facets to all the selected values to search on for that facet
     * @param: searchKeywords - A map of search locations (description, title etc) to a list of terms to search for in
     *         that field.
     * @param: onlyOwned - a value of true means that we only want to return entities in which the given account is the
     *         "owner". False or null mean all entities in which the user has read access.
     * @param: pageData - information on pagination. Should be passed to the DAO
     * @param: proxyTicket -
     * @param: includeDEList - if true, then the data element list for each form structure will be populated.
     */
    public List<FormStructure> compositeFormStructureSearch(Account account,
            Map<FormStructureFacet, Set<String>> selectedFacets, Set<String> searchTerms, Boolean onlyOwned,
            Boolean exactMatch, PaginationData pageData, boolean includeDEList, String proxyTicket);

    /**
     * Service level data element search.
     * 
     * @param: account - The account performing the search. A null value means anonymous user
     * @param: selectedFacets - a mapping of facets to all the selected values to search on for that facet
     * @param: searchKeywords - A map of search locations (description, title etc) to a list of terms to search for in
     *         that field.
     * @param: onlyOwned - a value of true means that we only want to return entities in which the given account is the
     *         "owner". False or null mean all entities in which the user has read access.
     * @param: pageData - information on pagination. Should be passed to the DAO
     * @param: proxyTicket -
     */
    public List<SemanticDataElement> semanticDataElementSearch(Account account, DictionarySearchFacets facets,
            Map<FacetType, Set<String>> searchKeywords, boolean exactMatch, boolean onlyOwned, 
            PaginationData pageData, String[] proxyTickets);

    /**
     * Service level data element search.
     * 
     * @param: account - The account performing the search. A null value means anonymous user
     * @param: selectedFacets - a mapping of facets to all the selected values to search on for that facet
     * @param: searchKeywords - A map of search locations (description, title etc) to a list of terms to search for in
     *         that field.
     * @param: onlyOwned - a value of true means that we only want to return entities in which the given account is the
     *         "owner". False or null mean all entities in which the user has read access.
     * @param: pageData - information on pagination. Should be passed to the DAO
     * @param: proxyTicket -
     */
    public List<DataElement> compositeDataElementSearch(Account account, DictionarySearchFacets facets,
            Map<FacetType, Set<String>> searchKeywords, boolean exactMatch, boolean onlyOwned, 
            PaginationData pageData, String[] proxyTickets);
    
    /**
     * 
     * @param facets
     * @param searchKeywords
     * @param pageData
     * @param onlyOwned
     * @return
     * public site search will only 
     */
    public List<SemanticDataElement> semanticDataElementSearchNoPermissions(DictionarySearchFacets facets,
            Map<FacetType, Set<String>> searchKeywords, PaginationData pageData,boolean onlyOwned);

    // GET FUNCTIONS
    /**
     * Retrieves composite form structure object by short name and version. If shortname/version does not exist they
     * null is returned. READ access necessary or an exception will be thrown. Shortname, proxy ticket are required. If
     * no verison is supplied then the latest version is retrieved. If no account is provided then anonymous access will
     * be checked.
     * 
     * @param account
     * @param name
     * @param version
     *            (optional)
     * @param proxyTicket
     * @return
     * @throws UnsupportedEncodingException
     * @throws UserPermissionException
     * @throws UserAccessDeniedException 
     */
    public FormStructure getFormStructure(Account account, String name, String version, String proxyTicket)
            throws UnsupportedEncodingException, UserPermissionException, UserAccessDeniedException;

    /**
     * Retrieves composite data element object by short name. If the data element does not exist then null is returned.
     * READ access necessary or an exception will be thrown. Short name and proxyTicket are required. If no version is
     * supplied then the latest version is retrieved. If no account is provided then anonymous access will be checked.
     * 
     * @param account
     * @param name
     * @param version
     *            (optional)
     * @param proxyTicket
     * @return
     * @throws UnsupportedEncodingException
     * @throws UserPermissionException
     * @throws UserAccessDeniedException 
     */
    public DataElement getDataElement(Account account, String name, String vesrion, String proxyTicket)
            throws UnsupportedEncodingException, UserPermissionException, UserAccessDeniedException;

    // EDIT/SAVE
    /**
     * Uses the Rules Engine to compare two form structures. If the form structures are the same then an empty list
     * would be returned. This function does not do anything other than call the rules engine. It will become part of
     * the web service interface.
     * 
     * @param originalDataDataStructure
     * @param alteredDataDataStructure
     * @return
     */
    public List<SeverityRecord> evaluateFormStructureChangeSeverity(FormStructure originalDataDataStructure,
            FormStructure alteredDataDataStructure);

    /**
     * Uses the Rules Engine to compare two data elements. If the data elements are the same then an empty list would be
     * returned. This function does not do anything other than call the rules engine. It will become part of the web
     * service interface.
     * 
     * @param originalDataElement
     * @param alteredDataElement
     * @return
     * @throws RulesEngineException
     */
    public List<SeverityRecord> evaluateDataElementChangeSeverity(DataElement originalDataElement,
            DataElement alteredDataElement) throws RulesEngineException;

    /**
     * Will retrieve the number of search results for a given query. Used for the public site web service calls
     * 
     * @param facets
     *            - search facets
     * @param searchKeywords
     *            - search keywords
     * @param onlyOwned
     *            - only owned searches
     * @return
     * @throws RulesEngineException
     */
    public int searchCount(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchKeywords, 
            boolean exactMatch, boolean onlyOwned);

    public int publicFormStructureSearchCount(Account account, Map<FormStructureFacet, Set<String>> selectedFacets,
            Set<String> searchTerms, Boolean exactMatch, Boolean onlyOwned, String proxyTicket);

    public List<FormStructure> getAttachedDataStructure(String deName, String deVersion, boolean isPublicData);
    
    public DictionaryEventLog saveChangeHistoryEventlog(DictionaryEventLog eventLog,List<SeverityRecord> severityRecords,SeverityLevel highestSeverityLevel,EntityType entityType,Long oldEntityID,Long entityID,String comment);
    
    public Long getOriginalDataElementIdByName(String dataElementName);

    public Long getOriginalFormStructureIdByName(String formStructureName);
    
    public Set<DictionaryEventLog> getAllDEEventLogs(Long entityId);
    
    public Set<DictionaryEventLog> getAllFSEventLogs(Long entityID);

}
