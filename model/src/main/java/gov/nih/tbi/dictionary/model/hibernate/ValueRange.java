package gov.nih.tbi.dictionary.model.hibernate;

import gov.nih.tbi.commons.model.DataType;

import gov.nih.tbi.dictionary.model.NativeTypeConverter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name = "VALUE_RANGE")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValueRange implements Serializable, Comparable<ValueRange> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8515876842821422224L;

	/**********************************************************************/

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VALUE_RANGE_SEQ")
	@SequenceGenerator(name = "VALUE_RANGE_SEQ", sequenceName = "VALUE_RANGE_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "VALUE_RANGE")
	private String valueRange;

	@Transient
	private String uri;

	// @XmlTransient
	@XmlIDREF
	@ManyToOne(targetEntity = StructuralDataElement.class)
	@JoinColumn(name = "DATA_ELEMENT_ID")
	private StructuralDataElement dataElement;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "OUTPUT_CODE")
	private Integer outputCode;

	// added by Ching-Heng
	@Column(name = "ITEM_RESPONSE_OID")
	private String itemResponseOid;

	@Column(name = "ELEMENT_OID")
	private String elementOid;


	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "valueRange", targetEntity = SchemaPv.class, orphanRemoval = true)
	private Set<SchemaPv> schemaPvs = new HashSet<SchemaPv>();


	/**********************************************************************/

	public ValueRange() {

	}

	public ValueRange(String valueRange, String description) {

		super();
		this.valueRange = valueRange;
		this.description = description;
	}

	public ValueRange(String valueRange, String description, String elementOid, String itemResponseOid) {
		super();
		this.valueRange = valueRange;
		this.description = description;
		this.elementOid = elementOid;
		this.itemResponseOid = itemResponseOid;
	}

	public ValueRange(String uri, String valueRange, String description, Integer outputCode) {

		super();
		this.valueRange = valueRange;
		this.uri = uri;
		this.description = description;
		this.outputCode = outputCode;
	}

	public ValueRange(Long id, String valueRange, String description, Integer outputCode) {

		this.id = id;
		this.valueRange = valueRange;
		this.description = description;
		this.outputCode = outputCode;
	}

	public ValueRange(Long id, String valueRange, String description, Integer outputCode, String elementOid,
			String itemResponseOid) {

		this.id = id;
		this.valueRange = valueRange;
		this.description = description;
		this.outputCode = outputCode;
		this.elementOid = elementOid;
		this.itemResponseOid = itemResponseOid;
	}

	@Deprecated
	public ValueRange(Long id, String valueRange, String description, StructuralDataElement dataElement) {

		this.id = id;
		this.valueRange = valueRange;
		this.description = description;
		this.dataElement = dataElement;
	}

	/**********************************************************************/

	public Long getId() {

		return id;
	}

	public String getUri() {

		return uri;
	}

	public void setUri(String uri) {

		this.uri = uri;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public String getValueRange() {

		return valueRange;
	}

	public String getItemResponseOid() {
		return itemResponseOid;
	}

	public void setItemResponseOid(String itemResponseOid) {
		this.itemResponseOid = itemResponseOid;
	}

	public String getElementOid() {
		return elementOid;
	}

	public void setElementOid(String elementOid) {
		this.elementOid = elementOid;
	}

	/**
	 * Used for comparing and sorting
	 *
	 * @return
	 */

	private Comparable getNativeValue() {

		NativeTypeConverter converter = null;

		// Even though it kinda doesn't make sense, it is possible for a permissible value
		// to not be associated with a data element, if it is loeaded from the semantic store.
		// in that case, we just treat the value as String for comparison purposes
		// even though it could be associate with a numeric data element
		if (this.dataElement != null && this.dataElement.getType() != null) {
			DataType type = this.dataElement.getType();
			converter = type.getTypeConverter();
		}

		Comparable result;

		if (converter != null) {
			result = converter.getNativeValue(this.valueRange);
		} else {
			result = this.valueRange.toUpperCase();
		}

		return result;
	}

	public void setValueRange(String valueRange) {

		this.valueRange = valueRange;
	}

	public StructuralDataElement getDataElement() {

		return dataElement;
	}

	public void setDataElement(StructuralDataElement dataElement) {

		this.dataElement = dataElement;
	}

	public String getDescription() {
		if(description == null) {
			return "";
		} else {
			return description.replaceAll("\\n", " ");
		}
	}

	public void setDescription(String description) {

		this.description = description;
	}

	public Integer getOutputCode() {
		return outputCode;
	}

	public void setOutputCode(Integer outputCode) {
		this.outputCode = outputCode;
	}

	public Set<SchemaPv> getSchemaPvs() {
		return schemaPvs;
	}

	public void setSchemaPvs(Set<SchemaPv> schemaPvs) {
		this.schemaPvs = schemaPvs;
	}

	public SchemaPv getSchemaPvBySchema(String schemaName) {

		if (schemaPvs != null && !schemaPvs.isEmpty() && schemaName != null) {
			for (SchemaPv schemaPv : schemaPvs) {
				if (schemaName.equals(schemaPv.getSchema().getName())) {
					return schemaPv;
				}
			}
		}
		return null;
	}

	public String toString() {

		return valueRange;
	}

	/**********************************************************************/

	@Override
	public int compareTo(ValueRange comp) {

		if (comp == null) {
			return 1;
		}
		if (this.getValueRange() == null) {
			return -1;
		}
		if (comp.getValueRange() == null) {
			return 1;
		}

		Comparable thisNatVal = this.getNativeValue();
		Comparable compNatVal = comp.getNativeValue();

		return thisNatVal.compareTo(compNatVal);
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		result = prime * result + ((valueRange == null) ? 0 : valueRange.hashCode());
		result = prime * result + ((outputCode == null) ? 0 : outputCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		ValueRange other = (ValueRange) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}

		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}

		if (valueRange == null) {
			if (other.valueRange != null) {
				return false;
			}
		} else if (!valueRange.equals(other.valueRange)) {
			return false;
		}

		if (outputCode == null) {
			if (other.outputCode != null) {
				return false;
			}
		} else if (!outputCode.equals(other.outputCode)) {
			return false;
		}

		return true;
	}

	/**
	 * Returns a map of schema name to it's schema permissible value
	 * 
	 * @return
	 */
	public Map<String, String> getSchemaPvMap() {
		if (schemaPvs == null) {
			return null;
		}

		Map<String, String> schemaPvMap = new HashMap<String, String>();
		for (SchemaPv currentSchemaPv : schemaPvs) {
			if (currentSchemaPv.getSchema().getName() != null) { // no use putting it in the map of the schema name
																 // doesn't exist
				schemaPvMap.put(currentSchemaPv.getSchema().getName(), currentSchemaPv.getPermissibleValue());
			}
		}

		return schemaPvMap;
	}
}
