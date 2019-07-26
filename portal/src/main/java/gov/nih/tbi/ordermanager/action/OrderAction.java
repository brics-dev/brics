
package gov.nih.tbi.ordermanager.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.result.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opensymphony.xwork2.util.CreateIfNull;
import com.opensymphony.xwork2.util.Element;
import com.opensymphony.xwork2.util.KeyProperty;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.exceptions.OrderManagerException;
import gov.nih.tbi.commons.model.hibernate.Address;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.ordermanagement.OrderManager;
import gov.nih.tbi.ordermanager.dao.BioRepositoryDao;
import gov.nih.tbi.ordermanager.dao.BiospecimenItemDao;
import gov.nih.tbi.ordermanager.model.BioRepository;
import gov.nih.tbi.ordermanager.model.BioRepositoryFileType;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.Comment;
import gov.nih.tbi.ordermanager.model.ItemQueue;
import gov.nih.tbi.ordermanager.model.OrderManagerDocument;
import gov.nih.tbi.ordermanager.model.OrderStatus;
import gov.nih.tbi.ordermanager.model.SessionOrder;
import gov.nih.tbi.ordermanager.model.exception.EmptyOrderException;
import gov.nih.tbi.ordermanager.model.exception.IllegalOrderStatusException;
import gov.nih.tbi.ordermanager.model.exception.MultipleRepositoryException;
import gov.nih.tbi.ordermanager.service.ItemQueueService;
import gov.nih.tbi.ordermanager.service.OrderService;
import gov.nih.tbi.repository.dao.DatafileEndpointInfoDao;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.taglib.datatableDecorators.OrderListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.OrderSampleListIdtDecorator;

/**
 * 
 * @author Ryan Stewart
 * 
 */
public class OrderAction extends OrderManagerBaseAction implements SessionAware
{

    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(OrderAction.class);

    private static final String ORDER_SESSION_KEY = "order_key";
    private static final String EMAIL_DELIMITER = ";";

    private Address addressBean;

    @Autowired
    MailEngine mailEngine;

    @Autowired
    private SessionAccount sessionAccount;

    @Autowired
    private SessionOrder sessionOrder;

    @Autowired
    private ItemQueueService itemQueueService;

    @Autowired
    private OrderManager orderManager;

    @Autowired
    DatafileEndpointInfoDao datafileEndpointInfoDao;

    @Autowired
    private BioRepositoryDao bioRepositoryDao;
    
    @Autowired
    private BiospecimenItemDao biospecimenItemDao;

    private BiospecimenOrder orderBean; // The files
    private OrderStatus orderStatusBean;

    // using SessionOrder object instead
    // private Map<String, Object> userSession;

    @Autowired
    private OrderService orderService;

    private String removeOrderList;
    private String orderComment;
    private String orderId;
    private String orderTitle;
    private String orderStatus;
    private Account orderOwner;
    private String orderOwnerId;
    private String[] itemsFromQueueAmount;
    private String[] itemCheckList;
    private ArrayList<String> uploadFileType;
    private String selectedUploadFileType;
    
    private String addToExistingOrderComments;
    private String editChangeComments;

    /*********************** File Upload Stuff **************************/

    private Map<UserFile, byte[]> uploadedFilesMap;
    private File upload;
    private String uploadContentType;
    private String uploadFileName;
    private String uploadDescription;
    private InputStream inputStream;
    /*********************************************************************/
    
    private Boolean  isAdmin;

    @KeyProperty(value = "id")
    @Element(value = gov.nih.tbi.ordermanager.model.BiospecimenItem.class)
    @CreateIfNull(value = true)
    private List<BiospecimenItem> itemsFromQueue = new ArrayList<BiospecimenItem>();

    private BiospecimenItem orderItem;

    private String itemToRemove;

    private String formSubmitStatus;

    private OrderStatus[] statuses = { OrderStatus.APPROVED, OrderStatus.CANCELLED, OrderStatus.CREATED,
            OrderStatus.PENDING_APPROVAL, OrderStatus.PENDING_SUBMISSION, OrderStatus.REJECTED,
            OrderStatus.REVISION_REQUESTED, OrderStatus.SHIPPED, OrderStatus.SUBMITTED };

    private Collection<BiospecimenOrder> userOrders;
    
    private String filteredData;
    
    private String selectedData;

    /*
     * Creates the order model object, returns success. The order model object
     * will be available to the view. (non-Javadoc)
     * 
     * @see com.opensymphony.xwork2.ActionSupport#execute()
     */

    @Override
    public String execute() throws Exception
    {

        uploadedFilesMap = getSessionAccountEdit().getUploadedFilesMap();

        if (uploadedFilesMap != null)
        {
            uploadedFilesMap.clear();
            uploadedFilesMap = new HashMap<UserFile, byte[]>();
            getSessionAccountEdit().setUploadedFilesMap(uploadedFilesMap);
        }

        // a user will always be creating a new order here, so let's clear out
        // the session
        this.sessionOrder.setOrder(null);
        this.sessionOrder.setComment(null);

        getQueueInfo();// from OrderManagerBaseAction
        getOrderInfo();// from OrderManagerBaseAction
        
        setOrderRequestedItems();

        getOrderBean();

        addressBean = new Address();

        return SUCCESS;
    }

