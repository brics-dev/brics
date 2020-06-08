
package gov.nih.tbi.guid.portal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ProcessingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonSyntaxException;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.GuidSubject;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.guid.exception.InvalidJwtException;
import gov.nih.tbi.guid.model.GuidSearchResult;
import gov.nih.tbi.guid.util.GuidPdfGenerator;
import gov.nih.tbi.guid.ws.GuidWebserviceProvider;
import gov.nih.tbi.guid.ws.exception.AuthenticationException;
import gov.nih.tbi.idt.ws.IdtColumnDescriptor;
import gov.nih.tbi.idt.ws.IdtFilterDescription;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.IdtRequest;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.taglib.datatableDecorators.GuidListIdtDecorator;

/**
 * Search Guid Action
 * 
 * @author Andrew Johnson
 */
public class SearchGuidAction extends BaseAction {

	private static final long serialVersionUID = 3313914058412954589L;

	@Autowired
	private GuidWebserviceProvider guidWebserviceProvider;

	@Autowired
	private ModulesConstants modulesConstants;

	/******************************************************************************************************/

	static Logger logger = Logger.getLogger(SearchGuidAction.class);

	private List<GuidSubject> filteredGuidList;
	private String jsonData;

	private Integer numSearchResults;
	private Integer page;
	private Integer pageSize;
	private String sort;
	private Boolean ascending;

	private String filteredData;
	private Long chunkedPart;

	private InputStream inputStream;

	private long contentLength;

	private String guidType;
	private String hideDuplicate;
	private String showGuidsEntity;
	private String searchInput;
	private int rowCount;
	private List<String> searchColumns;

	public List<String> getSearchColumns() {
		return searchColumns;
	}

	public void setSearchColumns(String searchColumns) {
		this.searchColumns = BRICSStringUtils.delimitedStringToList(searchColumns, ",");
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public String getGuidType() {
		return guidType;
	}

	public void setGuidType(String guidType) {
		this.guidType = guidType;
	}


	public String getHideDuplicate() {
		return hideDuplicate;
	}

	public void setHideDuplicate(String hideDuplicate) {
		this.hideDuplicate = hideDuplicate;
	}

	public String getShowGuidsEntity() {
		return showGuidsEntity;
	}

	public void setShowGuidsEntity(String showGuidsEntity) {
		this.showGuidsEntity = showGuidsEntity;
	}

	public String getSearchInput() {
		return searchInput;
	}

	public void setSearchInput(String searchInput) {
		this.searchInput = searchInput;
	}

	public long getContentLength() {
		return contentLength;
	}

	/******************************************************************************************************/

	public List<? extends GuidSubject> getFilteredGuidList() {

		return filteredGuidList;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public Integer getNumSearchResults() {

		return numSearchResults;
	}

	public void setNumSearchResults(Integer numSearchResults) {

		this.numSearchResults = numSearchResults;
	}

	public Integer getPage() {

		return page;
	}

	public void setPage(Integer page) {

		this.page = page;
	}

	public Integer getPageSize() {

		return pageSize;
	}

	public void setPageSize(Integer pageSize) {

		this.pageSize = pageSize;
	}

	public String getSort() {

		return sort;
	}

	public void setSort(String sort) {

		this.sort = sort;
	}

	public Boolean getAscending() {

		return ascending;
	}

	public void setAscending(Boolean ascending) {

		this.ascending = ascending;
	}

	/**
	 * A helper function to serve the total number of results pages to the jsp
	 * 
	 * @return
	 */
	public Integer getNumPages() {

		return (int) Math.ceil(((double) numSearchResults) / ((double) pageSize));
	}

	/******************************************************************************************************/

	public String downloadPdf() {
		try {
			PaginationData pageData = new PaginationData(0, rowCount, true, "guid");

			String filterType = guidType;
			boolean hideDuplicateEntries = PortalConstants.HIDE_DUPLICATE_ENTRIES.equals(hideDuplicate);
			boolean showAll = PortalConstants.SHOW_GUIDS_ACROSS_ENTITIES.equals(showGuidsEntity);

			List<String> searchColumn = this.searchColumns;

			String jwt;
			jwt = getGuidJwt();

			List<GuidSearchResult> searchResults = guidWebserviceProvider.searchGuids(jwt, pageData, searchInput,
					filterType, hideDuplicateEntries, showAll, searchColumn, true, getInAdmin());

			GuidPdfGenerator pdfGenerator = new GuidPdfGenerator(searchResults);
			byte[] fileData = pdfGenerator.generate(modulesConstants.getModulesOrgName());
			contentLength = fileData.length;
			inputStream = new ByteArrayInputStream(fileData);
		} catch (JsonSyntaxException | ProcessingException | InvalidJwtException | AuthenticationException e) {
			logger.error("failed to obtain a user JWT for the GUID service");
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("failed generate a PDF from GUID search results");
			e.printStackTrace();
		}

		return "pdf";
	}

	public void combinedSearchIdt() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			IdtRequest request = idt.getRequest();

			updateGuidOrder(request.getOrderColumn());

			PaginationData pageData = new PaginationData(request.getPageNumber(), request.getLength(), ascending, sort);

			String filterType = "";
			boolean hideDuplicate = false;
			boolean showAll = false;

			for (IdtFilterDescription idtFilter : request.getFilters()) {
				if (idtFilter.getName().equals("Show Duplicate Entries")) {
					hideDuplicate = idtFilter.getValue().equals("hide");
				}

				if (idtFilter.getName().equals("All")) {
					filterType = idtFilter.getValue();
				}

				if (idtFilter.getName().equals("Mine Only")) {
					showAll = idtFilter.getValue().equals("showAll");
				}

				if (idtFilter.getName().equals("Show GUIDs From My Entity")) {
					showAll = idtFilter.getValue().equals("showAll");
				}
			}

			List<String> searchColumn = new ArrayList<String>();
			for (IdtColumnDescriptor icd : request.getColumnDescriptions()) {
				if (icd.isSearchable()) {
					searchColumn.add(icd.getData());
				}
			}

			String jwt;
			jwt = getGuidJwt();

			List<GuidSearchResult> searchResults =
					guidWebserviceProvider.searchGuids(jwt, pageData, request.getSearchVal(), filterType, hideDuplicate,
							showAll, searchColumn, request.getExport(), getInAdmin());

			// pagedata has been updated to contain total count and filtered count
			idt.setTotalRecordCount(pageData.getNumSearchResults());
			idt.setFilteredRecordCount(pageData.getNumFilteredResults());

			idt.setList(new ArrayList<GuidSearchResult>(searchResults));
			idt.decorate(new GuidListIdtDecorator());

			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		} catch (JsonSyntaxException | ProcessingException | InvalidJwtException | AuthenticationException e) {
			logger.error("failed to obtain a user JWT for the GUID service");
			e.printStackTrace();
		}
	}

	private void updateGuidOrder(IdtColumnDescriptor orderColumn) {
		sort = "guid";
		if (orderColumn != null) {
			String orderData = orderColumn.getData();
			sort = orderData;
			// the sql injection protection and mapping happens in the DAO
			ascending = orderColumn.getOrderDirection().equals(IdtRequest.ORDER_ASCENDING);
		}
	}

	/******************************************************************************************************/

	/**
	 * Returns true if namespace is 'guidAdmin'
	 */
	public boolean getInAdmin() {

		return PortalConstants.NAMESPACE_GUID_ADMIN.equals(getNameSpace());
	}

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
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


	/******************************************************************************************************/
}
