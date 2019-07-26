package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.repository.model.SubmissionType;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;

@Entity
@Table(name = "STUDY_FORM")
public class StudyForm implements Serializable {

	private static final long serialVersionUID = 1876222241124295291L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDY_FORM_SEQ")
    @SequenceGenerator(name = "STUDY_FORM_SEQ", sequenceName = "STUDY_FORM_SEQ", allocationSize = 1)
    private Long id;
    
    @Expose
    @Column(name = "SHORT_NAME", nullable=false)
    private String shortName;
    
    @Column(name = "VERSION", nullable=false)
    private String version;
    
    @Expose
    @Column(name = "FORM_TITLE", nullable=false)
    private String title;
    
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "FORM_TYPE_ID")
	private SubmissionType submissionType;
    
	
	public StudyForm() {
		
	}
	
	public StudyForm(String shortName, String version, String title, SubmissionType submissionType) {
		setShortName(shortName);
		setVersion(version);
		setTitle(title);
		setSubmissionType(submissionType);
	}
	
	public StudyForm(String shortName, String version, String title, String submissionType) {
		setShortName(shortName);
		setVersion(version);
		setTitle(title);
		setSubmissionType(submissionType);
	}
	
	public StudyForm(StudyForm form) {
		setShortName(form.getShortName());
		setVersion(form.getVersion());
		setTitle(form.getTitle());
		setSubmissionType(form.getSubmissionType());
	}
	
	public StudyForm (JsonObject formJson) {
		setShortName(formJson.get("shortName").getAsString());
		setVersion(formJson.get("version").getAsString());
		setTitle(formJson.get("title").getAsString());
		setSubmissionType(formJson.get("submissionType").getAsLong());
	}
	
	public JsonObject toJson() {
		JsonObject output = new JsonObject();
		output.add("shortName", new JsonPrimitive(getShortName()));
		output.add("version", new JsonPrimitive(getVersion()));
		output.add("title", new JsonPrimitive(getTitle()));
		output.add("submissionType", new JsonPrimitive(getSubmissionType().getId()));
		return output;
	}

	public String getShortNameAndVersion() {
		return getShortName() + "V" + getVersion();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public SubmissionType getSubmissionType() {
		return submissionType;
	}

	public void setSubmissionType(SubmissionType submissionType) {
		this.submissionType = submissionType;
	}
	
	// I hate that I had to add all three types below.  For some reason, the form decides randomly
	// to interpret this parameter as a long or integer instead of always as a string.  That's doubly
	// interesting because I'm explicitly typecasting the submission type parameter to a Number
	// in the data object before sending it back here...
	public void setSubmissionType(String submissionTypeId) {
		setSubmissionType(Long.parseLong(submissionTypeId));
    }
	
	public void setSubmissionType(Long submissionTypeId) {
        this.submissionType = SubmissionType.getObject(submissionTypeId);
    }
	
	public void setSubmissionTypeId(String submissionTypeId) {
		setSubmissionType(Long.parseLong(submissionTypeId));
	}
	
	public boolean equals(Object o) {
		if (o instanceof StudyForm) {
			StudyForm sFo = (StudyForm) o;
			if (sFo.getShortName().equals(this.getShortName()) 
					&& sFo.getVersion().equals(this.getVersion())) {
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		int hashCode = 1;
		hashCode = 31 * hashCode + (shortName == null ? 0 : shortName.hashCode());
		hashCode = 31 * hashCode + (version == null ? 0 : version.hashCode());
		return hashCode;
	}
}
