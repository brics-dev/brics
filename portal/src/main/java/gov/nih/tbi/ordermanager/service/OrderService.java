
package gov.nih.tbi.ordermanager.service;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.exceptions.OrderManagerException;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.Comment;
import gov.nih.tbi.ordermanager.model.OrderManagerDocument;
import gov.nih.tbi.ordermanager.model.OrderStatus;
import gov.nih.tbi.ordermanager.model.exception.EmptyOrderException;
import gov.nih.tbi.ordermanager.model.exception.IllegalOrderStatusException;
import gov.nih.tbi.ordermanager.model.exception.MultipleRepositoryException;
import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.util.Collection;
import java.util.List;

/**
 * 
 * @author vpacha
 * 
 */
public interface OrderService
{

    /**
     * Method to instantiate a new BiospecimenOrder object, the created by this method won't be automatically persisted
     * in the database. In order to persist the object use the method {@link OrderService.validateAndPersist}
     * 
     * @throws MultipleRepositoryException
     */
    public BiospecimenOrder createNewBiospecimenOrder(String orderName, User user, Collection<BiospecimenItem> items)
            throws MultipleRepositoryException;

    /* this would be used to create new order, this should take in I guess a title, and persist the object in database, should this take in the user object, I think so, the user
     * object could very easily be injected in the action class
     */

    /**
     * Method to delete a BiospecimenOrder object which has not yet been persisted using
     * {@link OrderService.validateAndPersist}
     */
    public void deleteBiospecimenOrder(BiospecimenOrder oder);

    /*
     * this would be used to delete an existing order object, so I guess an identifier and user is required(is it?)
     */

    /**
     * Method to retrieve list of {@code BiospecimenOrder} objects for the user.
     * 
     * @param user
     */
    public Collection<BiospecimenOrder> findExistingOrdersForUser(User user);

    public BiospecimenOrder findOrderByIdForUser(Long orderId, User user);

    /**
     * Associate an uploaded file object with a {@code BiospecimenOrder}. Note that this method should
     * only be used to upload files to an existing order which has already been persisted to the database.
     * If the order received isn't already persisted then the behavior of this method is indeterminable.
     * 
     * @param fileObject
     *            The file object to associate with the BiospecimenOrder
     * @param order
     *            The BiospecimenOrder object with which the uploaded file object should be associated
     */
    public void associateFileWithOrder(UserFile userFile,
            BiospecimenOrder order, User user);

    /*
     * need to figure out the actual object type that will represent the file object, need
     *  to think about how to communicate success or failure, boolean return value
     * or some specialized exception?? the reason this method  could fail is if the 
     * order is in some illegal state (state representing already submitted or already approved,
     * I guess the only states in which a file could be associated with an order is created meaning
     * that the order object was just created and has not yet been approved or submitted, I guess
     * persisted could be another state representing that the order has been persisted in the database
     * but has not yet been sent for approval from brac, therefore objects in persisted state could 
     * accept file uploads as well)
     * 
     */

    /**
     * Method to associate a BiospecimenItem object with a BiospecimenOrder object
     * 
     * @param item
     *            The item to be associated with the order
     * @param order
     *            The order to which the BiospecimenItem should be added
     */
    public Boolean addBiospecimenItemToOrder(BiospecimenItem item, BiospecimenOrder order) throws MultipleRepositoryException;

    /*
     * this again need to be dependent on the state of the object selected (and return a boolean or exception)
     */

    /**
     * Method to remove a BiospecimenItem object from a BiospecimenOrder object
     * 
     * @param item
     *            The item to be removed from the order
     * @param order
     *            The order from which the item should be removed
     * @return {@code True} if the item was associated with the order and is successfully removed from the order and
     *         false if the item couldn't be removed (item was not associated with the order)
     */
    public Boolean removeBiospecimenItemFromOrder(BiospecimenItem item, BiospecimenOrder order);

    /*
     * this again need to be dependent on the state of the order object and definitely return the result
     */

