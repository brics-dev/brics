package gov.nih.tbi.download.view;

import gov.nih.tbi.commons.WebstartRestProvider;
import gov.nih.tbi.commons.model.DownloadStatus;
import gov.nih.tbi.download.Model.DownloadReturnCode;
import gov.nih.tbi.download.table.DownloadableRow;
import gov.nih.tbi.download.table.PackageRow;
import gov.nih.tbi.download.table.PackageTableModel;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

public class DownloadController {

	private static Logger logger = Logger.getLogger(DownloadController.class);

	/**
	 * Responsible is an object for handling the execution of the actionPerformed methods.
	 * 
	 * The DownloadFrontController is called in the DownloadFrontControllerManager class.
	 */

	private WebstartRestProvider provider;

	/**
	 * Initializes the DownloadFrontController with a RepositoryProvider.
	 * 
	 * @param provider
	 */
	public DownloadController(String serverLocation, String userName, String password) {
		provider = new WebstartRestProvider(serverLocation, userName, password);
	}

	public DownloadController(WebstartRestProvider provider) {
		this.provider = provider;
	}

	/**
	 * The method loops through the table rows represented as indices pass in and cancels them if they have a current
	 * operating download channel
	 * 
	 * @param model - The model
	 */
	public static void stopDownload() {
		DownloadUtilsMacro.cancelDownload();
	}
	public static void stop(PackageTableModel model) {
		DownloadUtilsMacro.cancelDownload();

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
	}

	/**
	 * Downloads the specified packages at the given indices
	 * 
	 * @param model - The model
	 * @param indices - The indices of the packages to download
	 * @param directory - The destination directory
	 */

	public DownloadReturnCode download(PackageTableModel model, List<Integer> indices, final String dest) {
		return DownloadUtilsMacro.downloadRows(model, indices, dest);
		//return ConcurrentDownloading.downloadPackages(model, indices, dest);
	}

