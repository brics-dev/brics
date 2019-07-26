
package gov.nih.tbi.account.model;

import gov.nih.tbi.account.model.hibernate.Account;

import java.io.Serializable;

/**
 * Stores the session account and user.
 * 
 * @author Francis Chen
 */
public class SessionAccount implements Serializable
{

    private static final long serialVersionUID = -347322149735726439L;

    /**********************************************************/

    Account account;
    Long diseaseId;

    /*********************************************************/

    public SessionAccount()
    {

        super();
    }

    public Account getAccount()
    {

        return account;
    }

    public void setAccount(Account account)
    {

        this.account = account;
    }

    public Long getDiseaseId()
    {

        return diseaseId;
    }

    public void setDiseaseId(Long diseaseId)
    {

        this.diseaseId = diseaseId;
    }
}
