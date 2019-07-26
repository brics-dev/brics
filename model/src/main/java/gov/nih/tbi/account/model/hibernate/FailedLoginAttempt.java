
package gov.nih.tbi.account.model.hibernate;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Failed Login Attempt join class
 * 
 * @author Andrew Johnson
 * 
 */
@Entity
@Table(name = "FAILED_LOGIN_ATTEMPT")
public class FailedLoginAttempt implements Serializable
{

    /**********************************************************************/

    /**
	 * 
	 */
    private static final long serialVersionUID = 6368002862243509095L;

    /**********************************************************************/
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FAILED_LOGIN_ATTEMPT_SEQ")
    @SequenceGenerator(name = "FAILED_LOGIN_ATTEMPT_SEQ", sequenceName = "FAILED_LOGIN_ATTEMPT_SEQ", allocationSize = 1)
    private Long id;

    @OneToOne(cascade = { CascadeType.DETACH })
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @Column(name = "EVENT_DATE")
    private Date eventDate;

    /**********************************************************************/

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public Account getAccount()
    {

        return account;
    }

    public void setAccount(Account account)
    {

        this.account = account;
    }

    public Date getEventDate()
    {

        return eventDate;
    }

    public void setEventDate(Date eventDate)
    {

        this.eventDate = eventDate;
    }

}
