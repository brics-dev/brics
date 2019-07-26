
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringUnit;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * This is the form for create/edit CDE value range page
 * 
 * @author Francis Chen
 * 
 */
public class ValueRangeForm
{

    @IgnoreFormField
    static Logger logger = Logger.getLogger(ValueRangeForm.class);

    @IgnoreFormField
    StaticReferenceManager staticManager;

    /****************************************************/
    DataType type;
    MeasuringUnit measuringUnit;
    String guidelines;
    String suggestedQuestion;
    Integer size;
    BigDecimal minimumValue;
    BigDecimal maximumValue;

    @IgnoreFormField
    InputRestrictions restrictions;
    @IgnoreFormField
    Boolean multiple;
    @IgnoreFormField
    Boolean defined;

    protected Population population;

    private Set<SubDomainElement> subDomainElementList;

    protected Set<ClassificationElement> classificationElementList;

    /****************************************************/

    public ValueRangeForm(StaticReferenceManager staticManager)
    {

        this.staticManager = staticManager;
    }

    /**
     * Constructor fetches data for each column in dataElement object
     * 
     * @param dataElement
     */
    public ValueRangeForm(StaticReferenceManager staticManager, DataElement dataElement)
    {

        this.staticManager = staticManager;

        this.type = dataElement.getType();
        this.measuringUnit = dataElement.getMeasuringUnit();
        this.guidelines = dataElement.getGuidelines();
        this.suggestedQuestion = dataElement.getSuggestedQuestion();
        this.size = dataElement.getSize();
        this.minimumValue = dataElement.getMinimumValue();
        this.maximumValue = dataElement.getMaximumValue();
        this.population = dataElement.getPopulation();
        this.subDomainElementList = dataElement.getSubDomainElementList();
        this.classificationElementList = dataElement.getClassificationElementList();

        defined = dataElement.getRestrictions() != InputRestrictions.FREE_FORM;
        multiple = dataElement.getRestrictions() == InputRestrictions.MULTIPLE;
    }

    public DataType getType()
    {

        return type;
    }

    public void setType(String typeString)
    {

        if (!ServiceConstants.EMPTY_STRING.equals(typeString))
        {
            for (DataType type : DataType.values())
            {
                if (type.getId().equals(Long.valueOf(typeString)))
                {
                    this.type = type;
                    break;
                }
            }
        }
        else
        {
            this.type = null;
        }
    }

    public Integer getSize()
    {

        return size;
    }

    public void setSize(String size)
    {

        if (!ServiceConstants.EMPTY_STRING.equals(size))
        {
            this.size = Integer.valueOf(size);
        }
    }

    public String getGuidelines()
    {

        return guidelines;
    }

    public void setGuidelines(String guidelines)
    {

        if (!ServiceConstants.EMPTY_STRING.equals(guidelines))
        {
            this.guidelines = carraigeRemover(guidelines);
        }
    }

    public String getSuggestedQuestion()
    {

        return suggestedQuestion;
    }

    public void setSuggestedQuestion(String suggestedQuestion)
    {

        this.suggestedQuestion = carraigeRemover(suggestedQuestion);
    }

    public String getMeasuringUnit()
    {

        return measuringUnit.toString();
    }

    public void setMeasuringUnit(String measuringUnit)
    {

        if (!ServiceConstants.EMPTY_STRING.equals(measuringUnit.trim()))
        {
            for (MeasuringUnit mu : staticManager.getMeasuringUnitList())
            {
                String muStr = measuringUnit.replace(',', ' ').trim();
                if (mu.toString().equalsIgnoreCase(muStr))
                {
                    this.measuringUnit = mu;
                }
            }
        }
    }

    public Boolean getMultiple()
    {

        return multiple;
    }

    public void setMultiple(Boolean multiple)
    {

        this.multiple = multiple;
    }

    public Boolean getDefined()
    {

        return defined;
    }

    public void setDefined(Boolean defined)
    {

        this.defined = defined;
    }

    /*************************************************************/

    public BigDecimal getMinimumValue()
    {

        return minimumValue;
    }

    public void setMinimumValue(String minimumValue)
    {

        if (minimumValue != null && !minimumValue.isEmpty())
        {
            BigDecimal value = new BigDecimal(minimumValue);
            this.minimumValue = value;
        }
    }

    public BigDecimal getMaximumValue()
    {

        return maximumValue;
    }

    public void setMaximumValue(String maximumValue)
    {

        if (maximumValue != null && !maximumValue.isEmpty())
        {
            BigDecimal value = new BigDecimal(maximumValue);
            this.maximumValue = value;
        }
    }

