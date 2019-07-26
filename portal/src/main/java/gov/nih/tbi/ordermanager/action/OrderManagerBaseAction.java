
package gov.nih.tbi.ordermanager.action;

import gov.nih.tbi.account.model.SessionAccountEdit;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.ItemQueue;
import gov.nih.tbi.ordermanager.service.ItemQueueService;
import gov.nih.tbi.ordermanager.service.OrderService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;

public class OrderManagerBaseAction extends BaseAction
{

    /**
	 * 
	 */

    @Autowired
    protected SessionAccountEdit sessionAccountEdit;

    private static final long serialVersionUID = 6670325400585309993L;

    private int queueSize = 0;
    private int orderSize = 0;
    private int adminOrderSize = 0;
    public ItemQueue queue;
    private Collection<BiospecimenOrder> userOrders;
    private Collection<BiospecimenOrder> adminOrders;
    private Collection<String> sampleTypes = new HashSet<String>();
    private Collection<String> visitTypes = new HashSet<String>();

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemQueueService itemQueueService;

    // private OrderStatus OrderStatusBean;

    private String errorMessage;

    public String getErrorMessage()
    {

        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {

        this.errorMessage = errorMessage;
    }

    public ItemQueueService getItemQueueService()
    {

        return itemQueueService;
    }

    public void setItemQueueService(ItemQueueService itemQueueService)
    {

        this.itemQueueService = itemQueueService;
    }

    public OrderService getOrderService()
    {

        return orderService;
    }

    public void setOrderService(OrderService orderService)
    {

        this.orderService = orderService;
    }

    public ItemQueue getQueue()
    {

        return queue;
    }

    public void setQueue(ItemQueue queue)
    {

        this.queue = queue;
    }

    public int getQueueSize()
    {

        ItemQueue existingItems = itemQueueService.getItemQueueForUser(this.sessionAccount.getAccount().getUser());

        if ((existingItems != null) && (existingItems.getItems().isEmpty() == false))
        {

            setQueueSize(existingItems.getItems().size());
        }
        else
        {
            setQueueSize(0);
        }

        return queueSize;
    }

    public void setQueueSize(int queueSize)
    {

        this.queueSize = queueSize;
    }

    public int getOrderSize()
    {

        Collection<BiospecimenOrder> existingOrders = this.orderService.findExistingOrdersForUser(this.sessionAccount
                .getAccount().getUser());
        if ((existingOrders != null) && (existingOrders.isEmpty() == false))
        {
            setOrderSize(existingOrders.size());

        }
        else
        {
            setOrderSize(0);

        }

        return orderSize;
    }

    public void setOrderSize(int orderSize)
    {

        this.orderSize = orderSize;
    }

    public int getAdminOrderSize()
    {

        boolean adminUser = getIsOrderManagerAdmin();

        if (adminUser)
        {
            Collection<BiospecimenOrder> adminOrdersList = this.orderService.getOrderListForBiorepositoryAdmin(
                    this.sessionAccount.getAccount().getUser());
            if ((adminOrdersList != null) && (adminOrdersList.isEmpty() == false))
            {
                setAdminOrderSize(adminOrdersList.size());

            }
            else
            {
                setAdminOrderSize(0);

            }

        }

        return adminOrderSize;
    }

    public void setAdminOrderSize(int adminOrderSize)
    {

        this.adminOrderSize = adminOrderSize;
    }

    public Collection<BiospecimenOrder> getUserOrders()
    {

        return userOrders;
    }

    public void setUserOrders(Collection<BiospecimenOrder> userOrders)
    {

        this.userOrders = userOrders;
    }

    public void getOrderInfo()
    {

        Collection<BiospecimenOrder> existingOrders = this.orderService.findExistingOrdersForUser(this.sessionAccount
                .getAccount().getUser());
        if ((existingOrders != null) && (existingOrders.isEmpty() == false))
        {

            setUserOrders(existingOrders);
        }
        else
        {

            setUserOrders(Collections.<BiospecimenOrder> emptyList());
        }

        boolean adminUser = getIsOrderManagerAdmin();

        if (adminUser)
        {
            Collection<BiospecimenOrder> adminOrdersList = this.orderService.getOrderListForBiorepositoryAdmin(
                    this.sessionAccount.getAccount().getUser());
            if ((adminOrdersList != null) && (adminOrdersList.isEmpty() == false))
            {

                setAdminOrders(adminOrdersList);

            }
            else
            {

                setAdminOrders(Collections.<BiospecimenOrder> emptyList());
            }

        }

    }

    public void getQueueInfo()
    {

        ItemQueue existingItems = itemQueueService.getItemQueueForUser(this.sessionAccount.getAccount().getUser());

        if ((existingItems != null) && (existingItems.getItems().isEmpty() == false))
        {
            setQueue(existingItems);
            setQueueSampleTypes(existingItems);
            setQueueVisitTypes(existingItems);

        }
        else
        {

            setQueue(new ItemQueue());
        }

    }

    public Boolean getIsOrderManagerAdmin()
    {

        if (dictionaryManager.hasRole(this.sessionAccount.getAccount(), RoleType.ROLE_ORDER_ADMIN))
        {
            return true;
        }

        return false;
    }

    public Collection<BiospecimenOrder> getAdminOrders()
    {

        return adminOrders;
    }

    public void setAdminOrders(Collection<BiospecimenOrder> adminOrders)
    {

        this.adminOrders = adminOrders;
    }

    public SessionAccountEdit getSessionAccountEdit()
    {

        if (sessionAccountEdit == null)
        {
            sessionAccountEdit = new SessionAccountEdit();
        }

        return sessionAccountEdit;
    }
    
    public void setQueueSampleTypes(ItemQueue itemQueue){
    	
    	for(BiospecimenItem item:itemQueue.getItems()){
    		if(item.getSampCollType()!=null){
    			sampleTypes.add(item.getSampCollType());
    		}
    	}
    	
    }
    public void setQueueVisitTypes(ItemQueue itemQueue){
    	
    	for(BiospecimenItem item:itemQueue.getItems()){
    		if(item.getVisitTypePDBP()!=null){
    			visitTypes.add(item.getVisitTypePDBP());
    		}	
    	}	
    }

	public Collection<String> getSampleTypes() {
		return sampleTypes;
	}

	public void setSampleTypes(Collection<String> sampleTypes) {
		this.sampleTypes = sampleTypes;
	}

	public Collection<String> getVisitTypes() {
		return visitTypes;
	}

	public void setVisitTypes(Collection<String> visitTypes) {
		this.visitTypes = visitTypes;
	}
	
}
