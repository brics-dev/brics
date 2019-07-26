
package gov.nih.tbi.dictionary.model.hibernate.formstructure.export;

import gov.nih.tbi.commons.model.RepeatableType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

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

@XmlRootElement(name = "repeatableGroup")
@XmlType(propOrder = { "id", "name", "type", "threshold", "position", "mapElements" })
@XmlAccessorType(XmlAccessType.FIELD)
public class RepeatableGroup implements Serializable
{

    /**
	 * 
	 */
    private static final long serialVersionUID = -8456773662819736917L;

    /**********************************************************************/

    private Long id;

    @XmlID
    private String name;

    private RepeatableType type;

    private Integer threshold;


    private Set<MapElementExport> mapElements;

    private Integer position;

    /**********************************************************************/

    public RepeatableGroup(RepeatableGroup oldRepeatableGroup)
    {

        name = oldRepeatableGroup.getName();
        type = oldRepeatableGroup.getType();
        threshold = oldRepeatableGroup.getThreshold();
        position = oldRepeatableGroup.getPosition();
        mapElements = (Set<MapElementExport>) oldRepeatableGroup.getMapElements();
    }

    public RepeatableGroup()
    {

        mapElements = new LinkedHashSet<MapElementExport>();
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
     * Collect all the MapElements that this RepeatableGroup contains
     * 
     * 
     * @return The collection of MapElements contained in this REG
     */
    // XXX, TODO: Burn this method
    public Set<MapElementExport> getDataElements()
    {

        return getMapElements();
    }

    /**
     * Collect all the MapElements that this RepeatableGroup contains
     * 
     * @return The collection of MapElements contained in this REG
     */
    public Set<MapElementExport> getMapElements()
    {

        if (mapElements == null)
        {
            mapElements = new LinkedHashSet<MapElementExport>();
        }

        return mapElements;
    }


    public void setMapElements(Set<MapElementExport> mapElementList)
    {

        if (this.mapElements == null)
        {
            mapElements = new HashSet<MapElementExport>();
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
}
