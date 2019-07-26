
package gov.nih.tbi.ordermanager.model;

import gov.nih.tbi.commons.model.hibernate.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 
 * @author vpacha
 * 
 */
@Entity
@Table(name = "Item_Queue")
public class ItemQueue implements Serializable
{

    private static final long serialVersionUID = 595521304077177082L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ITEM_QUEUE_SEQ")
    @SequenceGenerator(name = "ITEM_QUEUE_SEQ", sequenceName = "ITEM_QUEUE_SEQ", allocationSize = 1)
    private Long id;
    @OneToOne(targetEntity = User.class, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "User_ID")
    private User user;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "itemQueue", targetEntity = BiospecimenItem.class, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<BiospecimenItem> items;

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public List<BiospecimenItem> getItems()
    {
    	if(items == null) {
    		return new ArrayList<BiospecimenItem>();
    		
    	}
        return items;
    }

    public void setItems(List<BiospecimenItem> items)
    {

        this.items = items;
    }

    public User getUser()
    {

        return this.user;
    }

    public void setUser(User user)
    {

        this.user = user;
    }
}
