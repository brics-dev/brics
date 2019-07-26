
package gov.nih.tbi.commons.util;

import java.util.List;

/**
 * The PaginationData class transports pagination and sorting information from the TBI portal to the dao layer, where
 * they are used in hibernate calls to restrict the query results to a single sorted page of the desired size.
 * Information about the query is also carried back up the stack for use by the portal. Since this class is only a
 * container for multiple pieces of data, it contains only constructors and getter/setter methods.
 * 
 * @author mvalei
 * 
 */
public class PaginationData
{

    /**
     * This string names an element in the hibernate class that the user wishes to sort the results by.
     */
    private String sort;

    /**
     * If true, results will be sorted in ascending order.
     */
    private Boolean ascending;

    /**
     * page: the page number of results to query requesting pageSize: 2 and page: 3 would return the 5th and 6th query
     * result
     */
    private Integer page;

    /**
     * pageSize: the number of results to display per page
     */
    private Integer pageSize;
    
    /**
     * pageLength: the number of results to display per page
     */
    private Integer pageLength;

    /**
     * A return value the indicates the total number of results returned by the query without any pagination
     */
    private int numSearchResults;

	/**
	 * A return value that indicates the total number of results returned by the query with no pagination but with
	 * search/filters applied
	 */
	private int numFilteredResults;

    /**
     * A list of columns to include when looking for a searchkey in an entry.
     */
    private List<String> searchLocations;

    public PaginationData()
    {

    }

    public PaginationData(Integer page, Integer pageSize)
    {

        this.page = page;
        this.pageSize = pageSize;

    }

    public PaginationData(Integer page, Integer pageSize, Boolean ascending, String sort)
    {

        this.page = page;
        this.pageSize = pageSize;
        this.ascending = ascending;
        this.sort = sort;

    }

    public PaginationData(Integer page, Integer pageSize, Boolean ascending, String sort, List<String> searchLocations)
    {

        this.page = page;
        this.pageSize = pageSize;
        this.ascending = ascending;
        this.sort = sort;
        this.searchLocations = searchLocations;
    }

    /**
     * TODO: refactor the sorting on the jsp so we don't have to do this translation hack
     * 
     * @return the sort
     */
    public String getSort()
    {

        if ("datatype".equalsIgnoreCase(sort))
        {
            this.sort = "type";
        }
        return sort;
    }

    /**
     * @param sort
     *            the sort to set
     */
    public void setSort(String sort)
    {

        this.sort = sort;
    }

    /**
     * @return the ascending
     */
    public Boolean getAscending()
    {

        return ascending;
    }

    /**
     * @param ascending
     *            the ascending to set
     */
    public void setAscending(Boolean ascending)
    {

        this.ascending = ascending;
    }

    /**
     * @return the page
     */
    public Integer getPage()
    {

        return page;
    }

    /**
     * @param page
     *            the page to set
     */
    public void setPage(int page)
    {

        this.page = page;
    }

    /**
     * @return the pageSize
     */
    public Integer getPageSize()
    {

        return pageSize;
    }

    /**
     * @param pageSize
     *            the pageSize to set
     */
    public void setPageSize(int pageSize)
    {

        this.pageSize = pageSize;
    }
    
  
    /**
     * @return the pageLength
     */
    public Integer getPageLength()
    {

        return pageLength;
    }

    /**
     * @param pageSize
     *            the pageLength to set
     */
    public void setPageLength(int pageLength)
    {

        this.pageLength = pageLength;
    }

    /**
     * @return the numSearchResults
     */
    public int getNumSearchResults()
    {

        return numSearchResults;
    }

    /**
     * @param numSearchResults
     *            the numSearchResults to set
     */
    public void setNumSearchResults(int numSearchResults)
    {

        this.numSearchResults = numSearchResults;
    }

	public int getNumFilteredResults() {
		return numFilteredResults;
	}

	public void setNumFilteredResults(int numFilteredResults) {
		this.numFilteredResults = numFilteredResults;
	}

	/**
	 * @return the searchLocations
	 */
    public List<String> getSearchLocations()
    {

        return searchLocations;
    }

    /**
     * @param searchLocations
     *            the searchLocations to set
     */
    public void setSearchLocations(List<String> searchLocations)
    {

        this.searchLocations = searchLocations;
    }

}
