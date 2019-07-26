package gov.nih.tbi.dictionary.model.restful;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAnyElement;

/**
 * Created by amakar on 1/11/2017.
 */

//@XmlRootElement(name = "entityList")
//@XmlAccessorType(XmlAccessType.FIELD)
public class EntityList<T extends ListItem> implements Serializable {


    private static final long serialVersionUID = 5521291095378387338L;

    //@XmlElement(name = "item")
    @XmlAnyElement(lax=true)
    private List<T> itemList;
    private String type;


    public EntityList(String _type) {
        this();
        //this.itemList = new ArrayList<ListItem>();
        this.type = _type;
    }

    public EntityList() {
        this.itemList = new ArrayList<T>();
        this.type = null;
    }

    public void addEntity(ListableEntity<T> entity) {
        T item = entity.getListItem();
        this.itemList.add(item);
    }

    //@XmlAnyElement(lax=true)
    //public List<T> getItemList() {
    //    return this.itemList;
    //}

    public String getType() {
        return this.type;
    }
}
