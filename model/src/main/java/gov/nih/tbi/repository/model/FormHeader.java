
package gov.nih.tbi.repository.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="formHeader")
@XmlAccessorType(XmlAccessType.FIELD)
public class FormHeader implements Serializable {
    private static final long serialVersionUID = -5642239668249531054L;
    
	@XmlAttribute()
    private String name;
	
	private String version;
    
    @XmlElement(name="repeatableGroupHeader")
    private List<RepeatableGroupHeader> repeatableGroupHeaders;

	public FormHeader() {
		repeatableGroupHeaders = new ArrayList<RepeatableGroupHeader>();
	}

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<RepeatableGroupHeader> getRepeatableGroupHeaders()
    {

        return repeatableGroupHeaders;
    }

	public void setRepeatableGroupHeaders(List<RepeatableGroupHeader> repeatableGroupHeaders) {
		if (repeatableGroupHeaders != null) {
			this.repeatableGroupHeaders = repeatableGroupHeaders;
		} else {
			this.repeatableGroupHeaders = new ArrayList<RepeatableGroupHeader>(this.repeatableGroupHeaders.size());
		}
    }
    
	public void addRepeatableGroupHeader(RepeatableGroupHeader rgHeader) {
        repeatableGroupHeaders.add(rgHeader);
    }
}
