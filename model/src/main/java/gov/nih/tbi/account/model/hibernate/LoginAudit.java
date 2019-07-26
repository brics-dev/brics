package gov.nih.tbi.account.model.hibernate;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "LOGIN_AUDIT")
@XmlRootElement(name = "loginAudit")
@XmlAccessorType(XmlAccessType.FIELD)
public class LoginAudit
{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOGIN_AUDIT_SEQ")
    @SequenceGenerator(name = "LOGIN_AUDIT_SEQ", sequenceName = "LOGIN_AUDIT_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "ACCOUNT_ID")
    private Long accountId;

    @Column(name = "EVENT_DATE")
    private Date eventDate;

    /**
     * Default, blank constructor
     */
    public LoginAudit()
    {

    }

    /**
     * Full constructor for all fields
     * 
     * @param accountId
     * @param eventDate
     */
    public LoginAudit(Long accountId, Date eventDate)
    {

        this.accountId = accountId;
        this.eventDate = eventDate;
    }

    public Long getAccountId()
    {

        return accountId;
    }

    public void setAccountId(Long accountId)
    {

        this.accountId = accountId;
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
