
package gov.nih.tbi.ordermanager.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.OrderManagerDocument;

import java.util.List;

public interface OrderManagerDocumentDao extends GenericDao<OrderManagerDocument, Long>
{

    public List<OrderManagerDocument> getDocumentsForOrder(BiospecimenOrder order);
}
