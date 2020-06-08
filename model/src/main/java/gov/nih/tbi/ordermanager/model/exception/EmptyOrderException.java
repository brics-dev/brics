
package gov.nih.tbi.ordermanager.model.exception;

import gov.nih.tbi.ordermanager.model.BiospecimenOrder;

/**
 * Exception class to indicate the situation of trying to place an order with no items in it
 * 
 * @author vpacha
 * 
 */
public class EmptyOrderException extends Exception
{

    private static final long serialVersionUID = 7539894085890077802L;
    private BiospecimenOrder order;
    public EmptyOrderException(String message, BiospecimenOrder order)
    {

        super(message);
        this.order = order;
    }

    public EmptyOrderException(String message, Throwable cause, BiospecimenOrder order)
    {

        super(message, cause);
        this.order = order;
    }

    
    public BiospecimenOrder getOrder()
    {
    
        return order;
    }

    
    public void setOrder(BiospecimenOrder order)
    {
    
        this.order = order;
    }

}
