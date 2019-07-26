package gov.nih.tbi.query.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "derivedDataElements")
@XmlAccessorType(XmlAccessType.FIELD)
public class DerivedDataJaxbElements {

	@XmlElement(name = "derivedDataList")
	private List<DerivedDataJaxbElement> derivedDataList;

	public DerivedDataJaxbElements() {

	}

	public List<DerivedDataJaxbElement> getDerivedDataList() {
		return derivedDataList;
	}

	public void setDerivedDataList(List<DerivedDataJaxbElement> derivedDataList) {
		this.derivedDataList = derivedDataList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((derivedDataList == null) ? 0 : derivedDataList.hashCode());
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
		DerivedDataJaxbElements other = (DerivedDataJaxbElements) obj;
		if (derivedDataList == null) {
			if (other.derivedDataList != null)
				return false;
		} else if (!derivedDataList.equals(other.derivedDataList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DerivedDataJaxbElements [derivedDataList=" + derivedDataList + "]";
	}
}
