package gov.nih.tbi.account.portal;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ProcessingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonSyntaxException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.SessionLog;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.service.SessionLogManager;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.idt.ws.IdtColumnDescriptor;
import gov.nih.tbi.idt.ws.IdtFilterDescription;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.IdtRequest;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

/**
 * Action for the Session Log page
 * 
 * @author Joshua Park(jospark)
 *
 */
public class SessionLogAction extends BaseAction {

	private static final long serialVersionUID = -3630698104040688193L;
	
	static Logger logger = Logger.getLogger(SessionLogAction.class);
	
    private Integer numSearchResults;
    private Integer page;
    private Integer pageSize;
    private String sort;
    private Boolean ascending;
    
    private String filteredData;
    private Long chunkedPart;
	
	@Autowired
	protected SessionLogManager sessionLogManager;
	
	public String view() {
		return PortalConstants.ACTION_VIEW;
	}
	
	/**
	 * Provide the datatable query response in the form of an IdtInterface response.
	 * 
	 */
	public void combinedSearchIdt() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			IdtRequest request = idt.getRequest();

			updateOrder(request.getOrderColumn());

			PaginationData pageData = new PaginationData(request.getPageNumber(), request.getLength(), ascending, sort);
			
			String filterStatus = "";
			String filterStartDate = null;
			String filterEndDate = null;
			
			for (IdtFilterDescription idtFilter : request.getFilters()) {
				if (idtFilter.getName().equals("Status")) {
					filterStatus = idtFilter.getValue();
				}
				if (idtFilter.getName().equals("startDate")) {
					filterStartDate = idtFilter.getValue();
				}
				if (idtFilter.getName().equals("endDate")) {
					filterEndDate = idtFilter.getValue();
				}
			}
			
			List<String> searchColumn = new ArrayList<String>();
			for (IdtColumnDescriptor icd : request.getColumnDescriptions()) {
				if (icd.isSearchable()) {
					searchColumn.add(icd.getData());
				}
			}

			//List<GuidSearchResult> searchResults = guidWebserviceProvider.searchGuids(jwt, pageData,
			//		request.getSearchVal(), filterType, hideDuplicate, showAll, searchColumn, request.getExport());
			
			List<SessionLog> searchResults = sessionLogManager.search(pageData, request.getSearchVal(), searchColumn, filterStatus, filterStartDate, filterEndDate, request.getExport());

			// pagedata has been updated to contain total count and filtered count
			idt.setTotalRecordCount(pageData.getNumSearchResults());
			idt.setFilteredRecordCount(pageData.getNumFilteredResults());
			
			idt.setList(new ArrayList<SessionLog>(searchResults));
			
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		catch (JsonSyntaxException | ProcessingException e) {
			logger.error("failed to obtain a user JWT for the GUID service");
			e.printStackTrace();
		}
	}
	
	private void updateOrder(IdtColumnDescriptor orderColumn) {
		sort = "timeIn";
		ascending = false;
		if (orderColumn != null) {
			String orderData = orderColumn.getData();
			sort = orderData;
			// the sql injection protection and mapping happens in the DAO
			ascending = orderColumn.getOrderDirection().equals(IdtRequest.ORDER_ASCENDING);
		}
	}
	
	public Integer getNumSearchResults()
    {

        return numSearchResults;
    }

    public void setNumSearchResults(Integer numSearchResults)
    {

        this.numSearchResults = numSearchResults;
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

    public String getSort()
    {

        return sort;
    }

    public void setSort(String sort)
    {

        this.sort = sort;
    }

    public Boolean getAscending()
    {

        return ascending;
    }

    public void setAscending(Boolean ascending)
    {

        this.ascending = ascending;
    }

    /**
     * A helper function to serve the total number of results pages to the jsp
     * 
     * @return
     */
    public Integer getNumPages()
    {

        return (int) Math.ceil(((double) numSearchResults) / ((double) pageSize));
    }
    
	public String getFilteredData() {
		return filteredData;
	}

	public void setFilteredData(String filteredData) {
		this.filteredData = filteredData;
	}

	public Long getChunkedPart() {
		return chunkedPart;
	}

	public void setChunkedPart(Long chunkedPart) {
		this.chunkedPart = chunkedPart;
	}
}
