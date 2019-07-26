
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
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name = "VALIDATION_PLUGIN")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidationPlugin implements Serializable
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 6415656964663756456L;

    /**********************************************************************/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VALIDATION_PLUGIN_SEQ")
    @SequenceGenerator(name = "VALIDATION_PLUGIN_SEQ", sequenceName = "VALIDATION_PLUGIN_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "plugin_name")
    private String name;

    public ValidationPlugin()
    {

    }

    public ValidationPlugin(Long validatorId)
    {

        this.setId(validatorId);
    }

    public ValidationPlugin(String validatorName)
    {

        this.setName(validatorName);
    }

    /**********************************************************************/

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    /**********************************************************************/
}
