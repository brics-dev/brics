package gov.nih.tbi.download.workers;

import gov.nih.tbi.download.table.PackageRow;
import gov.nih.tbi.download.table.PackageTableModel;
import gov.nih.tbi.download.view.DownloadController;
import gov.nih.tbi.download.view.DownloadPackageView;

import java.util.Iterator;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * "Swingception" -Francis
 * 
 * Another swing worker to properly clear completed downloads, with the deletion occuring in the worker's thread while
 * the UI updates occur in the done() method.
 * 
 * @author wangvg
 * 
 */
public class ClearCompletedWorker extends SwingWorker<Void, Void> {

	private DownloadPackageView parentView;
	private DownloadController controller;

	public ClearCompletedWorker(DownloadPackageView view, DownloadController controller) {
		parentView = view;
		this.controller = controller;
	}

	@Override
	protected Void doInBackground() throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					controller.clearCompleted(parentView);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return null;
	}

	public void done() {
		
		PackageTableModel macro = parentView.getModel();

		Iterator<PackageRow> itemIter = macro.getRows().iterator();
		
		int index = parentView.getDisplayedRowIndex();
		while (itemIter.hasNext()) {
			PackageRow item = itemIter.next();
			if (item.getFiles().size() > 0) {
				// Refreshes the view of the displayed package
				if (index != -1 && item == macro.get(index)) {
					parentView.openEditor(index);
					// when partial set of file(s) remains, the package row should be unchecked.
					item.setSelected(false);
					item.getTotalFileSize(); // update package size
					// parentView.getModel().fireTableCellUpdated(index, PackageTableModel.CHECKBOX_COLUMN);
					parentView.getModel().fireTableRowsUpdated(index, index);
				}
			} else {
				// If a package is fully completed, it needs to be removed. If
				// said package was also displayed, clear the micro table.
				if (index != -1 && item == macro.get(index)) {
					parentView.getMicroView().clearTable();
				}
				itemIter.remove();

				// UI updating
				parentView.getModel().fireTableRowsDeleted(index, index);
				index = -1;
				parentView.setDisplayedRowIndex(-1);
			}
		}
		// clean up detail panel when the corresponding package has been deleted
		parentView.getMicroView().clearTable();
		parentView.getTable().getTableHeader().repaint();
		parentView.refreshButtonsAndStatistics();
		parentView.getLoadingDialog().setVisible(false);
	}

}
