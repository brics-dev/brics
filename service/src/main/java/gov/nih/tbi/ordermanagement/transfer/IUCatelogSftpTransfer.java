
package gov.nih.tbi.ordermanagement.transfer;

import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.jcraft.jsch.JSchException;

public class IUCatelogSftpTransfer {

	static Logger logger = Logger.getLogger(IUCatelogSftpTransfer.class);

	private DatafileEndpointInfo bricsEndpoint;

	public IUCatelogSftpTransfer(DatafileEndpointInfo bricsEndpoint) {
		this.bricsEndpoint = bricsEndpoint;
	}

	public void uploadToPDDEV(File uploadFile, String destinationFileName) throws Exception {
		SftpClient bricsClient = SftpClientManager.getClient(bricsEndpoint);
		bricsClient.upload(uploadFile, "", destinationFileName);
	}

	private String uploadCSVIntoBRICS(File localCatalogFile) throws JSchException {
		String errMsg = "";
		ByteArrayOutputStream baos = null;

		try {
			SftpClient bricsClient = SftpClientManager.getClient(bricsEndpoint);
			bricsClient.upload(localCatalogFile, localCatalogFile.getAbsolutePath(), localCatalogFile.getName());
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

		return errMsg;
	}

}
