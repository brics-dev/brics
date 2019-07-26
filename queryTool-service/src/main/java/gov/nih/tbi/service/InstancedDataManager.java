package gov.nih.tbi.service;

import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.DataTableColumnWithUri;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.service.cache.InstancedDataCache;

import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;

public interface InstancedDataManager {

	public void seedFormDataElements(FormResult form);

	public InstancedDataCache buildInstancedRowCache(List<FormResult> selectedForms, CodeMapping codeMapping,
			Node accountNode, boolean forDownload);

	public InstancedDataTable buildInstancedDataTable(List<FormResult> selectedForms, int offset, int limit,
			DataTableColumn sortColumn, String sortOrder, InstancedDataCache instancedRowCache, CodeMapping codeMapping,
			String userName, boolean forDownload, boolean doApplyFilter);

	public InstancedDataTable buildInstancedDataTableForDownload(List<FormResult> formList, DataTableColumn sortColumn,
			String sortOrder, InstancedDataCache instancedRowCache, CodeMapping codeMapping, String userName,
			boolean isCartDownload, boolean isNormalCsv);

	public void buildDataTableData(List<FormResult> selectedForms, InstancedDataTable instancedDataTable,
			InstancedDataCache instancedRowCache, CodeMapping codeMapping, Node accountNode, boolean forDownload,
			boolean doApplyFilter);

	public void loadRepeatableGroupRows(InstancedDataTable instancedDataTable, FormResult form, String rowUri,
			String rgName, CodeMapping codeMapping, String userName);

	public void collapseRepeatableGroupRows(InstancedDataTable instancedDataTable, FormResult form, String rowUri,
			String rgName);

	public void refreshRepeatableGroupRows(InstancedDataTable table, InstancedDataCache cache, FormResult form,
			String rowUri, String rgName, CodeMapping codeMapping, String userName);

	public void reloadRepeatableGroup(List<FormResult> selectedForms, FormResult form, RepeatableGroup group,
			InstancedDataTable instancedDataTable, CodeMapping codeMapping, Node accountNode);

	public Set<DataTableColumnWithUri> getColumnsWithNoData(List<FormResult> selectedForms, Node accountNode);

	public boolean hasHighlightedGuid(FormResult formResult, Node accountNode);

	/**
	 * Given a list of row URIs that should not be selected, returns a list of all selected biosamples.
	 * 
	 * @param biosampleForm
	 * @param unselectedRowUri
	 * @param accountNode
	 * @return
	 */
	public Set<String> getAllBiosampleIdsInverse(FormResult biosampleForm, Set<String> unselectedBiosample,
			Node accountNode);

	/**
	 * Given a list of row URIs that should not be selected, returns a list of all selected biosamples.
	 * 
	 * @param biosampleForm
	 * @param unselectedRowUri
	 * @param accountNode
	 * @return
	 */
	public Set<String> getRowUrisByBiosampleIdInverse(FormResult biosampleForm, Set<String> unselectedRowUri,
			Node accountNode);
}
