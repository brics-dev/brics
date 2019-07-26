package gov.nih.tbi.download.table;

import gov.nih.tbi.commons.model.DownloadStatus;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.hibernate.Downloadable;

/**
 * Analogous to <code>PackageRow</code> in that this class acts as a container for a Downloadable file. This is a
 * greatly simplified version because of the reduced amount of focus at the micro level.
 * 
 * @author wangvg
 * 
 */

public class DownloadableRow {

	private Downloadable item;

	private boolean selected;

	private long transferred;

	private long fileSize;

	private DownloadStatus status;

	// private String fileExt;
	
	private String fileType;

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public DownloadableRow(Downloadable download) {
		item = download;
		selected = false;
		transferred = 0;

		status = DownloadStatus.READY;
		fileSize = download.getUserFile().getSize();
		if (download.getType() != null)
			fileType = download.getType().getType(); // String of the enum type
		else
			fileType = SubmissionType.UNKNOWN.getType();
	}

	public void setSelected(boolean isSelected) {
		selected = isSelected;
	}

	public boolean isSelected() {
		return selected;
	}

	public Downloadable getDownloadable() {
		return item;
	}

	/**
	 * Instead of using percentage for progress, use amount transferred so that it is easier to calculate the parent
	 * package's progress.
	 * 
	 * @param prog
	 */
	public void setProgress(float prog) {
		transferred = (long) prog;
	}

	public float getProgress() {
		return transferred;
	}

	public int getProgressPercent() {
		return (int) (100 * transferred / fileSize);
	}

	public void setCompleted(boolean isComplete) {
		if (isComplete) {
			status = DownloadStatus.COMPLETED;
			transferred = fileSize;
		} else {
			status = DownloadStatus.READY;
			transferred = 0;
		}
	}

	public boolean isCompleted() {
		return status == DownloadStatus.COMPLETED;
	}

	public long getSize() {
		return fileSize;
	}

	public void setDownloadStatus(DownloadStatus status) {
		this.status = status;
	}

	public DownloadStatus getDownloadStatus() {
		return status;
	}

}
