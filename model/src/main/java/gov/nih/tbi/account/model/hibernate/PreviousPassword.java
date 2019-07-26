
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
 * Previous Password join table
 * 
 * @author Andrew Johnson
 */
@Entity
@Table(name = "PREVIOUS_PASSWORD")
public class PreviousPassword implements Serializable
{

    /**********************************************************************/

    /**
	 * 
	 */
    private static final long serialVersionUID = 3716092124345602305L;

    /**********************************************************************/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PREVIOUS_PASSWORD_SEQ")
    @SequenceGenerator(name = "PREVIOUS_PASSWORD_SEQ", sequenceName = "PREVIOUS_PASSWORD_SEQ", allocationSize = 1)
    private Long id;

    @OneToOne(cascade = { CascadeType.DETACH })
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @Column(name = "PASSWORD")
    private byte[] password;

    @Column(name = "EVENT_DATE")
    private Date eventDate;
    
    @Column(name = "SALT")
	private String salt;

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

    public byte[] getPassword()
    {

        return password;
    }

    public void setPassword(byte[] password)
    {

        this.password = password;
    }

    public Date getEventDate()
    {

        return eventDate;
    }

    public void setEventDate(Date eventDate)
    {

        this.eventDate = eventDate;
    }

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}
    
    

}
