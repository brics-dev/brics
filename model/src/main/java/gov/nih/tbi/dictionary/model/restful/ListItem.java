package gov.nih.tbi.dictionary.model.restful;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by amakar on 1/11/2017.
 */

@XmlRootElement(name = "genericListItem")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ListItem implements Serializable {

    private static final long serialVersionUID = 1611387829841666585L;

    private Long id;
    private String name;

    public ListItem(Long _id, String _name) {
        this.id = _id;
        this.name = _name;
    }

    public ListItem() {
        this.id = null;
        this.name = null;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
