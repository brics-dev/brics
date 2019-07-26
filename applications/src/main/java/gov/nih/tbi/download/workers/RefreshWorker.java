package gov.nih.tbi.download.workers;

import gov.nih.tbi.download.table.PackageRow;
import gov.nih.tbi.download.view.DownloadController;
import gov.nih.tbi.download.view.DownloadPackageView;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class RefreshWorker extends SwingWorker<Boolean, PackageRow> {
	private DownloadController myController;
	private DownloadPackageView view;

	public RefreshWorker(DownloadController controller, DownloadPackageView view) {
		this.myController = controller;
		this.view = view;
		view.getModel().clearAll();
		view.getMicroView().clearTable();
		view.enableRefreshButton(false);
	}


	@Override
	protected Boolean doInBackground() throws Exception {
		List<DownloadPackage> DownloadPackages = myController.retrieveDownloadPackages();
		for (DownloadPackage downloadPackage : DownloadPackages) {
			publish(new PackageRow(downloadPackage));
		}
		return myController.varifyPackages(DownloadPackages);
	}

	@Override
	public void process(List<PackageRow> chunks) {
		for (PackageRow row : chunks) {
			view.getModel().add(row);
		}
	}

	@Override
	public void done() {
		Boolean dataIntegrity = true;
		try {
			dataIntegrity = get();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane
					.showMessageDialog(
							null,
							"An error has occured in web service call to retrieve the download list from the server.\nPlease contact the system administrator.",
							"Download Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		// report any compromised data:
		if (!dataIntegrity) {
			JOptionPane
					.showMessageDialog(
							null,
							"Error was detected for the downloadable items retrieved from the server.\nPlease contact the system administrator.",
							"Download Data Error", JOptionPane.ERROR_MESSAGE);
		}

		view.checkHostForFiles();
		view.getLoadingDialog().setVisible(false);
		view.setButtonsStatus(false);
	}
}
