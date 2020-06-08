
package gov.nih.tbi.repository.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.guid.portal.GuidAction;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The action for download queues
 * 
 * @author Francis Chen
 */
public class DownloadQueueAction extends BaseRepositoryAction implements Serializable
{

    private static final long serialVersionUID = 5051826998207619124L;
    
    private static final Logger logger = Logger.getLogger(DownloadQueueAction.class);

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
    
    public String viewWebstartDownloadTool()
    {
    	return PortalConstants.ACTION_VIEW_WEBSTART_DT;
    }
    
    public String viewJsDownloadTool()
    {
    	return PortalConstants.ACTION_VIEW_JS_DT;
    }
    
    public String getJwt() {
    	return getMicroserviceJwt();
    }
}
