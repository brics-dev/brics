package gov.nih.tbi.repository;

import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;
import gov.nih.tbi.repository.service.io.SftpProgressMonitorImpl;
import gov.nih.tbi.repository.table.UploadTableModel;

import gov.nih.tbi.commons.AppConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * This thread is to open a connection to our sftp and begin upload. Also starts a thread to track the upload progress
 * 
 * @author Francis Chen
 */
public class UploadChannel implements Callable <UploadStatus>{
//public class UploadChannel implements Runnable {

	static Logger logger = Logger.getLogger(UploadChannel.class);

	// maximum amount of times to retry connection to FTP after connection failure
	private int uploadRetryCounter = 0;

	private final int TIMES_TO_RETRY = 10;
	private UploadProgressManager updater;
	private UploadItem uploadItem;

	private SftpProgressMonitorImpl progress = null;
	private String filePath;
	private DatafileEndpointInfo info = new DatafileEndpointInfo();
	private File uploadFile;

	public UploadChannel(UploadItem uploadItem) {

		AppConfig config = AppConfig.getInstance();
		String sftBaseDir = config.getProperty("SFTP_BASEDIR");
		String sftpUrl = config.getProperty("SFTP_URL");
		String sftpName = config.getProperty("SFTP_NAME");
		String sftpUser = config.getProperty("SFTP_USER");
		String sftpPasswd = config.getProperty("SFTP_PASSWORD");
		String sftpPortStr = config.getProperty("SFTP_PORT");

		this.uploadItem = uploadItem;
		this.filePath = sftBaseDir + uploadItem.getStudyPrefixedId() + "/" + uploadItem.getDatasetName() + "/";

		this.info.setEndpointName(sftpName);
		this.info.setUrl(sftpUrl);
		this.info.setUserName(sftpUser);
		this.info.setPassword(sftpPasswd);
		this.info.setPort(Integer.valueOf(sftpPortStr));

		this.uploadFile = new File(uploadItem.getDatasetFile().getLocalLocation());
	}

