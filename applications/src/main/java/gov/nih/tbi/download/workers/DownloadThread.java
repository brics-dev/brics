package gov.nih.tbi.download.workers;

import gov.nih.tbi.commons.model.DownloadStatus;
import gov.nih.tbi.download.Model.DownloadItem;
import gov.nih.tbi.download.table.DownloadableRow;
import gov.nih.tbi.download.table.PackageRow;
import gov.nih.tbi.download.table.PackageTableModel;
import gov.nih.tbi.download.view.DownloadController;
import gov.nih.tbi.download.view.DownloadPackageView;
import gov.nih.tbi.download.view.DownloadUtilsMacro;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;
import gov.nih.tbi.repository.service.io.SftpProgressMonitorImpl;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is used as part of the experimental multi-threading functionality. It essentially reuses the download
 * method in <code>DownloadUtilsMacro</code> which changes that allow for synchronous SFTP connections and monitoring.
 * 
 * @author wangvg
 * 
 */
//public class DownloadThread implements Runnable {
public class DownloadThread extends Thread {

	private PackageTableModel tableModel;

	private DownloadItem downloadItem;

	private SftpClient sftp;

	private boolean cancelled;

	private boolean lastFileInPackage;

	public DownloadThread(PackageTableModel model, DownloadItem item) {
		tableModel = model;
		downloadItem = item;
		lastFileInPackage = false;
	}

	/**
	 * This is basically the download method in DownloadUtilsMacro
	 */
	@Override
	public void run() {

		UserFile userFile = downloadItem.getUserFile();
		final int currentIndex = downloadItem.getTableIndex();
		final long id = downloadItem.getId();
		cancelled = false;

		SftpProgressMonitorImpl sftpMonitor = new SftpProgressMonitorImpl(downloadItem.getUserFileSize());
		ProgressRunnable progressUpdater = new ProgressRunnable(tableModel, sftpMonitor);
		// download can can be cancelled or network connection could be lost
		try {
			sftp = SftpClientManager.getClient(userFile.getDatafileEndpointInfo());
			(new Thread(progressUpdater)).start();
			//synchronized (sftp) {
				sftp.downloadOverwrite(userFile.getName(), userFile.getPath(), downloadItem.getDest(), sftpMonitor);
			//}
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					if (!cancelled) {
						DownloadController.downloadableComplete(tableModel, currentIndex, id);
					} else {
						// Just as a sanity check just in case the error on cancellation isn't thrown
						DownloadController
								.setDownloadableStatus(tableModel, currentIndex, id, DownloadStatus.CANCELLED);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (!cancelled) {
						DownloadController.changeRowStatus(tableModel, currentIndex, DownloadStatus.ERROR);
						DownloadController.setDownloadableStatus(tableModel, currentIndex, id, DownloadStatus.ERROR);
					} else {
						// Just as a sanity check just in case the error on cancellation isn't thrown
						DownloadController.changeRowStatus(tableModel, currentIndex, DownloadStatus.CANCELLED);
						DownloadController
								.setDownloadableStatus(tableModel, currentIndex, id, DownloadStatus.CANCELLED);
					}
				}
			});

			if (!cancelled) {
				progressUpdater.errorThrown();
			}
		}
		if (!cancelled) {
			// Signal that this thread is done to allow
			// another one to start
			DownloadUtilsMacro.threadDone();
		}
	}
	public void cancel() {
		cancelled = true;
		if (sftp != null){
			SftpClientManager.closeClient(sftp);
		}
	}

	/**
	 * Tell this thread that it needs to continue monitoring the package after completion to prevent premature updating
	 * of the macro view
	 */
	public void lastFile() {
		lastFileInPackage = true;
	}

	private class ProgressRunnable implements Runnable {
		private PackageTableModel packageModel;

		private SftpProgressMonitorImpl progressMonitor;

		private boolean error;

		public ProgressRunnable(PackageTableModel model, SftpProgressMonitorImpl progressMonitor) {
			super();
			this.packageModel = model;
			this.progressMonitor = progressMonitor;
		}

		@Override
		public void run() {

			final int currentIndex = downloadItem.getTableIndex();
			final long id = downloadItem.getId();

			do {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						DownloadController.updateProgress(packageModel, currentIndex, id,
								progressMonitor.getTransferred());
					}
				});
				try {
					Thread.sleep(DownloadPackageView.UI_UPDATE_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (progressMonitor.getPercentComplete() < 100 && !cancelled && !error);
			if (cancelled) {
				// adding catch block
				progressMonitor.cancel();
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						DownloadController.setDownloadableStatus(packageModel, currentIndex, id,
								DownloadStatus.CANCELLED);
					}
				});

			} else if (lastFileInPackage) {
				// The last thread spawned for this package should monitor the rest of the package
				// so that it doesn't prematurely update the macro view until the entire package
				// has finished
				final PackageRow item = tableModel.get(currentIndex);
				final ArrayList<DownloadableRow> rows = item.getDownloadingRows();
				ArrayList<Integer> checkRows = new ArrayList<Integer>();
				// Compile list of all indicies
				for (int i = 0; i < rows.size(); i++) {
					checkRows.add(i);
				}
				// Continuously monitor any downloading channels.
				while (!checkRows.isEmpty()) {
					Iterator<Integer> iter = checkRows.iterator();
					while (iter.hasNext()) {
						int i = iter.next();
						DownloadableRow row = rows.get(i);
						// Errors and completed are removed since they
						// should no longer be downloading
						if (row.isCompleted() || row.getDownloadStatus() == DownloadStatus.ERROR) {
							iter.remove();
						}
					}
					if (!checkRows.isEmpty()) {
						try {
							Thread.sleep(DownloadPackageView.UI_UPDATE_INTERVAL);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					// If the thread is cancelled while still monitoring, you need to stop.
					// The macro view gets updated elsewhere, so no other work needs to occur.
					if (cancelled) {
						return;
					}
				}

				// Set the proper download status on the parent package
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						for (int i = 0; i < rows.size(); i++) {
							DownloadableRow row = rows.get(i);
							if (row.getDownloadStatus() == DownloadStatus.ERROR) {
								item.setStatus(DownloadStatus.ERROR);
								break;
							}
						}
						if (item.getStatus() != DownloadStatus.ERROR) {
							item.setStatus(DownloadStatus.COMPLETED);
						}
						tableModel.fireTableRowsUpdated(currentIndex, currentIndex);
					}
				});

			}
		}

		private void errorThrown() {
			error = true;
		}

	}
}
