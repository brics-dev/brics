
package gov.nih.tbi.ordermanager.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.Comment;

import java.util.List;

public interface CommentDao extends GenericDao<Comment, Long>
{

    public List<Comment> findByOrder(BiospecimenOrder order);
}
