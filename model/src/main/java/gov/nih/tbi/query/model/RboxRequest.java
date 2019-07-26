package gov.nih.tbi.query.model;

import javax.xml.bind.annotation.XmlAccessType;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author rtromb
 * A request object wrapper for the rbox prototype webservice.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RboxRequest{
	
	@XmlElement
	public String script;
	
	@XmlElement
	public String dataBytes;

	
	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getDataBytes() {
		return dataBytes;
	}

	public void setDataBytes(String data) {
		this.dataBytes = data;
	}
	
	@Override
	public String toString() {
		return "RboxRequest [script=" + script + ", dataBytes=" + dataBytes + "]";
	}


}