    /**
     * Read the form fields on the page and set the columns in the dataElement column.
     */
    public void copyToDataElement(DataElement dataElement)
    {

        dataElement.setType(type);
        dataElement.setMeasuringUnit(measuringUnit);
        dataElement.setGuidelines(guidelines);
        dataElement.setSuggestedQuestion(suggestedQuestion);
        dataElement.setSize(size);
        dataElement.setMinimumValue(minimumValue);
        dataElement.setMaximumValue(maximumValue);
        dataElement.setPopulation(population);
        dataElement.setSubDomainElementList(subDomainElementList);
        dataElement.setClassificationElementList(classificationElementList);

        if (!defined)
        {
            dataElement.setRestrictions(InputRestrictions.FREE_FORM);
            dataElement.setValueRangeList(new HashSet<ValueRange>());
        }
        else
        {
            if (!multiple)
            {
                dataElement.setRestrictions(InputRestrictions.SINGLE);
            }
            else
            {
                dataElement.setRestrictions(InputRestrictions.MULTIPLE);
            }
        }
    }

    public String carraigeRemover(String s)
    {

        s = s.replace("\r\n", "\n");
        return s;

    }

    public Population getPopulation()
    {

        return population;
    }

    public void setPopulation(String populationString) throws NumberFormatException, MalformedURLException,
            UnsupportedEncodingException
    {

        if (populationString != null && !ServiceConstants.EMPTY_STRING.equals(populationString))
        {
            for (Population population : staticManager.getPopulationList())
            {
                if (population.getName().equals(populationString))
                {
                    this.population = population;
                }
            }
        }
    }

    /**
     * Return the entire set of classifications from the form. If the list is null, then this function creates a new
     * empty list.
     * 
     * Loading the dataElement details page always calls this function in order to draw the select boxes.
     * 
     * @return
     */
    public Set<ClassificationElement> getClassificationElementList()
    {

        if (classificationElementList == null)
        {
            classificationElementList = new TreeSet<ClassificationElement>();
        }
        return classificationElementList;
    }

    /**
     * Value must be a comma delimited string, with each entry in the format <Disease>.<Subgroup>.<Classification>.
     * 
     * @param value
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    public void setClassificationElementList(String value) throws MalformedURLException, UnsupportedEncodingException
    {

        classificationElementList = new HashSet<ClassificationElement>();

        String[] classificationList = value.split(",");

        if (classificationList != null)
        {
            for (String concatStr : classificationList)
            {
                String[] nameArr = concatStr.split("\\.");
                if (nameArr == null || nameArr.length != 3)
                {
                    logger.error("nameArr variable = " + nameArr + " nameArr length = " + nameArr.length);
                    continue;
                }

                String diseaseName = nameArr[0];
                String subgroupName = nameArr[1];
                String classificationName = nameArr[2];

                Disease disease = staticManager.getDiseaseByName(diseaseName);
                Subgroup subgroup = staticManager.getSubgroupByName(subgroupName);
                Classification classification = null;
                List<Classification> possibleClassifications = staticManager.getClassificationList(disease, true);
                for (Classification c : possibleClassifications)
                {
                    if (c.getName().equalsIgnoreCase(classificationName))
                    {
                        classification = c;
                        break;
                    }
                }
                if (disease == null || subgroup == null || classification == null)
                {
                    logger.error("Classification not found! Disease: " + diseaseName + ", Subgroup: " + subgroupName
                            + ", Classification: " + classificationName);
                    continue;
                }
                ClassificationElement ce = new ClassificationElement(disease, classification, subgroup);
                classificationElementList.add(ce);
            }
        }
    }

    // subdomainSelections is a comma delimited string of the selected subDomains, an example is like:
    // disease1.domain1.subdomain1,disease1.domain1.subdomain2,disease1.domain2.subdomain2 ...
    public void setSubdomainList(String subdomainSelections) throws MalformedURLException, UnsupportedEncodingException
    {

        subDomainElementList = new HashSet<SubDomainElement>();

        String[] subdomainList = subdomainSelections.split(";");

        if (subdomainList != null)
        {
            for (String concatStr : subdomainList)
            {
                String[] nameArr = concatStr.split("\\.");
                if (nameArr == null || nameArr.length != 3)
                {
                    logger.debug("nameArr variable = " + nameArr + " nameArr length = " + nameArr.length);
                    continue;
                }

                String diseaseName = nameArr[0];
                String domainName = nameArr[1];
                String subDomainName = nameArr[2];

                Disease disease = staticManager.getDiseaseByName(diseaseName);
                Domain domain = staticManager.getDomainByName(domainName);
                SubDomain subDomain = staticManager.getSubDomainByName(subDomainName);

                SubDomainElement sde = new SubDomainElement(disease, domain, subDomain);
                subDomainElementList.add(sde);
            }
        }
    }

    public Set<SubDomainElement> getSubDomainElementList()
    {

        return subDomainElementList;
    }

    public void setSubDomainElementList(Set<SubDomainElement> subDomainElementList)
    {

        this.subDomainElementList = subDomainElementList;
    }

}
