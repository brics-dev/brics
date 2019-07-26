
package gov.nih.tbi.ordermanager.model;

import gov.nih.tbi.commons.model.hibernate.User;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Class to capture the detail of comments left by the PI and BRAC users in the order manager component
 * 
 * @author vpacha
 * 
 */
@Entity
@Table(name = "Comment")
public class Comment implements Serializable
{

    private static final long serialVersionUID = 5860424740510429353L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COMMENT_SEQ")
    @SequenceGenerator(name = "COMMENT_SEQ", sequenceName = "COMMENT_SEQ", allocationSize = 1)
    private Long id;

    /*
     * need to associate this comment object to a user ( this is going to be many to one relation )
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User_ID")
    private User user;

    /*
     * need to capture the date and time 
     */
    @Column(name = "Date")
    private Date date;

    /*
     * need to capture the message as string
     */
    @Column(name = "Message")
    private String message;

    @ManyToOne(targetEntity = BiospecimenOrder.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "Biospecimen_Order_ID")
    private BiospecimenOrder biospecimenOrder;

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public User getUser()
    {

        return user;
    }

    public void setUser(User user)
    {

        this.user = user;
    }

    public Date getDate()
    {

        return date;
    }

    public void setDate(Date date)
    {

        this.date = date;
    }

    public String getMessage()
    {

        return message;
    }

    public void setMessage(String message)
    {

        this.message = message;
    }

    public BiospecimenOrder getBiospecimenOrder()
    {

        return biospecimenOrder;
    }

    public void setBiospecimenOrder(BiospecimenOrder biospecimenOrder)
    {

        this.biospecimenOrder = biospecimenOrder;
    }

    @Override
    public int hashCode()
    {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((biospecimenOrder == null) ? 0 : biospecimenOrder.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Comment other = (Comment) obj;
        if (biospecimenOrder == null)
        {
            if (other.biospecimenOrder != null)
                return false;
        }
        else
            if (!biospecimenOrder.equals(other.biospecimenOrder))
                return false;
        if (date == null)
        {
            if (other.date != null)
                return false;
        }
        else
            if (!date.equals(other.date))
                return false;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else
            if (!id.equals(other.id))
                return false;
        if (message == null)
        {
            if (other.message != null)
                return false;
        }
        else
            if (!message.equals(other.message))
                return false;
        if (user == null)
        {
            if (other.user != null)
                return false;
        }
        else
            if (!user.equals(other.user))
                return false;
        return true;
    }
}
