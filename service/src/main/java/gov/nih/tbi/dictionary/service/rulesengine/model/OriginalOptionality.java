//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.07.17 at 03:00:46 PM EDT 
//

package gov.nih.tbi.dictionary.service.rulesengine.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for OriginalOptionality complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OriginalOptionality">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="IncomingOptionality" type="{http://www.example.org/RulesEngineRules}IncomingOptionality" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OriginalOptionality", propOrder = { "name", "incomingOptionality" })
public class OriginalOptionality
{

    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "IncomingOptionality", required = true)
    protected List<IncomingOptionality> incomingOptionality;

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
     * Gets the value of the incomingOptionality property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the incomingOptionality property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getIncomingOptionality().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link IncomingOptionality }
     * 
     * 
     */
    public List<IncomingOptionality> getIncomingOptionality()
    {

        if (incomingOptionality == null)
        {
            incomingOptionality = new ArrayList<IncomingOptionality>();
        }
        return this.incomingOptionality;
    }

}
