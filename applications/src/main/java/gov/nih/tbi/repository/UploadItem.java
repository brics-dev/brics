package gov.nih.tbi.repository;

import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;

/**
 * A bean for storing information relevant to an item in the upload queue
 * 
 * @author Francis Chen
 * 
 */
public class UploadItem {

	private int tableIndex;
	private DatasetFile datasetFile;
	private Dataset dataset;
	private String studyName;
	private String studyPrefixedId;
	private UploadStatus uploadStatus;
	private UploadChannel uploadChannel;

	public UploadItem() {

	}

	public UploadItem(int tableIndex, Dataset dataset, DatasetFile datasetFile, String studyName,
			UploadStatus uploadStatus, String studyPrefixedId) {

		this.tableIndex = tableIndex;
		this.datasetFile = datasetFile;
		this.dataset = dataset;
		this.studyName = studyName;
		this.uploadStatus = uploadStatus;
		this.studyPrefixedId = studyPrefixedId;
	}

	public String getStudyPrefixedId() {
		return studyPrefixedId;
	}

	public void setStudyPrefixedId(String studyPrefixedId) {
		this.studyPrefixedId = studyPrefixedId;
	}

	public int getTableIndex() {

		return tableIndex;
	}

	public void setTableIndex(int tableIndex) {

		this.tableIndex = tableIndex;
	}

	public DatasetFile getDatasetFile() {

		return datasetFile;
	}

	public void setDatasetFile(DatasetFile datasetFile) {

		this.datasetFile = datasetFile;
	}

	public String getDatasetName() {

		return dataset.getName();
	}

	public void setDatasetName(String datasetName) {

		this.dataset.setName(datasetName);
	}

	public String getStudyName() {

		return studyName;
	}

	public void setStudyName(String studyName) {

		this.studyName = studyName;
	}

	public UploadStatus getUploadStatus() {

		return uploadStatus;
	}

	public void setUploadStatus(UploadStatus uploadStatus) {

		this.uploadStatus = uploadStatus;
	}

	public UploadChannel getUploadChannel() {

		return uploadChannel;
	}

	public void setUploadChannel(UploadChannel uploadChannel) {

		this.uploadChannel = uploadChannel;
	}

	public String getFileName() {

		return this.datasetFile.getUserFile().getName();
	}

	public Dataset getDataset() {

		return dataset;
	}

	public void setDataset(Dataset dataset) {

		this.dataset = dataset;
	}
}
