
package gov.nih.tbi.ordermanagement.transfer;

import gov.nih.tbi.ordermanager.model.BiospecimenOrder;

public interface TransferInterface
{

    /**
     * Send an order to a repository. Returns true if the process is successful and false if the transfer fails.
     * 
     * @param order
     * @return
     */
    public boolean processAndSend(BiospecimenOrder order);

    /**
     * Undoes a repository submission. Returns true if successful, and false if a failure occurs.
     * 
     * @return
     */
    public boolean rollback(BiospecimenOrder order);
}
