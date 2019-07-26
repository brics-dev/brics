
package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.commons.service.WebServiceManager;
import gov.nih.tbi.dictionary.model.DataElementForm;
import gov.nih.tbi.dictionary.model.KeywordForm;
import gov.nih.tbi.dictionary.model.MapElementForm;
import gov.nih.tbi.dictionary.model.ValueRangeForm;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringType;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringUnit;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.ValidationPlugin;
import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Action for Map Element
 * 
 * @author Francis Chen
 * 
 */
public class MapElementAction extends BaseDictionaryAction
{

    private static final long serialVersionUID = -377471540130596842L;

    static Logger logger = Logger.getLogger(MapElementAction.class);

    /******************************************************************************************************/
    @Autowired
    WebServiceManager webServiceManager;

    @Autowired
    StaticReferenceManager staticManager;

    MapElement currentMapElement;

    DataElementForm dataElementForm;

    KeywordForm keywordForm;

    MapElementForm mapElementForm;

    ValueRangeForm valueRangeForm;

    List<FormStructure> attachedDataStructures;

    String formType;

    String dataType;

    Long repeatableGroupId;

    Long dsId;

    List<RequiredType> requiredList;

    List<ValidationPlugin> validationPlugins;

    String currentPage;

    DataElement currentDataElement;

    Keyword newKeyword;

    String keywordNew;

    String fileErrors = "";

    /******************************************************************************************************/

    /**
     * Gets the static list of Measurement Types
     */
    public List<MeasuringType> getMeasuringTypeList()
    {

        return staticManager.getMeasuringTypeList();
    }

    /**
     * Gets the static list of Measurement Units
     */
    public List<MeasuringUnit> getMeasuringUnitList()
    {

        return staticManager.getMeasuringUnitList();
    }

    public MapElement getMapElement()
    {

        return currentMapElement;
    }

    public Keyword getNewKeyword()
    {

        return newKeyword;
    }

    public void setNewKeyword(Keyword newKeyword)
    {

        this.newKeyword = newKeyword;
    }

    public String getKeywordNew()
    {

        return keywordNew;
    }

    public void setKeywordNew(String keywordNew)
    {

        this.keywordNew = keywordNew;
    }

    public KeywordForm getKeywordForm()
    {

        return keywordForm;
    }

    /**
     * Returns the current page based on which form is not null. Gets from session if all null.
     * 
     * @return String
     */
    public String getCurrentPage()
    {

        if (currentPage == null || PortalConstants.EMPTY_STRING.equals(currentPage))
        {
            if (dataElementForm != null)
            {
                setCurrentPage(PortalConstants.DETAILS);
            }
            else
                if (valueRangeForm != null)
                {
                    setCurrentPage(CoreConstants.VALUERANGE);
                }
                else
                    if (keywordForm != null)
                    {
                        setCurrentPage(currentPage = CoreConstants.KEYWORDS);
                    }
                    else
                    {
                        currentPage = getSessionDataElement().getPrevPage();
                    }
        }

        return currentPage;
    }

    public ValueRangeForm getValueRangeForm()
    {

        return valueRangeForm;
    }

    public void setValueRangeForm(ValueRangeForm valueRangeForm)
    {

        this.valueRangeForm = valueRangeForm;
    }

    public void setCurrentPage(String currentPage)
    {

        getSessionDataElement().setPrevPage(currentPage);
        this.currentPage = currentPage;
    }

    public void setrepeatableGroupId(Long repeatableGroupId)
    {

        this.repeatableGroupId = repeatableGroupId;
    }

    public Long getrepeatableGroupId()
    {

        return repeatableGroupId;
    }

    public DataElementForm getDataElementForm()
    {

        return dataElementForm;
    }

    public void setDataElementForm(DataElementForm dataElementForm)
    {

        this.dataElementForm = dataElementForm;
    }

    public void setKeywordForm(KeywordForm keywordForm)
    {

        this.keywordForm = keywordForm;
    }

    public MapElementForm getMapElementForm()
    {

        if (mapElementForm == null)
        {
            mapElementForm = new MapElementForm(getSessionDataElement().getMapElement());
        }

        return mapElementForm;
    }

    public void setMapElementForm(MapElementForm mapElementForm)
    {

        this.mapElementForm = mapElementForm;
    }

    public void setCurrentMapElement(MapElement currentMapElement)
    {

        this.currentMapElement = currentMapElement;
    }

    public void setCurrentDataElement(DataElement currentDataElement)
    {

        this.currentDataElement = currentDataElement;
    }

    public String getFormType()
    {

        return formType;
    }

    public void setFormType(String formType)
    {

        this.formType = formType;
    }

    public String getFileErrors()
    {

        return fileErrors;
    }

    public void setFileErrors(String fileErrors)
    {

        this.fileErrors = fileErrors;
    }

