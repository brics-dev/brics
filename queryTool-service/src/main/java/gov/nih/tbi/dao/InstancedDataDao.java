package gov.nih.tbi.dao;

import gov.nih.tbi.pojo.CellPosition;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.DataTableColumnWithUri;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.service.cache.InstancedDataFormCache;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ListMultimap;
import com.hp.hpl.jena.graph.Node;

public interface InstancedDataDao {

	/**
	 * Loads all of the InstancedRows into the cache from the given form
	 * 
	 * @param form
	 * @return
	 */
	public InstancedDataFormCache loadAll(FormResult form, CodeMapping codeMapping, Node accountNode,
			boolean forDownload);

	/**
	 * returns a multimap of the repeatable group column to a list of row uris which for the repeatable group, only has
	 * one row of data * @param form
	 * 
	 * @param rowUris
	 * @return
	 */
	public Map<CellPosition, Integer> getRepeatableGroupRowCounts(FormResult form, Set<String> rowUris);


	public List<InstancedRepeatableGroupRow> getSelectedRepeatableGroupInstancedData(FormResult form,
			String submissionId, RepeatableGroup group, CodeMapping codeMapping, Node accountNode);

	/**
	 * Given a list of repeatable uris, load the instanced repeatable group data
	 * 
	 * @param column
	 * @param uris
	 */
	public void loadRepeatableGroupRowsByUris(FormResult form, DataTableColumn column, List<String> uris,
			List<FormResult> selectedForms, CodeMapping codeMapping, Node accountNode,
			InstancedDataTable instancedDataTable);

	public List<DataElement> getDataElementsForRepeatableGroup(FormResult form);

	public void addDataElementsToRepeatableGroups(FormResult form, List<DataElement> dataElements);


	public Set<DataTableColumnWithUri> getColumnsWithData(FormResult currentForm, Node accountNode);

	public boolean hasHighlightedGuid(FormResult form, Node accountNode);

	public ListMultimap<String, InstancedRepeatableGroupRow> getRepeatableGroupData(FormResult form, RepeatableGroup rg,
			Node accountNode, CodeMapping codeMapping);

	/**
	 * 
	 * @param form
	 * @param columnsToLoad
	 * @param instancedDataCache
	 * @param accountNode
	 */
	public void loadRgDataByColumns(FormResult form, List<RepeatingCellColumn> columnsToLoad,
			InstancedDataFormCache formCache, Node accountNode);

}
