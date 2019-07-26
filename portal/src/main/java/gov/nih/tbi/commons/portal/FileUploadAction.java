
package gov.nih.tbi.commons.portal;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jcraft.jsch.JSchException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class FileUploadAction extends BaseAction
{

    @Autowired
    RepositoryManager repositoryManager;

    /**
	 * 
	 */
    private static final long serialVersionUID = -3559461252664960474L;

    /********************************************************************************************/

    private File upload;

    private String uploadContentType;

    private String uploadFileName;
    
    private String fileErrors;

    /********************************************************************************************/

    public void setUpload(File upload)
    {

        this.upload = upload;
    }

    public void setUploadContentType(String uploadContentType)
    {

        this.uploadContentType = uploadContentType;
    }

    public void setUploadFileName(String uploadFileName)
    {

        this.uploadFileName = uploadFileName;
    }

    public String getFileErrors()
    {

        return fileErrors;
    }

    public void setFileErrors(String fileErrors)
    {

        this.fileErrors = fileErrors;
    }
    
    public String upload() throws SocketException, IOException
    {

        // repositoryManager.uploadFile( upload, uploadFileName, "" );

        return "";

    }

    
    public String view()
    {
        return PortalConstants.ACTION_VIEW;

    }
    
    public String commitChange() throws SocketException, IOException, JSchException
    {
    	 if (upload.length() > PortalConstants.FILE_UPLOAD_MAX)
         {
             logger.info(upload.length());
             fileErrors = "File not uploaded: This file is greater than 5MB.";
         }
         else
             if (upload != null && upload.exists() && uploadFileName != null && !uploadFileName.isEmpty()
                    )
             {

                 // Call webservice
                 /*                    UserFile userFile = webServiceManager.getRepositoryProvider(getAccount(), getDiseaseId())
                                             .uploadFile(upload, uploadFileName, uploadDescription,
                                                     ServiceConstants.FILE_TYPE_DICTIONARY);*/

                 // Replace with web service call after dictionary split

//                 UserFile userFile = new UserFile();
//
//                 if (!modulesConstants.getModulesDDTEnabled())
//                     userFile = repositoryManager.uploadFile(getUser().getId(), upload, uploadFileName,
//                             uploadDescription, ServiceConstants.FILE_TYPE_DICTIONARY);
//                 else
//                     userFile = repositoryManager.uploadFileDDT(getUser().getId(), upload, uploadFileName,
//                             uploadDescription, ServiceConstants.FILE_TYPE_DICTIONARY);
//
//                 dataStructure.setDocumentationFileId(userFile.getId());
//                 dataStructure.setDocumentationUrl(null);
             }
             else
             {

                 fileErrors = "File not uploaded: Please choose a file AND enter in a description!";
             }
        return PortalConstants.ACTION_REDIRECT;

    }
}
