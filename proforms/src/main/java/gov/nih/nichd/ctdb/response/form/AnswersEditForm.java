package gov.nih.nichd.ctdb.response.form;

import gov.nih.nichd.ctdb.common.CtdbForm;


/**
 * The ResolveForm represents the Java class behind the HTML
 * for nichd ctdb data entry resolve page
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class AnswersEditForm extends CtdbForm
{

    private String visitDate;
    private int intervalId;
    private int patientId;
    private String intervalChangeReason;
    private String patientChangeReason;
    private String visitDateChangeReason;
    private boolean attachFiles;

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public int getIntervalId() {
        return intervalId;
    }

    public void setIntervalId(int intervalId) {
        this.intervalId = intervalId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getIntervalChangeReason() {
        return intervalChangeReason;
    }

    public void setIntervalChangeReason(String intervalChangeReason) {
        this.intervalChangeReason = intervalChangeReason;
    }

    public String getPatientChangeReason() {
        return patientChangeReason;
    }

    public void setPatientChangeReason(String patientChangeReason) {
        this.patientChangeReason = patientChangeReason;
    }

    public String getVisitDateChangeReason() {
        return visitDateChangeReason;
    }

    public void setVisitDateChangeReason(String visitDateChangeReason) {
        this.visitDateChangeReason = visitDateChangeReason;
    }

    public boolean isAttachFiles() {
        return attachFiles;
    }

    public void setAttachFiles(boolean attachFiles) {
        this.attachFiles = attachFiles;
    }
}
