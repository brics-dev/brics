
package gov.nih.tbi.repository.portal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.AccountActionType;
import gov.nih.tbi.account.model.SessionAccountEdit;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.ws.RestDictionaryProvider;
import gov.nih.tbi.repository.model.hibernate.UserFile;

/**
 * File Download Action.
 * 
 * @author Andrew Johnson
 * 
 */
public class FileDownloadAction extends BaseAction
{

    private static final long serialVersionUID = 5481745726079966413L;

    /******************************************************************************************************/

    @Autowired
    RepositoryManager repositoryManager;

    @Autowired
    AccountManager accountManager;
    
    @Autowired
	protected SessionAccountEdit sessionAccountEdit;

	private String fileId;

	private String userId;

	private UserFile userFile;

	private InputStream myStream;
	
	private String zipFileName;
	
	private String contentType;
	
	private ByteArrayInputStream inputStream;
	
	public long deleteUserFileId;
	
	private Account currentAccount;
	


    /******************************************************************************************************/

    public void setFileId(String fileId)
    {

        this.fileId = fileId;
    }

	public String getFileId() {
		return fileId;
	}

    public void setUserId(String userId)
    {

        this.userId = userId;
    }

	public String getUserId() {
		return userId;
	}

    /**
     * returns the stream for the user to download the file
     * 
     * @return
     * @throws NumberFormatException
     * @throws JSchException
     * @throws SftpException
     */
    public InputStream getMyStream()
    {

        return myStream;
    }

    /**
     * Used during the stream to correctly set the file name
     * 
     * @return
     */
    public String getFilename()
    {

        return userFile.getName();
    }

	/**
	 * Used during the stream to set the file's size to support the browsers progress bar.
	 * 
	 * @return The file's size in bytes.
	 */
	public Long getFileSize() {
		return userFile.getSize();
	}
	
	
	
	
	public String deleteUserFile() { 
		
		
		userFile = repositoryManager.getFileById(deleteUserFileId);
		String fileName = userFile.getName();
		
		repositoryManager.removeUserFile(deleteUserFileId);
		
		currentAccount = sessionAccountEdit.getAccount();
		
		recordDeleteFile(fileName);
		currentAccount = accountManager.saveAccount(currentAccount);
		sessionAccountEdit.setAccount(currentAccount);
		
		
		return null;
	}
	
	

    /******************************************************************************************************/

	/**
	 * Action sets up the download
	 * 
	 * @return Action string.
	 * @throws SftpException When there was an error while establishing a SFTP connection to the SFTP site.
	 * @throws JSchException When there was an error retrieving data on the file.
	 */
	public String download() throws JSchException, SftpException {

		// Get the files meta data.
		userFile = repositoryManager.getFileById(Long.valueOf(fileId));

		// Get and set the file's size
		Long fileSize = repositoryManager.getFileSize(userFile);
		userFile.setSize(fileSize);

		// Get an input stream to the file.
		myStream = repositoryManager.getFileStream(userFile);

		return PortalConstants.ACTION_INPUT;
	}
	
	
	
	
	public String downloadAll() throws JSchException, SftpException {
		currentAccount = sessionAccountEdit.getAccount();
		List<UserFile> userFiles = repositoryManager.getAdminFiles(currentAccount.getUser().getId());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);

		zipFileName  = currentAccount.getUserName() + "_all.zip";
		contentType = ServiceConstants.APPLICATION_ZIP_FILE;

		if(userFiles != null && userFiles.size() > 0) {
				try {
					Iterator<UserFile> iter = userFiles.iterator();
					while(iter.hasNext()) {
						UserFile userFile = iter.next();
						InputStream data = repositoryManager.getFileStream(userFile);
						
						ZipEntry entry = new ZipEntry(userFile.getName());
						zos.putNextEntry(entry);
						byte[] buffer = new byte[256];
					    int bytesRead = 0;
					    while ((bytesRead = data.read(buffer)) != -1) {
					    	zos.write(buffer);
					    }
						zos.closeEntry();
					}
					zos.close(); 
					inputStream = new ByteArrayInputStream(baos.toByteArray());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
			        try {
						baos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }	
		}
		
		return PortalConstants.ACTION_DOWNLOAD;
	}
	


    /**
     * Action sets up the download
     * 
     * @return
     * @throws SftpException
     * @throws JSchException
     */
    public String downloadDDT() throws JSchException, SftpException
    {

        RestDictionaryProvider restProvider = new RestDictionaryProvider(
                modulesConstants.getModulesDDTURL(getDiseaseId()), null);
        userFile = restProvider.getDocument(Long.valueOf(fileId));
        myStream = repositoryManager.getFileStream( userFile);
        return PortalConstants.ACTION_INPUT;
    }

	public String getZipFileName() {
		return zipFileName;
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public ByteArrayInputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(ByteArrayInputStream inputStream) {
		this.inputStream = inputStream;
	}

	public long getDeleteUserFileId() {
		return deleteUserFileId;
	}

	public void setDeleteUserFileId(long deleteUserFileId) {
		this.deleteUserFileId = deleteUserFileId;
	}
    
    
    
	public void recordDeleteFile(String paramFileName){
		
		String actionTypeArguments = paramFileName;
		
		AccountHistory deleteFileHistory = new AccountHistory(currentAccount,AccountActionType.FILE_DELETE,actionTypeArguments,"",new Date(), getUser());
		currentAccount.addAccountHistory(deleteFileHistory);
		
	}

}
