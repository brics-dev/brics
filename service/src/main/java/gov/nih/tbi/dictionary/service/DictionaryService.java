
package gov.nih.tbi.dictionary.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.service.complex.BaseManagerImpl;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.commons.util.QueryConstructionUtil;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.dao.DataElementSparqlDao;
import gov.nih.tbi.dictionary.dao.DictionaryEventLogDao;
import gov.nih.tbi.dictionary.dao.FormLabelDao;
import gov.nih.tbi.dictionary.dao.FormStructureDao;
import gov.nih.tbi.dictionary.dao.FormStructureSparqlDao;
import gov.nih.tbi.dictionary.dao.FormStructureSqlDao;
import gov.nih.tbi.dictionary.dao.PublishedFormStructureDao;
import gov.nih.tbi.dictionary.dao.StructuralDataElementDao;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.model.StringFacet;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;
import gov.nih.tbi.dictionary.model.hibernate.DictionarySupportingDocumentation;
import gov.nih.tbi.dictionary.model.hibernate.FormLabel;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.PublishedFormStructure;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.dictionary.service.rulesengine.RulesEngineInterface;
import gov.nih.tbi.dictionary.service.rulesengine.model.InvalidOperationException;
import gov.nih.tbi.dictionary.service.rulesengine.model.RulesEngineException;
import gov.nih.tbi.repository.dao.UserFileDao;

@Service
@Scope("singleton")
public class DictionaryService extends BaseManagerImpl implements DictionaryServiceInterface
{
	private static final long serialVersionUID = -6718358795413834769L;
	private static final Logger logger = Logger.getLogger(DictionaryService.class);
    
    @Autowired
    private AccountManager accountManager;

    @Autowired
	private ModulesConstants modulesConstants;

    @Autowired
	private RulesEngineInterface rulesEngine;

    @Autowired
	private FormStructureSqlDao structuralFormStructureDao;

    @Autowired
	private StructuralDataElementDao structuralDataElementDao;

    @Autowired
	private FormStructureSparqlDao semanticFormStructureDao;

    @Autowired
	private DataElementSparqlDao semanticDataElementDao;

    @Autowired
	private FormStructureDao formStructureDao;

    @Autowired
	private DataElementDao dataElementDao;
    
    @Autowired
	private DictionaryEventLogDao dictionaryEventLogDao;
    
    @Autowired
	private FormLabelDao formLabelDao;
    
    @Autowired
    private DictionaryEventLogService dictionaryEventLogService;
    
    @Autowired
    private PublishedFormStructureDao publishedFormStructureDao;
    
    @Autowired
    private UserFileDao userFileDao;
    

    /**
     * {@inheritDoc}
     */
    @Override
	public List<SemanticFormStructure> semanticFormStructureSearch(Account account,
			Map<FormStructureFacet, Set<String>> selectedFacets, Set<String> searchTerms, Boolean exactMatch,
			Boolean onlyOwned, PaginationData pageData, String proxyTicket) {

		if (onlyOwned == null) {
            onlyOwned = false;
        }

        Set<String> permissionsList = buildFSPermissionsFacet(account, onlyOwned.booleanValue(), proxyTicket);

		if (permissionsList != null) {
            selectedFacets.put(FormStructureFacet.URI, permissionsList);
        }

        return semanticFormStructureDao.search(selectedFacets, searchTerms, exactMatch, pageData, onlyOwned);
    }

	/**
	 * Throw away method that will take the search and return a count of all the structures instead.
	 */
    public int publicFormStructureSearchCount(Account account, Map<FormStructureFacet, Set<String>> selectedFacets,
            Set<String> searchTerms, Boolean exactMatch, Boolean onlyOwned, String proxyTicket)

