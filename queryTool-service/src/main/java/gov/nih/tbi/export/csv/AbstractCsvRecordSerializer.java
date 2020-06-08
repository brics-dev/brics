package gov.nih.tbi.export.csv;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.FormHeader;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.util.InstancedDataUtil;

public abstract class AbstractCsvRecordSerializer implements CsvRecordSerializer {
	private static final String DELIMITER = ",";

	protected InstancedDataTable instancedDataTable;
	protected List<FormHeader> headers;
	protected String displayOption;
	protected Iterator<InstancedRecord> recordIterator;
	protected Set<DataTableColumn> visibleColumns;
	protected boolean showAgeRange;

	public AbstractCsvRecordSerializer(InstancedDataTable instancedDataTable, boolean showAgeRange) {
		this.instancedDataTable = instancedDataTable;
		this.headers = instancedDataTable.getHeaders();
		this.displayOption = instancedDataTable.getDisplayOption();
		this.recordIterator = instancedDataTable.getInstancedRecords().iterator();
		this.showAgeRange = showAgeRange;
		initVisibleColumns();
	}


	protected void initVisibleColumns() {
		this.visibleColumns = new HashSet<>();
		for (FormResult form : instancedDataTable.getForms()) {
			this.visibleColumns.addAll(InstancedDataUtil.getVisibleColumns(form));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return recordIterator.hasNext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String serializeNext() {
		InstancedRecord currentRecord = recordIterator.next();
		return serializeRecord(currentRecord, visibleColumns);
	}

	/**
	 * Formats the given 2D ArrayList to a single CSV string
	 * 
	 * @param recordList
	 * @return
	 */
	protected String recordListToString(List<List<String>> recordList) {
		StringBuffer sb = new StringBuffer();

		for (List<String> recordRow : recordList) {
			String rowString = BRICSStringUtils.concatWithDelimiter(recordRow, DELIMITER);
			sb.append(rowString).append("\n");
		}

		return sb.toString();
	}

	/**
	 * Sanitizes the given value and returns it. Currently only surrounds the value with double-quotes and escape
	 * double-quotes with two double-quotes.
	 * 
	 * @param value
	 * @return
	 */
	protected String sanitizeCsvValue(String value) {

		String sanitizedValue = value.replaceAll("\"", "\"\"");
		sanitizedValue = "\"" + sanitizedValue + "\"";

		return sanitizedValue;
	}
	
}
