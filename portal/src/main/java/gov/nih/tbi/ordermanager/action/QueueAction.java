
package gov.nih.tbi.ordermanager.action;

//import gov.nih.tbi.commons.model.hibernate.User;
//import gov.nih.tbi.guid.model.hibernate.SiteUser;
import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.SemanticFormStructureList;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.service.ItemQueueService;
import gov.nih.tbi.ordermanager.service.OrderService;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.repository.model.hibernate.Grant;
import gov.nih.tbi.repository.portal.StudyAction;
import gov.nih.tbi.taglib.datatableDecorators.OrderQueueListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.StudyFsIdtDecorator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.util.CreateIfNull;
import com.opensymphony.xwork2.util.Element;
import com.opensymphony.xwork2.util.KeyProperty;

//import gov.nih.tbi.ordermanager.model.OrderStatus;

//import gov.nih.tbi.ordermanager.model.MessageStore;

/**
 * 
 * @author Ryan Stewart
 * 
 */
public class QueueAction extends OrderManagerBaseAction implements SessionAware
{

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(QueueAction.class);
    // private User user;
    /* injecting the sessionAccount which will be porperly populated with the account object and the user object in the account object
     * AFTER the user logs in the portal and then comes to this page 
     */

    private Map<String, Object> userSession;

    @Autowired
    private SessionAccount sessionAccount;

    @Autowired
    private OrderService orderService;

    private String[] repositories = { "Coriell", "Harvard" };

    @Autowired
    private ItemQueueService itemQueueService;

    private String[] itemCheckList;

    @KeyProperty(value = "id")
    @Element(value = gov.nih.tbi.ordermanager.model.BiospecimenItem.class)
    @CreateIfNull(value = true)
    private List<BiospecimenItem> itemsFromQueue = new ArrayList<BiospecimenItem>();

    // private OrderStatus OrderStatusBean;

    public List<String> getRepositories()
    {

        return Arrays.asList(repositories);
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

    /*
     * Creates the MessageStore model object, 
     * increase helloCount by 1 and 
     * returns success.  The MessageStore model
     * object will be available to the view.
     * (non-Javadoc)
     * @see com.opensymphony.xwork2.ActionSupport#execute()
     */
    public String execute() throws Exception
    {

        getQueueInfo();// from OrderManagerBaseAction
        getOrderInfo();// from OrderManagerBaseAction
        return SUCCESS;
    }

    public SessionAccount getSessionAccount()
    {

        return sessionAccount;
    }

    public void setSessionAccount(SessionAccount sessionAccount)
    {

        this.sessionAccount = sessionAccount;
    }

    @Override
    public void setSession(Map<String, Object> session)
    {

        this.userSession = session;
    }

    /**
     * @return the itemsFromQueue
     */
    public List<BiospecimenItem> getItemsFromQueue()
    {

        return itemsFromQueue;
    }

    /**
     * @param itemsFromQueue
     *            the itemsFromQueue to set
     */
    public void setItemsFromQueue(List<BiospecimenItem> itemsFromQueue)
    {

        this.itemsFromQueue = itemsFromQueue;

    }

    public String[] getItemCheckList()
    {

        return itemCheckList;
    }

    public void setItemCheckList(String[] itemCheckList)
    {

        this.itemCheckList = itemCheckList;
    }


	public String getQueueList() {
        getQueueInfo();// from OrderManagerBaseAction
        getOrderInfo();// from OrderManagerBaseAction
		try {
			//ArrayList<BiospecimenItem> itemsList = this.queue.getItems();
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<BiospecimenItem> outputList = new ArrayList<BiospecimenItem>(this.queue.getItems());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new OrderQueueListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
    public String removeFromQueue() throws Exception
    {

        getQueueInfo();// from OrderManagerBaseAction
        getOrderInfo();// from OrderManagerBaseAction

        Collection<BiospecimenItem> itemsList = this.queue.getItems();

        for (BiospecimenItem b : this.itemsFromQueue)
        {
            /* ok I need to loop through my queue and add the items in there to my order */

            for (BiospecimenItem item : itemsList)
            {

                // first check if the item is in the user queue
                if (item.getId().equals(b.getId()))
                {
                    // now check if it's an item i actually chose
                    for (String checkedItem : this.itemCheckList)
                    {

                        if (item.getId().equals(Long.parseLong(checkedItem)))
                        {
                            boolean removed = this.itemQueueService.removeItemFromUserQueue(item, this.sessionAccount
                                    .getAccount().getUser());

                        }
                    }

                }

            }

        }
        getQueueInfo();// from OrderManagerBaseAction

        return SUCCESS;
    }

}
