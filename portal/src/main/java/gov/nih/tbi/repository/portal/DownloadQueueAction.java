
package gov.nih.tbi.repository.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.util.PaginationData;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * The action for download queues
 * 
 * @author Francis Chen
 */
public class DownloadQueueAction extends BaseRepositoryAction implements Serializable
{

    private static final long serialVersionUID = 5051826998207619124L;

    @Autowired
    RepositoryManager repositoryManager;

    private Integer page;
    private Integer pageSize;
    private PaginationData pageData;
    private Integer numSearchResults;
    private Boolean ascending;
    private String sort;
    private String selectedIds;
    private Integer numPages;
        
    
    public Integer getNumPages()
    {
    
        return numPages;
    }

    
    public void setNumPages(Integer numPages)
    {
    
        this.numPages = numPages;
    }

    public Boolean getAscending()
    {

        return ascending;
    }

    public String getSelectedIds()
    {

        return selectedIds;
    }

    public void setSelectedIds(String selectedIds)
    {

        this.selectedIds = selectedIds;
    }

    public void setAscending(Boolean ascending)
    {

        this.ascending = ascending;
    }

    public String getSort()
    {

        return sort;
    }

    public void setSort(String sort)
    {

        this.sort = sort;
    }

    public PaginationData getPageData()
    {

        return pageData;
    }

    public void setPageData(PaginationData pageData)
    {

        this.pageData = pageData;
    }


    public Integer getNumSearchResults()
    {

        return numSearchResults;
    }

    public Integer getPage()
    {

        return page;
    }

    public void setPage(Integer page)
    {

        this.page = page;
    }

    public Integer getPageSize()
    {

        return pageSize;
    }

    public void setPageSize(Integer pageSize)
    {

        this.pageSize = pageSize;
    }

    /******************************************************************************************************/

    /**
     * Action to view the download queue
     * 
     * @return
     */
    public String view()
    {

        return PortalConstants.ACTION_VIEW;
    }
}
