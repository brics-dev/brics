package gov.nih.nichd.ctdb.attachments.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jan 3, 2007
 * Time: 2:23:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class AttachmentCategory extends CtdbDomainObject {
	private static final long serialVersionUID = 8177238856697786882L;
	
	private String name;
    private String description;
    private int protocolId;
    private CtdbLookup type;

	public AttachmentCategory() {}

    public AttachmentCategory (int categoryId) {
        this.setId(categoryId);
    }
    public AttachmentCategory (int categoryId, String desc, String name, int protocolid) {
        setId (categoryId);
        setDescription(desc);
        setName(name);
        setProtocolId(protocolid);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public CtdbLookup getType() {
        return type;
    }

    public void setType(CtdbLookup type) {
        this.type = type;
    }


        public Document toXML() throws TransformationException, UnsupportedOperationException {
	    throw new UnsupportedOperationException("toXML() is not supported in AttachmentCategory.");
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + protocolId;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttachmentCategory other = (AttachmentCategory) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (protocolId != other.protocolId)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}


}
