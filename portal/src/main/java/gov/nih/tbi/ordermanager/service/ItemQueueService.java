
package gov.nih.tbi.ordermanager.service;

import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.model.ItemQueue;

public interface ItemQueueService
{

    /*
     * let's map out what work should be done in the item queue  service,
     * - retrieving the item queue object associated with this user
     * - deleting an item from the queue
     * what else??
     * can't think of anything else other than the basic crud operations
     * definitely not gonna put the count related operation in the queue service, they are going to go in the order service
     */
    /**
     * Method to retrieve the {@link gov.nih.tbi.ordermanger.model.ItemQueue} object for this user.
     * 
     * @param user
     *            User whose queue should be retrieved
     * @return The item queue object for the param user
     */
    public ItemQueue getItemQueueForUser(User user);

    /**
     * Method to create and add a new {@link BiospecimenItem} object to the user's {@link ItemQueue}. If no queue
     * currently exist for this user and a new queue will be created and the item will be added to that queue. If an
     * item already exist in this user's queue with similar coriell id AND repository id then a new one will not be
     * added and false will be returned.
     * 
     * @param coriellId
     *            The coriell id that will be assigned to the item that will be created
     * @param repoId
     *            The repository id that will be assigned to the item that will be created
     * @param user
     *            The user that will be assigned to the item what will be created
     * @return True if the object was properly created and added to the user's queue, false otherwise
     */
    public Boolean addItemToUserQueue(User user, String data);

    /**
     * Method to remove a {@link BiospecimenItem} from the user's {@link ItemQueue}. Method will return true if the item
     * currently exist in the user's queue and is properly removed. If the object doesn't exist in the user's queue or
     * if the item couldn't be removed from the queue then the method will return FALSE
     * 
     * @param item
     *            The item to be removed from the user's queue
     * @param user
     *            The user whose queue the item will be removed from
     * @return TRUE if the item is properly found and removed from the user's queue, false otherwise
     */
    public Boolean removeItemFromUserQueue(BiospecimenItem item, User user);

    /**
     * Method to remove all items from the user's {@link ItemQueue}
     * 
     * @param user
     *            The user whose item queue all the items will be removed from.
     * @return TRUE if atleast one item exist in the user's queue and is removed. FALSE otherwise
     */
    public Boolean removeAllItemsFromUserQueue(User user);

    /*
     * can't think of why i added this here
     */
    // public Boolean checkIfItemExistInUserQueue(BiospecimenItem item, User user);
}
