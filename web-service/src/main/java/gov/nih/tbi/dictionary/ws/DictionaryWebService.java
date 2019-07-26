
package gov.nih.tbi.dictionary.ws;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AccountProvider;
import gov.nih.tbi.account.ws.model.UserLogin;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;

import javax.jws.WebService;

/**
 * Interface that defines a web service contract
 * 
 * @author Andrew Johnson
 * 
 */
@WebService
public interface DictionaryWebService
{

    /*******************************************************
     * 
     * 
     * Data Structures
     * 
     * 
     *******************************************************/

    /**
     * Gets a list of all available data structures to the user. All data structures returned are only meta-data about
     * the data structure, and contain no data elements.
     * 
     * @return
     * @throws MalformedURLException
     */
    public List<FormStructure> getDataStructures(UserLogin user) throws MalformedURLException;

    /**
     * Gets a list of requested data structures that have been filled in with all known data. This includes data
     * elements, aliases, keywords, and value ranges. WARNING: If you request every data structure available in this
     * method it might take a long time; the quantity of data per data structure is large.
     * 
     * @param user
     * @param dataStructureList
     * @return
     */
    public List<FormStructure> getDataStructuresDetails(UserLogin user,
            List<FormStructure> dataStructureList);

    /**
     * Get details for data structure with matching Id
     * 
     * @param user
     * @param dataStructureId
     * @return
     */
    public FormStructure getDataStructureDetailsById(UserLogin user, Long dataStructureId);

    /**
     * Get details for data structure with matching Id
     * 
     * @param user
     * @param shortName
     *            - DS short Name
     * @Param version - DS Version
     * @return
     */
    public FormStructure getDataStructureDetailsByShortName(UserLogin user, String shortName, String version);
    
    /**
     * Get the details of the first version of a form structure passed
     * 
     * @param user
     * @param shortName
     *            - DS short Name
     * @return
     */
    public FormStructure getFormStructureFirstVersion(UserLogin user, String shortName);

    /**
     * Returns List of Map Elements (contains Data Elements) given a Form(data) Structure Id
     * 
     * @param user
     * @param dataStructureId
     * @return
     */
    public List<MapElement> getElementsForDataStructure(UserLogin user, Long dataStructureId);

    /*******************************************************
     * 
     * 
     * Elements
     * 
     * @throws MalformedURLException
     * 
     * 
     *******************************************************/

    public MapElement getMapElementById(UserLogin user, Long mapElementId);

    public DataElement getDataElementByMapElementId(UserLogin user, Long mapElementId);

    public DataElement getDataElementById(UserLogin user, Long dataElementId);

    /**
     * Returns a list of data structure with the ID that exist in the dsIdList
     * 
     * @param userLogin
     * @param dsIdList
     * @return
     */
    public List<FormStructure> getDataStructureDetailsByIds(UserLogin userLogin, List<Long> dsIdList);

    public AccountProvider getAdminAccountProvider(UserLogin userLogin);

    public Account getUserAccount(UserLogin userLogin);

    /*******************************************************
     * 
     * 
     * Public
     * 
     * @throws UnsupportedEncodingException
     * 
     * @throws MalformedURLException
     * 
     * 
     *******************************************************/

    public Integer listPublishedDataStructureCount(UserLogin userLogin, Integer page, Integer pageSize,
            Boolean ascending, String sort) throws UnsupportedEncodingException;

    public List<FormStructure> listPublishedDataStructure(UserLogin userLogin, Integer page, Integer pageSize,
            Boolean ascending, String sort) throws UnsupportedEncodingException;

    // auto-generated nightmare
    public Integer searchDataElementsCount(String diseaseSelection, String domainSelection, String subDomainSelection,
            String populationSelection, String subgroupSelection, String classificationSelection, Long filterId,
            Category category, String searchKey, Integer page, Integer pageSize, Boolean ascending, String sort,
            String searchLocations) throws UnsupportedEncodingException;

    // auto-generated nightmare
    public List<DataElement> searchDataElements(String diseaseSelection, String domainSelection,
            String subDomainSelection, String populationSelection, String subgroupSelection,
            String classificationSelection, Long filterId, Category category, String searchKey, Integer page,
            Integer pageSize, Boolean ascending, String sort, String searchLocations)
            throws UnsupportedEncodingException;

    public List<Domain> getDomainList();

    public List<Disease> getDiseaseList();

    public List<Subgroup> getSubgroupList();

    public List<Population> getPopulationList();

    public String getDiseasePrefix(Long diseaseId, UserLogin userLogin);

    public List<Classification> getClassificationList(Disease disease, boolean isAdmin) throws MalformedURLException,
            UnsupportedEncodingException;

    public List<Subgroup> getSubgroupsByDisease(Disease disease) throws MalformedURLException,
            UnsupportedEncodingException;

    public List<Domain> getDomainsByDisease(Disease disease) throws MalformedURLException, UnsupportedEncodingException;

    public List<SubDomain> getSubDomainsList(Domain domain, Disease disease) throws MalformedURLException,
            UnsupportedEncodingException;
}
