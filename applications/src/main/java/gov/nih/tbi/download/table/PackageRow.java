package gov.nih.tbi.download.table;

import gov.nih.tbi.commons.model.DownloadStatus;
import gov.nih.tbi.repository.model.DownloadPackageOrigin;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Downloadable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class is used to aggregate information regarding a download package so that it is useful for display in a table.
 * It basically acts as a container for a Download Package so that we can keep track of which rows are selected,
 * progress, etc.
 * 
 * @author wangvg
 * 
 */
public class PackageRow {
	private DownloadPackage item;

	private ArrayList<DownloadableRow> fileRows;

	/**
	 * A separate list of rows to make it easier to keep track of progress.
	 */
	private ArrayList<DownloadableRow> downloadingRows;

	private boolean selected;

	private DownloadStatus status;

	private int progress;

	private ChangeListener listener;

	private long downloadSize;
	private JCheckBox[] fileTypes;
	public static final String NO_EXT = "Unknown Type";
	private DownloadPackageOrigin origin;

	public PackageRow(DownloadPackage downloadItem) {
		item = downloadItem;
		selected = false;
		fileRows = new ArrayList<DownloadableRow>();
		downloadingRows = new ArrayList<DownloadableRow>();
		downloadSize = 0;
		origin = downloadItem.getOrigin();

		Set<String> fileTypeSet = new HashSet<String>();

		for (Downloadable file : item.getDownloadables()) {
			DownloadableRow row = new DownloadableRow(file);
			fileRows.add(row);
			fileTypeSet.add(file.getType().getType());
		}

		final PackageRow item = this;

		JCheckBox template = new JCheckBox(PackageRow.NO_EXT);

		fileTypes = new JCheckBox[fileTypeSet.size()];

		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object src = e.getSource();
				if (src instanceof JCheckBox) {
					listener.stateChanged(new ChangeEvent(item));
				}
			}

		};

		// Build the requisite checkboxes for every
		// possible file extension in the list
		Iterator<String> iter = fileTypeSet.iterator();
		int cnt = 0;
		while (iter.hasNext()) {
			String ext = iter.next();
			JCheckBox box = new JCheckBox(ext);
			box.setSelected(true);
			box.addActionListener(actionListener);
			box.setPreferredSize(template.getPreferredSize());
			fileTypes[cnt] = box;
			cnt++;
		}

		progress = 0;
		status = DownloadStatus.READY;

	}

	public void setChangeListener(ChangeListener changeListener) {
		listener = changeListener;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean isSelected) {
		selected = isSelected;
	}

	public DownloadPackage getPackage() {
		return item;
	}

	public ArrayList<DownloadableRow> getFiles() {
		return fileRows;
	}

	public int getSelectedNumber() {
		int count = 0;
		for (DownloadableRow row : fileRows) {
			if (row.isSelected()) {
				count++;
			}
		}
		return count;
	}

	public void setDownloadableProgress(long id, float transferred) {
		for (DownloadableRow row : fileRows) {
			if (row.getDownloadable().getId().equals(id)) {
				row.setProgress(transferred);
				if (listener != null) {
					listener.stateChanged(new ChangeEvent(row));
				}
				break;
			}
		}
	}

	public void setDownloadableStatus(long id, DownloadStatus status) {
		for (DownloadableRow row : fileRows) {
			if (row.getDownloadable().getId().equals(id)) {
				row.setDownloadStatus(status);
				if (listener != null) {
					listener.stateChanged(new ChangeEvent(row));
				}
				break;
			}
		}
	}

	public void setDownloadableStatus(DownloadableRow row, DownloadStatus status) {
		row.setDownloadStatus(status);
		if (listener != null) {
			listener.stateChanged(new ChangeEvent(row));
		}
	}

	public int getProgress() {

		if (isCompleted()) {
			return 100;
		} else if (downloadingRows.isEmpty()) {
			return 0;
		}

		float transferred = 0;

		for (DownloadableRow row : downloadingRows) {
			transferred += row.getProgress();
		}

		progress = (int) (100f * transferred / downloadSize);

		return progress;
	}

	public void setStatus(DownloadStatus stat) {
		status = stat;
	}

	public DownloadStatus getStatus() {
		return status;
	}

	public void setDownloadableComplete(Long id) {
		for (DownloadableRow row : fileRows) {
			if (row.getDownloadable().getId().equals(id)) {
				row.setCompleted(true);
				if (listener != null) {
					listener.stateChanged(new ChangeEvent(row));
				}
				break;
			}
		}
	}

	public boolean isCompleted() {
		return status == DownloadStatus.COMPLETED;
	}

	public long getTotalFileSize() {
		Long fileSize = 0L;

		for (DownloadableRow row : fileRows) {
			fileSize += row.getDownloadable().getUserFile().getSize();
		}
		downloadSize = fileSize;
		return fileSize;
	}

	public void clearDownloads() {
		downloadSize = 0;
		downloadingRows.clear();
	}

	public void addDownload(DownloadableRow row) {
		downloadSize += row.getDownloadable().getUserFile().getSize();
		downloadingRows.add(row);
	}

	public ArrayList<DownloadableRow> getDownloadingRows() {
		return downloadingRows;
	}

	public DownloadableRow getDownloadingRow(int getDownloadableId) {
		if (downloadingRows != null) {
			for (DownloadableRow row : downloadingRows) {
				if (row.getDownloadable().getId().equals(Long.valueOf(getDownloadableId))) {
					return row;
				}
			}
		}
		return null;
	}

	public void selectAll(boolean selectAll) {
		for (DownloadableRow row : fileRows) {
			row.setSelected(selectAll);
		}
	}

	public JCheckBox[] getFileTypes() {
		return fileTypes;
	}

	public void setFileTypes(JCheckBox[] fileBoxes) {
		this.fileTypes = fileBoxes;
	}

}
