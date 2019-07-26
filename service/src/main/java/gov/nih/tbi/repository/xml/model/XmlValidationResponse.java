package gov.nih.tbi.repository.xml.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "validationResponse")
public class XmlValidationResponse {
	private boolean valid;
	private String reason;
	private List<String> errors;

	/**
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}
	/**
	 * @param valid the valid to set
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	/**
	 * @return the errors
	 */
	public List<String> getErrors() {
		return errors;
	}
	/**
	 * @param errors the errors to set
	 */
	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	public String toXMLString() {
		// String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
		String s = "";
		s = s.concat("<validationResponse>\n");
		s = s.concat("\t<valid>" + valid + "</valid>\n");
		if (reason != null) {
			s = s.concat("\t<reason>" + reason + "</reason>\n");
		}
		if (errors != null && !errors.isEmpty()) {
			s = s.concat("\t<errors>\n");
			for (String e : errors) {
				s = s.concat("\t\t<error>\n");
				s = s.concat("\t\t\t" + e + "\n");
				s = s.concat("\t\t</error>\n");
			}
			s = s.concat("\t</errors>\n");
		}
		s = s.concat("</validationResponse>");
		return s;
	}
}
