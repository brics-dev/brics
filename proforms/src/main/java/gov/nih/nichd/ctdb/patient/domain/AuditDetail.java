package gov.nih.nichd.ctdb.patient.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * class for subject change details for audit purpose
 *
 * @author Jeff Liu
 * @version 1.0
 */
public class AuditDetail extends CtdbDomainObject {
	private static final long serialVersionUID = 2088836933708652881L;

	private String fieldName;
	private String fieldValueOriginal;
	private String fieldValueUpdated;
	private String reasonToChange;

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldValueOriginal() {
		return fieldValueOriginal;
	}

	public void setFieldValueOriginal(String fieldValueOriginal) {
		this.fieldValueOriginal = fieldValueOriginal;
	}

	public String getFieldValueUpdated() {
		return fieldValueUpdated;
	}

	public void setFieldValueUpdated(String fieldValueUpdated) {
		this.fieldValueUpdated = fieldValueUpdated;
	}

	public String getReasonToChange() {
		return reasonToChange;
	}

	public void setReasonToChange(String reasonToChange) {
		this.reasonToChange = reasonToChange;
	}

	@Override
	public Document toXML() throws TransformationException {
		throw new UnsupportedOperationException("toXML() is not supported in AuditDetail.");
	}
}