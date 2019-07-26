
package gov.nih.tbi.ordermanagement.transfer;

import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.ordermanager.coriell.CoriellXMLWrapper;
import gov.nih.tbi.ordermanager.model.BioRepository;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.OrderManagerDocument;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.service.io.SFTPConnectionClient;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.jcraft.jsch.JSchException;

public class CoriellSftpTransfer implements TransferInterface
{

    static Logger logger = Logger.getLogger(CoriellSftpTransfer.class);

    // (No autowired dependencies). Rather than pass in the DAO. It may be possible to just not save the coriell order
    // file form the brics repo (it is never fetched).
    // @Autowired
    // UserFileDao userFileDao;

    private static final String CORIELL = "CORIELL";
    private static final String BRICS = "BRICS";
    private static final String ORDER_NAME = "Order_";
    private static final String ORDER_DESCRIPTION = "Order: ";
    private static final String ORDER_FILE_SUFFIX = ".xml";

    private List<String> rollbackFiles = new ArrayList<String>();
    private String filePrefix = ServiceConstants.EMPTY_STRING;
    private BioRepository repo;

    private DatafileEndpointInfo coriellEndpoint;
    private DatafileEndpointInfo bricsEndpoint;

    /**
     * A constructor with all the configuration information needed to send an order to Coriell.
     * 
     * @param repo
     *            : The repository (e.g. NINDS or BioFIND) that the submission is for.
     * @param filePrefix
     *            : A string to append to files so Coriell knows what repository they are for
     * @param CoriellEndpoint
     *            : Endpoint connection information for the Coriell SFTP
     * @param BricsEndpoint
     *            : Endpoint connection information for the BRICS SFTP
     */
    public CoriellSftpTransfer(BioRepository repo, String filePrefix, DatafileEndpointInfo coriellEndpoint,
            DatafileEndpointInfo bricsEndpoint)
    {

        this.repo = repo;
        this.coriellEndpoint = coriellEndpoint;
        this.bricsEndpoint = bricsEndpoint;

        // If a repository name has been supplied then prepend an underscore
        if (filePrefix != null && !filePrefix.equals(ServiceConstants.EMPTY_STRING))
        {
            this.filePrefix = "_" + filePrefix;
        }
    }

    /**
     * Called by the Corriel order manager piece, given a BiospecimenOrder and list of files associated with the order,
     * the files are properly renamed then stored in the corriel sftp. Returns true if the transfer fails with an
     * exception.
     * 
     * @param order
     * @return
     */
    @Override
    public boolean processAndSend(BiospecimenOrder order)
    {

        // First we need to find out if there are any items in this order that match the given repo.
        if (!order.getBioRepositoryList().contains(repo))
        {
            return true; // Return true because no error occurred and as far as the transfer manager is concerned this
                         // is a success.
        }

        SftpClient bricsClient = null;
        SftpClient corrielClient = null;
        try
        {

            // We are currently sending ALL documents attached to the order. This can be altered to only send documents
            // associated with a given repository.
            List<UserFile> attachedFiles = new ArrayList<UserFile>();
            for (OrderManagerDocument doc : order.getDocumentList())
            {
                attachedFiles.add(doc.getUserFile());
            }

            // The orderXML is created here and saved to the BRICS SFTP (to be transfered to Coriell later)
            bricsClient = openConnection(BRICS);
            UserFile orderXMLFile = putOrderFileIntoBRICS(order, bricsClient);

            if (orderXMLFile == null)
            {
                logger.error("Failed to create order XML during Coriell transfer for repository: " + repo.getName()
                        + ". Printing stack trace...");
                return false;
            }

            // put all files into Coriell
            String remoteFileName = "" + order.getId();

            corrielClient = openConnection(CORIELL);
            putAllFilesToCoriell(orderXMLFile, attachedFiles, remoteFileName, bricsClient, corrielClient);

        }
        catch (JSchException e1)
        {
            logger.error("JSch Coriell transfer failure for repository: " + repo.getName()
                    + ". Printing stack trace...");
            e1.printStackTrace();
            return false;
        }
        catch (Exception e)
        {
            logger.error("Unknown Coriell transfer failure for repository: " + repo.getName()
                    + ". Printing stack trace...");
            e.printStackTrace();
            return false;
        }

        logger.info("Successful Coreill transfer for repository: " + repo.getName() + ".");
        return true;
    }

    private void putAllFilesToCoriell(UserFile userXMLFile, List<UserFile> attachedFiles, String remoteFileName,
            SftpClient bricsClient, SftpClient corrielClient) throws Exception
    {

        // byte[] data = IOUtils.readBytesFromStream(orderFileInputStream);
        byte[] data = null;
        data = bricsClient.downloadBytes(userXMLFile.getName(), userXMLFile.getPath());
        corrielClient.upload(data, ServiceConstants.TBI_ORDER_FILE_PATH, userXMLFile.getName());
        rollbackFiles.add(userXMLFile.getName());
        // upload the rest of the files
        // For each file, retrieve the inputstream, change name, and upload it to the corriel Sftp server
        String fileName = "";
        String ext = "";
        String fullFileName = "";
        String tempFileName = "";
        int i = 0;
        for (UserFile uf : attachedFiles)
        {
            data = null;
            data = bricsClient.downloadBytes(uf.getName(), uf.getPath());
            // re-building fullFileName
            fullFileName = uf.getName(); // there is no path.
            i = fullFileName.lastIndexOf('.');
            if (i > 0 && i < fullFileName.length() - 1)
            {
                fileName = fullFileName.substring(0, i);
                ext = fullFileName.substring(i + 1);
                ;
            }
            tempFileName = fileName + "_" + remoteFileName + filePrefix + "." + ext;
            // corrielClient.upload(data, ServiceConstants.TBI_ORDER_FILE_PATH, userFile.getName() + "_" +
            // remoteFileName);
            corrielClient.upload(data, ServiceConstants.TBI_ORDER_FILE_PATH, tempFileName);
            rollbackFiles.add(tempFileName);
        }

    }

