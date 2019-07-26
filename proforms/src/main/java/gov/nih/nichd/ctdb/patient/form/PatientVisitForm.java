package gov.nih.nichd.ctdb.patient.form;

import gov.nih.nichd.ctdb.common.CtdbForm;
/**
 * The PatientForm represents the Java class behind the HTML
 * for nichd ctdb patients
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientVisitForm extends CtdbForm
{
    private int patientId = Integer.MIN_VALUE;
    private int protocolId = Integer.MIN_VALUE;
    private int visitTypeId = Integer.MIN_VALUE;

	private String visitDate = null;
    private String firstName = null;
	private String lastName = null;

    public int getVisitTypeId() {
		return visitTypeId;
	}

	public void setVisitTypeId(int visitTypeId) {
		this.visitTypeId = visitTypeId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}


	public int getPatientId() {
		return patientId;
	}

	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}

	public String getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(String visitDate) {
		this.visitDate = visitDate;
	}
	public void reset(){
		this.action = "add";
		this.visitDate=null;
		this.visitTypeId=Integer.MIN_VALUE;
	}

	public int getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(int protocolId) {
		this.protocolId = protocolId;
	}
}