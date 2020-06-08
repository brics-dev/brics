package gov.nih.cit.brics.file.data.entity.dictionary;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;

@Entity
@Table(name = "DATA_ELEMENT")
public class BasicDataElement {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATA_ELEMENT_SEQ")
	@SequenceGenerator(name = "DATA_ELEMENT_SEQ", sequenceName = "DATA_ELEMENT_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "ELEMENT_NAME")
	private String name;

	@Column(name = "VERSION")
	private String version;

	@Column(name = "ELEMENT_SIZE")
	private Integer size;

	@Column(name = "MAXIMUM_VALUE")
	private String maximumValue;

	@Column(name = "MINIMUM_VALUE")
	private String minimumValue;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "ELEMENT_TYPE_ID")
	private DataType type;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "INPUT_RESTRICTION_ID")
	private InputRestrictions restrictions;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "DATA_ELEMENT_STATUS_ID")
	private DataElementStatus status;

	@Column(name = "DATE_CREATED")
	private LocalDateTime dateCreated;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(String maximumValue) {
		this.maximumValue = maximumValue;
	}

	public String getMinimumValue() {
		return minimumValue;
	}

	public void setMinimumValue(String minimumValue) {
		this.minimumValue = minimumValue;
	}

	public DataType getType() {
		return type;
	}

	public void setType(DataType type) {
		this.type = type;
	}

	public InputRestrictions getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(InputRestrictions restrictions) {
		this.restrictions = restrictions;
	}

	public DataElementStatus getStatus() {
		return status;
	}

	public void setStatus(DataElementStatus status) {
		this.status = status;
	}

	public LocalDateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(LocalDateTime dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dateCreated, id, maximumValue, minimumValue, name, restrictions, size, status, type,
				version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BasicDataElement)) {
			return false;
		}
		BasicDataElement other = (BasicDataElement) obj;
		return Objects.equals(dateCreated, other.dateCreated) && Objects.equals(id, other.id)
				&& Objects.equals(maximumValue, other.maximumValue) && Objects.equals(minimumValue, other.minimumValue)
				&& Objects.equals(name, other.name) && restrictions == other.restrictions
				&& Objects.equals(size, other.size) && status == other.status && type == other.type
				&& Objects.equals(version, other.version);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicDataElement [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", version=");
		builder.append(version);
		builder.append(", type=");
		builder.append(type);
		builder.append(", status=");
		builder.append(status);
		builder.append("]");
		return builder.toString();
	}
}
