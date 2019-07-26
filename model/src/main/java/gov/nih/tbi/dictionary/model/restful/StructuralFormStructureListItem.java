package gov.nih.tbi.dictionary.model.restful;

import gov.nih.tbi.commons.model.StatusType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by amakar on 1/11/2017.
 */

@XmlRootElement(name = "formStructureItem")
@XmlAccessorType(XmlAccessType.FIELD)
public class StructuralFormStructureListItem extends ListItem {

    private String version;
    private StatusType status;
    private String organization;

    public StructuralFormStructureListItem(Long _id, String _name, String _version,
                                           StatusType _status, String _organization) {
        super(_id, _name);
        this.version = _version;
        this.status = _status;
        this.organization = _organization;
    }

    public StructuralFormStructureListItem() {
        super();
    }

    public String getVersion() {
        return version;
    }
}
