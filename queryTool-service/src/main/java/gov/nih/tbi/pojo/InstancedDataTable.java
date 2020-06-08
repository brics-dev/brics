package gov.nih.tbi.pojo;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.InstancedDataException;
import gov.nih.tbi.repository.model.CellValue;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.FormHeader;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.RepeatableGroupHeader;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.repository.model.RepeatingCellValue;
import gov.nih.tbi.util.InstancedDataUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Tracks the state of the data table as well as the data
 * 
 * @author Francis Chen
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
public class InstancedDataTable implements Serializable {
	private static final long serialVersionUID = 131400073487968443L;

	@XmlElementWrapper(name = "headers")
	@XmlElement(name = "formHeader", type = FormHeader.class)
	private List<FormHeader> headers;

	@XmlElementWrapper(name = "instancedRecords")
	@XmlElement(name = "instancedRecord", type = InstancedRecord.class)
	private List<InstancedRecord> instancedRecords;

	@XmlElementWrapper(name = "rgExpandTrackers")
	@XmlElement(name = "RepeatableGroupExpansionTracker", type = RepeatableGroupExpansionTracker.class)
	private Set<RepeatableGroupExpansionTracker> rgExpandTrackers;

	@XmlTransient
	private ListMultimap<Long, String> attachedFilesMap;

	@XmlTransient
	private List<DownloadPVMappingRow> downloadPVMappings;

	@XmlTransient
	private List<FormResult> forms;

	private DataTableColumn sortColumn;
	private String sortOrder;
	private int limit;
	private int offset;
	private boolean isJoined;
	private int rowCount;
	private int rowCountSansFilters;
	private String displayOption;
	private String filterExpression;

	public InstancedDataTable() {
		this.headers = new ArrayList<FormHeader>();
		this.instancedRecords = new LinkedList<InstancedRecord>();
		this.rgExpandTrackers = new HashSet<RepeatableGroupExpansionTracker>();
	}

	public InstancedDataTable(int rowCount, int rowCountSansFilters, int limit, int offset, DataTableColumn sortColumn,
			String sortOrder, List<FormHeader> headers, String filterExpression, List<FormResult> forms) {
		this.rowCount = rowCount;
		this.limit = limit;
		this.offset = offset;
		this.sortColumn = sortColumn;
		this.sortOrder = sortOrder;
		this.headers = headers;
		this.instancedRecords = new LinkedList<InstancedRecord>();
		this.rowCountSansFilters = rowCountSansFilters;
		this.rgExpandTrackers = new HashSet<RepeatableGroupExpansionTracker>();
		this.filterExpression = filterExpression;
		this.forms = forms;
	}

	public InstancedDataTable(InstancedDataTable clone) {
		this.rowCount = clone.rowCount;
		this.headers = new ArrayList<FormHeader>(clone.headers);
		this.limit = clone.limit;
		this.offset = clone.offset;
		this.instancedRecords = new LinkedList<InstancedRecord>(clone.instancedRecords);
		this.sortColumn = clone.sortColumn;
		this.sortOrder = clone.sortOrder;
		this.rowCountSansFilters = clone.rowCountSansFilters;
		this.rgExpandTrackers = new HashSet<RepeatableGroupExpansionTracker>(clone.rgExpandTrackers);
		this.attachedFilesMap = ArrayListMultimap.create();
		if (clone.attachedFilesMap != null && !clone.attachedFilesMap.isEmpty()) {
			this.attachedFilesMap.putAll(clone.attachedFilesMap);
		}
		this.displayOption = clone.displayOption;
		this.filterExpression = clone.filterExpression;
		this.forms = clone.forms;
	}
	

	public void clear() {
		instancedRecords = null;
		offset = -1;
		limit = -1;
		sortOrder = null;
		sortColumn = null;
		rowCount = 0;
		rowCountSansFilters = 0;
		if (attachedFilesMap != null) {
			attachedFilesMap.clear();
		}
		
		filterExpression = null;
		forms = null;
	}


	public List<FormResult> getForms() {
		return forms;
	}

	public void setForms(List<FormResult> forms) {
		this.forms = forms;
	}

	public String getFilterExpression() {
		return filterExpression;
	}

