package gov.nih.tbi.reporting;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.constants.ReportingPortalConstants;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.taglib.datatableDecorators.AccountReportingListIdtDecorator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class AccountsReportingListAction extends BaseReportingAction {
	static Logger logger = Logger.getLogger(AccountsReportingListAction.class);

	private static final long serialVersionUID = -6130441485707145645L;

	private List<Account> accountList;


	// PaginationData
	private Integer numSearchResults;
	private Integer page;
	private Integer pageSize;
	private Boolean ascending;
	private Boolean howToReportingPage;
	private String sort;
	private String key;
	private Long ownerId;
	private Long filterId;
	private Long daysOld;

	// Access report download
	private String startAccessReportDate;
	private String endAccessReportDate;
	private InputStream inputStream;


	private String responseJson = "{}";
	private String inputJson = "{}";
	private String errRespMsg;
	private Long fsId;
	
	private static final String REPORT_TITLE = "Account Report";
	private static final String REPORT_TEMPLATE_URL = "/reportTemplates/account-report.jrxml";
	private static final String REPORT_FILE_NAME = "account-report";


	public String loginCheck() {

		if (getRequest().getRemoteUser() == null) {
			return ReportingPortalConstants.ACTION_IN;
		}
		return ReportingPortalConstants.ACTION_OUT;
	}


	/***************************************************************************/

	/**
	 * A helper function to serve the total number of results pages to the jsp
	 * 
	 * @return
	 */
	public Integer getNumPages() {
		return (int) Math.ceil(((double) numSearchResults) / ((double) pageSize));
	}


	/************************ ACTIONS *****************************/

	public StreamResult validationSuccess() {
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	public String showDownloadAccessReportLightbox() {
		return "accessRecordLightbox";
	}
	
	
	/**
	 * Navigates user to the list account page
	 * 
	 * @return
	 */
	public String list() {
		setIsHowToPage(false);
		return ReportingPortalConstants.ACTION_LIST;
	}

	
	public String howToReporting() {
		setIsHowToPage(true);
		return ReportingPortalConstants.HOW_TO_REPORT;
	}
	
	/**
	 * Search though account records and return a single page or results.
	 * 
	 * @return
	 */
	public String search() throws UnsupportedEncodingException {
		long startTime = System.nanoTime();

		// Get the studies
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		setAccountList(reportingManager.listAllAccounts());

		long endTime = System.nanoTime();
		logger.info("Execution time for " + accountList.size() + " studies: " + (endTime - startTime));

		return ReportingPortalConstants.ACTION_SEARCH;
	}


	public boolean getIsHowToPage() {
		return howToReportingPage;
	}
	
	public void setIsHowToPage(Boolean flag) {
		 howToReportingPage = flag;
	}
	
	public List<Account> getAccountList() {
		if (accountList == null) {
			accountList = new ArrayList<Account>();
		}
		return accountList;
	}

	// http://fitbir-portal-local.cit.nih.gov:8080/portal/account/accountAction!getAccountTableList.action
	public List<Account> getAccountsTableList() throws UnsupportedEncodingException {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			search();
			ArrayList<Account> outputList = new ArrayList<Account>(getAccountList());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new AccountReportingListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}



	public void setAccountList(List<Account> accountList) {
		this.accountList = accountList;
	}



	public String getStartAccessReportDate() {
		return startAccessReportDate;
	}

	public void setStartAccessReportDate(String startAccessReportDate) {
		this.startAccessReportDate = startAccessReportDate;
	}

	public String getEndAccessReportDate() {
		return endAccessReportDate;
	}

	public void setEndAccessReportDate(String endAccessReportDate) {
		this.endAccessReportDate = endAccessReportDate;
	}

	public String getCurrentDate() {
		return BRICSTimeDateUtil.formatDate(new Date());
	}

	public Integer getNumSearchResults() {
		return numSearchResults;
	}

	public void getNumSearchResults(Integer numSearchResults) {
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

	public Boolean getAscending() {
		return ascending;
	}

	public void setAscending(Boolean ascending) {
		this.ascending = ascending;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public boolean getEnforceStaticFields() {
		return false;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public Long getFilterId() {
		return filterId;
	}

	public void setFilterId(Long filterId) {
		this.filterId = filterId;
	}

	public Long getDaysOld() {
		return daysOld;
	}

	public void setDaysOld(Long daysOld) {
		this.daysOld = daysOld;
	}

	public String getResponseJson() {
		return responseJson;
	}

	public void setResponseJson(String responseJson) {
		this.responseJson = responseJson;
	}

	public Long getFsId() {
		return fsId;
	}

	public void setFsId(Long fsId) {
		this.fsId = fsId;
	}



	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getInputJson() {
		return inputJson;
	}

	public void setInputJson(String inputJson) {
		this.inputJson = inputJson;
	}

	public String getErrRespMsg() {
		return errRespMsg;
	}

	public void setErrRespMsg(String errRespMsg) {
		this.errRespMsg = errRespMsg;
	}

	public String exportHTML() {
		logger.info("In exportHTML()");
		JRDataSource jrDataSource = new JRBeanCollectionDataSource(reportingManager.listAllAccounts());
		jasperExportService.exportHTML(response, REPORT_TITLE, REPORT_TEMPLATE_URL, REPORT_FILE_NAME, null, jrDataSource);
		return NONE;
	}

	public String exportCSV() {
		logger.info("In exportCSV()");
		JRDataSource jrDataSource = new JRBeanCollectionDataSource(reportingManager.listAllAccounts());
		jasperExportService.exportCSV(response, REPORT_TITLE, REPORT_TEMPLATE_URL, REPORT_FILE_NAME, null, jrDataSource);
		return NONE;
	}

	public String exportXLS() {
		logger.info("In exportXLS()");
		JRDataSource jrDataSource = new JRBeanCollectionDataSource(reportingManager.listAllAccounts());
		jasperExportService.exportXLS(response, REPORT_TITLE, REPORT_TEMPLATE_URL, REPORT_FILE_NAME, null, jrDataSource);
		return NONE;
	}

	public String exportPDF() {
		logger.info("In exportPDF()");
		JRDataSource jrDataSource = new JRBeanCollectionDataSource(reportingManager.listAllAccounts());
		jasperExportService.exportPDF(response, REPORT_TITLE, REPORT_TEMPLATE_URL, REPORT_FILE_NAME, null, jrDataSource);
		return NONE;
	}

}