    private UserFile putOrderFileIntoBRICS(BiospecimenOrder order, SftpClient bricsClient) throws JSchException
    {

        UserFile orderXMLuserFile = null;
        ByteArrayOutputStream baos = null;

        try
        {

            // Create and save a userfile
            // Call the xml file generator where a output stream is returned.
            CoriellXMLWrapper coriellXML = new CoriellXMLWrapper(order, repo);

            // Create and save XML Order to userfile
            baos = new ByteArrayOutputStream();
            coriellXML.write(baos);
            byte[] data = baos.toByteArray();

            // Upload file to brics.
            String filePath = ServiceConstants.ORDER_MANAGER_FILE_PATH
                    + order.getUser().getFullName().replaceAll(" ", "")
                    + "_"
                    + order.getUser().getId()
                    + "_"
                    + new java.util.Date(order.getDateCreated().getTime()).toString().replaceAll(":", "")
                            .replaceAll(" ", "") + "/";
            String fileName = ORDER_NAME + order.getId() + filePrefix + ".xml";
            bricsClient.upload(data, filePath, fileName);
            orderXMLuserFile = createOrderXmlFile(order, data.length, filePath, fileName);
            // TODO:
            // File file = new File("C://brics/coriTemp.xml");
            // FileOutputStream out = new FileOutputStream("C://brics/coriTemp.xml");
            // out.write(data);
            // out.close();

            baos.close();

        }
        catch (JAXBException e)
        {
            e.printStackTrace();
            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            try
            {
                if (baos != null)
                    baos.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }

        return orderXMLuserFile;
    }

    /**
     * Opens connections to the sftp servers
     * 
     * @return
     * @throws JSchException
     */
    public SftpClient openConnection(String connectionInfo) throws JSchException
    {

        SftpClient client = null;
        if (connectionInfo == BRICS)
        {
            // DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);
            DatafileEndpointInfo info = bricsEndpoint;
            client = SftpClientManager.getClient(info);
        }
        else
        {
            // DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.CORRIEL_DATAFILE_ENDPOINT_ID);
            DatafileEndpointInfo info = coriellEndpoint;
            // info.setKeyPath("C:\\brics\\pdbp.ppk"); // For local testing
            SFTPConnectionClient clientKey = new SFTPConnectionClient(info.getUserName() , info.getUrl());
            client = SftpClientManager.getClient(clientKey, info.getUserName(), info.getPassword(), info.getKeyPath(),
                    info.getUrl(), info.getPort());
        }
        return client;
    }

    public UserFile createOrderXmlFile(BiospecimenOrder order, int fileSize, String filePath, String fileName)
    {

        Long orderId = order.getId();
        UserFile userFile = new UserFile();
        // userFile.setDatafileEndpointInfo(datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID));
        userFile.setDatafileEndpointInfo(bricsEndpoint);
        userFile.setDescription(ORDER_DESCRIPTION + orderId);
        userFile.setName(fileName);
        userFile.setPath(filePath);
        userFile.setUserId(order.getUser().getId());
        userFile.setSize(new Integer(fileSize).longValue());
        // userFile.setSize(output.);
        // return userFileDao.save(userFile);
        return userFile;
    }

    /**
     * Rollback code. Attempts to delete over SFTP all the attached files plus the order file.
     * 
     * @return
     */
    @Override
    public boolean rollback(BiospecimenOrder order)
    {

        SftpClient corrielClient = null;
        try
        {
            corrielClient = openConnection(CORIELL);
            for (String s : rollbackFiles)
            {
                corrielClient.delete(ServiceConstants.TBI_ORDER_FILE_PATH, s);
            }
        }
        catch (Exception e)
        {
            logger.error("Coriell rollback failure for repository: " + repo.getName() + ". Printing stack trace...");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean testProccessAndSend(BiospecimenOrder order, List<UserFile> filesToSend)
    {

        SftpClient bricsClient;
        SftpClient corrielClient;
        InputStream toReturn = null;
        UserFile orderXml;

        /* translate each item's local/BRICS id to foreign id just before sending
        * NO LONGER MASKING SAMPLE IDS
        * NO NEED TO TRANSLATE
        for (BiospecimenItem item : order.getRequestedItems())
        {

            String localId = item.getCoriellId();
            String foreignId = translate(localId);
            item.setCoriellId(foreignId);
        }*/

        try
        {

            CoriellXMLWrapper cXML = new CoriellXMLWrapper(order);

            // Call the xml file generator where a output stream is returned.
            cXML.write(System.out);

        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            return false;
        }

        return true;
    }
}
