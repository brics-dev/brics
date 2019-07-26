
package gov.nih.tbi.dictionary.model;

import java.io.Serializable;
import java.util.Set;

/**
 * Session bean to store previous values for data element search drop down options. This allows us to revert to previous
 * options when filtering to improve usability. // TODO: MV 5/30/2014 - This class is deprecated. It should be removed
 * from the project before release.
 * 
 * @author dhollo
 * 
 */

public class SessionDataElementFilterValues implements Serializable
{

    private static final long serialVersionUID = 1L;

    private Set<String> allDiseaseOptions;
    private Set<String> allDomainOptions;
    private Set<String> allSubDomainOptions;
    private Set<String> previousDomainOptions;
    private Set<String> previousSubDomainOptions;

    public Set<String> getAllDiseaseOptions()
    {

        return allDiseaseOptions;
    }

    public void setAllDiseaseOptions(Set<String> allDiseaseOptions)
    {

        this.allDiseaseOptions = allDiseaseOptions;
    }

    public Set<String> getAllDomainOptions()
    {

        return allDomainOptions;
    }

    public void setAllDomainOptions(Set<String> allDomainOptions)
    {

        this.allDomainOptions = allDomainOptions;
    }

    public Set<String> getAllSubDomainOptions()
    {

        return allSubDomainOptions;
    }

    public void setAllSubDomainOptions(Set<String> allSubDomainOptions)
    {

        this.allSubDomainOptions = allSubDomainOptions;
    }

    public Set<String> getPreviousDomainOptions()
    {

        return previousDomainOptions;
    }

    public void setPreviousDomainOptions(Set<String> previousDomainOptions)
    {

        this.previousDomainOptions = previousDomainOptions;
    }

    public Set<String> getPreviousSubDomainOptions()
    {

        return previousSubDomainOptions;
    }

    public void setPreviousSubDomainOptions(Set<String> previousSubDomainOptions)
    {

        this.previousSubDomainOptions = previousSubDomainOptions;
    }
}