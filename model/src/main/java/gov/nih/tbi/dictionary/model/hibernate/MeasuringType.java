
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
@Table(name = "MEASURING_TYPE")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class MeasuringType implements Serializable
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 8941891636904940799L;

    /**********************************************************************/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEASUREMENT_TYPE_SEQ")
    @SequenceGenerator(name = "MEASUREMENT_TYPE_SEQ", sequenceName = "MEASUREMENT_TYPE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "TYPE_NAME")
    private String name;

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

}
