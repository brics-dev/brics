package dataimport;

import gov.nih.tbi.repository.UploadItem;
import gov.nih.tbi.repository.UploadStatus;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;
import gov.nih.tbi.repository.service.io.SftpProgressMonitorImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import com.jcraft.jsch.JSchException;

import org.apache.log4j.Logger;

/**
 * This thread is to open a connection to our sftp and begin upload. 
 */
public class UploadChannel implements Runnable
{
	static Logger logger = Logger.getLogger(ImportDelegate.class);

    private Properties connectionConfig;

    private UploadItem uploadItem;
    private SftpClient sftp = null;
    private SftpProgressMonitorImpl progress = null;
    private String filePath;

    public UploadChannel(UploadItem uploadItem, Properties connectionConfig)
    {

        this.connectionConfig = connectionConfig;

        this.uploadItem = uploadItem;
        this.filePath = connectionConfig.getProperty("TBI_SFTP_BASEDIR") + uploadItem.getStudyName() + "/"
                + uploadItem.getDatasetName() + "/";
    }

    public void run()
    {

        try
        {
            DatafileEndpointInfo info = new DatafileEndpointInfo();
                       
            info.setEndpointName(connectionConfig.getProperty("TBI_SFTP_NAME"));
            info.setUrl(connectionConfig.getProperty("TBI_SFTP_URL"));
            info.setUserName(connectionConfig.getProperty("TBI_SFTP_USER"));
            info.setPassword(connectionConfig.getProperty("TBI_SFTP_PASSWORD"));
            info.setPort(Integer.valueOf(connectionConfig.getProperty("TBI_SFTP_PORT")));
            
            File uploadFile = new File(uploadItem.getDatasetFile().getLocalLocation());
            sftp = SftpClientManager.getClient(info);

            progress = new SftpProgressMonitorImpl(uploadFile.length());

            sftp.upload(uploadFile, filePath, uploadFile.getName(), progress);

        }
        catch (FileNotFoundException e)
        {
            this.cancelUpload();
  
        	logger.fatal(e);
        }
        catch (JSchException e)
        {
        	logger.fatal(e);
        }
        catch (Exception e)
        {
        	logger.fatal(e);

        }
    }

    /**
     * Returns the full file path in the sftp
     * 
     * @return
     */
    public String getFilePath()
    {

        return filePath != null ? (filePath + uploadItem.getDatasetFile().getUserFile().getName()) : null;
    }

    public void cancelUpload()
    {

            progress.cancel();
            uploadItem.setUploadStatus(UploadStatus.CANCELLED);
       
    }
}