    /**
     * Method to update the count of the given BiospecimenItem in the given BiospecimenOrder. If the count is 0 then
     * this item will be removed from the order and the result will be the same as calling
     * {@link OrderService.removeBiospecimenItemFromOrder} with this item and order
     * 
     * @param item
     *            The item whose count need to be updated
     * @param order
     *            The order in which the count of the given item should be updated
     * @param count
     *            The new value for count of the item in order
     */
    // public void updateCountOfBiospecimenItem(BiospecimenItem item, BiospecimenOrder order, Integer count);

    /*
     * this has all the same state dependencies as the delete method
     */

    /**
     * Method to temporarily save the biospciemen order object without validating the required attributes. An object can
     * only be saved if the order object is a new object and was just created or if the order is currently in a saved
     * state.Saving the object using this method will only change the state of the object from newly created state to
     * persisted state if the order was newly create but if the order is in a saved state then its state will remain the
     * same.The order will still need to be submitted by the user later using the method {@link
     * OrderService.validateAndPersist(BiospeciemenOrder order) to propagate it through the workflow of placing order}
     * 
     * @param order
     *            The order object which is to be saved
     * @param user
     *            The user who created the order
     * @return True if the order was saved successfully and false otherwise
     * @throws OrderManagerException 
     */
    public Boolean saveBiospecimenOrder(BiospecimenOrder order, User user, List<UserFile> associatedFiles) throws EmptyOrderException, MultipleRepositoryException, IllegalOrderStatusException, OrderManagerException;

    /**
     * Method to validate that all the required attributes of the biospecimen order are properly created like
     * description, attached document etc and then persist the object in the database with the appropriate status.
     * 
     * @param order
     * @throws OrderManagerException 
     */
    public Boolean validateAndPersist(BiospecimenOrder order, User user, List<UserFile> associatedFiles) throws EmptyOrderException, MultipleRepositoryException, IllegalOrderStatusException, OrderManagerException;

    /*
     * let's see what should be the api for the order service?
     * front end need this service for the following operations
     * - create a new order object
     * - delete an order object
     * - find all the existing orders for a user
     * - associate an uploaded file with an order
     * - associate a biospecimen item with an order object
     * - remove an association of biospecimen item with an order
     * - since there will be an association of a count with each biospecimen item, users would be able to change that count
     * - validate that all the business requirements are met before placing the order (files have been uploaded etc.)
     * - persist the object ( so the idea behind this currently is following -
     *                  according to the use case the user will log in to the order manager component, 
     *                  the landing page will be the queue of biospecimen items associated with the user
     *                  , from the landing page the user would be able to create a new bio specimen order
     *                  item which will be stored in the session, also at this piont, if the user prefers, multiple
     *                  bio specimen order items could be created and they all will be persisted in the session,
     *                  after that the user would be able to associate biospecimen items with that order,
     *                  once the user if finished associating the items with that order object, they will go to the
     *                  page displaying all the biospecimen orders associated with that user and then the user
     *                  would be able to upload files from local file system and associate those files with the order
     *                  object. After the user is finished with this process, they would try to place the order and at
     *                  point the service should validate to make sure that all the required data associated with the
     *                  order is present and valid and if so, persist the order. 
     */

    /*
     * let's sketch out the rough idea behind the service interaction between the ui displaying the queue of biosample items and the service (ItemQueueService)
     *  - the service need to give the item queue object back associated with that user
     *  - (um... so associating the bio-sample item with an order, does that fall under the boundary of.... it falls within the boundary of order service)
     */

    /*
     * let's think about the interface with the rest of the workflow
     * - when the BRAC UI is displayed we need to retrieve  the list of all the BiospecimenOrder objects that that BRAC is a registered admin of. In case the BRAC is the admin of more than 1
     * repository, the interface should enfore some restriction and not give back all the objects for all the repositories?? should we??
     * - from BRAC UI we should be able to change the status of one or a group of object to either accepted or rejected, this method should also validate that any requirement like rejection reason
     * is provided
     * - there should be a method to process an order approved by brac, this will most likely be called internally so should this be a public method? could this be called by some external component like
     * some scheduler? if we assume that this could be called by another component then how do we restrict this method from being called by a ui or action layer component accidentally?
     * for now let's not include this in the interface
     * 
     */

