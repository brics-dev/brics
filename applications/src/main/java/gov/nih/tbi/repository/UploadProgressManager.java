package gov.nih.tbi.repository;

import gov.nih.tbi.repository.service.io.SftpProgressMonitorImpl;
import gov.nih.tbi.repository.table.UploadTableModel;

import java.awt.EventQueue;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * This class runs in a separate thread to update the progress bar with upload progress.
 * 
 * @see UploadManager#UI_UPDATE_INTERVAL to set the update interval.
 * 
 */
public class UploadProgressManager implements Runnable {
	static Logger logger = Logger.getLogger(UploadProgressManager.class);
	private int currentProgress = 0;
	private Long startTime;
	private Long timeElapsed;
	private Long fileSize;
	private SftpProgressMonitorImpl progress;
	private String status = UploadStatus.QUEUED.name;
	private boolean cancel = false;
	private UploadItem uploadItem;

	public UploadProgressManager(UploadItem uploadItem, Long fileSize, SftpProgressMonitorImpl progress) {

		this.fileSize = fileSize;
		this.progress = progress;
		this.uploadItem = uploadItem;
	}

	@Override
	public void run() {

		startUpload();
	}

	public void startUpload() {

		cancel = false;

		do {
			try {
				status = UploadStatus.UPLOADING.name;

				Long currentTime = System.currentTimeMillis();

				if (startTime == null) {
					startTime = currentTime;
				} else {
					timeElapsed = currentTime - startTime;
				}

				currentProgress = progress.getPercentComplete();
				if (currentProgress == 100) {
					status = UploadStatus.COMPLETED.name;
					uploadItem.setUploadStatus(UploadStatus.COMPLETED);
				}
				//System.out.println("Running Procress update for: "  + uploadItem.getDatasetFile().getLocalLocation() + ": progress= " + currentProgress + "; uploadSatus="  + status);
				SwingUtilities.invokeLater(new Runnable(){
		            public void run() {
						((UploadTableModel) UploadManager.getTable().getModel()).fireTableRowsUpdated(
								uploadItem.getTableIndex(), uploadItem.getTableIndex());
		             }
		        });				
				Thread.sleep(UploadManager.UI_UPDATE_INTERVAL);
			} catch (InterruptedException e) {
				uploadItem.setUploadStatus(UploadStatus.CANCELLED);
				if(UploadManager.isUploading()){
					JOptionPane.showMessageDialog(null, "An error has occured while uploading the file.");
				}
				else{
					System.out.println("****As expected, the progressmonitor thread was interrupted***");
				}
				cancel = true;
				Thread.currentThread().interrupt();
			}
			logger.debug("Progress for file:" + uploadItem.getFileName() );

			// initiates the next upload in the queue
			//if (currentProgress == 100) {
			//	uploadItem.setUploadStatus(UploadStatus.COMPLETED);
			//	UploadManagerController.uploadCompleteAction(uploadItem);
			//}

		} while (currentProgress < 100 && !cancel && UploadManager.isUploading());
	}

	/**
	 * Gets the average upload speed of this thread.
	 * 
	 * @return
	 */
	public Long getUploadSpeed() {

		double speedByte;
		long currentSpeed;
		long previousSpeed = 0;
		float output;

		if (!cancel && timeElapsed != null && timeElapsed != 0 && progress.getPercentComplete() < 100) {
			// divides the byte transferred by milliseconds elapsed
			speedByte = progress.getTransferred() / timeElapsed;
			currentSpeed = (long) (speedByte);
		} else {
			return 0L;
		}

		if (previousSpeed == 0) {
			previousSpeed = currentSpeed;
			output = currentSpeed;
		} else {
			// averages out the previous update with current one to achieve average
			output = previousSpeed + currentSpeed / 2.0f;
		}

		return Long.valueOf((long) output);
	}

	public String getUploadSpeedString() {

		return (!cancel ? getUploadSpeed() : 0) + UploadTableModel.UPLOAD_SPEED_UNIT;
	}

	/**
	 * Returns the time remaining to completely upload the file
	 * 
	 * @return
	 */
	public String getTimeToCompletion() {

		if (cancel || getUploadSpeed() <= 0) {
			return "0h 0m 0s";
		}

		Long Millis =
				(long) ((fileSize - Long.valueOf((long) progress.getTransferred())) / (progress.getTransferred() / timeElapsed));

		String convert =
				String.format("%dh %dm %ds", Millis / (1000 * 60 * 60), (Millis % (1000 * 60 * 60)) / (1000 * 60),
						((Millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

		return convert;
	}

	public int getCurrentProgress() {

		return currentProgress;
	}

	public String getStatus() {

		return status;
	}

	/**
	 * Cancel the upload
	 */
	public void cancel() {
		System.out.println("Before cancel for: "  + uploadItem.getDatasetFile().getLocalLocation() + ": progress= " + currentProgress   + "; uploadSatus="  + status);
		cancel = true;
		currentProgress = 0;
		status = UploadStatus.CANCELLED.name;
		//UploadManagerController.singleItemUploadDone();
	}

	public void cancelAndStop() {
		System.out.println("Before cancel&Stop for: "  + uploadItem.getDatasetFile().getLocalLocation() + ": progress= " + currentProgress + "; uploadSatus="  + status);
		cancel = true;
		currentProgress = 0;
		status = UploadStatus.CANCELLED.name;
	}

	public void setToRetry() {
		System.out.println("Before retry for: "  + uploadItem.getDatasetFile().getLocalLocation() + ": progress= " + currentProgress + "; uploadSatus="  + status);
		cancel = true;
		progress = new SftpProgressMonitorImpl(fileSize);
		status = UploadStatus.RETRY.name;
	}

	public void setProgress(int i) {
		currentProgress = i;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
