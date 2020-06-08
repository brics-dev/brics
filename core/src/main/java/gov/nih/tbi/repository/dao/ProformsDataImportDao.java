package gov.nih.tbi.repository.dao;

import java.util.List;

import gov.nih.tbi.repository.dataimport.AdministeredFormProcessingInfo;
import gov.nih.tbi.repository.dataimport.DataImportDataElementData;

public interface ProformsDataImportDao {

	public List<AdministeredFormProcessingInfo> getAllAdminForms();
	public String getFinalLockByUser(String adminFormId);
	public List<String> getGroupDataElements(String adminFormId);
	public List<String> getMultiSelectGroupDataElements(String adminFormId);
	public List<DataImportDataElementData> getDataFromDataSubmissionView(String adminFormId);
	public void updateAdminFormSubmissionStatus(int status, String adminFormId);
	public void deleteAdminFormFromDataSubmissionTable(String adminFormId);
	public String getFinalLockDate(String adminFormId);
}
