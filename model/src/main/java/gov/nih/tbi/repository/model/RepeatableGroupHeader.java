
package gov.nih.tbi.repository.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="repeatableGroupHeader")
@XmlAccessorType(XmlAccessType.FIELD)
public class RepeatableGroupHeader implements Serializable
{

    private static final long serialVersionUID = -7751886247495122901L;

	@XmlAttribute()
    private String name;

	@XmlAttribute()
    private List<String> dataElementHeaders;

	@XmlAttribute()
    private boolean doesRepeat;
    
	public RepeatableGroupHeader() {
		dataElementHeaders = new ArrayList<String>();
	}

    public boolean isDoesRepeat()
    {
    
        return doesRepeat;
    }

    public void setDoesRepeat(boolean doesRepeat)
    {
    
        this.doesRepeat = doesRepeat;
    }

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public List<String> getDataElementHeaders()
    {

        return dataElementHeaders;
    }

	public void setDataElementHeaders(List<String> dataElementHeaders) {
		if (dataElementHeaders != null) {
			this.dataElementHeaders = dataElementHeaders;
		} else {
			this.dataElementHeaders = new ArrayList<String>(this.dataElementHeaders.size());
		}
    }

	public void addDataElementHeader(String name) {
        dataElementHeaders.add(name);
    }
}
