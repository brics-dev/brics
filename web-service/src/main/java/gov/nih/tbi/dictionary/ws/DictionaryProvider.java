
package gov.nih.tbi.dictionary.ws;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AccountProvider;
import gov.nih.tbi.account.ws.AuthenticationProvider;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;

import org.apache.log4j.Logger;

/**
 * Provides a lightweight implemenation of the client side classes needed to utilize a JaxWS Service.
 * 
 * @author Andrew Johnson
 * 
 */
public class DictionaryProvider extends AuthenticationProvider
{

    /***************************************************************************************************/

    private static Logger logger = Logger.getLogger(DictionaryProvider.class);

    private static final String namespaceURI = "http://cxf.ws.dictionary.tbi.nih.gov/";
    private static final String serviceName = "DictionaryWebServiceImplService";
    private static final String portName = "DictionaryWebServiceImplPort";
    private static final String wsdlExtension = "portal/ws/dictionaryWebService?wsdl";

    private static URL WSDL_LOCATION;

    /***************************************************************************************************/

    private Service service;
    private DictionaryWebService dictionaryWebService;

    /***************************************************************************************************/

    /**
     * Constructor that takes the web service WSDL URL, username, and a password
     * 
     * @param dictionaryWsdlLocation
     * @throws MalformedURLException
     */
    public DictionaryProvider(String serverLocation, String authenticationLocation, String userName, String password)
            throws MalformedURLException
    {

        super(authenticationLocation, userName, password);
        WSDL_LOCATION = new URL(DictionaryProvider.class.getResource("."), serverLocation + wsdlExtension);

        service = Service.create(WSDL_LOCATION, new QName(namespaceURI, serviceName));
        dictionaryWebService = getDataDictionaryService();
    }

    /**
     * Constructor that takes the web service WSDL URL
     * 
     * @param dictionaryWsdlLocation
     * @throws MalformedURLException
     */
    public DictionaryProvider(String serverLocation, String authenticationLocation) throws MalformedURLException
    {

        this(serverLocation, authenticationLocation, "anonymous", "");
    }

    /**
     * Creates a new connection to consume the web service. This should only need to be called once in a program.
     * 
     * @return
     */
    @WebEndpoint(name = "dictionaryWebService")
    private DictionaryWebService getDataDictionaryService()
    {

        return service.getPort(new QName(namespaceURI, portName), DictionaryWebService.class);
    }

    /***************************************************************************************************/

    /**
     * 
     * 
     * Returns basic information for all Form(data) Structures This function has been depricated. Please use another
     * data dictionary function. Questions? Ask Mike V.
     * 
     * @param username
     * @param dataStructureList
     * @return
     */
    @Deprecated
    public List<FormStructure> getDataStructures()
    {

        List<FormStructure> dsList = null;
        try
        {
            dsList = dictionaryWebService.getDataStructures(getUserLogin());
        }
        catch (MalformedURLException e)
        {
            dsList = new ArrayList<FormStructure>();
            e.printStackTrace();
        }
        return dsList;
    }

    /**
     * USE GET DATA STRUCTURE DETAILS
     * 
     * @param username
     * @param dataStructureList
     * @return
     */
    @Deprecated
    public List<FormStructure> getDataDictionary(List<FormStructure> dataStructureList)
    {

        return getDataStructuresDetails(dataStructureList);
    }

    /**
     * Return all information (Repeatable Groups, Map Elements, etc...) for given list of Data Structures
     * 
     * @param username
     * @param dataStructureList
     * @return
     */
    public List<FormStructure> getDataStructuresDetails(List<FormStructure> dataStructureList)
    {

        List<FormStructure> dsList = dictionaryWebService.getDataStructuresDetails(getUserLogin(), dataStructureList);
        return dsList;
    }

    /**
     * Get details for data structure with matching Id
     * 
     * @param user
     * @param dataStructureId
     * @return
     */
    public FormStructure getDataStructureDetails(Long dataStructureId)
    {

        return dictionaryWebService.getDataStructureDetailsById(getUserLogin(), dataStructureId);
    }

    /**
     * Get details for data structure with matching shortname and version
     * 
     * @param user
     * @param shortname
     *            - ds shortname
     * @param version
     *            - ds version
     * @return
     * @throws MalformedURLException
     */
    public FormStructure getDataStructureDetails(String shortName, String version) throws MalformedURLException
    {

        return dictionaryWebService.getDataStructureDetailsByShortName(getUserLogin(), shortName, version);
    }

    public FormStructure getFormStructureFirstVersion(String shortName) throws MalformedURLException
    {

        return dictionaryWebService.getFormStructureFirstVersion(getUserLogin(), shortName);
    }

    /**
     * Returns List of Map Elements (contains Data Elements) given a Form(data) Structure Id
     * 
     * @param user
     * @param dataStructureId
     * @return
     */
    // Error trying to unmarshal using Model
    // public List<MapElement> getElementsForDataStructure(Long dataStructureId)
    // {
    //
    // return dictionaryWebService.getElementsForDataStructure(getUserLogin(), dataStructureId);
    // }

