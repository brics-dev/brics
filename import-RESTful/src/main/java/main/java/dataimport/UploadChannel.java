package main.java.dataimport;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import gov.nih.tbi.commons.AppConfig;
import org.apache.log4j.Logger;

import com.jcraft.jsch.JSchException;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.ModulesConstants;

import gov.nih.tbi.repository.UploadItem;
import gov.nih.tbi.repository.UploadStatus;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;
import gov.nih.tbi.repository.service.io.SftpProgressMonitorImpl;


/**
 * This thread is to open a connection to our sftp and begin upload.
 */
public class UploadChannel implements Runnable {
	private static final Logger logger = Logger.getLogger(UploadChannel.class);

	//@Autowired
	//private ModulesConstants modulesConstants;

	//private Properties connectionConfig;
	private UploadItem uploadItem;
	private SftpClient sftp = null;
	private SftpProgressMonitorImpl progress = null;
	private String filePath;

	private AppConfig config = null;

	public UploadChannel(UploadItem uploadItem) {
		//this.connectionConfig = connectionConfig;
		this.uploadItem = uploadItem;

		this.config = AppConfig.getInstance();

		String sftpBaseDir = this.config.getProperty("SFTP_BASEDIR");

		// cant use File.separator here, it will break upload on local (windows) environments. Leaving it hard coded as
		// / since our sftp is always going to be unix based.
		this.filePath = sftpBaseDir + uploadItem.getStudyPrefixedId() + "/"
						+ uploadItem.getDatasetName() + "/";
	}

	public void run() {
		try {
			DatafileEndpointInfo info = new DatafileEndpointInfo();

			String sftpName = this.config.getProperty("SFTP_NAME");
			String sftpUrl = this.config.getProperty("SFTP_URL");
			String sftpUser = this.config.getProperty("SFTP_USER");
			String sftpPasswd = this.config.getProperty("SFTP_PASSWORD");
			String sftpPort = this.config.getProperty("SFTP_PORT");

			info.setEndpointName(sftpName);
			info.setUrl(sftpUrl);
			info.setUserName(sftpUser);
			info.setPassword(sftpPasswd);
			info.setPort(Integer.valueOf(sftpPort));

			File uploadFile = new File(uploadItem.getDatasetFile().getLocalLocation());
			sftp = SftpClientManager.getClient(info);

			progress = new SftpProgressMonitorImpl(uploadFile.length());
			sftp.upload(uploadFile, filePath, uploadFile.getName(), progress);

		} catch (FileNotFoundException e) {
			this.cancelUpload();

			logger.fatal(e);
		} catch (JSchException e) {
			logger.fatal(e);
		} catch (Exception e) {
			logger.fatal(e);

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

	public void cancelUpload() {
		progress.cancel();
		uploadItem.setUploadStatus(UploadStatus.CANCELLED);
	}
}