	/**
	 * Deletes the provided list of downloadable files by ID
	 * 
	 * @param ids
	 * @throws Exception 
	 */
	public void delete(List<Long> ids) throws Exception {
		if (ids == null || ids.isEmpty()) {
			return; // this means no items were selected for deletion
		}
		try {
			provider.deleteDownloadables(ids);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<DownloadPackage> retrieveDownloadPackages() throws Exception {
		return provider.getDownloadPackage();
	}

	public boolean varifyPackages(List<DownloadPackage> downloadPackages) {
		Boolean dataIntegrity = true;
		for (DownloadPackage downloadPackage : downloadPackages) {
			if (downloadPackage == null) {
				dataIntegrity = false;
				break;
			}
		}
		return dataIntegrity;
	}

	public PackageTableModel convertDownloadPackagesToTableModel(List<DownloadPackage> downloadPackages) {
		PackageTableModel tableModel = new PackageTableModel();
		for (DownloadPackage downloadPackage : downloadPackages) {
			PackageRow row = new PackageRow(downloadPackage);
			tableModel.add(row);
		}
		return tableModel;
	}


	public void populateTable(PackageTableModel dataDownloadModel) {
		List<DownloadPackage> downloadPackages = new ArrayList<DownloadPackage>();
		try {
			downloadPackages = provider.getDownloadPackage();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane
					.showMessageDialog(
							null,
							"An error has occured in web service call to retrieve the download list from the server.\nPlease contact the system administrator.",
							"Download Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		Boolean dataIntegrity = true;
		for (DownloadPackage downloadPackage : downloadPackages) {
			if (downloadPackage != null) {
				PackageRow row = new PackageRow(downloadPackage);
				dataDownloadModel.add(row);
			} else {
				dataIntegrity = false;
				logger.info("Error for DownloadPackage retrieved from server: null package object ");
			}
		}

		// report any compromised data:
		if (!dataIntegrity) {
			JOptionPane
					.showMessageDialog(
							null,
							"Error was detected for the downloadable items retrieved from the server.\nPlease contact the system administrator.",
							"Download Data Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * This is the current download method. This was done so that for the macro-view, the downloads would start in the
	 * same order that they are displayed in. This won't work for the micro-view however.
	 */
	/*
	 * public DownloadReturnCode startDownload(List<Integer> indices, PackageTableModel downloadModel, String rootPath)
	 * throws Exception { return download(downloadModel, indices, rootPath);
	 * 
	 * }
	 */
	public DownloadReturnCode startDownload(JTable table, final PackageTableModel downloadModel, String rootPath)
			throws Exception {

		List<Integer> indices = new ArrayList<Integer>();

		for (int i = 0; i < table.getRowCount(); i++) {
			final int index = table.convertRowIndexToModel(i);
			final PackageRow pkg = downloadModel.get(index);
			if (pkg.isSelected()) {
				DownloadStatus status = pkg.getStatus();

				if (status != DownloadStatus.PENDING || status != DownloadStatus.IN_PROGRESS
						|| status != DownloadStatus.ERROR) {
					indices.add(index);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							pkg.setStatus(DownloadStatus.PENDING);
							downloadModel.fireTableCellUpdated(index, PackageTableModel.STATUS_COLUMN);
						}
					});


				}
			}
		}
		return download(downloadModel, indices, rootPath);

	}
	/**
	 * Changes the status of a row in the data transfer table based on row index
	 * 
	 * @param model - model of the current DataTansferTable
	 * @param index - index of the row to be updated
	 * @param newStatus - new status the row should be changed to
	 */
	public static void changeRowStatus(PackageTableModel model, int index, DownloadStatus newStatus) {
		model.get(index).setStatus(newStatus);
		model.fireTableCellUpdated(index, PackageTableModel.STATUS_COLUMN);
	}

	public static void updateProgress(PackageTableModel model, int index, long id, float transferred) {
		model.get(index).setDownloadableProgress(id, transferred);
		model.fireTableCellUpdated(index, PackageTableModel.PROGRESS_COLUMN);
	}

	public static void downloadableComplete(PackageTableModel model, int index, long id) {
		model.get(index).setDownloadableComplete(id);
		model.fireTableCellUpdated(index, PackageTableModel.STATUS_COLUMN);
	}

	public static void setDownloadableStatus(PackageTableModel model, int index, long id, DownloadStatus newStatus) {
		model.get(index).setDownloadableStatus(id, newStatus);
	}

	/**
	 * Method for going through all the files and removing any that have already completed. It also checks to make sure
	 * that any updates in information between the macro and micro view are properly relayed.
	 * 
	 * @param view
	 * @throws Exception 
	 */
	public void clearCompleted(DownloadPackageView view) throws Exception {
		PackageTableModel macro = view.getModel();

		List<Long> ids = new ArrayList<Long>();

		Iterator<PackageRow> itemIter = macro.getRows().iterator();

		while (itemIter.hasNext()) {
			PackageRow pkg = itemIter.next();
			if (pkg.isSelected()) {
				Iterator<DownloadableRow> rowIter = pkg.getFiles().iterator();
				while (rowIter.hasNext()) {
					DownloadableRow row = rowIter.next();
					if (row.isCompleted()) {
						ids.add(row.getDownloadable().getId());
						rowIter.remove();
					}
				}
			}
		}
		delete(ids);
	}

	/**
	 * Sets the status to any row that is not at 100% and is selected by the user
	 * 
	 * @param model
	 * @param status
	 */
	public static void setNotCompletedCheckedRowStatus(PackageTableModel model, DownloadStatus status) {
		int rowCount = model.getRowCount();

		for (int i = 0; i < rowCount; i++) {
			PackageRow pkg = model.get(i);
			if (pkg.isSelected() && !pkg.isCompleted() && pkg.getStatus() != DownloadStatus.ERROR) {
				// We should be preserving errors in the event that a cancel occurs
				pkg.setStatus(status);
				for (DownloadableRow row : pkg.getFiles()) {
					if (row.isSelected() && !row.isCompleted() && row.getDownloadStatus() != DownloadStatus.ERROR) {
						// We should be preserving errors in the event that a cancel occurs
						pkg.setDownloadableStatus(row, status);
					}
				}
			}
		}

	}

}
