package gov.nih.tbi.download.workers;

import gov.nih.tbi.commons.model.DownloadStatus;
import gov.nih.tbi.download.table.DownloadableRow;
import gov.nih.tbi.download.table.PackageRow;
import gov.nih.tbi.download.table.PackageTableModel;
import gov.nih.tbi.download.view.DownloadController;
import gov.nih.tbi.download.view.DownloadPackageView;

import javax.swing.SwingWorker;

public class StopWorker extends SwingWorker<Void, Void> {
	private PackageTableModel model;
	private DownloadPackageView view;

	public StopWorker(PackageTableModel model, DownloadPackageView view) {
		super();
		this.model = model;
		this.view = view;
	}

	@Override
	protected Void doInBackground() throws Exception {
		// DownloadController.stop(model);
		DownloadController.stopDownload();

		// Here, we will keep the other buttons disabled for a cooldown time before re-enabling. Or else the user has a
		// chance
		// to overload the sftp connection and affect other user's experience.
		try {
			Thread.sleep(DownloadPackageView.UI_DOWNLOAD_COOL_DOWN);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void done() {
		int rowCount = model.getRowCount();

		for (int i = 0; i < rowCount; i++) {
			PackageRow pkg = model.get(i);
			if (pkg.isSelected()) {
				if (pkg.getStatus().equals(DownloadStatus.PENDING)
						|| pkg.getStatus().equals(DownloadStatus.IN_PROGRESS)) {
					pkg.setStatus(DownloadStatus.CANCELLED);
					model.fireTableCellUpdated(i, PackageTableModel.STATUS_COLUMN);
					for (DownloadableRow row : pkg.getFiles()) {
						if (row.getDownloadStatus().equals(DownloadStatus.PENDING)
								|| row.getDownloadStatus().equals(DownloadStatus.IN_PROGRESS)) {
							pkg.setDownloadableStatus(row, DownloadStatus.CANCELLED);
						}
					}
				}
			}
		}
		view.setButtonsStatus(model.getSelectedNumber() > 0);
		view.getLoadingDialog().setVisible(false);
	}
}
