package gov.nih.tbi.repository.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Represent a single instance data row. Has a hashmap called cell that maps a value to the appropriate column
 * 
 * @author Francis Chen
 * 
 */
@XmlRootElement(name = "instancedRow")
@XmlAccessorType(XmlAccessType.FIELD)
public class InstancedRow implements Serializable {

	private static final long serialVersionUID = -2999852105163465318L;

	@XmlAttribute
	private String rowUri;

	@XmlAttribute
	private String guid;

	@XmlAttribute
	private String submissionId;

	@XmlAttribute
	private Long datasetId;

	@XmlAttribute
	private String readableDatasetId;

	@XmlAttribute
	private String studyId;

	@XmlAttribute
	private String studyName;

	@XmlAttribute
	private String datasetName;

	@XmlAttribute
	private String formShortName;

	@XmlAttribute
	private String isDerived;

	@XmlAttribute
	private String studyPrefixedId;

	@XmlAttribute
	private boolean doHighlight;

	@XmlJavaTypeAdapter(CellValueAdapter.class)
	private LinkedHashMap<DataTableColumn, CellValue> cell;

	/**
	 * JAXB requires no-arg constructore :/
	 */
	protected InstancedRow() {
		cell = new LinkedHashMap<DataTableColumn, CellValue>();
	}

	public InstancedRow(String rowUri) {
		this.rowUri = rowUri;
		cell = new LinkedHashMap<DataTableColumn, CellValue>();
	}

	public InstancedRow(String rowUri, String submissionId, Long datasetId, String readableDatasetId, String studyId,
			String studyPrefixedId, String studyName, String datasetName, String isDerived, boolean doHighlight,
			String guid, String formShortName) {

		this.rowUri = rowUri;
		this.submissionId = submissionId;
		this.datasetId = datasetId;
		this.readableDatasetId = readableDatasetId;
		this.studyId = studyId;
		this.studyName = studyName;
		this.datasetName = datasetName;
		this.isDerived = isDerived;
		this.studyPrefixedId = studyPrefixedId;
		this.doHighlight = doHighlight;
		this.guid = guid;
		this.formShortName = formShortName;
		cell = new LinkedHashMap<DataTableColumn, CellValue>();
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public boolean isDoHighlight() {
		return doHighlight;
	}

	public void setDoHighlight(boolean doHighlight) {
		this.doHighlight = doHighlight;
	}

	public String getStudyPrefixedId() {
		return studyPrefixedId;
	}

	public void setStudyPrefixedId(String studyPrefixedId) {
		this.studyPrefixedId = studyPrefixedId;
	}

	/**
	 * Returns the cell by form structure shortname+version, repeatable group name, and data element name
	 * 
	 * @param fsName
	 * @param rgName
	 * @param deName
	 * @return
	 */
	public CellValue getCellValue(String fsName, String rgName, String deName) {

		DataTableColumn column = new DataTableColumn(fsName, rgName, deName);
		return getCellValue(column);
	}

	public void insertCell(DataTableColumn column, CellValue cellValue) {

		if (this.cell == null) {
			this.cell = new LinkedHashMap<DataTableColumn, CellValue>();
		}

		this.cell.put(column, cellValue);
	}

	public CellValue getCellValue(DataTableColumn column) {

		if (cell != null) {
			return cell.get(column);
		}

		return null;
	}

	public LinkedHashMap<DataTableColumn, CellValue> getCell() {

		return cell;
	}

	public void setCell(LinkedHashMap<DataTableColumn, CellValue> cell) {

		if (this.cell == null) {
			this.cell = new LinkedHashMap<DataTableColumn, CellValue>();
		}

		this.cell.clear();

		if (cell != null && !cell.isEmpty()) {
			this.cell.putAll(cell);
		}
	}

	public String getRowUri() {

		return rowUri;
	}

	public void setRowUri(String rowUri) {

		this.rowUri = rowUri;
	}

	public int getRowSize() {

		int size = 0;

		if (cell == null) {
			return size;
		}

		for (Entry<DataTableColumn, CellValue> cellEntry : cell.entrySet()) {
			int tempSize = cellEntry.getValue().getRowSize();
			if (tempSize > size) {
				size = tempSize;
			}
		}

		return size;
	}

	public String getSubmissionId() {

		return submissionId;
	}

	public void setSubmissionId(String submissionId) {

		this.submissionId = submissionId;
	}

	public Long getDatasetId() {

		return datasetId;
	}

	public void setDatasetId(Long datasetId) {

		this.datasetId = datasetId;
	}

	public String getReadableDatasetId() {

		return readableDatasetId;
	}

	public void setReadableDatasetId(String readableDatasetId) {

		this.readableDatasetId = readableDatasetId;
	}

	public String getStudyId() {
		return studyId;
	}

	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}

	public String getStudyName() {

		return studyName;
	}

	public void setStudyName(String studyName) {

		this.studyName = studyName;
	}

	public String getDatasetName() {

		return datasetName;
	}

	public void setDatasetName(String datasetName) {

		this.datasetName = datasetName;
	}

	public void setFormShortName(String shortName) {
		this.formShortName = shortName;
	}

	public String getFormShortName() {
		return formShortName;
	}

	public String getIsDerived() {
		return isDerived;
	}

	public void setIsDerived(String isDerived) {
		this.isDerived = isDerived;
	}
}
