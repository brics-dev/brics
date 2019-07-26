
package gov.nih.tbi.dictionary.model.hibernate;

import gov.nih.tbi.commons.model.RepeatableType;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * A hibernate representation of an entry from the repeatable_group table
 * 
 * @author mvalei
 */
@Entity
@Table(name = "REPEATABLE_GROUP")
@XmlRootElement(name = "repeatableGroup")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema", propOrder = { "id", "name", "type", "threshold",
        "position", "mapElements" })
@XmlAccessorType(XmlAccessType.FIELD)
public class RepeatableGroup implements Comparable<RepeatableGroup>, Serializable
{

    private static final long serialVersionUID = -8456773662819736917L;

    /**********************************************************************/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPEATABLE_GROUP_SEQ")
    @SequenceGenerator(name = "REPEATABLE_GROUP_SEQ", sequenceName = "REPEATABLE_GROUP_SEQ", allocationSize = 1)
    private Long id;

    @XmlID
    @Column(name = "NAME")
    private String name;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "TYPE_ID")
    private RepeatableType type;

    @Column(name = "THRESHOLD")
    private Integer threshold;

    @XmlTransient
    @ManyToOne(targetEntity = StructuralFormStructure.class)
    @JoinColumn(name = "DATA_STRUCTURE_ID")
    private StructuralFormStructure dataStructure;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "repeatableGroup", targetEntity = MapElement.class, orphanRemoval = true)
    @OrderBy(value = "position")
    private Set<MapElement> mapElements;

    @Column(name = "POSITION")
    private Integer position;

    @XmlTransient
    @Transient
    private String uri;

    /**********************************************************************/

    public RepeatableGroup(RepeatableGroup oldRepeatableGroup)
    {

        name = oldRepeatableGroup.getName();
        type = oldRepeatableGroup.getType();
        threshold = oldRepeatableGroup.getThreshold();
        position = oldRepeatableGroup.getPosition();
        dataStructure = oldRepeatableGroup.getDataStructure();
    }

    public RepeatableGroup()
    {

        mapElements = new LinkedHashSet<MapElement>();
    }

    /**
     * @return the id
     */
    public Long getId()
    {

        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id)
    {

        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName()
    {

        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name)
    {

        this.name = name.trim();
    }

    /**
     * @return the type
     */
    public RepeatableType getType()
    {

        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(RepeatableType type)
    {

        this.type = type;
    }

    /**
     * @return the threshold
     */
    public Integer getThreshold()
    {

        return threshold;
    }

    /**
     * @param threshold
     *            the threshold to set
     */
    public void setThreshold(Integer threshold)
    {

        this.threshold = threshold;
    }

    /**
     * @return the position
     */
    public Integer getPosition()
    {

        return position;
    }

    /**
     * @param position
     *            the position to set
     */
    public void setPosition(Integer position)
    {

        this.position = position;
    }

    /**
     * @return the dataStructure
     */
    public StructuralFormStructure getDataStructure()
    {

        return dataStructure;
    }

    /**
     * @param dataStructure
     *            the dataStructure to set
     */
    public void setDataStructure(StructuralFormStructure dataStructure)
    {

        this.dataStructure = dataStructure;
    }

    /**
     * Collect all the MapElements that this RepeatableGroup contains
     * 
     * 
     * @return The collection of MapElements contained in this REG
     */
    // XXX, TODO: Burn this method
    public Set<MapElement> getDataElements()
    {

        return getMapElements();
    }

    /**
     * Collect all the MapElements that this RepeatableGroup contains
     * 
     * @return The collection of MapElements contained in this REG
     */
    public Set<MapElement> getMapElements()
    {

        if (mapElements == null)
        {
            mapElements = new LinkedHashSet<MapElement>();
        }

        return mapElements;
    }

    public MapElement getMapElementByName(String name)
    {

        if (mapElements != null)
        {
            for (MapElement mapElement : mapElements)
            {
                if (name.equals(mapElement.getStructuralDataElement().getName()))
                {
                    return mapElement;
                }
            }
        }

        return null;
    }
    
    @Override
    public String toString()
    {

        String mapElementIds = "";
        for (MapElement me : getMapElements())
        {
            mapElementIds += me.getId();
        }

        return "RepeatableGroup [id=" + getId() + ", name=" + getName() + ", type=" + getType() + ", threshold="
                + getThreshold() + ", position=" + getPosition() + ", dataStructure=" + getDataStructure()
                + ", mapElements=" + mapElementIds + "]";
    }

    public void setMapElements(LinkedHashSet<MapElement> mapElementList)
    {

        if (this.mapElements == null)
        {
            mapElements = new LinkedHashSet<MapElement>();
        }

        this.mapElements.clear();

        if (mapElementList != null)
        {
            mapElements.addAll(mapElementList);
        }
    }

    /**
     * returns the number of mapElements associated with this repeatable group
     * 
     * @return
     */
    public Integer getSize()
    {

        return mapElements.size();
    }

    public String getUri()
    {

        return uri;
    }

    public void setUri(String uri)
    {

        this.uri = uri;
    }

	@Override
	public int compareTo(RepeatableGroup o) {
		return this.getPosition().compareTo(o.getPosition());
	}
}
