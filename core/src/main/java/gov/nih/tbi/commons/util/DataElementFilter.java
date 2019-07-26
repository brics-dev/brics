
package gov.nih.tbi.commons.util;

import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;

/**
 * DataElementFilter is a wrapper for different filters that can be used to filter data elements in the data element
 * search function. The package is created in the service layer and passed down to the dao to reduce the amount of
 * arguments required in the search funtions.
 * 
 * @author mvalei
 * 
 */
public class DataElementFilter
{

    /**
     * The search function will only return data elements with the given domain
     */
    private Domain domain;

    /**
     * Will subDomain to limit the data element query to
     */
    private SubDomain subDomain;

    /**
     * The disease to limit the data element query to
     */
    private Disease disease;

    /**
     * This field being non-null specifies that we want to limit data elements to elements with the given classification
     * in this subgroup
     */
    private Subgroup subgroup;

    /**
     * The classification to limit the search results to in the given subgroup
     */
    private Classification classification;

    /**
     * The Population to limit search results by
     */
    private Population population;

    /**
     * If true, then include general data elements in the search. Ignore if false or null
     */
    private Boolean generalSearch;

    public DataElementFilter()
    {

    }

    public DataElementFilter(Domain domain, SubDomain subDomain, Disease disease, Population population,
            Classification classification, Subgroup subgroup, Boolean generalSearch)
    {

        this.setDomain(domain);
        this.setSubDomain(subDomain);
        this.setDisease(disease);
        this.setClassification(classification);
        this.setSubgroup(subgroup);
        this.setPopulation(population);
        this.setGeneralSearch(generalSearch);
    }

    /**
     * @return the domain
     */
    public Domain getDomain()
    {

        return domain;
    }

    /**
     * @param domain
     *            the domain to set
     */
    public void setDomain(Domain domain)
    {

        this.domain = domain;
    }

    /**
     * @return the subDomain
     */
    public SubDomain getSubDomain()
    {

        return subDomain;
    }

    /**
     * @param subDomain
     *            the subDomain to set
     */
    public void setSubDomain(SubDomain subDomain)
    {

        this.subDomain = subDomain;
    }

    /**
     * @return the disease
     */
    public Disease getDisease()
    {

        return disease;
    }

    /**
     * @param disease
     *            the disease to set
     */
    public void setDisease(Disease disease)
    {

        this.disease = disease;
    }

    /**
     * @return the classification
     */
    public Classification getClassification()
    {

        return classification;
    }

    /**
     * @param classification
     *            the classification to set
     */
    public void setClassification(Classification classification)
    {

        this.classification = classification;
    }

    /**
     * @return the subgroup
     */
    public Subgroup getSubgroup()
    {

        return subgroup;
    }

    /**
     * @param subgroup
     *            the subgroup to set
     */
    public void setSubgroup(Subgroup subgroup)
    {

        this.subgroup = subgroup;
    }

    /**
     * @return the population
     */
    public Population getPopulation()
    {

        return population;
    }

    /**
     * @param population
     *            the population to set
     */
    public void setPopulation(Population population)
    {

        this.population = population;
    }

    /**
     * @return the generalSearch
     */
    public Boolean getGeneralSearch()
    {

        return generalSearch;
    }

    /**
     * @param generalSearch
     *            the generalSearch to set
     */
    public void setGeneralSearch(Boolean generalSearch)
    {

        this.generalSearch = generalSearch;
    }

}
