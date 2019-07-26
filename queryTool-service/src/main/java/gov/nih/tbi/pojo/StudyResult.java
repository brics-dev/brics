package gov.nih.tbi.pojo;

import gov.nih.tbi.constants.QueryToolConstants;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
public class StudyResult extends BaseResult implements Serializable, Comparable<StudyResult> {
	private static final long serialVersionUID = 8576845173050227661L;

	@XmlElement(required = true)
	private String uri;

	@XmlAttribute()
	private Long id;

	private String title;
	private String pi;
	private String status;

	@XmlElementWrapper(name = "forms")
	@XmlElement(name = "formResult", type = FormResult.class)
	private List<FormResult> forms;

	public StudyResult() {
		uri = "";
		forms = new LinkedList<FormResult>();
	}

	/**
	 * Creates a new StudyResult instance that is a clone of the given object. When cloning the list of forms, a new
	 * list will be created and populated with new instances of any study elements.
	 * 
	 * @param base - The object to be copied.
	 * @throws NullPointerException If the argument is null.
	 */
	public StudyResult(StudyResult base) throws NullPointerException {
		if (base == null) {
			throw new NullPointerException("The base object cannot be null.");
		}

		this.uri = base.uri;
		this.id = base.id;
		this.title = base.title;
		this.pi = base.pi;
		this.status = base.status;

		// Clone the forms list.
		this.forms = new LinkedList<FormResult>();

		for (FormResult form : base.forms) {
			this.forms.add(new FormResult(form));
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPi() {
		return pi;
	}

	public void setPi(String pi) {
		this.pi = pi;
	}

	public List<FormResult> getForms() {
		return forms;
	}

	public void setForms(List<FormResult> formsToAdd) {
		this.forms.clear();

		if (formsToAdd != null) {
			this.forms.addAll(formsToAdd);
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String getClassUri() {
		return QueryToolConstants.STUDY_URI;
	}

	public int getNumberOfForms() {
		return (forms == null) ? 0 : forms.size();
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public void setUri(String uri) {
		if (uri != null) {
			this.uri = uri;
		} else {
			this.uri = "";
		}
	}

	/**
	 * Implementing hash code generation support of any StudyResult instances. The generated hash code will only be
	 * based on hashing the URI.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + ((uri == null) ? 0 : uri.hashCode());

		return result;
	}

	/**
	 * Once it is determined that the given object is an instance of StudyResult, the equality test will only compare
	 * the URIs of both instances.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (obj instanceof StudyResult) {
			StudyResult other = (StudyResult) obj;

			return this.uri.equals(other.uri);
		}

		return false;
	}

	@Override
	public int compareTo(StudyResult o) {
		if (o == null) {
			return 1;
		} else if (this == o) {
			return 0;
		} else if (this.title == null) {
			return -1;
		} else if (o.title == null) {
			return 1;
		} else {
			return this.title.compareToIgnoreCase(o.title);
		}
	}
}
