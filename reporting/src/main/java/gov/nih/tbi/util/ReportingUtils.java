package gov.nih.tbi.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.ResearchManagementRole;
import gov.nih.tbi.commons.service.ReportingManager;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudyKeyword;
import gov.nih.tbi.repository.model.hibernate.StudySite;
import gov.nih.tbi.service.model.AccountReportModel;
import gov.nih.tbi.service.model.StudyReportModel;

@Component
public class ReportingUtils {

	@Autowired
	protected ReportingManager reportingManager;

	private String primaryPrincipalInvestigator;

	private String principalInvestigator;

	private String associatePrincipalInvestigator;

	public String getPrimaryPrincipalInvestigator() {
		return primaryPrincipalInvestigator;
	}

	public void setPrimaryPrincipalInvestigator(String primaryPrincipalInvestigator) {
		this.primaryPrincipalInvestigator = primaryPrincipalInvestigator;
	}

	public String getPrincipalInvestigator() {
		return principalInvestigator;
	}

	public void setPrincipalInvestigator(String principalInvestigator) {
		this.principalInvestigator = principalInvestigator;
	}

	public String getAssociatePrincipalInvestigator() {
		return associatePrincipalInvestigator;
	}

	public void setAssociatePrincipalInvestigator(String associatePrincipalInvestigator) {
		this.associatePrincipalInvestigator = associatePrincipalInvestigator;
	}

	public List<StudyReportModel> getStudyReportModel() {
		List<StudyReportModel> studyReport = new ArrayList<StudyReportModel>();
		StudyReportModel studyReportModel = null;
		List<Study> studies = reportingManager.getAllStudiesWithKeyword();

		for (Study study : studies) {
			studyReportModel = new StudyReportModel();

			if(!StringUtils.isBlank(study.getTitle())) {
				studyReportModel.setStudyTitle(study.getTitle());
			}else {
				studyReportModel.setStudyTitle("");
			}
			
			studyReportModel.setStudyPrefixID(study.getPrefixedId());
			studyReportModel.setStudyDOI(study.getDoi());
			studyReportModel.setStudyType(getStudyTyp(study));
			studyReportModel.setStudyURL(study.getStudyUrl());
			studyReportModel.setFundingSource(getFundSrc(study));
			studyReportModel.setStudyStarted(DateConverter.convertToString(study.getStudyStartDate()));
			studyReportModel.setStudyEnded(DateConverter.convertToString(study.getStudyEndDate()));
			studyReportModel.setStudyDuration(study.getStudyDuration()); 
			studyReportModel.setStudyStatus(getStatus(study));
			studyReportModel.setPrimarySiteName(getPrimarySiteName(study));

			
			setPrimaryPrincipalInvestigator("");
			setPrincipalInvestigator("");
			setAssociatePrincipalInvestigator("");
			
			getStudyInvestigators(study);

			studyReportModel.setPrimaryPrincipalInvestigator(getPrimaryPrincipalInvestigator());
			studyReportModel.setPrincipalInvestigators(getPrincipalInvestigator());
			studyReportModel.setAssociatePrincipalInvestigators(getAssociatePrincipalInvestigator());
			studyReportModel.setRecruitmentStatus(getRecruitmentStatus(study));
			
			if(study.getNumberOfSubjects() != null) {
				studyReportModel.setNumberOfSubjects(study.getNumberOfSubjects().toString());
			}else {
				studyReportModel.setNumberOfSubjects("");
			}

			
			studyReportModel.setKeywords(getStudykeyword(study));

			studyReport.add(studyReportModel);

		}

		return studyReport;
	}

	/**
	 * Will get primary site name for study
	 * @param study
	 * @return
	 */
	private String getPrimarySiteName(Study study) {
		String primarySiteName = "";

		Set<StudySite> studySiteSet = study.getStudySiteSet();
		for (StudySite studySite : studySiteSet) {
			if (studySite.isPrimary()) {
				primarySiteName = studySite.getSiteName();
			}
		}
		return primarySiteName;
	}

	/**
	 * 
	 * @param accounts
	 * @return
	 */
	public List<AccountReportModel> getAccountReportModel(List<Account> accounts) {
		List<AccountReportModel> accountReport = new ArrayList<AccountReportModel>();

		AccountReportModel accountReportModel = null;

		for (Account a : accounts) {
			accountReportModel = new AccountReportModel();

			accountReport.add(accountReportModel);

		}

		return accountReport;
	}

	private String getStatus(Study study) {
		String output = "";
		output += study.getStudyStatus().getName();
		return output;
	}

	private String getStudyTyp(Study study) {
		String output = "";
		if (study.getStudyType() != null) {
			output += study.getStudyType().getName();
		}
		return output;
	}

	private String getStudykeyword(Study study) {
		String output = "";
		StringBuffer keyWord = new StringBuffer();
		for (StudyKeyword word : study.getKeywordSet()) {
			keyWord.append(word.getKeyword()).append(",");
		}

		output += keyWord.toString();
		return output;
	}

	private String getFundSrc(Study study) {
		String output = "";
		if (study.getFundingSource() != null) {
			output += study.getFundingSource().getName();
		}
		return output;
	}

	private String getRecruitmentStatus(Study study) {
		String output = "";
		if (study.getRecruitmentStatus() != null) {
			output += study.getRecruitmentStatus().getName();
		}
		return output;
	}

	private void getStudyInvestigators(Study study) {
		StringBuffer primaryPrincipalInvestigator = new StringBuffer();
		StringBuffer principalInvestigator = new StringBuffer();
		StringBuffer associatePrincipalInvestigator = new StringBuffer();

		Set<ResearchManagement> researchMgmtSet = study.getResearchMgmtSet();

		for (ResearchManagement researchManagement : researchMgmtSet) {

			if (researchManagement.getRole() == ResearchManagementRole.PRIMARY_PRINCIPAL_INVESTIGATOR) {
				primaryPrincipalInvestigator.append(researchManagement.getSuffix()).append(" ")
						.append(researchManagement.getFirstName()).append(" ").append(researchManagement.getMi())
						.append(" ").append(researchManagement.getLastName());

			}

			if (researchManagement.getRole() == ResearchManagementRole.PRINCIPAL_INVESTIGATOR) {
				principalInvestigator.append(researchManagement.getSuffix()).append(" ")
						.append(researchManagement.getFirstName()).append(" ").append(researchManagement.getMi())
						.append(" ").append(researchManagement.getLastName()).append(", ");

			}

			if (researchManagement.getRole() == ResearchManagementRole.ASSOCIATE_PRINCIPAL_INVESTIGATOR) {

				associatePrincipalInvestigator.append(researchManagement.getSuffix()).append(" ")
						.append(researchManagement.getFirstName()).append(" ").append(researchManagement.getMi())
						.append(" ").append(researchManagement.getLastName()).append(", ");

			}
		}

		setPrimaryPrincipalInvestigator(primaryPrincipalInvestigator.toString());
		setPrincipalInvestigator(principalInvestigator.toString());
		setAssociatePrincipalInvestigator(associatePrincipalInvestigator.toString());
	}

}
