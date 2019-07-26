
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.dictionary.model.hibernate.Condition;

import java.io.Serializable;

public class SessionCondition implements Serializable
{

    private static final long serialVersionUID = 6050789702147335046L;

    private Condition condition;

    public Condition getCondition()
    {

        return condition;
    }

    public void setCondition(Condition condition)
    {

        this.condition = condition;
    }
}
