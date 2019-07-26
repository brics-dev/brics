package gov.nih.tbi.reporting;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.RecruitmentStatus;
import gov.nih.tbi.commons.model.ResearchManagementRole;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.constants.ReportingPortalConstants;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudyForm;
import gov.nih.tbi.repository.model.hibernate.StudyKeyword;
import gov.nih.tbi.taglib.datatableDecorators.StudyReportingListIdtDecorator;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class StudiesReportingListAction extends BaseReportingAction {
	static Logger logger = Logger.getLogger(StudiesReportingListAction.class);

	private static final long serialVersionUID = -6130441485707145645L;

	private List<Study> studyList;

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

	private StudyKeyword newKeyword;
	private String keywordSearchKey;

	// Access report download
	private String startAccessReportDate;
	private String endAccessReportDate;
	private InputStream inputStream;

	private StudyForm studyFormEntry;
	private String responseJson = "{}";
	private String inputJson = "{}";
	private String errRespMsg;
	private Long fsId;
	private String startDate;
	private String endDate;

	private String primaryPrincipalInvestigatorCount;
	private String principallInvestigatorCount;
	private String associatePrincipalInvestigatorCount;
	private String totalStudyInvestigatorCount;
	
	private String privateStudyCount;
	private String publicStudyCount;
	private String requestedStudyCount;
	private String rejectedStudyCount;
	private String totalStudyCount;
	
	private static final String REPORT_TITLE = "Study Metadata Report";
	private static final String REPORT_TEMPLATE_URL = "/reportTemplates/study-report.jrxml";
	private static final String REPORT_FILE_NAME = "study-metadata-report";
	
	public String getTotalStudyInvestigatorCount() {
		return totalStudyInvestigatorCount;
	}

	public void setTotalStudyInvestigatorCount(String totalStudyInvestigatorCount) {
		this.totalStudyInvestigatorCount = totalStudyInvestigatorCount;
	}

	public String getPrimaryPrincipalInvestigatorCount() {
		return primaryPrincipalInvestigatorCount;
	}

	public void setPrimaryPrincipalInvestigatorCount(String primaryPrincipalInvestigatorCount) {
		this.primaryPrincipalInvestigatorCount = primaryPrincipalInvestigatorCount;
	}

	public String getPrincipallInvestigatorCount() {
		return principallInvestigatorCount;
	}

	public void setPrincipallInvestigatorCount(String principallInvestigatorCount) {
		this.principallInvestigatorCount = principallInvestigatorCount;
	}

	public String getAssociatePrincipalInvestigatorCount() {
		return associatePrincipalInvestigatorCount;
	}

	public void setAssociatePrincipalInvestigatorCount(String associatePrincipalInvestigatorCount) {
		this.associatePrincipalInvestigatorCount = associatePrincipalInvestigatorCount;
	}

	public String getPrivateStudyCount() {
		return privateStudyCount;
	}

	public void setPrivateStudyCount(String privateStudyCount) {
		this.privateStudyCount = privateStudyCount;
	}

	public String getPublicStudyCount() {
		return publicStudyCount;
	}

	public void setPublicStudyCount(String publicStudyCount) {
		this.publicStudyCount = publicStudyCount;
	}

	public String getRequestedStudyCount() {
		return requestedStudyCount;
	}

	public void setRequestedStudyCount(String requestedStudyCount) {
		this.requestedStudyCount = requestedStudyCount;
	}

	public String getRejectedStudyCount() {
		return rejectedStudyCount;
	}

	public void setRejectedStudyCount(String rejectedStudyCount) {
		this.rejectedStudyCount = rejectedStudyCount;
	}

	public String getTotalStudyCount() {
		return totalStudyCount;
	}

	public void setTotalStudyCount(String totalStudyCount) {
		this.totalStudyCount = totalStudyCount;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

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

	public RecruitmentStatus[] getRecruitmentStatuses() {
		return RecruitmentStatus.getStudyOnlyRecruitmentStatus();
	}

	/************************ ACTIONS *****************************/

	public StreamResult validationSuccess() {
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	public String showDownloadAccessReportLightbox() {
		return "accessRecordLightbox";
	}

	/**
	 * Navigates user to the list study page
	 * 
	 * @return
	 */
	public String list() {
		setIsHowToPage(false);
		List<Study> studies = reportingManager.getAllStudiesWithKeyword();
		// setting the study investigator count
		getStudyInvestigatorTotalCount(studies);
		getStudyCounts(studies);
		
		return ReportingPortalConstants.ACTION_LIST;
	}

	public String howToReporting() {
		setIsHowToPage(true);
		return ReportingPortalConstants.HOW_TO_REPORT;
	}

	/**
	 * Search though study records and return a single page or results.
	 * 
	 * @return
	 */
	public String search() throws UnsupportedEncodingException {
		long startTime = System.nanoTime();
		List<Study> studies = reportingManager.getAllStudiesWithKeyword();
		List<Study> filteredList = new ArrayList<Study>();

		filteredList = filterStudy(studies);

		setStudyList(filteredList);
		long endTime = System.nanoTime();
		logger.info("Execution time for " + studyList.size() + " studies: " + (endTime - startTime));
		return ReportingPortalConstants.ACTION_SEARCH;
	}

	private List<Study> filterStudy(List<Study> studies) {
		logger.info("Inside filterStudy ");
		List<Study> filteredList = new ArrayList<Study>();
		LocalDate startDate = null;
		LocalDate endDate = null;

		if (getStartDate() != null && !getStartDate().isEmpty()) {
			startDate = LocalDate.parse(getStartDate());
		}

		if (getEndDate() != null && !getEndDate().isEmpty()) {
			endDate = LocalDate.parse(getEndDate());
		}

		for (Study study : studies) {
			LocalDate studyStartDate = getLocalDate(study.getStudyStartDate());
			LocalDate studyEndDate = getLocalDate(study.getStudyEndDate());
			logger.info("Inside filterStudy studyStartDate :: " + studyStartDate + " studyEndDate :: " + studyEndDate
					+ " startDate ::  " + startDate + " endDate ::  " + endDate);

			if (startDate != null && (getEndDate() == null || getEndDate().isEmpty())) {
				if (studyStartDate.isAfter(startDate) || studyStartDate.equals(startDate)) {
					filteredList.add(study);
				}

			} else if (endDate != null && (getStartDate() == null || getStartDate().isEmpty())) {
				if (studyEndDate.isBefore(endDate) || studyEndDate.equals(endDate)) {
					filteredList.add(study);
				}

			} else if (startDate != null && endDate != null) {
				if ((studyStartDate.isAfter(startDate) || studyStartDate.equals(startDate))
						&& (studyEndDate.isBefore(endDate) || studyEndDate.equals(endDate))) {
					filteredList.add(study);
				}
			} else {
				filteredList.add(study);
			}

		}

		return filteredList;
	}

	private LocalDate getLocalDate(Date date) {
		Instant instant = date.toInstant();
		ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
		LocalDate localDate = zdt.toLocalDate();
		return localDate;
	}

	public boolean getIsHowToPage() {
		return howToReportingPage;
	}

	public void setIsHowToPage(Boolean flag) {
		howToReportingPage = flag;
	}

	public List<Study> getStudyList() {
		if (studyList == null) {
			studyList = new ArrayList<Study>();
		}
		return studyList;
	}

	public List<Study> getStudyTableList() throws UnsupportedEncodingException {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			search();
			ArrayList<Study> outputList = new ArrayList<Study>(getStudyList());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new StudyReportingListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	public void setStudyList(List<Study> studyList) {
		this.studyList = studyList;
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

	public StudyKeyword getNewKeyword() {
		return newKeyword;
	}

	public void setNewKeyword(StudyKeyword newKeyword) {
		this.newKeyword = newKeyword;
	}

	public String getKeywordSearchKey() {
		return keywordSearchKey;
	}

	public void setKeywordSearchKey(String keywordSearchKey) {
		this.keywordSearchKey = keywordSearchKey;
	}

	public Long getFsId() {
		return fsId;
	}

	public void setFsId(Long fsId) {
		this.fsId = fsId;
	}

	public StudyForm getStudyFormEntry() {
		return studyFormEntry;
	}

	public void setStudyFormEntry(StudyForm studyFormEntry) {
		this.studyFormEntry = studyFormEntry;
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

	private void getStudyInvestigatorTotalCount(List<Study> studies) {
		long countPrimaryPrincipalInvestigator = 0;
		long countPrincipalInvestigator = 0;
		long countAssociatePrincipalInvestigator = 0;

		for (Study study : studies) {
			logger.info("--------study id " + study.getId());
			Set<ResearchManagement> researchMgmtSet = study.getResearchMgmtSet();
			logger.info("researchManagement size " + researchMgmtSet.size());
			
			for (ResearchManagement researchManagement : researchMgmtSet) {
				

				if (researchManagement.getRole() == ResearchManagementRole.PRIMARY_PRINCIPAL_INVESTIGATOR) {
					logger.info(" role : " + researchManagement.getRole());
					countPrimaryPrincipalInvestigator++;
				}

				if (researchManagement.getRole() == ResearchManagementRole.PRINCIPAL_INVESTIGATOR) {
					countPrincipalInvestigator++;
				}

				if (researchManagement.getRole() == ResearchManagementRole.ASSOCIATE_PRINCIPAL_INVESTIGATOR) {
					countAssociatePrincipalInvestigator++;
				}
			}
		}

		setPrimaryPrincipalInvestigatorCount(String.valueOf(countPrimaryPrincipalInvestigator));
		setPrincipallInvestigatorCount(String.valueOf(countPrincipalInvestigator));
		setAssociatePrincipalInvestigatorCount(String.valueOf(countAssociatePrincipalInvestigator));
		setTotalStudyInvestigatorCount(String.valueOf(countPrimaryPrincipalInvestigator+countPrincipalInvestigator+countAssociatePrincipalInvestigator));
	}
	
	private void getStudyCounts(List<Study> studies) {
		long privateStudyCount = 0;
		long publicStudyCount = 0;
		long requestedStudyCount = 0;
		long rejectedStudyCount = 0;
		long totalStudyCount = 0;
		for (Study study : studies) {
			switch (study.getStudyStatus()) {
				case PRIVATE:
					privateStudyCount++;
					break;
				case PUBLIC:
					publicStudyCount++;
					break;
				case REQUESTED:
					requestedStudyCount++;
					break;
				case REJECTED:
					rejectedStudyCount++;
					break;
			}
			totalStudyCount++;
		}
		setPrivateStudyCount(String.valueOf(privateStudyCount));
		setPublicStudyCount(String.valueOf(publicStudyCount));
		setRequestedStudyCount(String.valueOf(requestedStudyCount));
		setRejectedStudyCount(String.valueOf(rejectedStudyCount));
		setTotalStudyCount(String.valueOf(totalStudyCount));
	}

	public String exportHTML() {
		logger.info("In exportHTML()");
		JRDataSource jrDataSource = new JRBeanCollectionDataSource(reportingManager.getAllStudiesWithKeyword());
		jasperExportService.exportHTML(response, REPORT_TITLE, REPORT_TEMPLATE_URL, REPORT_FILE_NAME, null, jrDataSource);
		return NONE;
	}

	public String exportCSV() {
		logger.info("In exportCSV()");
		JRDataSource jrDataSource = new JRBeanCollectionDataSource(reportingManager.getAllStudiesWithKeyword());
		jasperExportService.exportCSV(response, REPORT_TITLE, REPORT_TEMPLATE_URL, REPORT_FILE_NAME, null, jrDataSource);
		return NONE;
	}

	public String exportXLS() {
		logger.info("In exportXLS()");
		JRDataSource jrDataSource = new JRBeanCollectionDataSource(reportingManager.getAllStudiesWithKeyword());
		jasperExportService.exportXLS(response, REPORT_TITLE, REPORT_TEMPLATE_URL, REPORT_FILE_NAME, null, jrDataSource);
		return NONE;
	}

	public String exportPDF() {
		logger.info("In exportPDF()");
		JRDataSource jrDataSource = new JRBeanCollectionDataSource(reportingManager.getAllStudiesWithKeyword());
		jasperExportService.exportPDF(response, REPORT_TITLE, REPORT_TEMPLATE_URL, REPORT_FILE_NAME, null, jrDataSource);
		return NONE;
	}

}
