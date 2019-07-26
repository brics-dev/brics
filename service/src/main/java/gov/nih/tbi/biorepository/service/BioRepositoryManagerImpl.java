package gov.nih.tbi.biorepository.service;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.service.complex.BaseManagerImpl;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.BioRepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.iubiorepo.ws.RestBioRepositoryProvider;
import gov.nih.tbi.ordermanagement.transfer.IUCatelogSftpTransfer;
import gov.nih.tbi.ordermanager.dao.BioRepositoryDao;
import gov.nih.tbi.ordermanager.dao.BioRepositoryFileTypeDao;
import gov.nih.tbi.ordermanager.dao.BiospecimenOrderDao;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.OrderStatus;
import gov.nih.tbi.repository.dao.DatafileEndpointInfoDao;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.JsonObject;
import com.jcraft.jsch.JSchException;


@Service
@Scope("singleton")
public class BioRepositoryManagerImpl extends BaseManagerImpl implements BioRepositoryManager {

	private static final long serialVersionUID = -6942551737214546264L;

	private static Logger logger = Logger.getLogger(BioRepositoryManagerImpl.class);

	@Autowired
	private BiospecimenOrderDao biospecimenOrderDaoImpl;

	@Autowired
	private ModulesConstants modulesConstants;


	@Autowired
	private DatafileEndpointInfoDao datafileEndpointInfoDao;

	@Autowired
	private BioRepositoryDao bioRepositoryDao;

	@Autowired
	private BioRepositoryFileTypeDao bioRepositoryFileTypeDao;

	@Autowired
	private MailEngine mailEngine;

	/**
	 * runs as nightly process to check IU status and handle if status is shipped...
	 * 
	 * 
	 * changes the brics state to 'Shipped' and email jenna to inform this and also get the IU manifest for this order
	 * which has the GUID and ST-Number pairings. Update the Biospecimen item with the order with the correst St-Number.
	 * 
	 * This Guid-St-NUmber will them be available from the admin order section when admin clicks on manifest for the
	 * order that is in shipped status
	 */
	public void checkIUStatusAndRetrieveManifest() {
		logger.log(Level.INFO, "checkIUStatusAndRetrieveManifest...");
		RestBioRepositoryProvider restBioRepositoryProvider = new RestBioRepositoryProvider(
				modulesConstants.getBiorepositoryIuWsBase(), modulesConstants.getBiorepositoryIuUsername(),
				modulesConstants.getBiorepositoryIuPassword(), modulesConstants.getBiorepositoryIuBricsManagerEmail());

		// get list of orders in status SUBMITTED
		List<BiospecimenOrder> biospecimenOrderList = biospecimenOrderDaoImpl.getAllOrders();

		for (int i = 0; i < biospecimenOrderList.size(); i++) {
			BiospecimenOrder order = biospecimenOrderList.get(i);
			if (order.getOrderStatus() == OrderStatus.SUBMITTED) {
				OrderStatus bricsOrderStatus = order.getOrderStatus();
				Long orderId = order.getId();
				JsonObject jsonObj = restBioRepositoryProvider.getIUStatus(orderId);
				int iuStatus = Integer.parseInt(jsonObj.getAsJsonPrimitive("status").getAsString());
				String message = jsonObj.getAsJsonPrimitive("message").getAsString();
				if (iuStatus == 200 && (message.equalsIgnoreCase(OrderStatus.SHIPPED.getValue()) 
											|| message.equalsIgnoreCase(OrderStatus.COMPLETED.getValue())) ) {
					logger.log(Level.INFO, "Order Id :  " + orderId + " now in IU shipped or completed status ");
					emailBiosampleManager(order);
					updateBricsOrderStatus(order);
					getManifest(order, restBioRepositoryProvider);
				} else if (iuStatus != 200) {
					logger.error("ERROR: received response " + iuStatus + " while calling IU API.");
				}
			}
		}


		// return Response.status(200).build();

	}



