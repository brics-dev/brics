package gov.nih.tbi.ordermanagement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.BaseManager;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.OrderStatus;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.service.io.SftpClient;

public abstract interface OrderManager extends BaseManager {

	public abstract SftpClient getBricsSftpClient();
	
	public abstract Boolean storeOrderFiles(UserFile userFile, byte[] byteStream,String path);
	
	/**
	 * This method returns true if an orderTitle has already been created in the system, excluding the order with the
	 * given orderId (i.e. when editing an existing order).
	 * 
	 * @param orderTitle - orderTitle
	 * @param orderId - orderId, to exclude the order in case of editing existing order, null if not new.
	 * @return true if an orderTitle has already been created in the system.
	 */
	public abstract boolean isOrderTitleUnique(String orderTitle,Long orderId);
	
	
	
	/**
	 * Beginning of a search capability for the biospecimenorder
	 * @param orderTitle
	 * @param submittedBeginDate
	 * @param submittedEndDate
	 * @param status
	 * @return
	 */
	public List<BiospecimenOrder> searchBioSpecimenOrder(Date submittedBeginDate, Date submittedEndDate, List<OrderStatus> status);
	
	public ByteArrayOutputStream generateBiospecimenOrderCSV(List<BiospecimenOrder> biospecimenOrders) throws IOException;
}
