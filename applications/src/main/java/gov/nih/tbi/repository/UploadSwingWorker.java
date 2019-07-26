package gov.nih.tbi.repository;

import gov.nih.tbi.repository.table.UploadTableModel;

import java.awt.HeadlessException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;

public class UploadSwingWorker extends 	SwingWorker<UploadStatus, Integer>{
	static Logger logger = Logger.getLogger(UploadSwingWorker.class);
		//private UploadManager view;
		UploadStatus status = UploadStatus.QUEUED;
		
		@Override
		protected  UploadStatus doInBackground() {
			try {
				status  = UploadManagerController.invokeUpload();
				// update all file statusin the database
				if (status.equals(UploadStatus.COMPLETED)){
					for(UploadItem uploadItem: UploadManagerController.getUploadList()){
						UploadManagerController.setUploadToComplete(uploadItem);
					}
				}
			} catch (CancellationException e) {
				status  = UploadStatus.CANCELLED;
				logger.info("Upload cancelled: " + e.getLocalizedMessage());
			} catch (HeadlessException e1) {
				status  = UploadStatus.CANCELLED;
				logger.info("Upload cancelled: " + e1.getLocalizedMessage());
			} catch (InterruptedException e1) {
				status  = UploadStatus.CANCELLED;
				logger.info("Upload cancelled: " + e1.getLocalizedMessage());
			} catch (Exception e1) {
				status  = UploadStatus.CANCELLED;
				logger.info("Upload cancelled: " + e1.getLocalizedMessage());
			}
			return status;
		}
		
		@Override
		protected void done() {
			if (status.equals(UploadStatus.COMPLETED)){
				JOptionPane.showMessageDialog(null, "All data uploads have been completed.", "Upload Complete",
						JOptionPane.INFORMATION_MESSAGE);
			}
			else{
				JOptionPane.showMessageDialog(null, "Upload is cancelled.", "Upload Incomplete",
						JOptionPane.WARNING_MESSAGE);
				//UploadTableModel tableModel  = (UploadTableModel) UploadManager.getTable().getModel();
				//tableModel.cancelAllUpload();
			}
			UploadManager.systemBusyDialog.setVisible(false); 
			UploadManager.setUploadingMode(false);
		}
}
