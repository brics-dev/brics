package gov.nih.nichd.ctdb.emailtrigger.domain;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Mar 22, 2007
 * Time: 8:24:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class SentEmail extends EmailTrigger {
	private static final long serialVersionUID = -3862324829449672184L;
	
	private Date dateSent;
    private String triggeredAnswer;
    private int patientId;
    private int formId;
    private int intervalId;
    private Date visitdate;

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public String getTriggeredAnswer() {
        return triggeredAnswer;
    }

    public void setTriggeredAnswer(String triggeredAnswer) {
        this.triggeredAnswer = triggeredAnswer;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public int getIntervalId() {
        return intervalId;
    }

    public void setIntervalId(int intervalId) {
        this.intervalId = intervalId;
    }

    public Date getVisitdate() {
        return visitdate;
    }

    public void setVisitdate(Date visitdate) {
        this.visitdate = visitdate;
    }
}
