
package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.dictionary.model.ClassificationFacet;
import gov.nih.tbi.dictionary.model.ClassificationFacetValue;
import gov.nih.tbi.dictionary.model.DateFacet;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.DiseaseFacet;
import gov.nih.tbi.dictionary.model.DiseaseFacetValue;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.StringFacet;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class BaseDataElementSearchAction extends BaseDictionaryAction
{

    private String modifiedDate;
    protected String searchKey;
    protected String exactMatch;

    // Selected values will be passed as concatenated string like "v1,v2,v3..." from ui side
    private String selectedStatuses;
    private String selectedElementTypes;

    private String populationSelection;
    private String selectedDiseases;
    private String selectedDomains;
    private String selectedSubdomains;
    private String selectedClassifications;
    private int ownerId;
    private String dataElementLocations;
    private String mapped;

    private static final long serialVersionUID = 5681968115683898064L;

    private static final int DOMAIN_INDEX = 1;
    private static final int SUBDOMAIN_INDEX = 2;
    private static final int SUBGROUP_INDEX = 1;
    private static final int DISEASE_INDEX = 0;
    private static final int CLASSIFICATION_INDEX = 2;

    public String getSelectedStatuses()
    {

        return selectedStatuses;
    }

    public void setSelectedStatuses(String selectedStatuses)
    {

        this.selectedStatuses = selectedStatuses;
    }

    public String getModifiedDate()
    {

        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate)
    {

        this.modifiedDate = modifiedDate;
    }

    public String getSelectedElementTypes()
    {

        return selectedElementTypes;
    }

    public void setSelectedElementTypes(String selectedElementTypes)
    {

        this.selectedElementTypes = selectedElementTypes;
    }

    public String getPopulationSelection()
    {

        return populationSelection;
    }

    public void setPopulationSelection(String populationSelection)
    {

        this.populationSelection = populationSelection;
    }

    public String getSelectedDiseases()
    {

        return selectedDiseases;
    }

    public void setSelectedDiseases(String selectedDiseases)
    {

        this.selectedDiseases = selectedDiseases;
    }

    public String getSelectedDomains()
    {

        return selectedDomains;
    }

    public void setSelectedDomains(String selectedDomains)
    {

        this.selectedDomains = selectedDomains;
    }

    public String getSelectedSubdomains()
    {

        return selectedSubdomains;
    }

    public void setSelectedSubdomains(String selectedSubdomains)
    {

        this.selectedSubdomains = selectedSubdomains;
    }

    public String getSelectedClassifications()
    {

        return selectedClassifications;
    }

    public void setSelectedClassifications(String selectedClassifications)
    {

        this.selectedClassifications = selectedClassifications;
    }

    
    protected Map<FacetType, Set<String>> buildSearchKeywordsMap(List<String> searchLocations) {
    	
		Map<FacetType, Set<String>> searchKeywords = new HashMap<FacetType, Set<String>>();

		if (!StringUtils.isBlank(searchKey) && !searchLocations.isEmpty()) {
			Set<String> searchTerms = new HashSet<String>();
			searchTerms.add(searchKey);

			for (String searchLocation : searchLocations) {
				FacetType facet = FacetType.valueOf(searchLocation.toUpperCase());
				searchKeywords.put(facet, searchTerms);
			}
		}
		
		return searchKeywords;
    }
    
    
    protected DictionarySearchFacets buildFacets()
    {

        DictionarySearchFacets facets = new DictionarySearchFacets();

        if (modifiedDate != null && !modifiedDate.isEmpty())
        {
            Date oldestModifiedDate = getOldestModifiedDate(Integer.valueOf(modifiedDate));

            if (oldestModifiedDate != null)
            {
                facets.addFacet(new DateFacet(FacetType.MODIFIED_DATE, oldestModifiedDate));
            }
        }

        if (selectedStatuses != null && !selectedStatuses.isEmpty() && !selectedStatuses.equals("all"))
        {
            List<String> statusList = this.parseSelectedOptions(selectedStatuses);
            if (!statusList.isEmpty())
            {
                facets.addFacet(new StringFacet(FacetType.STATUS, statusList));
            }
        }

        if (selectedElementTypes != null && !selectedElementTypes.isEmpty())
        {
            List<String> elementTypeSet = this.parseSelectedOptions(selectedElementTypes);
            if (!elementTypeSet.isEmpty())
            {
                facets.addFacet(new StringFacet(FacetType.CATEGORY, elementTypeSet));
            }
        }

        if (populationSelection != null && !populationSelection.isEmpty())
        {
            List<String> populationSet = this.parseSelectedOptions(populationSelection);
            if (!populationSet.isEmpty())
            {
                facets.addFacet(new StringFacet(FacetType.POPULATION, populationSet));
            }
        }

        if (!PortalConstants.EMPTY_STRING.equals(selectedDiseases)
                || !PortalConstants.EMPTY_STRING.equals(selectedSubdomains)
                || !PortalConstants.EMPTY_STRING.equals(selectedDomains))
        {
            facets.addFacet(new DiseaseFacet(buildDiseaseFacetValues()));
        }

        if (selectedClassifications != null && !selectedClassifications.isEmpty())
        {
            facets.addFacet(new ClassificationFacet(buildClassificationFacetValues()));
        }

        return facets;
    }

    private List<ClassificationFacetValue> buildClassificationFacetValues()
    {

        Set<String> addedDiseases = new HashSet<String>();
        Set<String> toBeAddedDisease = new HashSet<String>();
        List<ClassificationFacetValue> values = new ArrayList<ClassificationFacetValue>();

        if (selectedClassifications != null && !selectedClassifications.isEmpty()
                && !selectedClassifications.equals("all"))
        {
            List<String> classificationList = this.parseSelectedOptions(selectedClassifications);

            for (String classificationCombination : classificationList)
            {
                String[] classificationParts = classificationCombination
                        .split(PortalConstants.DE_DISEASE_SPLIT_EXPRESSION);
                String disease = classificationParts[DISEASE_INDEX];
                String subgroup = classificationParts[SUBGROUP_INDEX];
                String classification = classificationParts[CLASSIFICATION_INDEX];

                ClassificationFacetValue value = new ClassificationFacetValue(classification, subgroup);
                values.add(value);
                addedDiseases.add(disease);
            }
        }

        if (selectedSubdomains != null && !selectedSubdomains.isEmpty() && !selectedSubdomains.equals("all"))
        {
            List<String> subdomainList = this.parseSelectedOptions(selectedSubdomains);

            for (String subDomainCombinations : subdomainList)
            {
                String[] subdomainParts = subDomainCombinations.split(PortalConstants.DE_DISEASE_SPLIT_EXPRESSION);
                String disease = subdomainParts[DISEASE_INDEX];
                if (!addedDiseases.contains(disease))
                {
                    addedDiseases.add(disease);
                    toBeAddedDisease.addAll(getDiseaseOrSubgroup(disease));
                }
            }
        }

        if (selectedDomains != null && !selectedDomains.isEmpty() && !selectedDomains.equals("all"))
        {
            List<String> domains = this.parseSelectedOptions(selectedDomains);

            for (String domainCombination : domains)
            {
                String[] domainParts = domainCombination.split(PortalConstants.DE_DISEASE_SPLIT_EXPRESSION);
                String disease = domainParts[DISEASE_INDEX];
                if (!addedDiseases.contains(disease))
                {
                    addedDiseases.add(disease);
                    toBeAddedDisease.addAll(getDiseaseOrSubgroup(disease));
                }
            }
        }

        if (selectedDiseases != null && !selectedDiseases.isEmpty() && !selectedDiseases.equals("all"))
        {
            List<String> diseases = this.parseSelectedOptions(selectedDiseases);

            for (String disease : diseases)
            {
                if (!addedDiseases.contains(disease))
                {
                    addedDiseases.add(disease);
                    toBeAddedDisease.addAll(getDiseaseOrSubgroup(disease));
                }
            }
        }

        for (String disease : toBeAddedDisease)
        {
            ClassificationFacetValue value = new ClassificationFacetValue(null, disease);
            values.add(value);
        }

        return values;
    }
    
    private List<String> getDiseaseOrSubgroup(String disease)
    {
        List<String> toBeAdded = new ArrayList<String> ();
        
        List<Subgroup> subgroupList = null;
        try
        {
            subgroupList = staticManager.getSubgroupsByDisease(staticManager.getDiseaseByName(disease));
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        if (subgroupList == null || subgroupList.isEmpty())
        {
            toBeAdded.add(disease);
        }
        else
        {
            for (Subgroup subgroup : subgroupList)
            {
                toBeAdded.add(subgroup.getSubgroupName());
            }
        }
        
        return toBeAdded;
    }

    private List<DiseaseFacetValue> buildDiseaseFacetValues()
    {

        List<DiseaseFacetValue> values = new ArrayList<DiseaseFacetValue>();
        List<String> addedDomains = new ArrayList<String>();
        List<String> addedDiseases = new ArrayList<String>();

        if (selectedSubdomains != null && !selectedSubdomains.isEmpty() && !selectedSubdomains.equals("all"))
        {
            List<String> subdomainList = this.parseSelectedOptionsBySemiColon(selectedSubdomains);

            for (String subDomainCombinations : subdomainList)
            {
                String[] subdomainParts = subDomainCombinations.split(PortalConstants.DE_DISEASE_SPLIT_EXPRESSION);
                String disease = subdomainParts[DISEASE_INDEX];
                String domain = subdomainParts[DOMAIN_INDEX];
                String subdomain = subdomainParts[SUBDOMAIN_INDEX];

                addedDomains.add(domain);
                addedDiseases.add(disease);

                DiseaseFacetValue value = new DiseaseFacetValue();
                value.setDisease(disease);
                value.setDomain(domain);
                value.setSubdomain(subdomain);
                values.add(value);
            }
        }

        if (selectedDomains != null && !selectedDomains.isEmpty() && !selectedDomains.equals("all"))
        {
            List<String> domains = this.parseSelectedOptions(selectedDomains);

            for (String domainCombination : domains)
            {
                String[] domainParts = domainCombination.split(PortalConstants.DE_DISEASE_SPLIT_EXPRESSION);
                String disease = domainParts[DISEASE_INDEX];
                String domain = domainParts[DOMAIN_INDEX];

                if (!addedDomains.contains(domain))
                {
                    addedDiseases.add(disease);
                    addedDomains.add(domain);
                    DiseaseFacetValue value = new DiseaseFacetValue();
                    value.setDisease(disease);
                    value.setDomain(domain);
                    values.add(value);
                }
            }
        }

        if (selectedDiseases != null && !selectedDiseases.isEmpty() && !selectedDiseases.equals("all"))
        {
            List<String> diseases = this.parseSelectedOptions(selectedDiseases);

            for (String disease : diseases)
            {
                if (!addedDiseases.contains(disease))
                {
                    addedDiseases.add(disease);
                    DiseaseFacetValue value = new DiseaseFacetValue();
                    value.setDisease(disease);
                    values.add(value);
                }
            }
        }

        return values;
    }

    /**
     * Returns the date used to compare to the current date if we want to search by how old a date is
     * 
     * @param daysOld
     * @return
     */
    protected Date getOldestModifiedDate(int daysOld)
    {

        return new Date(BRICSTimeDateUtil.getStartOfCurrentDay() - (daysOld * BRICSTimeDateUtil.ONE_DAY));
    }

    protected List<String> parseSelectedOptions(String options)
    {

        List<String> optionSet = new ArrayList<String>();
        if (options != null && !options.isEmpty())
        {
            String[] optionArr = options.split(",");
            optionSet.addAll(Arrays.asList(optionArr));
        }

        return optionSet;
    }
    
    protected List<String> parseSelectedOptionsBySemiColon(String options)
    {

        List<String> optionSet = new ArrayList<String>();
        if (options != null && !options.isEmpty())
        {
            String[] optionArr = options.split(";");
            optionSet.addAll(Arrays.asList(optionArr));
        }

        return optionSet;
    }

    public String getSearchKey()
    {

        return searchKey;
    }

    public void setSearchKey(String searchKey)
    {

        this.searchKey = searchKey;
    }
    
    public String getExactMatch()
    {
    
        return exactMatch;
    }
    
    public void setExactMatch(String exactMatch)
    {
    
        this.exactMatch = exactMatch;
    }

    public int getOwnerId()
    {

        return ownerId;
    }

    public void setOwnerId(int ownerId)
    {

        this.ownerId = ownerId;
    }

    public String getDataElementLocations()
    {

        return dataElementLocations;
    }

    public void setDataElementLocations(String dataElementLocations)
    {

        this.dataElementLocations = dataElementLocations;
    }

    public String getMapped()
    {

    	if(mapped == null){
    		return "false";
    	}
        return mapped;
    }

    public void setMapped(String mapped)
    {

        this.mapped = mapped;
    }

}