    public Long getDsId()
    {

        return dsId;
    }

    public void setDsId(Long dsId)
    {

        this.dsId = dsId;
    }

    public String getDataType()
    {

        return PortalConstants.MAPELEMENT;
    }

    public void setDataType(String dataType)
    {

        this.dataType = dataType;
    }

    public List<RequiredType> getRequiredList()
    {

        return requiredList;
    }

    public void setRequiredList(List<RequiredType> requiredList)
    {

        this.requiredList = requiredList;
    }

    public MapElement getCurrentMapElement()
    {

        if (getSessionDataElement() != null && getSessionDataElement().getMapElement() != null)
        {
            return getSessionDataElement().getMapElement();
        }

        return null;
    }

    public List<FormStructure> getAttachedDataStructures()
    {

        return attachedDataStructures;
    }

    public List<ValidationPlugin> getValidationPlugins()
    {

        return dictionaryManager.getValidationPlugins();
    }

    public void setValidationPluginNames(List<ValidationPlugin> validationPlugins)
    {

        this.validationPlugins = validationPlugins;
    }

    /**
     * In addition to setting the current MapElement, this method will also set a list DataStructures that are attached
     * to the said MapElement
     * 
     * @return String
     */
    public String view()
    {

        String meId = getRequest().getParameter(PortalConstants.MAPELEMENT_ID);
        String meName = getRequest().getParameter(PortalConstants.MAPELEMENT_NAME);

        // If the dataStructureId argument is present, we want to store it in dsId and nagivate to
        // that DS view when the close button is clicked.
        try
        {
            dsId = Long.valueOf(getRequest().getParameter(PortalConstants.DATASTRUCTURE_ID));
        }
        catch (NumberFormatException e)
        {
            dsId = null;
        }

        // Sets the session map element
        if (meName != null)
        {

            FormStructure dataStructure = getSessionDataStructure().getDataStructure();

            for (MapElement me : dataStructure.getMapElements())
            {
                if (me.getStructuralDataElement().getName().equalsIgnoreCase(meName))
                {
                    currentMapElement = me;
                    getSessionDataElement().setMapElement(currentMapElement);
                }
            }
        }
        else
            if (meId != null)
            {
                // gets the session map element
                currentMapElement = this.dictionaryManager.getMapElement(Long.valueOf(meId));
                getSessionDataElement().setMapElement(currentMapElement);
                currentDataElement = this.dictionaryManager.getDataElement(currentMapElement.getStructuralDataElement()
                        .getId());
            }

        dataType = PortalConstants.MAPELEMENT;

        return PortalConstants.ACTION_VIEW;
    }

    public String viewDetails()
    {

        String meId = getRequest().getParameter(PortalConstants.MAPELEMENT_ID);
        String meName = getRequest().getParameter(PortalConstants.MAPELEMENT_NAME);

        if (meName != null)
        {
            currentDataElement = dictionaryManager.getLatestDataElementByName(meName);
            if (currentDataElement == null)
            {
                Long groupElementId = getSessionDataStructure().getRepeatableGroup().getId();
                RepeatableGroup rg = dictionaryManager.findRepeatableGroupInList(Long.valueOf(groupElementId),
                        getSessionDataStructure().getDataStructure());
                currentMapElement = dictionaryManager.findMapElementInList(meName, rg);
            }

            if (currentMapElement == null)
            {
                currentMapElement = new MapElement();
                currentMapElement.setStructuralDataElement(currentDataElement.getStructuralObject());
            }
        }
        else
            if (meId != null)
            {
                currentMapElement = this.dictionaryManager.getMapElement(Long.valueOf(meId));
                currentDataElement = this.dictionaryManager.getDataElement(currentMapElement.getStructuralDataElement()
                        .getId());
            }

        dataType = PortalConstants.MAPELEMENT;

        return PortalConstants.ACTION_VIEW_DETAILS;
    }

    /**
     * This action is performed as an ajax call when creating a new keyword A new keyword object is created here and
     * stored in newKeyword where the json call retrieves it.
     * 
     * @return a string directing struts to a json object
     */
    public String createKeyword()
    {

        newKeyword = new Keyword();
        newKeyword.setKeyword(keywordNew);
        newKeyword.setCount(0L);
        // Set temporary id and update session id. Add element to session newKeyword list
        getSessionDataElement().setNewKeywordId(getSessionDataElement().getKeywordId() - 1);
        getSessionDataElement().addNewKeyword(newKeyword);

        return PortalConstants.ACTION_ADDKEYWORD;
    }

