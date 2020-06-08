
package gov.nih.tbi.ordermanager.model.exception;

import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.OrderStatus;

/**
 * Exception class to indicate that an order is currently in an illegal state. The attributes order and targetStatus
 * should capture the order whose status is being tried to be changed and the targetStaus which is the target status
 * 
 * @author vpacha
 * 
 */
public class IllegalOrderStatusException extends Exception
{

    private BiospecimenOrder order;

    private OrderStatus targetStatus;

    private static final long serialVersionUID = 5945520455225006354L;

    public IllegalOrderStatusException(String message, BiospecimenOrder order, OrderStatus status)
    {

        super(message);
        this.order = order;
        this.targetStatus = status;

    }

    public IllegalOrderStatusException(String message, Throwable cause, BiospecimenOrder order, OrderStatus status)
    {

        super(message, cause);
        this.order = order;
        this.targetStatus = status;
    }

    public BiospecimenOrder getOrder()
    {

        return order;
    }

    public void setOrder(BiospecimenOrder order)
    {

        this.order = order;
    }

    public OrderStatus getTargetStatus()
    {

        return targetStatus;
    }

    public void setTargetStatus(OrderStatus targetStatus)
    {

        this.targetStatus = targetStatus;
    }
}
