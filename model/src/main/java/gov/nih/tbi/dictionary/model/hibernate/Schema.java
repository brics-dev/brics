package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name = "SCHEMA")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlRootElement(name = "schema")
@XmlAccessorType(XmlAccessType.FIELD)
public class Schema implements Serializable {

	private static final long serialVersionUID = -3447946446254442699L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SCHEMA_SEQ")
	@SequenceGenerator(name = "SCHEMA_SEQ", sequenceName = "SCHEMA_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "SCHEMA_NAME")
	private String name;

	public Schema() {

	}

	public Schema(String name) {
		super();
		this.name = name;
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Schema other = (Schema) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Schema [id=" + id + ", name=" + name + "]";
	}

}