    public String create()
    {

        formType = PortalConstants.FORMTYPE_CREATE;
        dataType = PortalConstants.MAPELEMENT;
        currentPage = "details";

        if (getRequest().getParameter(PortalConstants.REPEATABLEGROUP_ID) != null)
        {
            repeatableGroupId = Long.valueOf((getRequest().getParameter(PortalConstants.REPEATABLEGROUP_ID)));
        }

        currentMapElement = new MapElement();
        currentMapElement.setRequiredType(RequiredType.RECOMMENDED);
        currentDataElement = new DataElement();

        // for (Disease disease : staticManager.getDiseaseList())
        // {
        // if (PortalConstants.TBI.equals(disease.getName()))
        // {
        // Set<DiseaseElement> newDiseaseSet = new HashSet<DiseaseElement>();
        // DiseaseElement diseaseElement = new DiseaseElement(disease, (DataElement) currentDataElement);
        // newDiseaseSet.add(diseaseElement);
        // currentDataElement.setDiseaseList(newDiseaseSet);
        // }
        // }

        // When creating a new Data Element, use the staticManager to create a the default list of classifications
        // NOTE: This cannot be done in the dataElementForm - The staticManager is not available when calling getters to
        // build the page.

        // Only do this when a disease is selected.
        // currentDataElement.setClassificationElementList(new HashSet<ClassificationElement>());
        // for (Subgroup s : staticManager.getSubgroupList())
        // {
        // ClassificationElement ce = new ClassificationElement();
        // ce.setSubgroup(s);
        // currentDataElement.getClassificationElementList().add(ce);
        // }
        currentMapElement.setStructuralDataElement(currentDataElement.getStructuralObject());

        getSessionDataElement().setMapElement(currentMapElement);

        if (dataElementForm == null)
        {
            dataElementForm = new DataElementForm(currentDataElement);
        }

        return PortalConstants.ACTION_INPUT;
    }

    public String edit()
    {

        dataType = PortalConstants.MAPELEMENT;
        currentPage = "details";
        String meName = getRequest().getParameter("mapElementName");
        String requestId = getRequest().getParameter("repeatableGroupId");

        if (meName != null)
        {

            RepeatableGroup repeatableGroup = getSessionDataStructure().getRepeatableGroup();

            // Make sure the repeatableGroup in session matches the id in the parameters
            if (requestId != null && !Long.valueOf(requestId).equals(repeatableGroup.getId()))
            {
                logger.warn("Session repeatableGroup does not match parameter. Saving to Session");
            }

            for (MapElement me : repeatableGroup.getMapElements())
            {
                if (me.getStructuralDataElement().getName().equalsIgnoreCase(meName))
                {
                    currentMapElement = me;
                    getSessionDataElement().setMapElement(currentMapElement);
                }
            }
        }

        repeatableGroupId = getSessionDataStructure().getRepeatableGroup().getId();

        currentMapElement = getSessionDataElement().getMapElement();

        if (mapElementForm == null)
        {
            mapElementForm = new MapElementForm(currentMapElement);
        }

        formType = PortalConstants.FORMTYPE_EDIT;

        return PortalConstants.ACTION_INPUT;
    }

    public String submit() throws Exception
    {

        if (getSessionDataElement().getDataElement() == null)
        {
            create();

            if (getSessionDataStructure().getRepeatableGroup() != null)
            {
                currentMapElement.setRepeatableGroup(getSessionDataStructure().getRepeatableGroup());
            }
            else
            {
                throw new Exception("Error: Missing session RepeatableGroup.");
            }
        }
        else
        {
            edit();
        }

        // Remove old map element first
        this.dictionaryManager.removeMapElementFromList(currentMapElement.getStructuralDataElement().getName(),
                getSessionDataStructure().getRepeatableGroup());

        currentMapElement = getSessionDataElement().getMapElement();

        List<String> errors = new ArrayList<String>();

        getSessionDataElement().setMapElement(currentMapElement);

        // Add new map element
        this.dictionaryManager.addMapElementToList(currentMapElement, getSessionDataStructure().getRepeatableGroup());

        // Give this new map element an temporary (negative) id and a required type if it does not already have one
        // and set a new type if there isn't one (set to required)
        if (currentMapElement.getId() == null)
        {
            currentMapElement.setId(Long.valueOf(getSessionDataStructure().getNewMappedElements()));
            currentMapElement.getStructuralDataElement().setStatus(DataElementStatus.DRAFT);
            getSessionDataStructure().setNewMappedElements(getSessionDataStructure().getNewMappedElements() - 1);
        }
        if (currentMapElement.getRequiredType() == null)
        {
            currentMapElement.setRequiredType(RequiredType.REQUIRED);
        }

        if (errors.size() > 0)
        {
            for (String error : errors)
            {
                this.addActionError(error);
            }

            return PortalConstants.ACTION_INPUT;
        }
        else
        {
            return PortalConstants.ACTION_REDIRECT;
        }
    }

    // TODO: Implement this when we know which fields are static
    private Boolean getEnforceStaticFields()
    {

        return false;
    }

}