	public void setFilterExpression(String filterExpression) {
		this.filterExpression = filterExpression;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public List<DownloadPVMappingRow> getDownloadPVMappings() {
		return downloadPVMappings;
	}

	public void setDownloadPVMappings(List<DownloadPVMappingRow> downloadPVMappings) {
		this.downloadPVMappings = downloadPVMappings;
	}

	public ListMultimap<Long, String> getAttachedFilesMap() {
		return attachedFilesMap;
	}

	public void setAttachedFilesMap(ListMultimap<Long, String> attachedFilesMap) {
		this.attachedFilesMap = attachedFilesMap;
	}

	public List<InstancedRecord> getInstancedRecords() {
		return instancedRecords;
	}

	public void setInstancedRecords(List<InstancedRecord> instancedRecords) {
		if (this.instancedRecords == null) {
			this.instancedRecords = new ArrayList<InstancedRecord>();
		}

		this.instancedRecords.clear();

		if (instancedRecords != null) {
			this.instancedRecords.addAll(instancedRecords);
		}
	}

	public DataTableColumn getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(DataTableColumn sortColumn) {
		this.sortColumn = sortColumn;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public List<FormHeader> getHeaders() {
		return headers;
	}

	public void setHeaders(List<FormHeader> headers) {
		this.headers = headers;
	}

	public boolean isJoined() {
		return isJoined;
	}

	public void setJoined(boolean isJoined) {
		this.isJoined = isJoined;
	}

	public Set<RepeatableGroupExpansionTracker> getRgExpandTrackers() {
		return rgExpandTrackers;
	}

	public void setRgExpandTrackers(Set<RepeatableGroupExpansionTracker> rgExpandTrackers) {
		if (rgExpandTrackers != null) {
			this.rgExpandTrackers = rgExpandTrackers;
		} else {
			this.rgExpandTrackers.clear();
		}
	}

	public void addRgExpandTracker(RepeatableGroupExpansionTracker rgExpandTracker) {
		this.rgExpandTrackers.add(rgExpandTracker);
	}

	public void removeRgExpandTracker(RepeatableGroupExpansionTracker toRemove) {
		this.rgExpandTrackers.remove(toRemove);
	}


	public int getRowCountSansFilters() {

		return rowCountSansFilters;
	}


	public void setRowCountSansFilters(int rowCountSansFilters) {

		this.rowCountSansFilters = rowCountSansFilters;
	}

	public String getDisplayOption() {
		return displayOption;
	}

	public void setDisplayOption(String displayOption) {
		this.displayOption = displayOption;
	}

	public String getSelectedFormName() {

		return getFormNameByIndex(0);
	}

	public String getJoinFormName() {

		return getFormNameByIndex(1);
	}

	public List<String> getFormNames() {
		List<String> formNames = new ArrayList<String>();
		for (FormHeader header : headers) {
			formNames.add(header.getName());
		}

		return formNames;
	}

	public String getFormNameByIndex(int i) {

		if (getHeaders() != null && getHeaders().size() > i && getHeaders().get(i) != null) {
			return getHeaders().get(i).getName();
		}

		return null;
	}

	public int getSelectedFormColumnLength() {

		return getColumnLengthByFormIndex(0);
	}

	public int getJoinFormColumnLength() {

		return getColumnLengthByFormIndex(1);
	}

	/**
	 * Given an index of the header list, return the column length
	 * 
	 * @param i - The index of the header.
	 * @return
	 */
	public int getColumnLengthByFormIndex(int i) {
		if (headers.size() > i && headers.get(i) != null) {
			int headerCount = 0;

			for (RepeatableGroupHeader rgHeader : headers.get(i).getRepeatableGroupHeaders()) {
				headerCount += rgHeader.getDataElementHeaders().size();
			}

			return headerCount;
		}

		return -1;
	}

	/**
	 * Using the headers array, construct a flat arrayList representation of the FormHeaders array object.
	 * 
	 * @return
	 */
	public List<String> getCsvHeaders() {

		boolean displaySchema = InstancedDataUtil.isDisplaySchema(displayOption);
		Map<String, String> schemaDeHeaderMap = new HashMap<String, String>();
		if (displaySchema && downloadPVMappings != null) {
			for (DownloadPVMappingRow mappingRow : downloadPVMappings) {
				if (mappingRow.getSchemaDeId() != null) {
					schemaDeHeaderMap.put(mappingRow.getDeName(), mappingRow.getSchemaDeId());
				}
			}
		}

		List<String> flatHeaders = new ArrayList<String>();
		if (isJoined()) {
			flatHeaders.add("GUID");
		} else {
			flatHeaders.add("Study ID");
			flatHeaders.add("Dataset");
		}

		for (FormHeader formHeader : headers) {
			for (RepeatableGroupHeader rgHeader : formHeader.getRepeatableGroupHeaders()) {
				for (String deHeader : rgHeader.getDataElementHeaders()) {

					if (displaySchema && schemaDeHeaderMap.containsKey(deHeader)) {
						deHeader = schemaDeHeaderMap.get(deHeader);
					}

					if (QueryToolConstants.EMPTY_STRING.equals(rgHeader.getName())) {
						flatHeaders.add(formHeader.getName() + "." + deHeader);
					} else {
						flatHeaders.add(formHeader.getName() + "." + rgHeader.getName() + "." + deHeader);
					}
				}
			}
		}

		return flatHeaders;
	}

	/**
	 * Using the headers array, construct a flat arrayList representation of the FormHeaders array object.
	 * 
	 * @return
	 */
	public List<String> getFlattenedCSVHeaders() {

		boolean displaySchema = InstancedDataUtil.isDisplaySchema(displayOption);
		Map<String, String> schemaDeHeaderMap = new HashMap<String, String>();
		if (displaySchema && downloadPVMappings != null) {
			for (DownloadPVMappingRow mappingRow : downloadPVMappings) {
				if (mappingRow.getSchemaDeId() != null) {
					schemaDeHeaderMap.put(mappingRow.getDeName(), mappingRow.getSchemaDeId());
				}
			}
		}

		List<String> flatHeaders = new ArrayList<String>();
		if (isJoined()) {
			flatHeaders.add("GUID");
		} else {
			flatHeaders.add("Study ID");
			flatHeaders.add("Dataset");
		}

		Map<String, Integer> greatestNumber = getGreatestNumber();

		for (FormHeader formHeader : headers) {
			for (RepeatableGroupHeader rgHeader : formHeader.getRepeatableGroupHeaders()) {
				for (String deHeader : rgHeader.getDataElementHeaders()) {

					if (displaySchema && schemaDeHeaderMap.containsKey(deHeader)) {
						deHeader = schemaDeHeaderMap.get(deHeader);
					}

					// TODO: PUSCH: notice the problems of viewable formHeader names as opposed to actual (the version
					// no)

					String rgKeyColumnKey = "";
					if (!QueryToolConstants.EMPTY_STRING.equals(rgHeader.getName())) {
						rgKeyColumnKey = rgHeader.getName() + ".";
					}

					String version = formHeader.getVersion();
					String deFullName = formHeader.getName() + "V" + version + "." + rgKeyColumnKey + deHeader;
					Integer fullCount = greatestNumber.get(deFullName);
					if (fullCount == null) {
						fullCount = 1;
					}
					for (int i = 1; i <= fullCount; i++) {
						if (i == 1 && fullCount == 1)
							flatHeaders.add(deFullName);
						else
							flatHeaders.add(deFullName + "_" + i);
					}
				}
			}
		}

		return flatHeaders;
	}

	public Set<Long> getDatasetIdsFromRecords() {
		Set<Long> dataSetIdSet = new HashSet<Long>();
		for (InstancedRecord record : instancedRecords) {
			for (InstancedRow row : record.getSelectedRows()) {
				dataSetIdSet.add(row.getDatasetId());
			}
		}

		return dataSetIdSet;
	}


	/**
	 * Given an URI of a instanced repeatable group, returns the instanced row in instanced data table that has the
	 * instanced repeatable group.
	 * 
	 * @param uri
	 * @return
	 */
	public RepeatingCellValue getRepeatingCellValueByUriAndColumn(FormResult form, DataTableColumn column, String uri) {

		if (instancedRecords == null) {
			throw new InstancedDataException("Trying to search through null instanced records");
		}

		for (InstancedRecord record : instancedRecords) {
			for (InstancedRow row : record.getSelectedRows()) {
				if (row != null && row.getRowUri().equals(uri)) {
					CellValue cv = row.getCellValue(column);

					if (cv instanceof RepeatingCellValue) {
						return (RepeatingCellValue) cv;
					}

				}
			}
		}

		return null;
	}

	public Map<Long, Long> getDatasetIdsWithRecordCounts() {
		Map<Long, Long> arMap = new HashMap<Long, Long>();
		for (InstancedRecord record : instancedRecords) {
			for (InstancedRow row : record.getSelectedRows()) {
				// in certain conditions, not all records will have all rows, so skip the rows that aren't available)
				if (row != null) {
					Long dsId = row.getDatasetId();
					Long count = arMap.get(dsId);
					if (count == null) {
						arMap.put(dsId, 1L);
					} else {
						arMap.put(dsId, count + 1);
					}
				}
			}
		}
		return arMap;
	}

	public Map<String, Integer> getGreatestNumber() {
		Map<String, Integer> greatestNumber = new HashMap<String, Integer>();

		// loop through all instanced rows
		for (InstancedRecord record : instancedRecords) {
			for (InstancedRow row : record.getSelectedRows()) {
				if (row != null)
					for (DataTableColumn dtc : row.getCell().keySet()) {
						CellValue cv = row.getCell().get(dtc);

						Map<String, Integer> localCounts = new HashMap<String, Integer>();

						if (cv instanceof RepeatingCellValue && cv.getIsRepeating()) {
							RepeatingCellValue rcv = (RepeatingCellValue) cv;
							for (InstancedRepeatableGroupRow rgrow : rcv.getRows()) {

								for (RepeatingCellColumn rcc : rgrow.getCell().keySet()) {
									String fullName =
											rcc.getForm() + "." + rcc.getRepeatableGroup() + "." + rcc.getDataElement();

									if (localCounts.containsKey(fullName)) {
										localCounts.put(fullName, localCounts.get(fullName) + 1);
									} else {
										localCounts.put(fullName, 1);
									}
								}
							}
						} else {
							// TODO: PUSCH: rename this variable
							String dtcvlmnop =
									dtc.getForm() + "." + dtc.getRepeatableGroup() + "." + dtc.getDataElement();
							if (!localCounts.containsKey(dtcvlmnop)) {
								localCounts.put(dtcvlmnop, 1);
							}
						}

						for (String key : localCounts.keySet()) {
							if ((!greatestNumber.containsKey(key)) || greatestNumber.get(key) < localCounts.get(key)) {
								greatestNumber.put(key, localCounts.get(key));
							}
						}

					}
			}
		}
		return greatestNumber;
	}
}
