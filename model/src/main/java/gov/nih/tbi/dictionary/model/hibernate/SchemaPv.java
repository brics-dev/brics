package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name = "SCHEMA_PV")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlRootElement(name = "schemaPv")
@XmlAccessorType(XmlAccessType.FIELD)
public class SchemaPv implements Serializable {

	private static final long serialVersionUID = 823282123534131195L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SCHEMA_PV_SEQ")
	@SequenceGenerator(name = "SCHEMA_PV_SEQ", sequenceName = "SCHEMA_PV_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "SCHEMA_PV_ID")
	private String schemaPvId;

	@Column(name = "SCHEMA_PV")
	private String permissibleValue;

	@Column(name = "SCHEMA_DE_ID", nullable = false)
	private String schemaDeId;

	@ManyToOne(targetEntity = Schema.class, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "SCHEMA_ID", nullable = false)
	private Schema schema;

	@XmlTransient
	@ManyToOne(targetEntity = ValueRange.class)
	@JoinColumn(name = "VALUE_RANGE_ID")
	private ValueRange valueRange;

	@XmlTransient
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "DATA_ELEMENT_ID", nullable = false)
	private StructuralDataElement dataElement;

	@Column(name = "SCHEMA_DE_NAME", nullable = false)
	private String schemaDataElementName;

	public String getSchemaDataElementName() {
		return schemaDataElementName;
	}

	public void setSchemaDataElementName(String schemaDataElementName) {
		this.schemaDataElementName = schemaDataElementName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public StructuralDataElement getDataElement() {
		return dataElement;
	}

	public void setDataElement(StructuralDataElement dataElement) {
		this.dataElement = dataElement;
	}

	public String getSchemaPvId() {
		return schemaPvId;
	}

	public void setSchemaPvId(String schemaPvId) {
		this.schemaPvId = schemaPvId;
	}

	public String getPermissibleValue() {
		return permissibleValue;
	}

	public void setPermissibleValue(String permissibleValue) {
		this.permissibleValue = permissibleValue;
	}

	public String getSchemaDeId() {
		return schemaDeId;
	}

	public void setSchemaDeId(String schemaDeId) {
		this.schemaDeId = schemaDeId;
	}

	public Schema getSchema() {
		return schema;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	public ValueRange getValueRange() {
		return valueRange;
	}

	public void setValueRange(ValueRange valueRange) {
		this.valueRange = valueRange;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((permissibleValue == null) ? 0 : permissibleValue.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		result = prime * result + ((schemaDataElementName == null) ? 0 : schemaDataElementName.hashCode());
		result = prime * result + ((schemaDeId == null) ? 0 : schemaDeId.hashCode());
		result = prime * result + ((schemaPvId == null) ? 0 : schemaPvId.hashCode());
		result = prime * result + ((valueRange == null) ? 0 : valueRange.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SchemaPv other = (SchemaPv) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (permissibleValue == null) {
			if (other.permissibleValue != null)
				return false;
		} else if (!permissibleValue.equals(other.permissibleValue))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		if (schemaDataElementName == null) {
			if (other.schemaDataElementName != null)
				return false;
		} else if (!schemaDataElementName.equals(other.schemaDataElementName))
			return false;
		if (schemaDeId == null) {
			if (other.schemaDeId != null)
				return false;
		} else if (!schemaDeId.equals(other.schemaDeId))
			return false;
		if (schemaPvId == null) {
			if (other.schemaPvId != null)
				return false;
		} else if (!schemaPvId.equals(other.schemaPvId))
			return false;
		if (valueRange == null) {
			if (other.valueRange != null)
				return false;
		} else if (!valueRange.equals(other.valueRange))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SchemaPv [id=" + id + ", schemaPvId=" + schemaPvId + ", permissibleValue=" + permissibleValue
				+ ", schemaDeId=" + schemaDeId + ", schema=" + schema + ", valueRange=" + valueRange
				+ ", schemaDataElementName=" + schemaDataElementName + "]";
	}
}
