package gov.nih.tbi.query.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author rtromb
 * A response object wrapper for the rbox prototype webservice.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RboxResponse {
	
	@XmlElement
	public String consoleOutput;
	
	@XmlElement
	public String graphImage;

	
	public String getConsoleOutput() {
		return consoleOutput;
	}

	public void setConsoleOutput(String consoleOutput) {
		this.consoleOutput = consoleOutput;
	}

	public String getGraphImage() {
		return graphImage;
	}

	public void setGraphImage(String graphImage) {
		this.graphImage = graphImage;
	}
	
	@Override
	public String toString() {
		return "RboxResponse [consoleOutput=" + consoleOutput + ", graphImage=" + graphImage + "]";
	}

}
