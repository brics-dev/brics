
package gov.nih.tbi.repository.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a single cell the record. Can either be non-repeating cell or a repeating cell (button)
 * 
 * @author Francis Chen
 * 
 */
@XmlRootElement(name = "cellValue")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class CellValue implements Serializable
{

    private static final long serialVersionUID = -2105972822830366761L;

    /**
     * Type of the data element this cell represents. Basically used to generate the link to view biosample
     */
    @XmlAttribute
    protected String dataElementType;

    @XmlAttribute
    protected boolean isRepeating;

	protected CellValue() {}

    public CellValue(String dataElementType, boolean isRepeating)
    {

        this.dataElementType = dataElementType;
        this.isRepeating = isRepeating;
    }

    /**
     * Denotes whether or not this cell is a repeating cell or a non-repeating cell
     * 
     * @return
     */
    public boolean getIsRepeating()
    {

        return isRepeating;
    }

    public abstract String getDataElementType();

    public abstract void setDataElementType(String dataElementType);

    public abstract int getRowSize();

    @Override
    public int hashCode()
    {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataElementType == null) ? 0 : dataElementType.hashCode());
        result = prime * result + (isRepeating ? 1231 : 1237);
        return result;
    }

    @Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
            return true;
		}

		if (obj instanceof CellValue) {
			CellValue cv = (CellValue) obj;

			return (this.dataElementType == cv.dataElementType || (this.dataElementType != null && this.dataElementType
					.equals(cv.dataElementType))) && (this.isRepeating == cv.isRepeating);
		}

		return false;
    }
}