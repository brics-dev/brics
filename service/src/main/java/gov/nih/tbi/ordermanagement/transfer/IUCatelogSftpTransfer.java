
package gov.nih.tbi.ordermanagement.transfer;

import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;

import java.io.File;

import org.apache.log4j.Logger;

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
}
