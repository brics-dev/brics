//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.08.31 at 05:32:23 AM EDT 
//


package gov.nih.tbi.ordermanager.iu;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}Title"/>
 *         &lt;element ref="{}Abstract"/>
 *         &lt;element ref="{}Number"/>
 *         &lt;element ref="{}FundingStatus"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "title",
    "_abstract",
    "number",
    "fundingStatus"
})
@XmlRootElement(name = "Grant")
public class Grant {

    @XmlElement(name = "Title", required = true)
    protected String title;
    @XmlElement(name = "Abstract", required = true)
    protected String _abstract;
    @XmlElement(name = "Number", required = true)
    protected String number;
    @XmlElement(name = "FundingStatus", required = true)
    protected FundingStatus fundingStatus;

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the abstract property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAbstract() {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAbstract(String value) {
        this._abstract = value;
    }

    /**
     * Gets the value of the number property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumber(String value) {
        this.number = value;
    }

    /**
     * Gets the value of the fundingStatus property.
     * 
     * @return
     *     possible object is
     *     {@link FundingStatus }
     *     
     */
    public FundingStatus getFundingStatus() {
        return fundingStatus;
    }

    /**
     * Sets the value of the fundingStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link FundingStatus }
     *     
     */
    public void setFundingStatus(FundingStatus value) {
        this.fundingStatus = value;
    }

}
