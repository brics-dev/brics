
package gov.nih.tbi.ordermanager.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.ordermanager.model.ItemQueue;

public interface ItemQueueDao extends GenericDao<ItemQueue, Long>
{

    public ItemQueue findByUser(User user);
}
