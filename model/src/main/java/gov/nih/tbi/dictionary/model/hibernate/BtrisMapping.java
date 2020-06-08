package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

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
@Table(name = "BTRIS_MAPPING")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlRootElement(name = "BtrisMapping")
@XmlAccessorType(XmlAccessType.FIELD)
public class BtrisMapping implements Serializable {

	private static final long serialVersionUID = 823282123534131195L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BTRIS_MAPPING_SEQ")
	@SequenceGenerator(name = "BTRIS_MAPPING_SEQ", sequenceName = "BTRIS_MAPPING_SEQ", allocationSize = 1)
	private Long id;

	@XmlTransient
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "BRICS_DE_ID", nullable = false)
	private StructuralDataElement bricsDataElement;

	@Column(name = "BRICS_DE_NAME", nullable = false)
	private String bricsDEName;

	@XmlTransient
	@ManyToOne(targetEntity = ValueRange.class)
	@JoinColumn(name = "BRICS_DE_VALUE_RANGE_ID")
	private ValueRange bricsValueRange;

	@Column(name = "BTRIS_OBSERVATION_NAME", nullable = false)
	private String btrisObservationName;

	@Column(name = "BTRIS_RED_CONCEPT_CODE")
	private String btrisRedCode;

	@Column(name = "BTRIS_SPECIMEN_TYPE")
	private String btrisSpecimenType;

	@Column(name = "BTRIS_TABLE")
	private String btrisTable;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public StructuralDataElement getBricsDataElement() {
		return bricsDataElement;
	}

	public void setBricsDataElement(StructuralDataElement bricsDataElement) {
		this.bricsDataElement = bricsDataElement;
	}

	public String getBricsDataElementName() {
		return bricsDEName;
	}

	public void setBricsDataElementName(String bricsDEName) {
		this.bricsDEName = bricsDEName;
	}

	public ValueRange getBricsValueRange() {
		return bricsValueRange;
	}

	public void setBricsValueRange(ValueRange bricsValueRange) {
		this.bricsValueRange = bricsValueRange;
	}

	public String getBtrisObservationName() {
		return btrisObservationName;
	}

	public void setBtrisObservationName(String btrisObservationName) {
		this.btrisObservationName = btrisObservationName;
	}

	public String getBtrisRedCode() {
		return btrisRedCode;
	}

	public void setBtrisRedCode(String btrisRedCode) {
		this.btrisRedCode = btrisRedCode;
	}

	public String getBtrisSpecimenType() {
		return btrisSpecimenType;
	}

	public void setBtrisSpecimenType(String btrisSpecimenType) {
		this.btrisSpecimenType = btrisSpecimenType;
	}


	public String getBtrisTable() {
		return btrisTable;
	}

	public void setBtrisTable(String btrisTable) {
		this.btrisTable = btrisTable;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((bricsDataElement == null) ? 0 : bricsDataElement.hashCode());
		result = prime * result + ((bricsDEName == null) ? 0 : bricsDEName.hashCode());
		result = prime * result + ((bricsValueRange == null) ? 0 : bricsValueRange.hashCode());
		result = prime * result + ((btrisObservationName == null) ? 0 : btrisObservationName.hashCode());
		result = prime * result + ((btrisRedCode == null) ? 0 : btrisRedCode.hashCode());
		result = prime * result + ((btrisSpecimenType == null) ? 0 : btrisSpecimenType.hashCode());
		result = prime * result + ((btrisTable == null) ? 0 : btrisTable.hashCode());
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
		BtrisMapping other = (BtrisMapping) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (bricsDataElement == null) {
			if (other.bricsDataElement != null)
				return false;
		} else if (!bricsDataElement.equals(other.bricsDataElement))
			return false;
		if (bricsDEName == null) {
			if (other.bricsDEName != null)
				return false;
		} else if (!bricsDEName.equals(other.bricsDEName))
			return false;
		if (bricsValueRange == null) {
			if (other.bricsValueRange != null)
				return false;
		} else if (!bricsValueRange.equals(other.bricsValueRange))
			return false;
		if (btrisObservationName == null) {
			if (other.btrisObservationName != null)
				return false;
		} else if (!btrisObservationName.equals(other.btrisObservationName))
			return false;
		if (btrisRedCode == null) {
			if (other.btrisRedCode != null)
				return false;
		} else if (!btrisRedCode.equals(other.btrisRedCode))
			return false;
		if (btrisSpecimenType == null) {
			if (other.btrisSpecimenType != null)
				return false;
		} else if (!btrisSpecimenType.equals(other.btrisSpecimenType))
			return false;
		if (btrisTable == null) {
			if (other.btrisTable != null)
				return false;
		} else if (!btrisTable.equals(other.btrisTable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BtrisMapping [id=" + id + ", bricsDEName=" + bricsDEName + ", bricsValueRange=" + bricsValueRange
				+ ", btrisObservationName=" + btrisObservationName + ", btrisRedCode=" + btrisRedCode
				+ ", btrisSpecimenType=" + btrisSpecimenType
				+ ", btrisTable="
				+ btrisTable + "]";
	}
}

