
package gov.nih.tbi.dictionary.model.hibernate;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.RequiredType;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "MAP_ELEMENT")
@XmlRootElement(name = "mapElement")
@XmlAccessorType(XmlAccessType.FIELD)
public class MapElement implements Comparable<MapElement>, Serializable
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 2352968968139244483L;

    /**********************************************************************/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MAP_ELEMENT_SEQ")
    @SequenceGenerator(name = "MAP_ELEMENT_SEQ", sequenceName = "MAP_ELEMENT_SEQ", allocationSize = 1)
    private Long id;

    @XmlIDREF
    @ManyToOne(targetEntity = RepeatableGroup.class)
    @JoinColumn(name = "REPEATABLE_GROUP_ID")
    private RepeatableGroup repeatableGroup;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "DATA_ELEMENT_ID")
    private StructuralDataElement dataElement;

    @Column(name = "POSITION")
    private Integer position;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "REQUIRED_TYPE_ID")
    private RequiredType requiredTypeId;

    /**********************************************************************/

    public MapElement(MapElement mapElement)
    {

        repeatableGroup = mapElement.getRepeatableGroup();
        dataElement = mapElement.getStructuralDataElement();
        position = mapElement.getPosition();
        requiredTypeId = mapElement.getRequiredType();
    }

    public MapElement()
    {

        this.dataElement = new StructuralDataElement();
    }

    public MapElement(StructuralDataElement dataElement)
    {

        this.dataElement = dataElement;
    }

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public RepeatableGroup getRepeatableGroup()
    {

        return repeatableGroup;
    }

    public void setRepeatableGroup(RepeatableGroup repeatableGroup)
    {

        this.repeatableGroup = repeatableGroup;
    }

    public StructuralDataElement getStructuralDataElement()
    {

        return dataElement;
    }

    public void setStructuralDataElement(StructuralDataElement dataElement)
    {

        this.dataElement = dataElement;
    }

    public Integer getPosition()
    {

        return position;
    }

    public void setPosition(Integer position)
    {

        this.position = position;
    }

    public RequiredType getRequiredType()
    {

        return requiredTypeId;
    }

    public void setRequiredType(RequiredType requiredType)
    {

        this.requiredTypeId = requiredType;
    }

    /**********************************************************************/

    public FormStructure getDataStructure()
    {

        return new FormStructure(repeatableGroup.getDataStructure());
    }

    @Override
    public String toString()
    {

        return "MapElement [dataElement=" + dataElement + ", repeatableGroup=" + repeatableGroup + ", id=" + id
                + ", position=" + position + ", requiredTypeId=" + requiredTypeId + "]";
    }

    public String getMapElementNameWithGroup()
    {

        String out = dataElement.getName() + ModelConstants.WHITESPACE + ModelConstants.LEFT_PAREN
                + getRepeatableGroup().getName() + ModelConstants.RIGHT_PAREN;
        return out;
    }

	@Override
	public int compareTo(MapElement o) {
		return this.getPosition().compareTo(o.getPosition());
	}

}
