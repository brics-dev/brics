package gov.nih.tbi.repository;

import gov.nih.tbi.repository.table.UploadTableModel;

import javax.swing.SwingWorker;

public class CancelUploadWorker extends SwingWorker<Void, Void> {
	private UploadManager view;

	public CancelUploadWorker(UploadManager frame) {
		this.view = frame;
	}

	@Override
	protected Void doInBackground() throws Exception {
		UploadManagerController.cancelAllUpload();
		return null;
	}

	@Override
	public void done() {
		//System.out.println("CancelUploadWorker: Cancel worker job is completed!");
		UploadManager.systemBusyDialog.setVisible(false);
		UploadTableModel tableModel  = (UploadTableModel) UploadManager.getTable().getModel();
		tableModel.cancelAllUpload();
		view.setUploadingMode(false);
	}
}
