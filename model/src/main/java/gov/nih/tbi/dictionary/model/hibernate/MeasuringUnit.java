
package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name = "MEASURING_UNIT")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class MeasuringUnit implements Serializable
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 6223968601618363919L;

    /**********************************************************************/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEASURING_UNIT_SEQ")
    @SequenceGenerator(name = "MEASURING_UNIT_SEQ", sequenceName = "MEASURING_UNIT_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "UNIT_NAME")
    private String name;

    @Column(name = "ABREVIATION")
    private String abreviation;

    @Column(name = "FACTOR_OF_BASE")
    private String factorOfBase;

    @ManyToOne(cascade = { CascadeType.DETACH })
    @JoinColumn(name = "UNIT_TYPE_ID")
    private MeasuringType measuringType;

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

    public String getAbreviation()
    {

        return abreviation;
    }

    public void setAbreviation(String abreviation)
    {

        this.abreviation = abreviation;
    }

    public String getFactorOfBase()
    {

        return factorOfBase;
    }

    public void setFactorOfBase(String factorOfBase)
    {

        this.factorOfBase = factorOfBase;
    }

    public MeasuringType getMeasuringType()
    {

        return measuringType;
    }

    public void setMeasuringType(MeasuringType measuringType)
    {

        this.measuringType = measuringType;
    }

    public String getDisplayName()
    {

        return this.toString();
    }

    public String toString()
    {

        return name + " ( " + measuringType.getName() + " )";
    }

}
