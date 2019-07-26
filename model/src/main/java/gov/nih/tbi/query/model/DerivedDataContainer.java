package gov.nih.tbi.query.model;

import java.util.HashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This class is to support JAXB marshalling and unmarshalling of derived data from query tool
 * @author fchen
 *
 */
@XmlRootElement(name = "derivedData")
@XmlAccessorType(XmlAccessType.FIELD)
public class DerivedDataContainer {

	@XmlJavaTypeAdapter(DerivedDataAdapter.class)
	private HashMap<DerivedDataKey, DerivedDataRow> dataMap;

	public HashMap<DerivedDataKey, DerivedDataRow> getDataMap() {
		return dataMap;
	}

	public void setDataMap(HashMap<DerivedDataKey, DerivedDataRow> dataMap) {
		this.dataMap = dataMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataMap == null) ? 0 : dataMap.hashCode());
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
		DerivedDataContainer other = (DerivedDataContainer) obj;
		if (dataMap == null) {
			if (other.dataMap != null)
				return false;
		} else if (!dataMap.equals(other.dataMap))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DerivedDataContainer [dataMap=" + dataMap + "]";
	}
}
