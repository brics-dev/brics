
package gov.nih.tbi.ordermanagement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import gov.nih.tbi.account.service.complex.BaseManagerImpl;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.ordermanagement.util.OrderManagerCSVUtil;
import gov.nih.tbi.ordermanager.dao.BiospecimenOrderDao;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.OrderStatus;
import gov.nih.tbi.repository.dao.DatafileEndpointInfoDao;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.JSchException;

@Service
@Scope("singleton")
public class OrderManagerImpl extends BaseManagerImpl implements OrderManager
{
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(OrderManagerImpl.class);
    
    @Autowired
    DatafileEndpointInfoDao datafileEndpointInfoDao;
    
    @Autowired
    BiospecimenOrderDao biospecimenOrderDao;

    @Override
    public SftpClient getBricsSftpClient()
    {

        DatafileEndpointInfo info = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);
        SftpClient client = null;
        try
        {
            client = SftpClientManager.getClient(info);
        }
        catch (JSchException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return client;
    }

    @Override
    public Boolean storeOrderFiles(UserFile userFile, byte[] byteStream, String path)
    {

        SftpClient client = getBricsSftpClient();
        return client.upload(byteStream, ServiceConstants.ORDER_MANAGER_FILE_PATH + path + "/", userFile.getName());
        // TODO Auto-generated method stub
    }

	/**
	 * This method returns true if an orderTitle has already been created in the system, excluding the order with the
	 * given orderId (i.e. when editing an existing order).
	 * 
	 * @param orderTitle - orderTitle
	 * @param orderId - orderId, to exclude the order in case of editing existing order, null if not new.
	 * @return true if an orderTitle has already been created in the system
	 */
    public boolean isOrderTitleUnique(String orderTitle, Long orderId) {
    	return biospecimenOrderDao.isOrderTitleUnique(orderTitle,orderId);
    }
	/**
	 * Beginning of a search capability for the biospecimenorder
	 * @param orderTitle
	 * @param submittedBeginDate
	 * @param submittedEndDate
	 * @param status
	 * @return
	 */
    public List<BiospecimenOrder> searchBioSpecimenOrder(Date submittedBeginDate, Date submittedEndDate, List<OrderStatus> status){
    	return biospecimenOrderDao.search(submittedBeginDate, submittedEndDate, status);
    }
    
    public ByteArrayOutputStream generateBiospecimenOrderCSV(List<BiospecimenOrder> biospecimenOrders) throws IOException{
    	return OrderManagerCSVUtil.biospecimenOrderToCsv(biospecimenOrders);
    }
}
