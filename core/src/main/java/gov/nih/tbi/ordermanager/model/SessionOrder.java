
package gov.nih.tbi.ordermanager.model;

import java.io.Serializable;

public class SessionOrder implements Serializable
{

    private static final long serialVersionUID = 2737244925375846014L;
    private BiospecimenOrder order;
    private String comment;

    public SessionOrder()
    {

        super();
    }

    public BiospecimenOrder getOrder()
    {

        return order;
    }

    public void setOrder(BiospecimenOrder order)
    {

        this.order = order;
    }

    public String getComment()
    {

        return comment;
    }

    public void setComment(String comment)
    {

        this.comment = comment;
    }
}
