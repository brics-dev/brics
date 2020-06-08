package gov.nih.nichd.ctdb.emailtrigger.domain;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Mar 6, 2007
 * Time: 7:26:18 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailTrigger extends CtdbDomainObject {
	private static final long serialVersionUID = -6404769313359912701L;
	
	private int questionattributesid = Integer.MIN_VALUE;
    private String toEmailAddress = "";
    public static final String fromEmailAddress = "no-reply@ctdb.nichd.nih.gov";
    private String ccEmailAddress = "";
    private String subject = "";
    private String body = "";
	// @XmlElementWrapper(name="Answers")
	// private List<String> triggerAnswers = new ArrayList<String>();
	// private String[] selectedAnswers = new String [0];
	@XmlElementWrapper(name = "TriggerValues")
	private Set<EmailTriggerValue> triggerValues = new HashSet<EmailTriggerValue>();
    private String[] selectedAnswers = new String [0];


    public int getQuestionattributesid() {
        return questionattributesid;
    }

    public void setQuestionattributesid(int questionattributesid) {
        this.questionattributesid = questionattributesid;
    }

    public String getToEmailAddress() {
        return toEmailAddress;
    }

    public void setToEmailAddress(String toEmailAddress) {
        this.toEmailAddress = toEmailAddress;
    }

    public String getCcEmailAddress() {
        return ccEmailAddress;
    }

    public void setCcEmailAddress(String ccEmailAddress) {
        this.ccEmailAddress = ccEmailAddress;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

	// public List <String> getTriggerAnswers() {
	// return triggerAnswers;
	// }
	//
	// public void setTriggerAnswers(List <String>triggerAnswers) {
	// this.triggerAnswers = triggerAnswers;
	// }

	public Set<EmailTriggerValue> getTriggerValues() {
		if (this.triggerValues == null) {
			this.triggerValues = new HashSet<EmailTriggerValue>();
		}
		return triggerValues;
	}

	public void setTriggerValues(Set<EmailTriggerValue> triggerValues) {
		this.triggerValues = triggerValues;
	}

	public void addToTriggerValues(EmailTriggerValue triggerValue) {
		getTriggerValues().add(triggerValue);
	}

    public String[] getSelectedAnswers() {
        return selectedAnswers;
    }

    public void setSelectedAnswers(String[] selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }

    public Document toXML() throws TransformationException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not Implemented for Email Trigger");
    }
}
