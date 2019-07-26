package gov.nih.tbi.download.workers;

import gov.nih.tbi.commons.model.DownloadStatus;
import gov.nih.tbi.download.Model.DownloadReturnCode;
import gov.nih.tbi.download.view.DownloadController;
import gov.nih.tbi.download.view.DownloadPackageView;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class DownloadWorker extends SwingWorker<Void, Void> {

	private DownloadController controller;
	private String rootPath;
	private DownloadReturnCode returnCode;
	private DownloadPackageView view;
	private List<Integer> indices;

	public DownloadWorker(DownloadController controller, List<Integer> selectedIndices, DownloadPackageView view,
			String rootPath) {
		this.controller = controller;
		this.rootPath = rootPath;
		this.view = view;
		this.indices = selectedIndices;
	}

	public DownloadWorker(DownloadController controller, DownloadPackageView view, String rootPath) {
		this.controller = controller;
		this.rootPath = rootPath;
		this.view = view;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			//returnCode = controller.startDownload(indices, view.getModel(), rootPath);
			returnCode = controller.startDownload(view.getTable(), view.getModel(), rootPath);
		} catch (JSchException e) {
			e.printStackTrace();
			returnCode = DownloadReturnCode.ERROR;
		} catch (SftpException e) {
			e.printStackTrace();
			returnCode = DownloadReturnCode.ERROR;
		}
		return null;
	}

	@Override
	public void done() {

		switch (returnCode) {
			case COMPLETED:
				int value =
						JOptionPane.showConfirmDialog(view, "Your download(s) have been completed. Would "
								+ "you like to clear ALL completed downloads?",
								"Downloads Complete",
								JOptionPane.YES_NO_OPTION);
				if (value == JOptionPane.YES_OPTION) {
					// "Swingception" - Francis
					view.getLoadingDialog().setVisible(true);
					new ClearCompletedWorker(view, controller).execute();
				}
				break;
			case CANCELLED:
				DownloadController.setNotCompletedCheckedRowStatus(view.getModel(), DownloadStatus.CANCELLED);
				break;
			case ERROR:
				JOptionPane.showMessageDialog(null, "An error has occured during download.", "Download Error",
						JOptionPane.ERROR_MESSAGE);
				break;
			default:
				view.setButtonsStatus(view.getModel().getSelectedNumber() > 0);
				throw new UnsupportedOperationException("An invalid returned code was set.");
		}
		view.setButtonsStatus(view.getModel().getSelectedNumber() > 0);
		view.getModel().setCheckboxEditable(true);
		view.getMicroView().getFileTableModel().setCheckboxEditable(true);
	}
}
