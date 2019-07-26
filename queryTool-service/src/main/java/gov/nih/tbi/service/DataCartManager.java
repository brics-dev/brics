package gov.nih.tbi.service;

import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.pojo.DataTableColumnWithUri;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.model.DataCart;

import java.util.List;
import java.util.Set;

public interface DataCartManager {

	public void addToCart(DataCart dataCart, FormResult formToAdd, StudyResult studyToAdd);

	public void removeFromCart(DataCart dataCart, FormResult formToRemove, StudyResult studyToRemove);

	public List<FormResult> loadSelectedFormDataElements(DataCart dataCart);

	public void rebuildCodeMapping(DataCart dataCart, List<FormResult> selectedForms);

	public void generateInstancedDataTable(DataCart dataCart, int offset, int limit, String sortColName,
			String sortOrder, String userName, boolean doApplyFilter);

	public void rebuildDataTableData(DataCart dataCart, int offset, int limit, String sortColName,
			String sortOrder, String userName);
	
	public String getTableHeaderJson(DataCart dataCart);

	public String getTableDataJson(DataCart dataCart);

	public void expandRepeatableGroup(DataCart dataCart, String rowUri, String rgFormUri, String rgName, String userName);

	public void collapseRepeatableGroup(DataCart dataCart, String rowUri, String rgFormUri, String rgName);
	
	public void refreshRepeatableGroup(DataCart dataCart, String rowUri, String rgFormUri, String rgName, String userName);

	public String getSelectedFormDetailsJson(DataCart dataCart);
	
	public byte[] downloadThumbnailBytes(String studyName, String datasetName, String imageName);
	
	public boolean hasMatchingGuid(DataCart dataCart);
	
	public String getDeDetailPage(String deName);
	
	public List<Schema> getSchemaOptions();
	
	public List<Schema> getSchemaOptionsByFormNames(List<String> formStuctureNames);

	/**
	 * Returns a set of all columns that have no data in the selected forms
	 * @param dataCart
	 * @param userName
	 * @return
	 */
	public Set<DataTableColumnWithUri> getColumnsWithNoData(DataCart dataCart, String userName);
	
	public Long getFileSize(String studyName, String datasetName, String fileName);
	
	public byte[] downloadFileBytes(String studyName, String datasetName, String fileName);

	public boolean hasHighlightedGuid(List<FormResult> selectedForms, String userName);

}
