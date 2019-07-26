
package gov.nih.tbi.dictionary.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class SessionDataStructureSearchCriteria implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String searchKey;
    private Set<String> searchTerms;
    
    private int ownerId;
    private String requiredForm;
    private Set<String> statuses;
    private Set<String> standardizations;
    private Set<String> formTypes;
    private Set<String> diseases;
    private Map<String, Set<String>> facets;
    private String isCopyrighted;

    public String getSearchKey()
    {

        return searchKey;
    }

    public void setSearchKey(String searchKey)
    {

        this.searchKey = searchKey;
    }

    public Set<String> getSearchTerms()
    {
    
        return searchTerms;
    }

    
    public void setSearchTerms(Set<String> searchTerms)
    {
    
        this.searchTerms = searchTerms;
    }

    public int getOwnerId()
    {

        return ownerId;
    }

    public void setOwnerId(int ownerId)
    {

        this.ownerId = ownerId;
    }

    public String getRequiredForm() {
		return requiredForm;
	}

	public void setRequiredForm(String selectedRequiredOptions) {
		this.requiredForm = selectedRequiredOptions;
	}

	public Set<String> getStatuses()
    {

        return statuses;
    }

    public void setStatuses(Set<String> statuses)
    {

        this.statuses = statuses;
    }

    public Set<String> getStandardizations() {
		return standardizations;
	}

	public void setStandardizations(Set<String> standardizations) {
		this.standardizations = standardizations;
	}

	public Set<String> getFormTypes()
    {

        return formTypes;
    }

    public void setFormTypes(Set<String> formTypes)
    {

        this.formTypes = formTypes;
    }

    public Set<String> getDiseases()
    {

        return diseases;
    }

    public void setDiseases(Set<String> diseases)
    {

        this.diseases = diseases;
    }

    public Map<String, Set<String>> getFacets()
    {

        return facets;
    }

    public void setFacets(Map<String, Set<String>> facets)
    {

        this.facets = facets;
    }
    
	public String getIsCopyrighted() {

		return this.isCopyrighted;
	}

	public void setIsCopyrighted(String isCopyrighted) {

		this.isCopyrighted = isCopyrighted;
	}

}