	/*
	 * This method has been coverted to callable.  J. Liu
	 */
	@Deprecated
	public void run() {
		try {
			startUpload();
		} catch (FileNotFoundException e) {
			this.cancelUpload();
			// displays error/confirm message for the user to delete an uploading dataset.
			int confirmed =
					JOptionPane.showConfirmDialog(null, "Cannot find the file '" + uploadItem.getFileName()
							+ "', would you like to remove this pending dataset?", "File Not Found",
							JOptionPane.YES_NO_OPTION);

			if (confirmed == JOptionPane.OK_OPTION) {
				try {
					UploadManager.provider.deleteDatasetByName(uploadItem.getStudyName(), uploadItem.getDatasetName());
				} catch (HttpException e1) {
					JOptionPane.showMessageDialog(null,
							"Error occured while trying the delete the dataset.  The dataset was not deleted",
							"Deletion Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null,
							"Error occured while trying the delete the dataset.  The dataset was not deleted",
							"Deletion Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			//e.printStackTrace();
			System.out.println("I: Exception Error occured");
			this.cancelUpload();
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Given a sftpclient, do the upload
	 * 
	 * @param sftp
	 * @throws Exception
	 */
	private void doUpload(SftpClient sftp) throws Exception {
		try {
			sftp.upload(uploadFile, filePath, uploadFile.getName(), progress);
		} catch (JSchException e) {
			// retry the upload, dont worry, we will printstack trace if upload retry does work
			System.out.println("Error occured, retrying upload...");
			retryUpload(sftp);
		} catch (IOException e) {
			// retry the upload, dont worry, we will printstack trace if upload retry does work
			System.out.println("Error occured, retrying upload...");
			retryUpload(sftp);
		}

		logger.info("Upload complete!");
	}

	private void startUpload() throws Exception {
		uploadFile = new File(uploadItem.getDatasetFile().getLocalLocation());
		progress = new SftpProgressMonitorImpl(uploadFile.length());
		updater = new UploadProgressManager(uploadItem, uploadFile.length(), progress);
		Thread UploadProgresThread = new Thread(updater);
		UploadProgresThread.start();

		SftpClient sftp = SftpClientManager.getClient(info);
		doUpload(sftp);
	}

	private void retryUpload(SftpClient sftp) {
		try {
			Thread.sleep(1000); // wait before trying to upload again
			if (uploadRetryCounter < TIMES_TO_RETRY) {
				uploadRetryCounter++;
				System.out.println("Retrying upload..");
				doUpload(sftp);
			} else {
				System.out.println("Reached max upload retry...stopping");
			}
		} catch (JSchException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error has occured during upload. Check your internet connection.",
					"Upload Error", JOptionPane.ERROR_MESSAGE);
			this.cancelUpload();
		} catch (InterruptedException e) {
			JOptionPane.showMessageDialog(null, "Fatal error has occured during upload.", "Upload Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			this.cancelUpload();
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Fatal error has occured during upload.", "Upload Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			this.cancelUpload();
		}
	}

	public String getStatus() {

		if (updater == null) {
			return UploadStatus.QUEUED.getName();
		} else {
			return updater.getStatus();
		}
	}

	/**
	 * Returns the full file path in the sftp
	 * 
	 * @return
	 */
	public String getFilePath() {

		return filePath != null ? (filePath + uploadItem.getDatasetFile().getUserFile().getName()) : null;
	}

	public Integer getUploadProgress() {

		if (updater == null) {
			return 0;
		} else {
			return updater.getCurrentProgress();
		}
	}

	public String getUploadSpeed() {

		return updater == null ? 0 + UploadTableModel.UPLOAD_SPEED_UNIT : updater.getUploadSpeedString();
	}

	public String getTimeToCompletion() {

		return updater == null ? "0h 0m 0s" : updater.getTimeToCompletion();
	}

	public void cancelUpload() {
		updater.cancel();
		uploadItem.setUploadStatus(UploadStatus.CANCELLED);
		progress.cancel();
	}
	
	public void cancelUploadAndStop() {
		updater.cancelAndStop();
		uploadItem.setUploadStatus(UploadStatus.CANCELLED);
		progress.cancel();
	}

	@Override
	public UploadStatus call() throws Exception {
		SftpClient sftp = null;
			try {
				uploadFile = new File(uploadItem.getDatasetFile().getLocalLocation());
				progress = new SftpProgressMonitorImpl(uploadFile.length());
				updater = new UploadProgressManager(uploadItem, uploadFile.length(), progress);
				Thread UploadProgresThread = new Thread(updater);
				//UploadManagerController.MonitorExecutor.submit(updater);
				UploadProgresThread.start();
				sftp = SftpClientManager.getClient(info);
				try {
					sftp.upload(uploadFile, filePath, uploadFile.getName(), progress);
					UploadProgresThread.join();
				} catch (JSchException e) {
					uploadItem.setUploadStatus(UploadStatus.CANCELLED);
					logger.info("Upload cancelled with JSchException exception: " + e.getMessage());
				} catch (SftpException e) {
					uploadItem.setUploadStatus(UploadStatus.CANCELLED);
					logger.info("Upload cancelled with SftpException exception: " + e.getMessage());
				} catch (InterruptedException e) {
					uploadItem.setUploadStatus(UploadStatus.CANCELLED);
					logger.info("Upload cancelled with InterruptedException exception: " + e.getMessage());
					UploadProgresThread.interrupt();
					UploadProgresThread.join();
				} catch (InterruptedIOException e) {
					logger.info("Upload cancelled with InterruptedIOException exception: " + e.getMessage());
					uploadItem.setUploadStatus(UploadStatus.CANCELLED);
				} catch (IOException e) {
					logger.info("Upload cancelled with IOException exception: " + e.getMessage());
					uploadItem.setUploadStatus(UploadStatus.CANCELLED);
				}
				logger.info("Upload complete!");

			} catch (FileNotFoundException e) {
				logger.info("Upload cancelled with FileNotFoundException exception: " + e.getMessage());
				this.cancelUpload();
				// displays error/confirm message for the user to delete an uploading dataset.
				int confirmed =
						JOptionPane.showConfirmDialog(null, "Cannot find the file '" + uploadItem.getFileName()
								+ "', would you like to remove this pending dataset?", "File Not Found",
								JOptionPane.YES_NO_OPTION);

				if (confirmed == JOptionPane.OK_OPTION) {
					try {
						UploadManager.provider.deleteDatasetByName(uploadItem.getStudyName(), uploadItem.getDatasetName());
					} catch (HttpException e1) {
						JOptionPane.showMessageDialog(null,
								"Error occured while trying the delete the dataset.  The dataset was not deleted",
								"Deletion Error", JOptionPane.ERROR_MESSAGE);
						logger.error("Error during deleting dataset: HttpException exception: " + e.getMessage());
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null,
								"Error occured while trying the delete the dataset.  The dataset was not deleted",
								"Deletion Error", JOptionPane.ERROR_MESSAGE);
						logger.error("Error during deleting dataset: IOException exception: " + e.getMessage());
					}
				}
			} catch (InterruptedException e) {
				uploadItem.setUploadStatus(UploadStatus.CANCELLED);
				logger.info("InterruptedException was detected: " + e.getMessage());
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				uploadItem.setUploadStatus(UploadStatus.CANCELLED);
				logger.info("Exception was detected: " + e.getMessage());
			}
			return uploadItem.getUploadStatus();
		}
		public void setupCancelStatus(){
			if (updater != null) {
				updater.setProgress(0);
				updater.setStatus(UploadStatus.CANCELLED.getName());
			}
		}
		public UploadProgressManager getUploadProgressManager(){
			return updater;
		}
}
