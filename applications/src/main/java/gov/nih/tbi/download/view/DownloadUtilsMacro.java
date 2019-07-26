package gov.nih.tbi.download.view;

import gov.nih.tbi.commons.model.DownloadStatus;
import gov.nih.tbi.download.Model.DownloadItem;
import gov.nih.tbi.download.Model.DownloadReturnCode;
import gov.nih.tbi.download.table.DownloadableRow;
import gov.nih.tbi.download.table.PackageRow;
import gov.nih.tbi.download.table.PackageTableModel;
import gov.nih.tbi.download.workers.DownloadThread;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadUtilsMacro {

	private static boolean cancelled;

	public final static int MAX_CHANNELS = 4;

	private static int currentChannels;



	/**
	 * Multi-threaded version (in the sense that multiple files will be downloaded at a time). It's a little rudimentary
	 * for now, but generally works as expected.
	 * 
	 * @param model
	 * @param indices
	 * @param dest
	 * @return
	 */
	public static DownloadReturnCode downloadRows(final PackageTableModel model, final List<Integer> indices,
			final String dest) {

		ArrayList<DownloadThread> threadPool = new ArrayList<DownloadThread>();
		cancelled = false;

		// update all download package status and associated files. force it to EDT thread.
		try {
			EventQueue.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				for (final int index : indices) {
					final PackageRow pkg = model.get(index);
					pkg.clearDownloads();
					// we're starting download, so change current row to 'in progress'
					DownloadController.changeRowStatus(model, index, DownloadStatus.IN_PROGRESS);
					for (DownloadableRow row : pkg.getFiles()) {
						if (row.isSelected()) {
							pkg.setDownloadableStatus(row, DownloadStatus.PENDING);
							pkg.addDownload(row);
							row.setCompleted(false);
						}
					}
				}
			}

		});
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (final int index : indices) {
			final PackageRow pkg = model.get(index);
			// Only care about keeping track of progress for the downloading rows
			ArrayList<DownloadableRow> downloadingRows = pkg.getDownloadingRows();
			for (int i = 0; i < downloadingRows.size(); i++) {
				final DownloadableRow row = downloadingRows.get(i);
				if (row.isSelected()) {
					DownloadItem downloadItem =
							new DownloadItem(index, dest + File.separator + pkg.getPackage().getName(), row
									.getDownloadable().getUserFile(), pkg.getStatus(), row.getDownloadable().getId());

					DownloadThread downloadThread = new DownloadThread(model, downloadItem);
					currentChannels++;
					// Rudimentary way to prevent too many threads from starting
					// Block until there are available threads
					while (currentChannels > MAX_CHANNELS) {
						try {
							Thread.sleep(DownloadPackageView.UI_UPDATE_INTERVAL);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (cancelled) {
							// Cancel all threads. Threads that have already
							// finished won't do anything.
							try {
								for (DownloadThread thread : threadPool) {
									thread.cancel();
								}
							} catch (Exception e) {
								e.printStackTrace();
								return DownloadReturnCode.CANCELLED;
							}
							currentChannels = 0;
							return DownloadReturnCode.CANCELLED;
						}
					}

					threadPool.add(downloadThread);

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							pkg.setDownloadableStatus(row, DownloadStatus.IN_PROGRESS);
						}
					});
					if (i == downloadingRows.size() - 1) {
						// The last file in the package will monitor the rest of
						// the package for completion
						downloadThread.lastFile();
					}
					// downloadThread.run();
					downloadThread.start();
				}
			}
		}

		// Holds until all threads are done before cleaning up
		// and sending out the proper error codes to the
		// swing worker
		while (currentChannels > 0) {
			try {
				Thread.sleep(DownloadPackageView.UI_UPDATE_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (cancelled) {
				for (DownloadThread thread : threadPool) {
					thread.cancel();
				}
				currentChannels = 0;
				return DownloadReturnCode.CANCELLED;
			}
		}

		for (final int index : indices) {
			final PackageRow pkg = model.get(index);
			if (pkg.getStatus() == DownloadStatus.ERROR)
				return DownloadReturnCode.ERROR;
		}

		return DownloadReturnCode.COMPLETED;
	}

	/**
	 * Sets the cancelled flag. The individual threads will be notified of this and will take care of clean up.
	 */
	public static void cancelDownload() {
		cancelled = true;
	}

	/**
	 * Called from a DownloadThread. Decrements the number of available running threads so that another thread can start
	 * downloading.
	 */
	public static void threadDone() {
		currentChannels--;
	}

}
