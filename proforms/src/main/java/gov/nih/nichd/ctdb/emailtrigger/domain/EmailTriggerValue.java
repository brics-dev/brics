package gov.nih.nichd.ctdb.emailtrigger.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

@XmlAccessorType(XmlAccessType.FIELD)
public class EmailTriggerValue extends CtdbDomainObject {
	private static final long serialVersionUID = -6404769313359912701L;

	private String answer;
	private String triggerCondition;

	public EmailTriggerValue(String answer, String triggerCond) {
		this.setAnswer(answer);
		this.setTriggerCondition(triggerCond);
	}

	public EmailTriggerValue() {}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getTriggerCondition() {
		return triggerCondition;
	}

	public void setTriggerCondition(String triggerCondition) {
		this.triggerCondition = triggerCondition;
	}

	public Document toXML() throws TransformationException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Not Implemented for Email Trigger");
	}
}
