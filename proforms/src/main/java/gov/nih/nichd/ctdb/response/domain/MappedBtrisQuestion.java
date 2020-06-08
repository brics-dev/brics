package gov.nih.nichd.ctdb.response.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class MappedBtrisQuestion extends CtdbDomainObject {
	private static final long serialVersionUID = 2955962442255393014L;

	public String sectionQuestionId;
	public String sectionName;
	public String questionName;

	public MappedBtrisQuestion() {
		super();
	}

	public String getSectionQuestionId() {
		return this.sectionQuestionId;
	}

	public void setSectionQuestionId(String sectionQuestionId) {
		this.sectionQuestionId = sectionQuestionId;
	}

	public String getSectionName() {
		return this.sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public String getQuestionName() {
		return this.questionName;
	}

	public void setQuestionName(String questionName) {
		this.questionName = questionName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof MappedBtrisQuestion) {
			MappedBtrisQuestion other = (MappedBtrisQuestion) obj;
			return sectionQuestionId.equals(other.sectionQuestionId) && sectionName.equals(other.sectionName)
					&& questionName == other.questionName;
		}
		return false;
	}

	@Override
	public Document toXML() throws TransformationException {
		throw new TransformationException(
				"Unable to transform object " + this.getClass().getName() + " with id = " + this.getId());
	}
}
