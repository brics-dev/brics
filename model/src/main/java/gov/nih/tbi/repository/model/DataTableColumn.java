package gov.nih.tbi.repository.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;

@XmlRootElement(name = "dataTableColumn")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataTableColumn implements Serializable {

	private static final long serialVersionUID = 4446862766590129656L;

	@XmlAttribute()
	private String form;

	@XmlAttribute()
	private String repeatableGroup;

	@XmlAttribute()
	private String dataElement;

	@XmlTransient()
	private String hardCoded;

	// jaxb needs a no-arg constructor
	protected DataTableColumn() {
	}

	/**
	 * Makes a copy of the argument object.
	 * 
	 * @param clone - The object to copy.
	 * @throws NullPointerException If the argument is null.
	 */
	public DataTableColumn(DataTableColumn clone) throws NullPointerException {
		if (clone == null) {
			throw new NullPointerException("The clone argument cannot be null.");
		}

		this.form = clone.form;
		this.repeatableGroup = clone.repeatableGroup;
		this.dataElement = clone.dataElement;
		this.hardCoded = clone.hardCoded;
	}

	public boolean isHardCoded() {
		return hardCoded != null;
	}

	public DataTableColumn(String form, String hardCoded) {
		this.form = form;
		this.hardCoded = hardCoded;
	}

	public DataTableColumn(String hardCoded) {
		this.hardCoded = hardCoded;
	}

	public DataTableColumn(String form, String repeatableGroup, String dataElement) {

		this.form = form;
		this.repeatableGroup = repeatableGroup;
		this.dataElement = dataElement;
	}

	public String getForm() {

		return form;
	}

	public void setForm(String form) {

		this.form = form;
	}

	public String getRepeatableGroup() {

		return repeatableGroup;
	}

	public void setRepeatableGroup(String repeatableGroup) {

		this.repeatableGroup = repeatableGroup;
	}

	public String getDataElement() {

		return dataElement;
	}

	public void setDataElement(String dataElement) {

		this.dataElement = dataElement;
	}

	@Override()
	public String toString() {
		if (!StringUtils.isEmpty(dataElement)) {
			return form + "." + repeatableGroup + "." + dataElement;
		} else {
			return form + "." + repeatableGroup;
		}
	}

	public String getHardCoded() {
		return hardCoded;
	}

	public void setHardCoded(String hardCoded) {

		this.hardCoded = hardCoded;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataElement == null) ? 0 : dataElement.hashCode());
		result = prime * result + ((form == null) ? 0 : form.hashCode());
		result = prime * result + ((hardCoded == null) ? 0 : hardCoded.hashCode());
		result = prime * result + ((repeatableGroup == null) ? 0 : repeatableGroup.hashCode());
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
		DataTableColumn other = (DataTableColumn) obj;
		if (dataElement == null) {
			if (other.dataElement != null)
				return false;
		} else if (!dataElement.equals(other.dataElement))
			return false;
		if (form == null) {
			if (other.form != null)
				return false;
		} else if (!form.equals(other.form))
			return false;
		if (hardCoded == null) {
			if (other.hardCoded != null)
				return false;
		} else if (!hardCoded.equals(other.hardCoded))
			return false;
		if (repeatableGroup == null) {
			if (other.repeatableGroup != null)
				return false;
		} else if (!repeatableGroup.equals(other.repeatableGroup))
			return false;
		return true;
	}
}
