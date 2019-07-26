package gov.nih.tbi.dictionary.model.hibernate.eform;

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

@Entity
@Table(name = "EFORM_LAYOUT")
@XmlRootElement(name = "EformLayout")
@XmlAccessorType(XmlAccessType.FIELD)
public class EformLayout implements Serializable{

	private static final long serialVersionUID = -3701434117976727162L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EFORM_LAYOUT_SEQ")
	@SequenceGenerator(name = "EFORM_LAYOUT_SEQ", sequenceName = "EFORM_LAYOUT_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "FORM_ROW")
	private Integer formRow;

	@Column(name = "NUM_COLS")
	private Integer numColumns;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Integer getFormRow() {
		return formRow;
	}

	public void setFormRow(Integer formRow) {
		this.formRow = formRow;
	}

	public Integer getNumColumns() {
		return numColumns;
	}

	public void setNumColumns(Integer numColumns) {
		this.numColumns = numColumns;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((formRow == null) ? 0 : formRow.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((numColumns == null) ? 0 : numColumns.hashCode());
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
		EformLayout other = (EformLayout) obj;
		if (formRow == null) {
			if (other.formRow != null)
				return false;
		} else if (!formRow.equals(other.formRow))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (numColumns == null) {
			if (other.numColumns != null)
				return false;
		} else if (!numColumns.equals(other.numColumns))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FormLayout [FormLayout Id=" + id + ", formRow=" + formRow + ", numColumns=" + numColumns + "]";
	}
}