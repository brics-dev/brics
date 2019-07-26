
package gov.nih.tbi.ordermanagement.transfer;

import gov.nih.tbi.ordermanager.model.BiospecimenOrder;

/**
 * An implementation of the TransferInterface that will always fail. This will trigger rollbacks (for testing).
 * 
 * @author mvalei
 * 
 */
public class FailTestTransfer implements TransferInterface
{

    /* (non-Javadoc)
     * @see gov.nih.tbi.ordermanagement.transfer.TransferInterface#processAndSend(gov.nih.tbi.ordermanager.model.BiospecimenOrder)
     */
    @Override
    public boolean processAndSend(BiospecimenOrder order)
    {

        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see gov.nih.tbi.ordermanagement.transfer.TransferInterface#rollback(gov.nih.tbi.ordermanager.model.BiospecimenOrder)
     */
    @Override
    public boolean rollback(BiospecimenOrder order)
    {

        // TODO Auto-generated method stub
        return true;
    }

}