    public String openOrderList()
    {

        // clear the map before opening an new order so session files don't carry over
        if (getSessionAccountEdit().getUploadedFilesMap() != null)
        {
            getSessionAccountEdit().getUploadedFilesMap().clear();
        }

        getQueueInfo();// from OrderManagerBaseAction
        getOrderInfo();// from OrderManagerBaseAction

        getOrderBean();

        // add the list to the session to be retrieved if the order is removed
        getSession().setAttribute("userOrders", getUserOrders());

        return SUCCESS;
    }
    
    public String reportLightbox(){
    	
    	return "reportLightbox";
    }

    public String openAdminOrderList()
    {

        // clear the map before opening an new order so session files don't carry over
        if (getSessionAccountEdit().getUploadedFilesMap() != null)
        {
            getSessionAccountEdit().getUploadedFilesMap().clear();
        }

        getQueueInfo();// from OrderManagerBaseAction
        getOrderInfo();// from OrderManagerBaseAction

        // add the list to the session to be retrieved if the order is removed
        getSession().setAttribute("adminOrders", getAdminOrders());

        return SUCCESS;
    }
    
	// http://fitbir-portal-local.cit.nih.gov:8080/portal/ordermanager/adminOrder!getAdminOrdersList.ajax
	public String getAdminOrdersList() throws UnsupportedEncodingException {
        getQueueInfo();// from OrderManagerBaseAction
        getOrderInfo();// from OrderManagerBaseAction
        
        getSession().setAttribute("adminOrders", getAdminOrders());

		try {
			IdtInterface idt = new Struts2IdtInterface();
			List<BiospecimenOrder> outputList =  new ArrayList<BiospecimenOrder>(getAdminOrders());
			idt.setList((ArrayList<BiospecimenOrder>) outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new OrderListIdtDecorator(outputList));
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

    public String addToExistingOrder()
    {

        getQueueInfo();// from OrderManagerBaseAction
        getOrderInfo();// from OrderManagerBaseAction

        if (this.orderId != "" && this.orderId != null)
        {

            BiospecimenOrder order = this.orderService.findOrderByIdForUser(Long.parseLong(this.orderId),
                    this.sessionAccount.getAccount().getUser());
            setOrderBean(order);
            
            if(addToExistingOrderComments != null && !addToExistingOrderComments.trim().equals("")) {
            	try { 
    				orderService.addCommentToOrder(this.sessionAccount.getAccount().getUser(), this.addToExistingOrderComments, order);
    			} catch (OrderManagerException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    				addActionError(this.getText(PortalConstants.ORDER_ERROR_KEY));
    			}
            }
            	
            
            
            
            this.sessionOrder.setOrder(order);
            // this.userSession.put(ORDER_SESSION_KEY, order);

        }
        getOrderBean();

        if (this.orderBean != null)
        {
            Account tempAccount = accountManager.getAccountByUser(this.orderBean.getUser());

            setOrderOwner(tempAccount);

            setOrderTitle(this.orderBean.getOrderTitle());

            if (this.orderBean.getAddress() != null)
            {
                this.setAddressBean(this.orderBean.getAddress());
            }

        }

        setOrderRequestedItems();

        return SUCCESS;
    }

    public String openAdminOrder()
    {

        getQueueInfo();// from OrderManagerBaseAction
        getOrderInfo();// from OrderManagerBaseAction

        if (this.orderId != "" && this.orderId != null)
        {

            for (BiospecimenOrder o : this.getAdminOrders())
            {
                // loop through admin orders and get chosen order

                if (o.getId().equals(Long.parseLong(this.orderId)))
                {
                    setOrderBean(o);
                    this.sessionOrder.setOrder(o);
                    // this.userSession.put(ORDER_SESSION_KEY, o);

                }

            }

        }
        else
            if (this.sessionOrder.getOrder() != null)
            {
                BiospecimenOrder order = this.sessionOrder.getOrder();
                setOrderBean(order);
            }

        getOrderBean();

        if (this.orderBean != null)
        {
            Account tempAccount = accountManager.getAccountByUser(this.orderBean.getUser());

            setOrderOwner(tempAccount);

            setOrderTitle(this.orderBean.getOrderTitle());

            if (this.sessionOrder.getComment() != null || StringUtils.isBlank(this.sessionOrder.getComment()) == false)
            {
                setOrderComment(this.sessionOrder.getComment());
            }

            if (this.orderBean.getAddress() != null)
            {
                this.setAddressBean(this.orderBean.getAddress());
            }

        }

        return SUCCESS;

    }
    
    
	// http://fitbir-portal-local.cit.nih.gov:8080/portal/ordermanager/adminOrder!getOidBiosampleOrderTableList.ajax
	public String getOidBiosampleOrderTableList() throws UnsupportedEncodingException {

		try {
			IdtInterface idt = new Struts2IdtInterface();
			List<BiospecimenItem> outputList =  new ArrayList<BiospecimenItem>(this.getCurrentOrder().getRequestedItems());
			idt.setList((ArrayList<BiospecimenItem>) outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new OrderSampleListIdtDecorator(outputList));
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	// http://fitbir-portal-local.cit.nih.gov:8080/portal/ordermanager/viewOrders!getBiosampleOrdersList.ajax
	public String getBiosampleOrdersList() throws UnsupportedEncodingException {
		
	   getQueueInfo();// from OrderManagerBaseAction
	   getOrderInfo();// from OrderManagerBaseAction
	   getOrderBean();
	   getSession().setAttribute("userOrders", getUserOrders());// add the list to the session to be retrieved if the order is removed
		
		try{
			IdtInterface idt = new Struts2IdtInterface();
			List<BiospecimenOrder> outputList = new ArrayList<BiospecimenOrder>(getUserOrders());
			idt.setList((ArrayList<BiospecimenOrder>)outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new OrderListIdtDecorator(outputList));
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: "+ e);
			e.printStackTrace();
		}
		return null;
	}

    public String openOrder()
    {

        getQueueInfo();// from OrderManagerBaseAction
        getOrderInfo();// from OrderManagerBaseAction

        if (this.orderId != "" && this.orderId != null)
        {

            BiospecimenOrder order = this.orderService.findOrderByIdForUser(Long.parseLong(this.orderId),
                    this.sessionAccount.getAccount().getUser());
            setOrderBean(order);
            this.sessionOrder.setOrder(order);
            // this.userSession.put(ORDER_SESSION_KEY, order);

        }
        else
            if (this.sessionOrder.getOrder() != null)
            {
                BiospecimenOrder order = this.sessionOrder.getOrder();
                setOrderBean(order);
            }
        getOrderBean();

        if (this.orderBean != null)
        {

            setOrderTitle(this.orderBean.getOrderTitle());

            if (this.sessionOrder.getComment() != null || StringUtils.isBlank(this.sessionOrder.getComment()) == false)
            {
                setOrderComment(this.sessionOrder.getComment());
            }

            if (this.orderBean.getAddress() != null)
            {
                this.setAddressBean(this.orderBean.getAddress());
            }
        }

        return SUCCESS;
    }

    public OrderStatus getDefaultOrderStatus()
    {

        return this.sessionOrder.getOrder().getOrderStatus();
    }

    public void setOrderRequestedItems()
    {

        Collection<BiospecimenItem> coll;
        if ((this.orderBean == null) || (orderBean.getRequestedItems() == null))
        {
            coll = new ArrayList<BiospecimenItem>();

        }
        else
        {
            coll = this.orderBean.getRequestedItems();

        }

        Collection<BiospecimenItem> itemsList = this.queue.getItems();

        for (BiospecimenItem b : this.itemsFromQueue)
        {
            /*
             * ok I need to loop through my queue and add the items in there to
             * my order
             */

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
                            item.setNumberOfAliquots(b.getNumberOfAliquots());
                            coll.add(item);
                        }
                    }

                }

            }

        }
        if ((this.orderBean == null) || (this.orderBean.getId() == null))
        {
            try
            {
                this.orderBean = orderService.createNewBiospecimenOrder("", this.sessionAccount.getAccount().getUser(),
                        coll);

            }
            catch (MultipleRepositoryException e)
            {
                /*
                 * NOTE TO RYAN - this situation indicate that the the
                 * collection contains multiple items belonging to different
                 * repositories so display and error or redirect to some error
                 * page
                 */
                this.setErrorMessage(e.getMessage());
            }
        }
        this.sessionOrder.setOrder(this.orderBean);
        // this.userSession.put(ORDER_SESSION_KEY, this.orderBean);

    }

