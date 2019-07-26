
package gov.nih.tbi.ordermanager.service.impl;

import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.exceptions.OrderManagerException;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.ordermanagement.transfer.CoriellSftpTransfer;
import gov.nih.tbi.ordermanagement.transfer.IUWebServiceTransfer;
import gov.nih.tbi.ordermanagement.transfer.TransferInterface;
import gov.nih.tbi.ordermanager.dao.BioRepositoryDao;
import gov.nih.tbi.ordermanager.dao.BioRepositoryFileTypeDao;
import gov.nih.tbi.ordermanager.dao.BiospecimenOrderDao;
import gov.nih.tbi.ordermanager.dao.CommentDao;
import gov.nih.tbi.ordermanager.dao.ItemQueueDao;
import gov.nih.tbi.ordermanager.dao.OrderManagerDocumentDao;
import gov.nih.tbi.ordermanager.model.BioRepository;
import gov.nih.tbi.ordermanager.model.BioRepositoryFileType;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.Comment;
import gov.nih.tbi.ordermanager.model.ItemQueue;
import gov.nih.tbi.ordermanager.model.OrderManagerDocument;
import gov.nih.tbi.ordermanager.model.OrderStatus;
import gov.nih.tbi.ordermanager.model.exception.EmptyOrderException;
import gov.nih.tbi.ordermanager.model.exception.IllegalOrderStatusException;
import gov.nih.tbi.ordermanager.model.exception.MultipleRepositoryException;
import gov.nih.tbi.ordermanager.service.OrderService;
import gov.nih.tbi.repository.dao.DatafileEndpointInfoDao;
import gov.nih.tbi.repository.dao.UserFileDao;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class OrderServiceImpl implements OrderService
{

    private static Logger log = Logger.getLogger(OrderServiceImpl.class);

    @Autowired
    private BiospecimenOrderDao biospecimenOrderDao;

    /*    @Autowired
        private BiospecimenItemDao biospecimenItemDao;*/

    @Autowired
    private ItemQueueDao itemQueueDao;

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private DatafileEndpointInfoDao datafileEndpointInfoDao;

    @Autowired
    private UserFileDao userFileDao;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private OrderManagerDocumentDao orderManageDocumentrDao;

    @Autowired
    private BioRepositoryDao bioRepositoryDao;

    @Autowired
    private BioRepositoryFileTypeDao bioRepositoryFileTypeDao;
    
    @Autowired
    IUWebServiceTransfer iUWebServiceTransfer;

    private static final String ORDER_SAVED_MESSAGE_TEXT = "order status changed to "
            + OrderStatus.PERSISTED.getValue();
    private static final String ORDER_PERSISTED_MESSAGE_TEXT = "order status changed to "
            + OrderStatus.PENDING_APPROVAL.getValue();
    private static final String ORDER_APPROVED_MESSAGE_TEXT = "order status changed to "
            + OrderStatus.APPROVED.getValue();
    private static final String ORDER_REJECTED_MESSAGE_TEXT = "order status changed to "
            + OrderStatus.REJECTED.getValue();
    private static final String ORDER_REVISION_REQUESTED_MESSAGE_TEXT = "order status changed to "
            + OrderStatus.REVISION_REQUESTED.getValue();
    private static final String ORDER_PENDING_SUBMISSION_MESSAGE_TEXT = "order status changed to "
            + OrderStatus.PENDING_SUBMISSION.getValue();
    private static final String ORDER_SUBMITTED_MESSAGE_TEXT = "order status changed to "
            + OrderStatus.SUBMITTED.getValue();
    private final static String ORDER_STATUS_CHANGED_TEXT = "order status manually changed by admin ";

    @Override
    public BiospecimenOrder createNewBiospecimenOrder(String orderName, User user, Collection<BiospecimenItem> items)
            throws MultipleRepositoryException
    {

        /*
         * there is a validation step involved here to make sure that the repository id of all the orders are the same, can't create
         * an order with items that belong to two different repositories
         */
        /*
         * check to make sure that all the items belong to the same repository
         */
        if (items.isEmpty())
        {
            return null;
        }
        BiospecimenOrder order = new BiospecimenOrder();
        Account userAccount = this.getAccount(user);
        order.setInstitution(userAccount.getAffiliatedInstitution());
        order.setRequestedItems(items);
        order.setUser(user);
        order.setOrderStatus(OrderStatus.CREATED);
        order.setOrderTitle(orderName);
        order.setDateCreated(new Date());
        /*
         * persist this object in the session?? but if we are using struts2 then I don't need to store this, if there is a private attribute then shouldn't need to be stored
         * not just that, the service layer shouldn't interact with the session, that is breaking abstraction and layer boundaries
         */
        return order;
    }

    @Override
    public void deleteBiospecimenOrder(BiospecimenOrder order)
    {

        /*
         * retrieve the biospecimen from session or db?? or could I just have it in the parameter
         * 
         * i think this need to be deleted from both the session and the db using the dao?? yeah that sounds right, wait a sec...
         * shouldn't the session be handled by the action layer and only the dao and db should cleared here
         */
        this.biospecimenOrderDao.remove(order.getId());
    }

    /*
     * This will take a list of biospecimenorders and remove them from the system
     */
    public void deleteBiospecimenOrder(List<BiospecimenOrder> orderRemovalList)
    {

        this.biospecimenOrderDao.removeAll(orderRemovalList);
    }

    @Override
    public Collection<BiospecimenOrder> findExistingOrdersForUser(User user)
    {

        /*
         * no validation needed so simply use the dao to return the collection
         */
        List<BiospecimenOrder> result = this.biospecimenOrderDao.getByUser(user);
        return result;
    }

    @Override
    public BiospecimenOrder findOrderByIdForUser(Long orderId, User user)
    {

        BiospecimenOrder order = this.biospecimenOrderDao.getOrderByIdForUser(orderId, user);
        return order;
    }

    @Override
    public void associateFileWithOrder(UserFile userFile, BiospecimenOrder order, User user)
    {

        if (userFile == null)
        {
            log.error("associateFileWithOrder method called with null user file object");
            return;
        }
        if (order == null)
        {
            log.error("associateFileWithOrder method called with null order object");
            return;
        }
        if (user == null)
        {
            log.error("associateFileWithOrder method called with null user object");
            return;
        }
        userFile = this.userFileDao.save(userFile);
        /* create a new order manager document object omd */
        OrderManagerDocument doc = new OrderManagerDocument();
        /* save the available information in omf (user file, order, etc) */
        doc.setDescription(userFile.getDescription());
        doc.setOrder(order);
        doc.setUserFile(userFile);
        BioRepositoryFileType bioFileType = bioRepositoryFileTypeDao.get(userFile.getDescription());
        doc.setFileType(bioFileType);

        /*
         * save the user file
         */
        /* use the order manager document dao to save the document */
        this.orderManageDocumentrDao.save(doc);
        /* save the document in the order and save the order using the dao */
        List<OrderManagerDocument> existingDocs = order.getDocumentList();
        if (existingDocs == null)
        {
            existingDocs = new ArrayList<OrderManagerDocument>();
        }
        existingDocs.add(doc);
        order.setDocumentList(existingDocs);
        // this.biospecimenOrderDao.save(order);
    }

    @Override
    public Boolean addBiospecimenItemToOrder(BiospecimenItem item, BiospecimenOrder order)
            throws MultipleRepositoryException
    {

        if (item == null)
        {
            log.error("addBiospecimenItemToOrder method called with null item");
            return Boolean.FALSE;
        }
        if (order == null)
        {
            log.error("addBiospecimenItemToOrder method called with null order");
            return Boolean.FALSE;
        }

        Collection<BiospecimenItem> existing = order.getRequestedItems();
        existing.add(item);
        order.setRequestedItems(existing);
        return Boolean.TRUE;
    }

    @Override
    public Boolean removeBiospecimenItemFromOrder(BiospecimenItem item, BiospecimenOrder order)
    {

        if (item == null)
        {
            log.error("removeBiospecimenItemFromOrder method called with null item");
            return Boolean.FALSE;
        }
        if (order == null)
        {
            log.error("removeBiospecimenItemFromOrder method called with null item");
            return Boolean.FALSE;
        }
        /*
         * again - do i need to touch the dao or just removing the reference from the collection
         */
        /*
         * here i think returning the boolean makes sense 
         */
        Collection<BiospecimenItem> existing = order.getRequestedItems();
        if (existing.contains(item) == true)
        {
            // can't remove the item if there is only one item in the order
            /*            if ( existing.size() == 1 )
                        {
                            // only 1 item in the order, return false
                            log.error("can't remove an item from order which contains only one item in it");
                            return Boolean.FALSE;
                        }*/
            existing.remove(item);
            order.setRequestedItems(existing);
            /* persist if it has already been persisted once */
            if (order.getId() != null)
            {
                this.biospecimenOrderDao.save(order);
            }
            // this.biospecimenItemDao.remove(item.getId());
            return Boolean.TRUE;
        }
        else
        {
            return Boolean.FALSE;
        }
    }

    @Override
    public Boolean saveBiospecimenOrder(BiospecimenOrder order, User user, List<UserFile> associatedFiles)
            throws EmptyOrderException, MultipleRepositoryException, IllegalOrderStatusException, OrderManagerException
    {

        /*
         * need to implement this the first thing in the morning and i need to keep in mind the new requirement of updating the
         * queue object and remove this item from the queue when saving the order object because one item shouldn't exist both in
         * a persisted order object and the queue object at the same time, so the removal should be done in both the save and
         * validate methods, however there still seem to be a little breach of the layer boundary because this requirement of
         * removing the item from queue before saving it to the 
         */
        /*
         * okay so what i was planning to do in this is that i have to make sure that the item in this order doesn't already existing 
         * in some other order which is in the persist state (?? think about this some more ??) and that the items in this order
         * are removed from the queue of the user
         */
        /*
         * an item could be in persisted state in two different scenarios, if he just created and saved the object, and if the object
         * is updated after being rejected from admin - let's not worry about the second scenario
         */

        /*
         * make sure that the order object is in the right state
         */
        if (order.getRequestedItems() == null)
        {
            // if (log.isEnabledFor(Level.WARN) == true)
            // {
            log.error("The list of BiospecimenItem in this order is null, returning false. "
                    + "Cannot save an order without a list of items in it.");
            // }
            throw new EmptyOrderException(
                    "Error. No items found in order. Cannot save an order without any items in it.", order);

        }
        if (order.getRequestedItems().isEmpty() == true)
        {

            log.error("The list of BiospecimenItem in this order is empty, returning false. "
                    + "Cannot save an order without a list of items in it.");
            throw new EmptyOrderException(
                    "Error. No items found in order. Cannot save an order without any items in it.", order);
        }
        /* can only save an order if it is recently created, is already in persisted state, or if it was marked for revision by admin */
        if ((order.getOrderStatus() != OrderStatus.CREATED) && (order.getOrderStatus() != OrderStatus.PERSISTED)
                && (order.getOrderStatus() != OrderStatus.REVISION_REQUESTED))
        {

            log.error("The order is in invalid status currently. Can't save an order while it is in "
                    + order.getOrderStatus().getValue() + " status");

            throw new IllegalOrderStatusException("Error. Current status of order doesn't allow any modifications.",
                    order, order.getOrderStatus());
        }
        if (StringUtils.isBlank(order.getOrderTitle()) == true)
        {

            log.error("The list of order doesn't contain a title. Can't save an order without a title");

            return Boolean.FALSE;
        }

        Collection<BiospecimenItem> items = order.getRequestedItems();

        /* if the item doesn't exist in any of the existing order then remove it from the item queue for this user and persist the order object */
        /* find the user's item queue */

        /* this need to only be done if the order is currently in CREATED status */
        if (order.getOrderStatus() == OrderStatus.CREATED)
        {
            if (log.isDebugEnabled() == true)
            {
                log.debug("order being saved is a new order so removing any item in this order from the user's queue");
            }
            ItemQueue queue = this.itemQueueDao.findByUser(user);
            /* remove all the items from the queue and save the queue back */
            List<BiospecimenItem> existingItems = queue.getItems();
            existingItems.removeAll(items);
            queue.setItems(existingItems);
            this.itemQueueDao.save(queue);
        }

        /* remove the reference of the item queue from the items */
        for (BiospecimenItem item : items)
        {
            item.setItemQueue(null);
            item.setBiospecimenOrder(order);
        }
        /* update the status of the order */
        /* if the status is about to be changed then I should add a system comment at this point */
        if (order.getOrderStatus() != OrderStatus.PERSISTED)
        {
            this.addCommentToOrder(user, ORDER_SAVED_MESSAGE_TEXT + " old status - "
                    + order.getOrderStatus().getValue(), order);
        }
        order.setOrderStatus(OrderStatus.PERSISTED);
        /* save the order */
        order = this.biospecimenOrderDao.save(order);
        if ((associatedFiles != null) && (associatedFiles.isEmpty() == false))
        {
            for (UserFile userFile : associatedFiles)
            {
                this.associateFileWithOrder(userFile, order, user);
            }
        }
        /* cross fingers */

        /*
         * let's just short circuit the xml generation and file upload test here
         */
        // this.corrielSftpTransfer.proccessAndSend(order, new java.util.ArrayList<UserFile>());
        return Boolean.TRUE;
    }

    @Override
    public Boolean validateAndPersist(BiospecimenOrder order, User user, List<UserFile> associatedFiles)
            throws EmptyOrderException, MultipleRepositoryException, IllegalOrderStatusException, OrderManagerException
    {

        /*
         * I am wondering if I should further decouple the validation business logic from the service class, If I inject a different
         * bean which does the validation then I could implement the logic seperate from this bean and the logic could be changed without
         * changing the code in this class --- strategy pattern ---
         */
        /*
         * steps i need to perform in order to validate,
         */
        /*
         * the object must be in either created or persisted state
         */
        /*
         * the object must have the required attribute populated (just description at this point, comment??)
         */
        if (StringUtils.isBlank(order.getOrderTitle()) == true)
        {
            log.error("The list of order doesn't contain a title. Can't validate an order without a title");
            return Boolean.FALSE;
        }
        if (order.getRequestedItems() == null)
        {
            // if (log.isEnabledFor(Level.WARN) == true)
            // {
            log.error("The list of BiospecimenItem in this order is null, returning false. "
                    + "Cannot validate an order without a list of items in it.");
            // }
            throw new EmptyOrderException(
                    "Error. No items found in order. Cannot validate an order without any items in it.", order);

        }
        if (order.getRequestedItems().isEmpty() == true)
        {

            log.error("The list of BiospecimenItem in this order is empty, returning false. "
                    + "Cannot validate an order without a list of items in it.");
            throw new EmptyOrderException(
                    "Error. No items found in order. Cannot validate an order without any items in it.", order);
        }

        if ((order.getOrderStatus() != OrderStatus.PERSISTED) && (order.getOrderStatus() != OrderStatus.CREATED)
                && (order.getOrderStatus() != OrderStatus.REVISION_REQUESTED))
        {
            log.error("The order is in invalid status currently. Can't validate an order while it is in "
                    + order.getOrderStatus().getValue() + " status");
            throw new IllegalOrderStatusException("Error. Current status of order doesn't allow any modifications.",
                    order, order.getOrderStatus());
        }

        Collection<BiospecimenItem> items = order.getRequestedItems();

        /* this need to only be done if the order is currently in CREATED status */

        /* if the item doesn't exist in any of the existing order then remove it from the item queue for this user and persist the order object */

        if (order.getOrderStatus() == OrderStatus.CREATED)
        {
            if (log.isDebugEnabled() == true)
            {
                log.debug("order being saved is a new order so removing any item in this order from the user's queue");
            }
            /* find the user's item queue */
            ItemQueue queue = this.itemQueueDao.findByUser(user);
            /* remove all the items from the queue and save the queue back */
            List<BiospecimenItem> existingItems = queue.getItems();
            existingItems.removeAll(items);
            queue.setItems(existingItems);
            this.itemQueueDao.save(queue);

        }

        /* remove the reference of the item queue from the items */
        for (BiospecimenItem item : items)
        {
            item.setItemQueue(null);
            item.setBiospecimenOrder(order);
        }

        /*
         * now just change the state and persist the order object
         */

        order.setOrderStatus(OrderStatus.PENDING_APPROVAL);
        order.setDateSubmitted(new Date());
        this.addCommentToOrder(user, ORDER_PERSISTED_MESSAGE_TEXT, order);
        order = this.biospecimenOrderDao.save(order);
        if (associatedFiles != null)
        {
            for (UserFile userFile : associatedFiles)
            {
                this.associateFileWithOrder(userFile, order, user);
            }
        }
        return Boolean.TRUE;

    }

    @Override
    public Collection<BiospecimenOrder> getOrderListForBiorepositoryAdmin(User admin)
    {

        List<BiospecimenOrder> result = this.biospecimenOrderDao.getAllOrders();
        return result;
    }

    @Override
    public Boolean approveBiospecimenOrder(User brac, BiospecimenOrder order) throws OrderManagerException
    {

        /*
         * same issue with the user parameter as in the rejected method case
         */
        /*
         * need to perform the following steps:
         */
        /* check to make sure that the order is in the right status (pending approval) */

        /* check to make sure that the user created a note for this object */

        /* update the status of the order */

        // wrong if test, the admin change the status of any order from any to any
        // if (order.getOrderStatus() == OrderStatus.PENDING_APPROVAL)
        // {
        /*
         * update the status and save the object back
         */
    	
    	
        
        /*
         * don't need to do anything else here, next step will be performed when the PI logs in...stupid stupid stupid, forgot to persist the object
         */

        List<UserFile> orderFiles = new java.util.ArrayList<UserFile>();
        for (OrderManagerDocument doc : order.getDocumentList())
        {
            orderFiles.add(doc.getUserFile());
        }

        // BL: moved up
        order.setBracUser(brac);

        // HARD CODING - This is where we will define what transfers are going to take place.
        //BioRepository nindsRepo = bioRepositoryDao.get(345L);
        //BioRepository biofindRepo = bioRepositoryDao.get(346L);
        DatafileEndpointInfo bricsEndpoint = datafileEndpointInfoDao.get(ServiceConstants.TBI_DATAFILE_ENDPOINT_ID);
        //DatafileEndpointInfo coriellEndpoint = datafileEndpointInfoDao
                //.get(ServiceConstants.CORRIEL_DATAFILE_ENDPOINT_ID);
        // Create a list of transfers to execute.
       // List<TransferInterface> transfers = new ArrayList<TransferInterface>();
        //List<TransferInterface> successfulTransfers = new ArrayList<TransferInterface>();
        //transfers.add(new CoriellSftpTransfer(nindsRepo, "NINDS", coriellEndpoint, bricsEndpoint));
        // transfers.add(new FailTestTransfer()); // For local testing
        //transfers.add(new CoriellSftpTransfer(biofindRepo, "BioFIND", coriellEndpoint, bricsEndpoint));
        // END HARD CODING
        //IUWebServiceTransfer ti = new IUWebServiceTransfer(null, "", bricsEndpoint);
        //transfers.add(new IUWebServiceTransfer(biofindRepo, "NINDS", bricsEndpoint));
        
         iUWebServiceTransfer.setBricsEndpoint(bricsEndpoint);
         boolean result = iUWebServiceTransfer.processAndSend(order);
          
        
        
        
        // If a transfer failed then roll back any successful ones
        if (!result)
        {
            
        	log.error("IU submit order faliure for order id  " + order.getId());
            // old code executed on a failed transfer
            // save it in pending submission state
            order.setOrderStatus(OrderStatus.PENDING_SUBMISSION);
            
            this.addCommentToOrder(brac, ORDER_PENDING_SUBMISSION_MESSAGE_TEXT, order);
            // BL: moved above (XML Order needs this) order.setBracUser(brac);
            this.biospecimenOrderDao.save(order);

            return Boolean.FALSE;
        }
        else
        {
            
        	order.setOrderStatus(OrderStatus.SUBMITTED);
            order.setDateSubmitted(new Date());
        	
        	
        	// old code executed on a successful transfer
            // if successful in sending the package to coriell then save it as approved
            // add system comment
            this.addCommentToOrder(brac, ORDER_APPROVED_MESSAGE_TEXT, order);
            // BL: moved above (XML Order needs this) order.setBracUser(brac);
            this.biospecimenOrderDao.save(order);

            return Boolean.TRUE;
        }

    }

    @Override
    public Boolean approveBiospecimenOrderList(User brac, Collection<BiospecimenOrder> orderList)
            throws OrderManagerException
    {

        boolean toReturn = true;
        for (BiospecimenOrder order : orderList)
        {
            if (this.approveBiospecimenOrder(brac, order) == false)
            {
                toReturn = false;
            }
        }
        return Boolean.valueOf(toReturn);
    }

    @Override
    public Boolean rejectBiospecimenOrder(User brac, BiospecimenOrder order) throws OrderManagerException
    {

        /*
         * not sure if we need to keep the user object in the parameter list because I am not certain if I should explicitly
         * do the step to validate and see if the user is the BRAC for the repository that this order is for
         */
        /*
         * need to execute the following steps:
         */
        /*
         * check to make sure that the order object must be in the right state (pending approval)
         */
        // wrong if test, the admin change the status of any order from any to any
        // if (order.getOrderStatus() == OrderStatus.PENDING_APPROVAL)
        // {
        /*
         * update the status and save the object back
         */
        order.setOrderStatus(OrderStatus.REJECTED);
        /*
         * don't need to do anything else here, next step will be perfomed when the PI logs in...stupid stupid stupid, forgot to persist the object
         */
        this.addCommentToOrder(brac, ORDER_REJECTED_MESSAGE_TEXT, order);
        this.biospecimenOrderDao.save(order);
        return Boolean.TRUE;
        // }
        // else
        // {
        // return Boolean.FALSE;
        // }
    }

    @Override
    public Boolean rejectBiospecimenOrderList(User brac, Collection<BiospecimenOrder> orderList)
            throws OrderManagerException
    {

        boolean toReturn = true;
        for (BiospecimenOrder order : orderList)
        {
            if (this.rejectBiospecimenOrder(brac, order) == false)
            {
                toReturn = false;
            }
        }
        return toReturn;
    }

    @Override
    public Boolean requestRevisionForOrders(User brac, Collection<BiospecimenOrder> orderList)
            throws OrderManagerException
    {

        boolean toReturn = true;
        for (BiospecimenOrder order : orderList)
        {
            if (this.rejectBiospecimenOrder(brac, order) == false)
            {
                toReturn = false;
            }
        }
        return toReturn;
    }

    @Override
    public Boolean requestRevisionForOrder(User brac, BiospecimenOrder order) throws OrderManagerException
    {

        // wrong if check, the admin can change the status of any order from any to any
        // if (order.getOrderStatus() == OrderStatus.PENDING_APPROVAL)
        // {
        /*
         * update the status and save the object back
         */
        order.setOrderStatus(OrderStatus.REVISION_REQUESTED);
        /*
         * don't need to do anything else here, next step will be perfomed when the PI logs in...stupid stupid stupid, forgot to persist the object
         */
        this.addCommentToOrder(brac, ORDER_REVISION_REQUESTED_MESSAGE_TEXT, order);
        this.biospecimenOrderDao.save(order);
        return Boolean.TRUE;
        // }

        /*        else
                {
                    return Boolean.FALSE;
                }*/
    }

    @Override
    public Comment addCommentToOrder(User user, String message, BiospecimenOrder order) throws OrderManagerException
    {

        if (user == null)
        {
            log.error("addCommentToOrder method called with null user");
            throw new OrderManagerException("addCommentToOrder method called with null user");
        }

        if (StringUtils.isBlank(message) == true)
        {
            log.error("addCommentToOrder method called with blank message");
            throw new OrderManagerException("addCommentToOrder method called with blank message");
        }
        if (order == null)
        {
            log.error("addCommentToOrder method called with null order");
            throw new OrderManagerException("addCommentToOrder method called with null order");
        }
        Comment comment = new Comment();
        comment.setDate(new Date());
        comment.setUser(user);
        comment.setMessage(message);
        comment.setBiospecimenOrder(order);
        java.util.List<Comment> commentList = order.getCommentList();
        if (commentList == null)
        {
            commentList = new java.util.ArrayList<Comment>();
        }
        commentList.add(comment);
        order.setCommentList(commentList);
        /*
         * I'm thinking that I shouldn't simply save the object back, maybe instead i should check and if the order object has been persisted then i should
         * save it back otherwise I should only add the comment to the object and return it back
         */
        if (order.getId() != null)
        {
            comment = this.commentDao.save(comment);
        }

        return comment;
    }

    @Override
    public void removeCommentFromOrder(User user, Comment comment, BiospecimenOrder order)
    {

        if (comment == null)
        {
            log.error("removeCommentFromOrder() method called with null comment");
            return;
        }
        if (user == null)
        {
            log.error("removeCommentFromOrder method called with null user");
            return;
        }
        if (order == null)
        {
            log.error("removeCommentFromOrder method called with null order");
            return;
        }
        // get the list of existing comments in order
        List<Comment> existingComments = order.getCommentList();
        if (existingComments != null)
        {
            if (existingComments.isEmpty() == false)
            {
                // remove this comment from existing comments
                existingComments.remove(comment);
                // set the comment list back in the order
                order.setCommentList(existingComments);
                // just like create comment method, I don't think I should save the order object, it should be returned
                // as is and the caller should take care of saving the order if needed
            }
        }
        if (comment.getId() != null)
        {
            // id exist so comment has already been persisted, remove it using dao
            this.commentDao.remove(comment.getId());
        }

    }

    public List<Comment> getAllCommentsForOrder(BiospecimenOrder order)
    {

        List<Comment> result = this.commentDao.findByOrder(order);
        return result;
    }

    public Account getAccount(User user)
    {

        return accountDao.getByUser(user);
    }

    @Override
    public void setOrderStatus(User brac, BiospecimenOrder order, OrderStatus status) throws OrderManagerException
    {

        /*
         * okay so this looks like there is going to be a big chain of if else here
         */
        if (status == OrderStatus.APPROVED)
        {
            this.approveBiospecimenOrder(brac, order);
        }
        else
            if (status == OrderStatus.REJECTED)
            {
                this.rejectBiospecimenOrder(brac, order);
            }
            else
                if (status == OrderStatus.REVISION_REQUESTED)
                {
                    this.requestRevisionForOrder(brac, order);
                }
                else
                {
                    // how is this going to be handled in terms of maintaining the accurate workflow
                    // seems like the only thing to do here is set the status and save, there is not automated
                    // background
                    // component at this point so don't have anything to worry about

                    // also doesn't seem like there is any sort of status check or validation that can be done at this
                    // point
                    order.setOrderStatus(status);
                    order.setBracUser(brac);
                    this.addCommentToOrder(brac, ORDER_STATUS_CHANGED_TEXT + " - " + status.getValue(), order);
                    this.biospecimenOrderDao.save(order);
                }
    }

    /*
     * This will remove a document from an biospecimenorder.
     */
    public void deleteOrderManageDocument(OrderManagerDocument removeDocument)
    {

        this.orderManageDocumentrDao.remove(removeDocument.getId());
    }

}
