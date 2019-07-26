package gov.nih.tbi.download.workers;

import gov.nih.tbi.download.view.DownloadController;
import gov.nih.tbi.download.view.DownloadPackageView;

import java.util.List;

import javax.swing.SwingWorker;

public class DeleteWorker extends SwingWorker<Void, Void> {
	private DownloadController controller;
	private List<Long> downloadableIds;
	private DownloadPackageView view;

	public DeleteWorker(DownloadController controller, DownloadPackageView view, List<Long> downloadableIds) {
		this.controller = controller;
		this.downloadableIds = downloadableIds;
		this.view = view;
		view.setButtonsStatusRunning(false);
	}

	@Override
	protected Void doInBackground() throws Exception {
		controller.delete(downloadableIds);
		return null;
	}

	@Override
	public void done() {
		view.setButtonsStatus(view.getModel().getSelectedNumber() > 0);
		// If selected package is deleted, then also remove its contents from detail view
		if (view.getMicroView().getDisplayedPackage() == null) {
			view.getMicroView().clearTable();
		}
		// all download packages have been deleted, checkbox on the header may need to reset
		if (view.getModel().getRowCount() == 0) {
			view.getTable().getTableHeader().repaint();
		}
	}
}