    /**
     * Method to retrieve a list of BiospecimenOrder object which are pending review by a BRAC. This method will return
     * null if the parameter is not a BRAC for the parameter Biospecimen Repository
     * 
     * @param admin
     *            User for whom the list is being retrieved
     * @param repositoryId
     *            The id of the repository that the returned orders must be for
     * @return List of all the BiospecimenOrder that are for the parameter repository
     */
    public Collection<BiospecimenOrder> getOrderListForBiorepositoryAdmin(User admin);

    /**
     * Method to approve a biospecimen order
     * 
     * @param brac
     *            User who is approving the order
     * @param order
     *            Order which is being approved
     * @return True if the order was successfully approved, false otherwise
     * @throws OrderManagerException 
     */
    public Boolean approveBiospecimenOrder(User brac, BiospecimenOrder order) throws OrderManagerException;

    /**
     * Method to approve a collection of orders
     * 
     * @param brac
     *            User who is approving the order list
     * @param orderList
     *            List of orders that are being approved
     * @return True if the orders were successfully approved, false otherwise
     * @throws OrderManagerException 
     */
    public Boolean approveBiospecimenOrderList(User brac, Collection<BiospecimenOrder> orderList) throws OrderManagerException;

    public Boolean rejectBiospecimenOrder(User brac, BiospecimenOrder order) throws OrderManagerException;

    public Boolean requestRevisionForOrder(User brac, BiospecimenOrder order) throws OrderManagerException;

    public Boolean requestRevisionForOrders(User brac, Collection<BiospecimenOrder> orderList) throws OrderManagerException;

    public Boolean rejectBiospecimenOrderList(User brac, Collection<BiospecimenOrder> orderList) throws OrderManagerException;

    /*
     * need to add a method to create and associate a comment with this order object
     */
    /**
     * Method to create a Comment using the provided message and the user and associate it with the order. This method
     * could be used at any stage of order creating however if this method is called before the order object is saved
     * then this method will not implicitly save the order object, saving need to be done separately using the
     * {@link OrderService.saveBiospecimenOrder} or {@link OrderService.validateAndPersist}
     * 
     * @param user
     *            The user who created the comment
     * @param message
     *            The text of the comment
     * @param order
     *            The order object with which the comment should be associated with
     * @return The comment created
     * @throws OrderManagerException 
     */
    public Comment addCommentToOrder(User user, String message, BiospecimenOrder order) throws OrderManagerException;

    /**
     * Method to remove an existing comment from an order
     * 
     * @param user
     *            The user who created the order
     * @param comment
     *            The comment which is to be removed
     * @param order
     *            The order to which the comment belong
     */
    public void removeCommentFromOrder(User user, Comment comment, BiospecimenOrder order);

    /**
     * Method to manually change the status of an order. This is to be used during the admin actions, in case the
     * administrator of a repository need to manually change the status of an order due to information received from
     * outside the system. This method should never be used during a normal PI's interaction with the system
     * 
     * @param user
     *            The administrator who is changing the order status
     * @param order
     *            The order whose status is to be changed
     * @param status
     *            The status that should be the new status of the order
     * @throws OrderManagerException 
     */
    public void setOrderStatus(User user, BiospecimenOrder order, OrderStatus status) throws OrderManagerException;
    

    /**
     * Pass through to DAO method to retrieve the Account for this User
     */
    public Account getAccount(User user);
    
    /*
     * This will take a list of biospecimenorders and remove them from the system
     */
    public void deleteBiospecimenOrder(List<BiospecimenOrder> orderRemovalList);
    
    /*
     * This will remove a document from an biospecimenorder.
     */
    public void deleteOrderManageDocument(OrderManagerDocument removeDocument);
    
}