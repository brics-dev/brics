//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.07.17 at 03:00:46 PM EDT 
//

package gov.nih.tbi.dictionary.service.rulesengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for IncomingOptionality complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IncomingOptionality">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Severity" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IncomingOptionality", propOrder = { "name", "severity" })
public class IncomingOptionality
{

    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "Severity", required = true)
    protected String severity;

    /**
     * Gets the value of the name property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getName()
    {

        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setName(String value)
    {

        this.name = value;
    }

    /**
     * Gets the value of the severity property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSeverity()
    {

        return severity;
    }

    /**
     * Sets the value of the severity property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSeverity(String value)
    {

        this.severity = value;
    }

}
