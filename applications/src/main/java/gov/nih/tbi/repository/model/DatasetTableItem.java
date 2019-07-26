package gov.nih.tbi.repository.model;

import gov.nih.tbi.commons.model.DatasetFileStatus;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;

/**
 * This model represents a single item in the dataset re-upload table UI
 * 
 * @author Francis Chen
 *
 */
public class DatasetTableItem {
	private boolean checked;
	private Long id;
	private String name;
	private String studyTitle;
	private int completedFilesCount;
	private int totalFilesCount;

	public DatasetTableItem(Dataset dataset) {
		this.checked = false;
		this.id = dataset.getId();
		this.name = dataset.getName();
		this.studyTitle = dataset.getStudy().getTitle();

		this.completedFilesCount = 0;
		this.totalFilesCount = 0;

		for (DatasetFile datasetFile : dataset.getDatasetFileSet()) {
			totalFilesCount++;

			if (DatasetFileStatus.COMPLETE.equals(datasetFile.getDatasetFileStatus())) {
				completedFilesCount++;
			}
		}
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStudyTitle() {
		return studyTitle;
	}

	public void setStudyTitle(String studyTitle) {
		this.studyTitle = studyTitle;
	}

	public void setPendingFilesCount(int pendingFilesCount) {
		this.completedFilesCount = pendingFilesCount;
	}

	public void setTotalFilesCount(int totalFilesCount) {
		this.totalFilesCount = totalFilesCount;
	}

	public String getUploadProgress() {
		return completedFilesCount + "/" + totalFilesCount;
	}
}