	private void emailBiosampleManager(BiospecimenOrder order) {
		// email Jenna...one email per order...give order id, date, submitter
		Long orderId = order.getId();
		Date dateSubmitted = order.getDateSubmitted();
		User user = order.getUser();
		String userName = user.getFullName();

		String subject = "Biosample Order " + orderId + " has been changed";
		String message = "The biosample order " + orderId + " has been changed to SHIPPED for " + userName;
		String emailAddress = modulesConstants.getBiorepositoryIuBricsManagerEmail();
		try {
			logger.log(Level.INFO, "emailing to " + emailAddress);
			mailEngine.sendMail(subject, message, null, emailAddress);
		} catch (MessagingException e) {
			logger.error(
					"There was an exception in sending email from BioRepositoryServiceImpl " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}


	private void updateBricsOrderStatus(BiospecimenOrder order) {
		// change status in DB
		logger.log(Level.INFO, "changing status to shipped in brics...");
		order.setOrderStatus(OrderStatus.SHIPPED);
		biospecimenOrderDaoImpl.save(order);
	}


	private void getManifest(BiospecimenOrder order, RestBioRepositoryProvider restBioRepositoryProvider) {
		// call ws to get manifest...loop over all items in manifest and update db with st_number and
		Long orderId = order.getId();
		logger.log(Level.INFO, "calling get manifest for order id: " + orderId + "...");
		Response r = restBioRepositoryProvider.getManifest(orderId);
		if (r.getStatus() == 200) {
			String manifestXML = r.readEntity(String.class);

			// now read the manifedt xml for each sample and get stnumber/guid and populate stnumber column in db...in
			// biospecimenitem
			Collection<BiospecimenItem> biospecimenItems = order.getRequestedItems();
			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = docFactory.newDocumentBuilder();
				ByteArrayInputStream is = new ByteArrayInputStream(manifestXML.getBytes("UTF-8"));
				Document doc = builder.parse(is);
				NodeList sampleNodes = doc.getElementsByTagName("sample");
				for (int i = 0; i < sampleNodes.getLength(); i++) {
					Node node = sampleNodes.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						String manifestGuid = element.getElementsByTagName("guid").item(0).getTextContent();
						String manifestStNumber = element.getElementsByTagName("st_number").item(0).getTextContent();

						for (BiospecimenItem item : biospecimenItems) {
							String guid = item.getGuid();
							if (guid.equals(manifestGuid)) {
								item.setStNumber(manifestStNumber);
								break;
							}
						}
					}
				}
				biospecimenOrderDaoImpl.save(order);


			} catch (ParserConfigurationException | SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void retrieveIUBiosampleCatalog() {
		logger.log(Level.INFO, "Executing retrieveIUBiosampleCatalog...");
		List<String> catalogs = modulesConstants.getBiorepositoryIuCatalogs();
		String errorMessage = "";
		String emailAddress = modulesConstants.getBiorepositoryIuBricsManagerEmail();

		if (catalogs == null || catalogs.isEmpty()) {
			errorMessage = "The biorepository configuration property can't be found or empty";
			logger.error(errorMessage);
			emailBiosampleManager("Error reported when transferring IU biosample Catalogs", errorMessage, null,
					emailAddress);
			return;
		}

		RestBioRepositoryProvider restBioRepositoryProvider = new RestBioRepositoryProvider(
				modulesConstants.getBiorepositoryIuWsBase(), modulesConstants.getBiorepositoryIuUsername(),
				modulesConstants.getBiorepositoryIuPassword(), modulesConstants.getBiorepositoryIuBricsManagerEmail());

		IUCatelogSftpTransfer transfer =
				new IUCatelogSftpTransfer(datafileEndpointInfoDao.get(ServiceConstants.PDBP_HOST_DEV_ENDPOINT_ID));
		int i = 0;
		for (String repo : catalogs) {
			try {
				String csvStr = restBioRepositoryProvider.getCatalog(repo);
				File tmpFile = new File(
						ServiceConstants.FILE_SEPARATER + "tmp" + ServiceConstants.FILE_SEPARATER + repo + ".csv");
				writeDataToTempfile(csvStr, tmpFile);

				String targetFileName = ServiceConstants.BIOSAMPLE_CATALOG_FILE_NAMES[i++];
				transfer.uploadToPDDEV(tmpFile, targetFileName);
				Files.deleteIfExists(tmpFile.toPath());

			} catch (JSchException e1) {
				errorMessage = e1.getMessage();
				e1.printStackTrace();
			} catch (Exception e1) {
				errorMessage = e1.getMessage();
				e1.printStackTrace();
			} finally {
				if (!errorMessage.isEmpty()) {
					logger.error(errorMessage);
					emailBiosampleManager("Error reported when transferring IU biosample Catalogs", errorMessage, null,
							emailAddress);
					return;
				}
			}
		}
		logger.log(Level.INFO, "Ending retrieveIUBiosampleCatalog...");
		// emailBiosampleManager("Biosample catalogs are transferred successfully", "", null, emailAddress);
		return;
	}

	public synchronized void writeDataToTempfile(String c, File tmpFile) throws IOException {
		Writer fout = null;
		try {
			fout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile, true), "utf-8"));
			fout.write(c);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fout != null) {
				fout.close();
			}
		}
		return;
	}

	private void emailBiosampleManager(String emailSubject, String emailBody, String from, String to) {
		try {
			logger.log(Level.INFO, "emailing to " + to);
			mailEngine.sendMail(emailSubject, emailBody, from, to);
		} catch (MessagingException e) {
			logger.error(
					"There was an exception in sending email from BioRepositoryServiceImpl " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

}
