
package gov.nih.tbi.dictionary.model;

import java.io.Serializable;
import java.util.List;

/***
 * This is a session object that will store the criteria for Data Element search in the data dictionary
 * 
 * @author mgree1
 * 
 */

public class SessionDataElementSearchCriteria implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String searchKey;
    private List<String> searchLocations;

    private int ownerId;
    private String modifiedDate;

    private List<String> statuses;
    private List<String> elementTypes;

    private List<String> populations;
    private List<String> diseases;
    private List<String> domains;
    private List<String> subdomains;
    private List<String> classifications;
    private List<String> subgroups;

    public String getSearchKey()
    {

        return searchKey;
    }

    public void setSearchKey(String searchKey)
    {

        this.searchKey = searchKey;
    }

    public int getOwnerId()
    {

        return ownerId;
    }

    public void setOwnerId(int ownerId)
    {

        this.ownerId = ownerId;
    }

    public String getModifiedDate()
    {

        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate)
    {

        this.modifiedDate = modifiedDate;
    }

    public List<String> getSearchLocations()
    {

        return searchLocations;
    }

    public void setSearchLocations(List<String> searchLocations)
    {

        this.searchLocations = searchLocations;
    }

    public List<String> getStatuses()
    {

        return statuses;
    }

    public void setStatuses(List<String> statuses)
    {

        this.statuses = statuses;
    }

    public List<String> getElementTypes()
    {

        return elementTypes;
    }

    public void setElementTypes(List<String> elementTypes)
    {

        this.elementTypes = elementTypes;
    }

    public List<String> getPopulations()
    {

        return populations;
    }

    public void setPopulations(List<String> populations)
    {

        this.populations = populations;
    }

    public List<String> getDiseases()
    {

        return diseases;
    }

    public void setDiseases(List<String> diseases)
    {

        this.diseases = diseases;
    }

    public List<String> getDomains()
    {

        return domains;
    }

    public void setDomains(List<String> domains)
    {

        this.domains = domains;
    }

    public List<String> getSubdomains()
    {

        return subdomains;
    }

    public void setSubdomains(List<String> subdomains)
    {

        this.subdomains = subdomains;
    }

    public List<String> getClassifications()
    {

        return classifications;
    }

    public void setClassifications(List<String> classifications)
    {

        this.classifications = classifications;
    }

    public List<String> getSubgroups()
    {

        return subgroups;
    }

    public void setSubgroups(List<String> subgroups)
    {

        this.subgroups = subgroups;
    }

}
