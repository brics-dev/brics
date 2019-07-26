package gov.nih.nichd.ctdb.response.domain;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Apr 4, 2008
 * Time: 9:01:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataEntryHeader {

    public static final String ORIGIN_CERTIFY = "_origin_certify";
    public static final String ORIGIN_DATAENTRY = "_origin_dataentry";

    private String patientDisplay;
    private String intervalDisplay;
    private String dateDisplay;
    private String dateDisplay2;
    private String scheduledVisitDateDisplay;
    private String formDisplay;
    private String origin;
    private String entry;
    private String entry2;
    private String studyName;
    private String studyNum;
    private String finalLockDate;
    private String lockDate;
    private String lockDate2;
    private String guid;
    private int singleDoubleKeyFlag;;
    
    public int getSingleDoubleKeyFlag() {
		return singleDoubleKeyFlag;
	}

	public void setSingleDoubleKeyFlag(int singleDoubleKeyFlag) {
		this.singleDoubleKeyFlag = singleDoubleKeyFlag;
	}

	public String getPatientDisplay() {
        return patientDisplay;
    }

    public void setPatientDisplay(String patientDisplay) {
        this.patientDisplay = patientDisplay;
    }

    public String getIntervalDisplay() {
        return intervalDisplay;
    }

    public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public void setIntervalDisplay(String intervalDisplay) {
        this.intervalDisplay = intervalDisplay;
    }

    public String getDateDisplay() {
        return dateDisplay;
    }

    public void setDateDisplay(String dateDisplay) {
        this.dateDisplay = dateDisplay;
    }
    
    

    public String getScheduledVisitDateDisplay() {
		return scheduledVisitDateDisplay;
	}

	public void setScheduledVisitDateDisplay(String scheduledVisitDateDisplay) {
		this.scheduledVisitDateDisplay = scheduledVisitDateDisplay;
	}

	public String getFormDisplay() {
        return formDisplay;
    }

    public void setFormDisplay(String formDisplay) {
        this.formDisplay = formDisplay;
    }


    public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}

	public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public String getStudyNum() {
		return studyNum;
	}

	public void setStudyNum(String studyNum) {
		this.studyNum = studyNum;
	}

	public String getFinalLockDate() {
		return finalLockDate;
	}

	public void setFinalLockDate(String finalLockDate) {
		this.finalLockDate = finalLockDate;
	}

	public String getEntry2() {
		return entry2;
	}

	public void setEntry2(String entry2) {
		this.entry2 = entry2;
	}

	public String getLockDate() {
		return lockDate;
	}

	public void setLockDate(String lockDate) {
		this.lockDate = lockDate;
	}

	public String getDateDisplay2() {
		return dateDisplay2;
	}

	public void setDateDisplay2(String dateDisplay2) {
		this.dateDisplay2 = dateDisplay2;
	}

	public String getLockDate2() {
		return lockDate2;
	}

	public void setLockDate2(String lockDate2) {
		this.lockDate2 = lockDate2;
	}   
}
