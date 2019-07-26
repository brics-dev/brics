package gov.nih.tbi.ordermanagement.transfer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.JSchException;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.iubiorepo.ws.RestBioRepositoryProvider;
import gov.nih.tbi.ordermanager.iu.IUXMLWrapper;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.OrderManagerDocument;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;

@Service
@Scope("singleton")
public class IUWebServiceTransfer {


	static Logger logger = Logger.getLogger(IUWebServiceTransfer.class);


	private static final String BRICS = "BRICS";
	private static final String ORDER_NAME = "Order_";
	private static final String ORDER_DESCRIPTION = "Order: ";

	private DatafileEndpointInfo bricsEndpoint;


	@Autowired
	ModulesConstants modulesConstants;


	public IUWebServiceTransfer() {

	}



	public IUWebServiceTransfer(DatafileEndpointInfo bricsEndpoint) {

		this.bricsEndpoint = bricsEndpoint;

	}



	public boolean processAndSend(BiospecimenOrder order) {
		SftpClient bricsClient = null;

		try {
			// We are currently sending ALL documents attached to the order. This can be altered to only send documents
			// associated with a given repository.
			List<UserFile> attachedFiles = new ArrayList<UserFile>();
			for (OrderManagerDocument doc : order.getDocumentList()) {
				attachedFiles.add(doc.getUserFile());
			}

			// The orderXML is created here and saved to the BRICS SFTP (to be transfered to Coriell later)
			bricsClient = openConnection(BRICS);
			String orderXML = putOrderFileIntoBRICS(order, bricsClient);

			if (orderXML == null) {
				logger.error("Failed to create order XML during IU transfer Printing stack trace...");
				return false;
			}

			// ws call to IU
			String orderXMLFileName = ORDER_NAME + order.getId() + ".xml";
			RestBioRepositoryProvider restBioRepositoryProvider = new RestBioRepositoryProvider(
					modulesConstants.getBiorepositoryIuWsBase(), modulesConstants.getBiorepositoryIuUsername(),
					modulesConstants.getBiorepositoryIuPassword(),
					modulesConstants.getBiorepositoryIuBricsManagerEmail());
			int responseCode =
					restBioRepositoryProvider.submitOrder(orderXML, orderXMLFileName, attachedFiles, bricsClient);
			if (responseCode == 201) {
				logger.info("Successful IU transfer");
				return true;
			} else {
				logger.error("IU transfer failure ");
				return false;
			}


		} catch (JSchException e1) {
			logger.error("IU transfer failure ");
			e1.printStackTrace();
			return false;
		} catch (Exception e) {
			logger.error("IU transfer failure  ");
			e.printStackTrace();
			return false;
		}



	}



	private String putOrderFileIntoBRICS(BiospecimenOrder order, SftpClient bricsClient) throws JSchException {

		String iuXMLString = null;
		ByteArrayOutputStream baos = null;

		try {

			// Create and save a userfile
			// Call the xml file generator where a output stream is returned.
			IUXMLWrapper iuXML = new IUXMLWrapper(order, null);



			// Create and save XML Order to userfile
			baos = new ByteArrayOutputStream();
			iuXML.write(baos);
			byte[] data = baos.toByteArray();

			// Upload file to brics.
			String filePath =
					ServiceConstants.ORDER_MANAGER_FILE_PATH + order.getUser().getFullName().replaceAll(" ", "") + "_"
							+ order.getUser().getId() + "_" + new java.util.Date(order.getDateCreated().getTime())
									.toString().replaceAll(":", "").replaceAll(" ", "")
							+ "/";
			String fileName = ORDER_NAME + order.getId() + ".xml";
			bricsClient.upload(data, filePath, fileName);
			baos.close();

			// need to now unmarshall
			iuXMLString = iuXML.toString();

		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (baos != null)
					baos.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return iuXMLString;
	}



	/**
	 * Opens connections to the sftp servers
	 * 
	 * @return
	 * @throws JSchException
	 */
	public SftpClient openConnection(String connectionInfo) throws JSchException {

		SftpClient client = null;
		if (BRICS.equals(connectionInfo)) {
			// DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);
			DatafileEndpointInfo info = bricsEndpoint;
			client = SftpClientManager.getClient(info);
		}

		return client;
	}

	public UserFile createOrderXmlFile(BiospecimenOrder order, int fileSize, String filePath, String fileName) {

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



	public DatafileEndpointInfo getBricsEndpoint() {
		return bricsEndpoint;
	}

	public void setBricsEndpoint(DatafileEndpointInfo bricsEndpoint) {
		this.bricsEndpoint = bricsEndpoint;
	}



}