    /**
     * Convenience method
     * 
     * @param connection
     * @param structureNames
     * @return
     */
    public static List<FormStructure> getDataDictionary(DictionaryProvider connection, String[] structureNames)
    {

        List<FormStructure> dataStructureRequestList = new ArrayList<FormStructure>();

        for (String name : structureNames)
        {
            FormStructure bds = new FormStructure();

            name = name.toLowerCase();

            // XXX: break apart short name and create temp list of structures

            try
            {
                bds.setShortName(name.substring(0, name.length() - 2));
                /*
                 * Need to make sure this is getting the full version
                 */
                bds.setVersion(name.substring(name.length() - 2));

                dataStructureRequestList.add(bds);
            }
            catch (NumberFormatException ex)
            {
                logger.debug("Tried to get a data structure by compostie shortname that has no version.  Likely this is because it is not actually a short name.");
            }
            catch (StringIndexOutOfBoundsException ex)
            {
                logger.debug("Tried to get a data structure by compostie shortname that is not long enough to have a version.  Likely this is because it is not actually a short name.");
            }
        }

        return connection.getDataStructuresDetails(dataStructureRequestList);
    }

    /*******************************************************
     * 
     * 
     * Elements
     * 
     * @throws MalformedURLException
     * 
     * 
     *******************************************************/

    public MapElement getMapElementById(Long mapElementId)
    {

        return dictionaryWebService.getMapElementById(getUserLogin(), mapElementId);
    }

    public DataElement getDataElementByMapElementId(Long mapElementId)
    {

        return dictionaryWebService.getDataElementByMapElementId(getUserLogin(), mapElementId);
    }

    public DataElement getDataElementById(Long dataElementId)
    {

        return dictionaryWebService.getDataElementById(getUserLogin(), dataElementId);
    }

    public List<FormStructure> getDataStructureDetails(List<Long> dsIdList)
    {

        return dictionaryWebService.getDataStructureDetailsByIds(getUserLogin(), dsIdList);
    }

    public Account getUserAccount()
    {

        return dictionaryWebService.getUserAccount(getUserLogin());
    }

    public AccountProvider getAccountProvider()
    {

        return dictionaryWebService.getAdminAccountProvider(getUserLogin());
    }

    public Integer listPublishedDataStructureCount(Integer page, Integer pageSize, Boolean ascending, String sort)
            throws UnsupportedEncodingException
    {

        return dictionaryWebService.listPublishedDataStructureCount(getUserLogin(), page, pageSize, ascending, sort);
    }

    public List<FormStructure> listPublishedDataStructure(Integer page, Integer pageSize, Boolean ascending, String sort)
            throws UnsupportedEncodingException
    {

        return dictionaryWebService.listPublishedDataStructure(getUserLogin(), page, pageSize, ascending, sort);
    }

    public Integer searchDataElementsCount(String diseaseSelection, String domainSelection, String subDomainSelection,
            String populationSelection, String subgroupSelection, String classificationSelection, Long filterId,
            Category category, String searchKey, Integer page, Integer pageSize, Boolean ascending, String sort,
            String searchLocations) throws UnsupportedEncodingException
    {

        return dictionaryWebService.searchDataElementsCount(diseaseSelection, domainSelection, subDomainSelection,
                populationSelection, subgroupSelection, classificationSelection, filterId, category, searchKey, page,
                pageSize, ascending, sort, searchLocations);
    }

    public List<DataElement> searchDataElements(String diseaseSelection, String domainSelection,
            String subDomainSelection, String populationSelection, String subgroupSelection,
            String classificationSelection, Long filterId, Category category, String searchKey, Integer page,
            Integer pageSize, Boolean ascending, String sort, String searchLocations)
            throws UnsupportedEncodingException
    {

        List<DataElement> list = dictionaryWebService.searchDataElements(diseaseSelection, domainSelection,
                subDomainSelection, populationSelection, subgroupSelection, classificationSelection, filterId,
                category, searchKey, page, pageSize, ascending, sort, searchLocations);
        return list;
    }

    public List<Domain> getDomainList()
    {

        return dictionaryWebService.getDomainList();
    }

    public List<Disease> getDiseaseList()
    {

        return dictionaryWebService.getDiseaseList();
    }

    public List<Subgroup> getSubgroupList()
    {

        return dictionaryWebService.getSubgroupList();
    }

    public List<Population> getPopulationList()
    {

        return dictionaryWebService.getPopulationList();
    }

    public String getDiseasePrefix(Long diseaseId)
    {

        return dictionaryWebService.getDiseasePrefix(diseaseId, getUserLogin());
    }

    public List<Classification> getClassificationList(Disease disease, boolean isAdmin) throws MalformedURLException,
            UnsupportedEncodingException
    {

        return dictionaryWebService.getClassificationList(disease, isAdmin);
    }

    public List<Subgroup> getSubgroupsByDisease(Disease disease) throws MalformedURLException,
            UnsupportedEncodingException
    {

        return dictionaryWebService.getSubgroupsByDisease(disease);
    }

    public List<Domain> getDomainsByDisease(Disease disease) throws MalformedURLException, UnsupportedEncodingException
    {

        return dictionaryWebService.getDomainsByDisease(disease);
    }

    public List<SubDomain> getSubDomainsList(Domain domain, Disease disease) throws MalformedURLException,
            UnsupportedEncodingException
    {

        return dictionaryWebService.getSubDomainsList(domain, disease);
    }

    public FormStructure getDataStructureDetailsByShortName(String shortName, Integer valueOf)
    {

        // TODO Auto-generated method stub
        return null;
    }
}