    /**
     * 
     */
    private void specifyRequiredBioRepositoryFileTypes()
    {

        Set<String> filesTypeMap = new HashSet<String>();
        for (BiospecimenItem item : getOrderBean().getRequestedItems())
        {
            BioRepository br = item.getBioRepository();

            for (BioRepositoryFileType fileType : br.getRequiredFileTypes())
            {
                filesTypeMap.add(fileType.getName());
            }
        }
        uploadFileType = new ArrayList<String>(filesTypeMap);
    }

    public String saveOrder() throws MessagingException
    {

        boolean sendEmail = false;
        String orderStatusChanging = "";
        BiospecimenOrder order = this.sessionOrder.getOrder();

        order.setOrderTitle(this.orderTitle);
        order.setAbstractText(this.orderBean.getAbstractText());
        order.setExperimentalDesignPowerAnalysis(this.orderBean.getExperimentalDesignPowerAnalysis());
        order.setShipToName(this.orderBean.getShipToName());
        order.setShipToInstitution(this.orderBean.getShipToInstitution());

        order.setInvestigatorName(this.orderBean.getInvestigatorName());
        order.setInstitution(this.orderBean.getInstitution());

        order.setPhone(this.orderBean.getPhone());
        order.setAffiliation(this.orderBean.getAffiliation());
        order.setAffiliationPhone(this.orderBean.getAffiliationPhone());
        order.setAffiliationEmail(this.orderBean.getAffiliationEmail());
        order.setAffiliationSpecialInstructions(this.orderBean.getAffiliationSpecialInstructions());

        // create new address object
        Address addressTemp = new Address();
        addressTemp.setId((long) 0);
        addressTemp.setAddress1(this.addressBean.getAddress1());
        addressTemp.setAddress2(this.addressBean.getAddress2());
        addressTemp.setCity(this.addressBean.getCity());
        addressTemp.setOldState(this.addressBean.getOldState());
        addressTemp.setZipCode(this.addressBean.getZipCode());

        order.setAddress(addressTemp);

        // we need this piece in case the user decides to change the amount.
        for (BiospecimenItem a : this.itemsFromQueue)
        {
            for (BiospecimenItem b : order.getRequestedItems())
            {
                if (a.getId().equals(b.getId()) && !a.getNumberOfAliquots().equals(b.getNumberOfAliquots())) {
                    b.setNumberOfAliquots(a.getNumberOfAliquots());
                }
            }
        }

        /*
         * This will remove all the saved orders from the system.
         */
        List<OrderManagerDocument> docList = order.getDocumentList();
        if (order.getOrderStatus() != OrderStatus.CREATED)
        {
            // populate a list of persisted documents to remove
            List<OrderManagerDocument> documentsToRemove = new ArrayList<OrderManagerDocument>();
            for (OrderManagerDocument document : docList)
            {
                if (document.getRemoveFile())
                {
                    documentsToRemove.add(document);
                }
            }
            // if the list is not empty remove the documents from the order list
            if (!documentsToRemove.isEmpty())
            {
                for (OrderManagerDocument document : documentsToRemove)
                {
                    docList.remove(document);
                }
            }
        }

        /*
         * This is where the files are saved using the orderManager (Service).
         */
        Map<UserFile, byte[]> uploadedFilesMap = getSessionAccountEdit().getUploadedFilesMap();
        Set<UserFile> successfullyUploadedFilesToRemove = new java.util.HashSet<UserFile>();
        if (uploadedFilesMap != null)
        {
            Set<UserFile> upFileKeySet = uploadedFilesMap.keySet();

            for (UserFile ufKey : upFileKeySet)
            {
                Boolean success = orderManager.storeOrderFiles(ufKey, uploadedFilesMap.get(ufKey), this.sessionAccount
                        .getAccount().getUser().getFullName().replaceAll(" ", "")
                        + "_"
                        + this.sessionAccount.getAccount().getUser().getId()
                        + "_"
                        + new java.util.Date(order.getDateCreated().getTime()).toString().replaceAll(":", "")
                                .replaceAll(" ", ""));

                if (success) {
                    successfullyUploadedFilesToRemove.add(ufKey);  // 1. uploadFileName/uploadedFilesMap/successfullyUploadedFilesToRemove
                    sendEmail = true;
                }
            }
        }

        Comment comment = null;

        /*
         * should update this logic of saving the comment and then the order, if
         * something goes wrong while saving the order then i should remove the comment
         */
        try
        {
            if (StringUtils.isBlank(this.orderComment) == false
                    && (!formSubmitStatus.equals("SAVE") || (formSubmitStatus.equals("SAVE") && order.getOrderStatus() != OrderStatus.CREATED)))
            {
                comment = orderService.addCommentToOrder(this.sessionAccount.getAccount().getUser(), this.orderComment,
                        order); // 2. orderComment
                sendEmail = true;
            }
        }
        catch (OrderManagerException e)
        {
            e.printStackTrace();
            addActionError(this.getText(PortalConstants.ORDER_ERROR_KEY));
        }

        Boolean saveResult = null;

        if (formSubmitStatus.equals("SUBMIT")) // "Save and Submit" button //3-1: status if()
        {
            // save order

            try
            {
                if (order.getOrderStatus() != OrderStatus.PENDING_APPROVAL)
                {
                    sendEmail = true;
                    orderStatusChanging = OrderStatus.PENDING_APPROVAL.getValue();
                }
                ArrayList<Long> repositoryItems = new ArrayList<Long>();
                for (BiospecimenItem bioItem : order.getRequestedItems())
                {
                    repositoryItems.add(bioItem.getBioRepository().getId());
                }

                saveResult = orderService.validateAndPersist(order, this.sessionAccount.getAccount().getUser(),
                        new ArrayList<UserFile>(successfullyUploadedFilesToRemove));
                
                
                if(saveResult) {
                	//handle any editChangeComments
                	if(editChangeComments != null && !editChangeComments.trim().equals("")) {
                		JsonParser jsonParser = new JsonParser();
                		JsonElement jsonElement = jsonParser.parse(editChangeComments);
                		JsonArray jsonArray = jsonElement.getAsJsonArray();
                		int size = jsonArray.size();
                		for(int i=0;i<size;i++) {
                			String jsonComment = jsonArray.get(i).getAsString();
                			orderService.addCommentToOrder(this.sessionAccount.getAccount().getUser(), jsonComment, order);
                		}
                	}	
                }
                
                
            }
            catch (EmptyOrderException e)
            {
                this.setErrorMessage(e.getMessage());
                return INPUT;
            }
            catch (MultipleRepositoryException e)
            {
                this.setErrorMessage(e.getMessage());
                return INPUT;
            }
            catch (IllegalOrderStatusException e)
            {
                this.setErrorMessage(e.getMessage());
                return INPUT;
            }
            catch (OrderManagerException e)
            {
                e.printStackTrace();
                addActionError(this.getText(PortalConstants.ORDER_ERROR_KEY));
            }

        }
        else
            if (formSubmitStatus.equals("SAVE")) // "Save and Exit" button //3-2: status if()
            {
                // save order
                try
                {
                    if (order.getOrderStatus() != OrderStatus.PERSISTED
                            && order.getOrderStatus() != OrderStatus.CREATED) // for new order
                    {
                        sendEmail = true;
                        orderStatusChanging = OrderStatus.PERSISTED.getValue();
                    }
                    saveResult = orderService.saveBiospecimenOrder(order, this.sessionAccount.getAccount().getUser(),
                            new ArrayList<UserFile>(successfullyUploadedFilesToRemove));
                    
                    if(saveResult) {
                    	//handle any editChangeComments
                    	if(editChangeComments != null && !editChangeComments.trim().equals("")) {
                    		
                    		JsonParser jsonParser = new JsonParser();
                    		JsonElement jsonElement = jsonParser.parse(editChangeComments);
                    		JsonArray jsonArray = jsonElement.getAsJsonArray();
                    		int size = jsonArray.size();
                    		for(int i=0;i<size;i++) {
                    			String jsonComment = jsonArray.get(i).getAsString();
                    			orderService.addCommentToOrder(this.sessionAccount.getAccount().getUser(), jsonComment, order);
                    		}
                    	}	
                    }
                }
                catch (EmptyOrderException e)
                {
                    this.setErrorMessage(e.getMessage());
                    return INPUT;
                }
                catch (MultipleRepositoryException e)
                {
                    this.setErrorMessage(e.getMessage());
                    return INPUT;
                }
                catch (IllegalOrderStatusException e)
                {

                    this.setErrorMessage(e.getMessage());
                    return INPUT;
                }
                catch (OrderManagerException e)
                {
                    e.printStackTrace();
                    addActionError(this.getText(PortalConstants.ORDER_ERROR_KEY));
                }
            }
            else
            {
                /*attached any files uploaded by the admin user*/
                /* save the available information in omf (user file, order, etc) */
                for (UserFile userFile : successfullyUploadedFilesToRemove)
                {
                    OrderManagerDocument doc = new OrderManagerDocument();
                    doc.setDescription(userFile.getDescription());
                    doc.setOrder(order);
                    doc.setUserFile(userFile);
                    order.getDocumentList().add(doc);
                }
                // update status of order
                try
                {
                    if (order.getOrderStatus() != OrderStatus.valueOf(formSubmitStatus))
                    {
                        sendEmail = true;
                        orderStatusChanging = formSubmitStatus;
                    }
                    
                    if(editChangeComments != null && !editChangeComments.trim().equals("")) {
                		
                		JsonParser jsonParser = new JsonParser();
                		JsonElement jsonElement = jsonParser.parse(editChangeComments);
                		JsonArray jsonArray = jsonElement.getAsJsonArray();
                		int size = jsonArray.size();
                		for(int i=0;i<size;i++) {
                			String jsonComment = jsonArray.get(i).getAsString();
                			orderService.addCommentToOrder(this.sessionAccount.getAccount().getUser(), jsonComment, order);
                		}
                	}	
                    
                    orderService.setOrderStatus(this.sessionAccount.getAccount().getUser(), order,
                            OrderStatus.valueOf(formSubmitStatus)); // 3-3: status if()
                }
                catch (OrderManagerException e)
                {
                    e.printStackTrace();
                    addActionError(this.getText(PortalConstants.ORDER_ERROR_KEY));
                }
                saveResult = true;
            }

        if (saveResult == false)
        {
            orderService.removeCommentFromOrder(this.sessionAccount.getAccount().getUser(), comment, order);
            // TODO: sendEmail=false??
        }
        else
        {
            for (UserFile toRemoveFromSession : successfullyUploadedFilesToRemove)
            {
                getSessionAccountEdit().getUploadedFilesMap().remove(toRemoveFromSession);
            }
            this.sessionOrder.setOrder(null);
        }

        // send out the email notice
        String uploadedFilesName = "";
        for (UserFile userFile : successfullyUploadedFilesToRemove)
        {
            if ("".equals(uploadedFilesName))
            {
                uploadedFilesName = uploadedFilesName + userFile.getName();
            }
            else
            {
                uploadedFilesName = uploadedFilesName + ", " + userFile.getName();
            }
        }
		// The order id will be null if the user does SAVE AND SUBMIT upon creation. In this case we
		// just omit the id like the existing code suggested should happen.
		String subject =
				this.getText(PortalConstants.MAIL_RESOURCE_ORDER_CHANGES + PortalConstants.MAIL_RESOURCE_SUBJECT,
						Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
								(order != null && order.getId() != null) ? "" + order.getId() : ""));
        String messageText = this.getText(PortalConstants.MAIL_RESOURCE_ORDER_CHANGES
                + PortalConstants.MAIL_RESOURCE_BODY, Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()), // {0}
                // order.getOrderStatus().getValue(), // {1}
                orderStatusChanging, // {1}
                uploadedFilesName, // {2}
                comment != null ? comment.getMessage() : "", // {3}
                (order != null && order.getUser() != null) ? order.getUser().getFullName() : "", // {4} submitter
                order != null ? order.getOrderTitle() : "", // {5}
                order != null ? "" + order.getId() : "" // {6}
        ));
        // to: brac mail addresses
        String toAddrStr = this.getText(PortalConstants.MAIL_RESOURCE_ORDER_CHANGES + PortalConstants.MAIL_RESOURCE_TO);
        List<String> toAddrList = new ArrayList<String>();
        toAddrList = this.stringToList(toAddrStr);
        // Account tempAccount = null;
        if (order != null && order.getUser() != null && !order.getUser().getEmail().trim().isEmpty())
        {
            toAddrStr = toAddrStr + ";" + order.getUser().getEmail(); // submitter
            toAddrList.add(order.getUser().getEmail()); // submitter
        }
        String[] toAddrArray = toAddrList.toArray(new String[toAddrList.size()]);
        logger.info("[LOCAL ENV ONLY MESSAGE] order's user email= " + Arrays.toString(toAddrArray));
        logger.info("[LOCAL ENV ONLY MESSAGE] sendEmail= " + sendEmail);

        if (sendEmail)
        {
            boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();

            if (!isLocalEnv && !"".equals(toAddrStr))
            {
                try
                {
                    mailEngine.sendMail(subject, messageText, null, toAddrArray);
                }
                catch (MessagingException e)
                {
                    e.printStackTrace();
                    this.setErrorMessage(e.getMessage());
                }
            }
            else
            {
                logger.info("[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the order notice. ");
            }

            sendEmail = false;
        }

        return openOrderList();
    }

    /*
     * removes list when user comes from your orders
     */
    public String removeBiospeceminOrder()
    {

        removeBiospecimenOrderList(false);
        return openOrderList();
    }

    /*
     * removed list when user comes from admin orders
     */
    public String removeBiospeceminOrderAdmin()
    {

        removeBiospecimenOrderList(true);
        return openAdminOrderList();
    }

    /*
     * This method will take a comma separated list of order ids and remove them
     * from the DB
     */
    @SuppressWarnings("unchecked")
    public void removeBiospecimenOrderList(boolean isAdminList)
    {

        List<BiospecimenOrder> removalList = new ArrayList<BiospecimenOrder>();

        if (removeOrderList != null && !removeOrderList.trim().isEmpty())
        {
            // create array of IDs
            String[] idList = removeOrderList.split(",");

            List<BiospecimenOrder> populatedList = new ArrayList<BiospecimenOrder>();
            // populate list depending on where the user came from
            if (isAdminList)
            {
                populatedList = (List<BiospecimenOrder>) getSession().getAttribute("adminOrders");

            }
            else
            {
                populatedList = (List<BiospecimenOrder>) getSession().getAttribute("userOrders");
            }

            for (int i = 0; i < idList.length; i++)
            {
                for (BiospecimenOrder order : populatedList)
                {

                    Long id = Long.parseLong(idList[i]);
                    // loop through admin orders and get chosen order
                    if (order.getId().equals(id))
                    {
                        removalList.add(order);
                    }
                }
            }
            if (!removalList.isEmpty())
            {
                this.orderService.deleteBiospecimenOrder(removalList);
            }
        }

    }

    /**
     * @return the orderBean
     */
    public BiospecimenOrder getOrderBean()
    {

        if (orderBean == null)
        {
            orderBean = this.sessionOrder.getOrder();

        }
        return orderBean;
    }

    /**
     * @param orderBean
     *            the orderBean to set
     */
    public void setOrderBean(BiospecimenOrder orderBean)
    {

        this.orderBean = orderBean;
    }

    public String getEditChangeComments() {
		return editChangeComments;
	}

	public void setEditChangeComments(String editChangeComments) {
		this.editChangeComments = editChangeComments;
	}

	public OrderStatus getOrderStatusBean()
    {

        return orderStatusBean;
    }

    public void setOrderStatusBean(OrderStatus orderStatusBean)
    {

        this.orderStatusBean = orderStatusBean;
    }

    /**
     * @return the statuses array
     */
    public OrderStatus[] getStatuses()
    {

        return statuses;
    }

    public ItemQueue getQueue()
    {

        return queue;
    }

    public void setQueue(ItemQueue queue)
    {

        this.queue = queue;
    }

    public String getOrderComment()
    {

        return orderComment;
    }

    public void setOrderComment(String orderComment)
    {

        this.orderComment = orderComment;
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

    public String[] getItemsFromQueueAmount()
    {

        return itemsFromQueueAmount;
    }

    public void setItemsFromQueueAmount(String[] itemsFromQueueAmount)
    {

        this.itemsFromQueueAmount = itemsFromQueueAmount;
    }

    public BiospecimenItem getOrderItem()
    {

        return orderItem;
    }

    public void setOrderItem(BiospecimenItem orderItem)
    {

        this.orderItem = orderItem;
    }

    public Collection<BiospecimenOrder> getUserOrders()
    {

        return userOrders;
    }

    public void setUserOrders(Collection<BiospecimenOrder> userOrders)
    {

        this.userOrders = userOrders;
    }

    public String getOrderId()
    {

        return orderId;
    }

    public void setOrderId(String orderId)
    {

        this.orderId = orderId;
    }

    @Override
    public void setSession(Map<String, Object> session)
    {

        // userSession = session;
    }

    /**
     * Returns true if user is in admin or studyAdmin namespace
     * 
     * @return
     */
    public boolean getInAdmin()
    {

        return (PortalConstants.NAMESPACE_ADMIN.equals(getNameSpace()) || PortalConstants.NAMESPACE_STUDYADMIN
                .equals(getNameSpace()));

    }

    public Address getAddressBean()
    {

        return addressBean;
    }

    public void setAddressBean(Address addressBean)
    {

        this.addressBean = addressBean;
    }

    public Account getOrderOwner()
    {

        Account tempAccount = accountManager.getAccountByUser(this.getSessionOrder().getOrder().getUser());

        setOrderOwner(tempAccount);

        return orderOwner;
    }

    public String getOrderOwnerId()
    {

        return orderOwnerId;
    }

    public void setOrderOwnerId(String orderOwnerId)
    {

        this.orderOwnerId = orderOwnerId;
    }

    public void setOrderOwner(Account ownerAccount2)
    {

        this.orderOwner = ownerAccount2;
    }

    public String getOrderTitle()
    {

        return orderTitle;
    }

    public void setOrderTitle(String orderTitle)
    {

        this.orderTitle = orderTitle;
    }

    public String getOrderStatus()
    {

        return orderStatus;
    }

    public void setOrderStatus(String orderStatus)
    {

        this.orderStatus = orderStatus;
    }

    public Map<UserFile, byte[]> getUploadedFilesMap()
    {

        return uploadedFilesMap;
    }

    public void setUploadedFilesMap(Map<UserFile, byte[]> uploadedFilesMap)
    {

        this.uploadedFilesMap = uploadedFilesMap;
    }

    public ArrayList<UserFile> getFilesAttached()
    {

        ArrayList<UserFile> filesAttached = new ArrayList<UserFile>();
        uploadedFilesMap = getSessionAccountEdit().getUploadedFilesMap();
        if (uploadedFilesMap != null)
        {
            filesAttached.addAll(uploadedFilesMap.keySet());
        }
        if (this.orderBean != null)
        {
            if (this.orderBean.getDocumentList() != null)
            {
                for (OrderManagerDocument doc : this.orderBean.getDocumentList())
                {
                    if (!doc.getRemoveFile())
                    {
                        filesAttached.add(doc.getUserFile());
                    }
                }
            }
        }
        return filesAttached;
    }

    public File getUpload()
    {

        return upload;
    }

    public void setUpload(File upload)
    {

        this.upload = upload;
    }

    public String getUploadContentType()
    {

        return uploadContentType;
    }

    public void setUploadContentType(String uploadContentType)
    {

        this.uploadContentType = uploadContentType;
    }

    public String getUploadFileName()
    {

        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName)
    {

        this.uploadFileName = uploadFileName;
    }

    public String getUploadDescription()
    {

        return uploadDescription;
    }

    public void setUploadDescription(String uploadDescription)
    {

        this.uploadDescription = uploadDescription;
    }

    public String upload()
    {

        BiospecimenOrder order = this.sessionOrder.getOrder();
        // set order title
        order.setOrderTitle(this.orderTitle);
        // set abstract
        order.setAbstractText(this.orderBean.getAbstractText());
        // set Exp
        order.setExperimentalDesignPowerAnalysis(this.orderBean.getExperimentalDesignPowerAnalysis());
        // set ship To User
        order.setShipToName(this.orderBean.getShipToName());
        // set ship to Institution
        order.setShipToInstitution(this.orderBean.getShipToInstitution());

        order.setInvestigatorName(this.orderBean.getInvestigatorName());
        order.setInstitution(this.orderBean.getInstitution());

        order.setPhone(this.orderBean.getPhone());
        order.setAffiliation(this.orderBean.getAffiliation());
        order.setAffiliationPhone(this.orderBean.getAffiliationPhone());
        order.setAffiliationEmail(this.orderBean.getAffiliationEmail());
        order.setAffiliationSpecialInstructions(this.orderBean.getAffiliationSpecialInstructions());

        // setOrderBean(order);
        // create new address object
        Address addressTemp = new Address();
        // set address Id = 0 --- do i need to do this?
        addressTemp.setId((long) 0);
        // set address obj Address 1
        addressTemp.setAddress1(this.addressBean.getAddress1());
        // set address obj Address 2
        addressTemp.setAddress2(this.addressBean.getAddress2());
        // set address obj City
        addressTemp.setCity(this.addressBean.getCity());
        // set address obj State
        addressTemp.setOldState(this.addressBean.getOldState());
        // set address obj Zip
        addressTemp.setZipCode(this.addressBean.getZipCode());

        order.setAddress(addressTemp);

        /*
         * should update this logic of saving the comment and then the order, if
         * something goes wrong while saving the order then i should remove the
         * comment
         */
        if (StringUtils.isBlank(this.orderComment) == false)
        {
            this.sessionOrder.setComment(this.orderComment);
        }

        // we need this piece in case the user decides to change the amount.
        for (BiospecimenItem a : this.itemsFromQueue)
        {
            for (BiospecimenItem b : order.getRequestedItems())
            {
                if (a.getId().equals(b.getId()) && !a.getNumberOfAliquots().equals(b.getNumberOfAliquots()))
                {
                    b.setNumberOfAliquots(a.getNumberOfAliquots());

                }

            }
        }

        if (uploadFileName != null)
        {
            // get the map of uploaded files in session
            uploadedFilesMap = getSessionAccountEdit().getUploadedFilesMap();

            // initialize map if there are no files that have bene uploaded
            if (uploadedFilesMap == null)
            {
                uploadedFilesMap = new HashMap<UserFile, byte[]>();
            }

            // set the user file information from the request
            UserFile userFile = new UserFile();
            userFile.setName(uploadFileName);
            userFile.setDescription(selectedUploadFileType);
            userFile.setPath(ServiceConstants.ORDER_MANAGER_FILE_PATH
                    + this.sessionAccount.getAccount().getUser().getFullName().replaceAll(" ", "")
                    + "_"
                    + this.sessionAccount.getAccount().getUser().getId()
                    + "_"
                    + new java.util.Date(order.getDateCreated().getTime()).toString().replaceAll(":", "")
                            .replaceAll(" ", "") + "/");
            userFile.setDatafileEndpointInfo(datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID));
            userFile.setUserId(this.sessionAccount.getAccount().getUser().getId());

            // if(filesAttached==null){
            // filesAttached = new ArrayList<String>();
            // }
            // filesAttached.add(uploadFileName);

            try
            {
                // load the file into stream and save the byte array and
                // userfile into session
                InputStream in = new FileInputStream(upload);
                byte[] bytes = new byte[(int) upload.length()];
                in.read(bytes);
                uploadedFilesMap.put(userFile, bytes);
                getSessionAccountEdit().setUploadedFilesMap(uploadedFilesMap);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return PortalConstants.ACTION_REDIRECT;
    }

    public String view()
    {

        return PortalConstants.ACTION_VIEW;

    }

    public String[] getItemCheckList()
    {

        return itemCheckList;
    }

    public void setItemCheckList(String[] itemCheckList)
    {

        this.itemCheckList = itemCheckList;
    }

    public String removeBiospecimenItemFromOrder()
    {

        BiospecimenOrder order = this.sessionOrder.getOrder();
        BiospecimenItem removeItem = null;
        String itemsToRemove = this.itemToRemove.replaceAll("\\[|\\]|\"", "");
        List<String> itemsToRemoveList = new ArrayList<String>(Arrays.asList(itemsToRemove.split(",")));

        for (BiospecimenItem item : order.getRequestedItems())
        {

        	if(itemsToRemoveList.contains(String.valueOf(item.getId())))
            {

                removeItem = item;
                break;
            }
        }

        boolean result = this.orderService.removeBiospecimenItemFromOrder(removeItem, order);
        return SUCCESS;
    }

    public String getItemToRemove()
    {

        return itemToRemove;
    }

    public void setItemToRemove(String itemToRemove)
    {

        this.itemToRemove = itemToRemove;
    }

    public SessionOrder getSessionOrder()
    {

        return sessionOrder;
    }

    public void setSessionOrder(SessionOrder sessionOrder)
    {

        this.sessionOrder = sessionOrder;
    }

    public BiospecimenOrder getCurrentOrder()
    {

        return this.getSessionOrder().getOrder();

    }

    public String getAddToExistingOrderComments() {
		return addToExistingOrderComments;
	}

	public void setAddToExistingOrderComments(String addToExistingOrderComments) {
		this.addToExistingOrderComments = addToExistingOrderComments;
	}

	public String getFormSubmitStatus()
    {

        return formSubmitStatus;
    }

    public void setFormSubmitStatus(String formSubmitStatus)
    {

        this.formSubmitStatus = formSubmitStatus;
    }

    /**
     * Removes ValueRange with permissible value from session.
     * 
     * @return String
     */
    public String removeFile()
    {

        if (this.sessionOrder != null)
        {
            if (sessionOrder.getOrder().getOrderStatus() != OrderStatus.CREATED)
            {
                List<OrderManagerDocument> docList = sessionOrder.getOrder().getDocumentList();
                if (docList != null)
                {
                    // get document list from session
                    for (OrderManagerDocument doc : docList)
                    {
                        UserFile uf = doc.getUserFile();

                        // flag file to be removed on save
                        if (uf.getName().equals(uploadFileName))
                        {
                            // if the document was marked as removed, uploaded again, and marked as removed again
                            // the file needs to be removed from the session and this file will be removed on save
                            if (!doc.getRemoveFile())
                            {
                                doc.setRemoveFile(true);
                                return SUCCESS;
                            }
                            break;
                        }
                    }
                }
            }
            // remove session file
            uploadedFilesMap = getSessionAccountEdit().getUploadedFilesMap();

            UserFile toRemove = new UserFile();
            for (UserFile uf : uploadedFilesMap.keySet())
            {
                if (uf.getName().equals(uploadFileName))
                {
                    toRemove = uf;
                }
            }
            uploadedFilesMap.remove(toRemove);
        }

        return SUCCESS;
    }

    public Boolean getIsOrderEditable()
    {

        // created, persisted, revision requested allow edit
        if (this.getSessionOrder().getOrder().getOrderStatus().equals(OrderStatus.CREATED)
                || this.getSessionOrder().getOrder().getOrderStatus().equals(OrderStatus.PERSISTED)
                || this.getSessionOrder().getOrder().getOrderStatus().equals(OrderStatus.REVISION_REQUESTED))
        {
            return true;
        }

        return false;
    }

    public String getRemoveOrderList()
    {

        return removeOrderList;
    }

    public void setRemoveOrderList(String removalIds)
    {

        this.removeOrderList = removalIds;
    }

    private List<String> stringToList(String str)
    {

        List<String> strList = new ArrayList<String>();

        if (str != null && !str.isEmpty())
        {
            String[] strArray = str.split(EMAIL_DELIMITER);

            for (String s : strArray)
            {
                if (s != null && !s.trim().isEmpty())
                    strList.add(s);
            }
        }

        return strList;
    }

    /**
     * @return the uploadFileType
     */
    public ArrayList<String> getUploadFileType()
    {

        if (uploadFileType == null)
        {
            // Specify the require files
            specifyRequiredBioRepositoryFileTypes();
        }
        return uploadFileType;
    }

    /**
     * @param uploadFileType
     *            the uploadFileType to set
     */
    public void setUploadFileType(ArrayList<String> uploadFileType)
    {

        this.uploadFileType = uploadFileType;
    }

    /**
     * @return the fileType
     */
    public String getSelectedUploadFileType()
    {

        return selectedUploadFileType;
    }

    /**
     * @param fileType
     *            the fileType to set
     */
    public void setSelectedUploadFileType(String fileType)
    {

        this.selectedUploadFileType = fileType;
    }
    
    public String getFilteredData() {
		return filteredData;
	}

	public void setFilteredData(String filteredData) {
		this.filteredData = filteredData;
	}

	public String getSelectedData() {
		return selectedData;
	}

	public void setSelectedData(String selectedData) {
		this.selectedData = selectedData;
	}

}