    {

        if (onlyOwned == null)
        {
            onlyOwned = false;
        }
        Set<String> permissionsList = buildFSPermissionsFacet(account, onlyOwned.booleanValue(), proxyTicket);
        if (permissionsList != null)
        {
            selectedFacets.put(FormStructureFacet.URI, permissionsList);
        }

        return semanticFormStructureDao.searchCount(selectedFacets, searchTerms, exactMatch, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FormStructure> compositeFormStructureSearch(Account account,
            Map<FormStructureFacet, Set<String>> selectedFacets, Set<String> searchTerms, Boolean onlyOwned,
            Boolean exactMatch, PaginationData pageData, boolean includeDEList, String proxyTicket)

    {

        if (onlyOwned == null)
        {
            onlyOwned = false;
        }
        Set<String> permissionsList = buildFSPermissionsFacet(account, onlyOwned.booleanValue(), proxyTicket);
        if (permissionsList != null)
        {
            selectedFacets.put(FormStructureFacet.URI, permissionsList);
        }
        List<FormStructure> list = formStructureDao.search(selectedFacets, searchTerms, exactMatch, pageData, 
               includeDEList, onlyOwned);
        return list;
    }

    private Set<String> buildFSPermissionsFacet(Account account, boolean onlyOwned, String proxyTicket)
    {

        // Determine what type of permission we are looking for
        PermissionType permissionType = null;
        if (!onlyOwned)
        {
            permissionType = PermissionType.READ;
        }
        else
        {
            permissionType = PermissionType.OWNER;
        }

        // Make a web service call that gets the ids the user has the chosen access to. (Possible results for the
        // search).
        Set<Long> accessIds = null;
        RestAccountProvider accountProvider = new RestAccountProvider(modulesConstants.getModulesAccountURL(Long
                .valueOf(account.getDiseaseKey())), proxyTicket);
        try
        {
            accessIds = accountProvider.listUserAccess(account.getId(), EntityType.DATA_STRUCTURE, permissionType,
                    false);
     
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO: MV 5/20/2014 - This needs to abort the process by throwing an exception. WS Call failed. Also look
            // into newShortNameFacet being null after DB call. (Shouldn't be)
			logger.error("Could not build FS permission facet.", e);
        }

        List<NameAndVersion> nameAndVersionsToFilter = null;

        Set<String> permissionFacetValues = null;

		if (!accountManager.hasRole(account, RoleType.ROLE_ADMIN)
				&& !accountManager.hasRole(account, RoleType.ROLE_DICTIONARY_ADMIN) && !onlyOwned) {
            nameAndVersionsToFilter = structuralFormStructureDao.getAllDraftAndAPById(accessIds);
		} else if (onlyOwned) {
			nameAndVersionsToFilter = structuralFormStructureDao.getAllShortNameAndVersionById(accessIds);
        }

        if (nameAndVersionsToFilter != null)
        {
            permissionFacetValues = new HashSet<String>();
            for (NameAndVersion nameAndVersion : nameAndVersionsToFilter)
            {
                permissionFacetValues.add(QueryConstructionUtil.createFormStructureUri(nameAndVersion.getName(),
                        nameAndVersion.getVersion()));
            }
        }

        return permissionFacetValues;
    }

    private StringFacet buildDEPermissionsFacet(Account account, boolean onlyOwned, String[] twoProxyTickets) {

        // Determine what type of permission we are looking for
        PermissionType permissionType = null;
        if (!onlyOwned) {
            permissionType = PermissionType.READ;
        } else {
            permissionType = PermissionType.OWNER;
        }

        // Make a web service call that gets the ids the user has the chosen access to. (Possible results for the
        // search).
        Set<Long> accessIds = null;
        RestAccountProvider accountProvider = new RestAccountProvider(modulesConstants.getModulesAccountURL(Long.valueOf(account.getDiseaseKey())), twoProxyTickets[0]);
        try {
            accessIds = accountProvider.listUserAccess(account.getId(), EntityType.DATA_ELEMENT, permissionType, false);
            
            //No need to add attached data elements to the list if the user is global admin or dictionary admin
            // because an admin has access to FS, DS that are not published
            if(!onlyOwned &&(!account.isAdmin() || !hasRole(account, RoleType.ROLE_DICTIONARY_ADMIN))){
            	accessIds.addAll(addAdditionalDataElements(account,twoProxyTickets[1]));
            }

        } catch (UnsupportedEncodingException e) {
            // TODO: MV 5/21/2014 - This needs to abort the process by throwing an exception. WS Call failed. Also look
            // into newShortNameFacet being null after DB call. (Shouldn't be)
			logger.error("Could not build DE permission facet.", e);
        } 
        List<NameAndVersion> dataElementsToFilterBy = null;

        StringFacet permissionsFacet = null;
        List<String> permissionFacetValues = null;

		if (!accountManager.hasRole(account, RoleType.ROLE_ADMIN) && !accountManager.hasRole(account, RoleType.ROLE_DICTIONARY_ADMIN) && !onlyOwned) {
            dataElementsToFilterBy = structuralDataElementDao.getAllDraftAndArchivedByIdMax(accessIds);
		} else if (onlyOwned) {
			dataElementsToFilterBy = structuralDataElementDao.getNameVersionByIdList(accessIds);
		}

        if (dataElementsToFilterBy != null) {
            permissionFacetValues = new ArrayList<String>();
            for (NameAndVersion nameAndVersion : dataElementsToFilterBy) {
                permissionFacetValues.add(QueryConstructionUtil.createDataElementUriString(nameAndVersion.getName(), nameAndVersion.getVersion()));
            }
        }

        if (permissionFacetValues != null) {
            permissionsFacet = new StringFacet(FacetType.PERMISSIONS, permissionFacetValues);
        }

        return permissionsFacet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SemanticDataElement> semanticDataElementSearch(Account account, DictionarySearchFacets facets,
            Map<FacetType, Set<String>> searchKeywords, boolean exactMatch, boolean onlyOwned, 
            PaginationData pageData, String[] proxyTickets) {

        // add the permissions facet to filter out results the current user does not have access to
        StringFacet permissionsFacet = buildDEPermissionsFacet(account, onlyOwned, proxyTickets);

        if (permissionsFacet != null)
        {
            facets.addFacet(permissionsFacet);
        }

        return semanticDataElementDao.search(facets, searchKeywords, exactMatch, pageData, onlyOwned);
    }
    
    public List<SemanticDataElement> semanticDataElementSearchNoPermissions(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchKeywords, PaginationData pageData, boolean exactMatch){
    	return semanticDataElementDao.search(facets, searchKeywords, exactMatch, pageData, false);
    }
    
	/**
	 * Return a count of all dataElements.
	 */
    @Override
    public int semanticDataElementSearchCount(DictionarySearchFacets facets,
            Map<FacetType, Set<String>> searchKeywords, boolean exactMatch, boolean onlyOwned) {

        return semanticDataElementDao.searchCount(facets, searchKeywords, exactMatch, onlyOwned);             
    }    

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataElement> compositeDataElementSearch(Account account, DictionarySearchFacets facets,
            Map<FacetType, Set<String>> searchKeywords, boolean exactMatch, boolean onlyOwned, 
            PaginationData pageData, String[] proxyTickets)

    {

        // add the permissions facet to filter out results the current user does not have access to
        StringFacet permissionsFacet = buildDEPermissionsFacet(account, onlyOwned, proxyTickets);

        if (permissionsFacet != null)
        {
            facets.addFacet(permissionsFacet);
        }

        return dataElementDao.searchDetailed(facets, searchKeywords, exactMatch, pageData, onlyOwned);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormStructure getFormStructure(Account account, String name, String version, String proxyTicket)
            throws UnsupportedEncodingException, UserPermissionException, UserAccessDeniedException
    {

        FormStructure formStructure;
        // convert Long to Integer. There is probably a better way to do this.

        if (version == null)
        {
            formStructure = formStructureDao.getLatestVersionByShortName(name);
        }
        else
        {
            formStructure = formStructureDao.get(name, version);
        }

        // get the object from the daos.

        // If no object is returned then skip permission check
        if (formStructure == null)
        {
            return null;
        }

        // check for read access for the from structure
        verifyAccess(account, EntityType.DATA_STRUCTURE, PermissionType.READ, formStructure.getId(), proxyTicket);

        return formStructure;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataElement getDataElement(Account account, String name, String version, String proxyTicket)
            throws UnsupportedEncodingException, UserPermissionException, UserAccessDeniedException
    {

        // get the object from the daos.
        DataElement dataElement = null;
        if (version == null)
        {
            dataElement = dataElementDao.getLatestByName(name);
        }
        else
        {
            dataElement = dataElementDao.getByNameAndVersion(name, version);
        }

        // If no object is returned then skip permission check
        if (dataElement == null)
        {
            return null;
        }

        // check for read access for the from structure
        verifyAccess(account, EntityType.DATA_ELEMENT, PermissionType.READ, dataElement.getId(), proxyTicket);

        return dataElement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SeverityRecord> evaluateFormStructureChangeSeverity(FormStructure originalDataDataStructure,
            FormStructure alteredDataDataStructure)
    {

        // TODO: MV 5/21/2014 - Discuss with MG and Michelle about how to handle these exceptions and properly handle
        // these functions.
        try
        {
            return rulesEngine.evaluateFormStructureChangeSeverity(originalDataDataStructure, alteredDataDataStructure);
        }
        catch (InvalidOperationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        catch (RulesEngineException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        catch (NoSuchMethodException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws RulesEngineException
     */
    @Override
    public List<SeverityRecord> evaluateDataElementChangeSeverity(DataElement originalDataElement,
            DataElement alteredDataElement) throws RulesEngineException
    {

        // TODO: MV 5/21/2014 - Discuss with MG and Michelle about how to handle these exceptions and properly handle
        // these functions.
        // TB 8/1/2014 - Needed a way to prevent users from saving DE & FS that created exceptions. Threw exceptions
        // as a quick fix.
        try
        {
            return rulesEngine.evaluateDataElementChangeSeverity(originalDataElement, alteredDataElement);
        }
        catch (InvalidOperationException e)
        {
            throw new RulesEngineException(e.getMessage());
        }
        catch (RulesEngineException e)
        {
            throw new RulesEngineException(e);
        }
    }

    public String formatFieldName(String name)
    {

        String newFieldName = name;
        CharSequence cs = ".";
        if (name.contains(cs))
        {
            newFieldName = name.substring(0, name.indexOf('.'));
        }
        int capLetters = detectCapitalLettersInFieldName(newFieldName);
        while (capLetters != -1)
        {
            newFieldName = addSpacesInFieldName(newFieldName, capLetters);
            capLetters = detectCapitalLettersInFieldName(newFieldName);
        }
        return newFieldName;

    }

    public int detectCapitalLettersInFieldName(String name)
    {

        for (int i = name.length() - 1; i >= 0; i--)
        {
            // This is to look for camelCase letters
            if (i != 0 && Character.isUpperCase(name.charAt(i)) && (name.charAt(i - 1) != ' '))
            {
                return i;
            }
        }
        return -1;
    }

    public String addSpacesInFieldName(String name, int spaceLocal)
    {

        StringBuilder newFieldName = new StringBuilder();
        for (int i = name.length() - 1; i >= 0; i--)
        {
            if (i == spaceLocal)
            {
                newFieldName.append(name.subSequence(0, (i)));
                newFieldName.append(" ");
                newFieldName.append(name.subSequence(i, name.length()));
            }
        }
        return newFieldName.toString();
    }

    /**
     * This function will throw an exception if the user does not have the given access to the given entity.
     * 
     * @param account
     * @param type
     * @param permissionLevel
     * @param entityId
     * @param proxyTicket
     * @throws UnsupportedEncodingException
     * @throws UserPermissionException
     */
    private void verifyAccess(Account account, EntityType type, PermissionType permissionLevel, Long entityId,
            String proxyTicket) throws UnsupportedEncodingException, UserPermissionException, UserAccessDeniedException
    {

        // check for read access for the from structure
        RestAccountProvider accountProvider = new RestAccountProvider(modulesConstants.getModulesAccountURL(Long
                .valueOf(account.getDiseaseKey())), proxyTicket);
        PermissionType permission = accountProvider.getAccess(account.getId(), type, entityId).getPermission();
        if (!(PermissionType.compare(permission, permissionLevel) >= 0))
        {
            String errorMsg = null;
            switch (permissionLevel)
            {
            case READ:
                errorMsg = ServiceConstants.READ_ACCESS_DENIED;
                break;
            case WRITE:
                errorMsg = ServiceConstants.WRITE_ACCESS_DENIED;
                break;
            case ADMIN:
                errorMsg = ServiceConstants.ADMIN_ACCESS_DENIED;
                break;
            case OWNER:
                errorMsg = ServiceConstants.ADMIN_ACCESS_DENIED;
                break;
            default:
                errorMsg = "Access denied at unknown level";
                break;
            }
            throw new UserPermissionException(errorMsg);
        }
    }

    @Override
    public int searchCount(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchKeywords, 
            boolean exactMatch, boolean onlyOwned)
    {

        return semanticDataElementDao.searchCount(facets, searchKeywords, exactMatch, onlyOwned);
    }

    public List<FormStructure> getAttachedDataStructure(String deName, String deVersion, boolean isPublicData)
    {

        return formStructureDao.getAttachedDataStructure(deName, deVersion, isPublicData);
    }
    
    /**
     * This function gets form structure ids the user has permission for first and then gets data element ids
     * from data elements attached to this form structure.
     * 
     * @param account
     * @param proxyTicketForDEs
     * @throws UnsupportedEncodingException
     */
    
	public Set<Long> addAdditionalDataElements(Account account, String proxyTicket)
			throws UnsupportedEncodingException {

		RestAccountProvider accountProvider = new RestAccountProvider(
				modulesConstants.getModulesAccountURL(Long.valueOf(account.getDiseaseKey())), proxyTicket);

		// get form structure ids that the user has access to
		Set<Long> entityMaps = accountProvider.listUserAccess(account.getId(), EntityType.DATA_STRUCTURE,PermissionType.READ, true);

		// get data element ids that are attached to the form structure from
		// database using form structure list of ids
		Set<Long> dataEntityId = dataElementDao.getDEIdsFormListOfFSIds(entityMaps);

		return dataEntityId;
	}

	public DictionaryEventLog saveChangeHistoryEventlog(DictionaryEventLog eventLog,
			List<SeverityRecord> severityRecords, SeverityLevel highestSeverityLevel, EntityType entityType,
			Long oldEntityID, Long entityID, String comment) {
		eventLog.setComment(comment);
		eventLog.setMinorMajorChange(true);
		eventLog.setEventType(EventType.MINOR_MAJOR_CHANGE);
		eventLog.setOldValue(oldEntityID.toString());
		eventLog.setNewValue(entityID.toString());

		String minorMajorDes = dictionaryEventLogService.getMinorMajorChangeLog(severityRecords, highestSeverityLevel,
				entityType);
		eventLog.setMinorMajorDesc(minorMajorDes);

		dictionaryEventLogDao.save(eventLog);

		return eventLog;
	}

	@Override
	public Long getOriginalDataElementIdByName(String dataElementName) {
		return structuralDataElementDao.getOriginalDataElementByName(dataElementName).getId();
	}

	@Override
	public Long getOriginalFormStructureIdByName(String formStructureName) {
		return structuralFormStructureDao.getOriginalFormStructureByName(formStructureName).getId();
	}

	@Override
	public Set<DictionaryEventLog> getAllDEEventLogs(Long entityID) {
		return dictionaryEventLogDao.searchDEEventLogs(entityID);
	}

	@Override
	public Set<DictionaryEventLog> getAllFSEventLogs(Long entityID) {
		return dictionaryEventLogDao.searchFSEventLogs(entityID);
	}
		
	@Override
	public List<FormLabel> getFormLabels() {
		return formLabelDao.getAllFormLabels();
	}
    
	@Override
	public FormLabel getFormLabel(Long formLabelId) {
		return formLabelDao.get(formLabelId);
	}
	
	@Override
	public boolean isFormLabelUnique(String formLabel) {
		return formLabelDao.isFormLabelUnique(formLabel);
	}
	
	@Override
	public FormLabel saveFormLabel(FormLabel formLabel) {
		return formLabelDao.save(formLabel);
	}
    
	@Override
	public void updateFormLabel(FormLabel formLabel, String newLabel) {
		formLabel.setLabel(newLabel);
		formLabelDao.save(formLabel);
		
		// also update the form label associated with any forms 
		semanticFormStructureDao.updateFormLabel(formLabel, newLabel);
	}
	
	@Override
	public void deleteFormLabelById(Long formLabelId) {
		formLabelDao.remove(formLabelId);
		
		// also delete the form label associated with any forms 
		semanticFormStructureDao.removeFormLabel(formLabelId);
	}

	@Override
	public boolean isFormStructurePublished(Long formStructureId, Long diseaseId) {
		
		PublishedFormStructure publishedFormstructure = publishedFormStructureDao.getFormStructurePublished(formStructureId, diseaseId);
		
		if(publishedFormstructure==null || !publishedFormstructure.isPublished()) {
			return false;
		}
		return true;
	}

	@Override
	public void saveFormStructurePublished(PublishedFormStructure publishedFormStructure) {
		
		publishedFormStructureDao.save(publishedFormStructure);
	}
	
	@Override
	public DictionaryEventLog saveEventLog(DictionaryEventLog eventLog, FormStructure formStructure,
			Set<DictionarySupportingDocumentation> docList, String statusChangeComment, EventType eventType) {
		
		
		Long originalEntityId = structuralFormStructureDao.getOriginalFormStructureByName(formStructure.getShortName()).getId();
		eventLog.setFormStructureID(originalEntityId);
		
		if (docList != null && !docList.isEmpty()) {
			for (DictionarySupportingDocumentation sd : docList) {
				if (sd.getUserFile() != null)
					sd.setUserFile(userFileDao.save(sd.getUserFile()));

				sd.setDictionaryEventLog(eventLog);
			}
		}
		
		eventLog.setSupportingDocumentationSet(docList);
		eventLog.setComment(statusChangeComment);
		eventLog.setOldValue(formStructure.getStatus().getId().toString());
		eventLog.setNewValue(StatusType.PUBLISHED.getId().toString());
		eventLog.setEventType(eventType);
		
		dictionaryEventLogDao.save(eventLog);
		
		return eventLog;
	}
	
}
