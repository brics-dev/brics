//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.10.25 at 05:07:49 PM EDT 
//


package gov.nih.tbi.commons.model;


import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import gov.nih.tbi.ModelConstants;


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
 *         &lt;element name="required_header" type="{}required_header_struct"/>
 *         &lt;element name="id_info" type="{}id_info_struct"/>
 *         &lt;element name="brief_title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="acronym" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="official_title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sponsors" type="{}sponsors_struct"/>
 *         &lt;element name="source" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="oversight_info" type="{}oversight_info_struct" minOccurs="0"/>
 *         &lt;element name="brief_summary" type="{}textblock_struct" minOccurs="0"/>
 *         &lt;element name="detailed_description" type="{}textblock_struct" minOccurs="0"/>
 *         &lt;element name="overall_status" type="{}overall_status_enum"/>
 *         &lt;element name="why_stopped" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="start_date" type="{}year_month_type" minOccurs="0"/>
 *         &lt;element name="completion_date" type="{}year_month_struct" minOccurs="0"/>
 *         &lt;element name="primary_completion_date" type="{}year_month_struct" minOccurs="0"/>
 *         &lt;element name="phase" type="{}phase_enum"/>
 *         &lt;element name="study_type" type="{}study_type_enum"/>
 *         &lt;element name="study_design" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="target_duration" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="primary_outcome" type="{}protocol_outcome_struct" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="secondary_outcome" type="{}protocol_outcome_struct" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="other_outcome" type="{}protocol_outcome_struct" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="number_of_arms" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="number_of_groups" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="enrollment" type="{}enrollment_struct" minOccurs="0"/>
 *         &lt;element name="condition" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="arm_group" type="{}arm_group_struct" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="intervention" type="{}intervention_struct" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="biospec_retention" type="{}biospec_retention_enum" minOccurs="0"/>
 *         &lt;element name="biospec_descr" type="{}textblock_struct" minOccurs="0"/>
 *         &lt;element name="eligibility" type="{}eligibility_struct" minOccurs="0"/>
 *         &lt;element name="overall_official" type="{}investigator_struct" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="overall_contact" type="{}contact_struct" minOccurs="0"/>
 *         &lt;element name="overall_contact_backup" type="{}contact_struct" minOccurs="0"/>
 *         &lt;element name="location" type="{}location_struct" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="location_countries" type="{}countries_struct" minOccurs="0"/>
 *         &lt;element name="removed_countries" type="{}countries_struct" minOccurs="0"/>
 *         &lt;element name="link" type="{}link_struct" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="reference" type="{}reference_struct" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="results_reference" type="{}reference_struct" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="verification_date" type="{}year_month_type" minOccurs="0"/>
 *         &lt;element name="lastchanged_date" type="{}year_month_day_type" minOccurs="0"/>
 *         &lt;element name="firstreceived_date" type="{}year_month_day_type"/>
 *         &lt;element name="firstreceived_results_date" type="{}year_month_day_type" minOccurs="0"/>
 *         &lt;element name="firstreceived_results_disposition_date" type="{}year_month_day_type" minOccurs="0"/>
 *         &lt;element name="responsible_party" type="{}responsible_party_struct" minOccurs="0"/>
 *         &lt;element name="keyword" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="is_fda_regulated" type="{}yes_no_enum" minOccurs="0"/>
 *         &lt;element name="is_section_801" type="{}yes_no_enum" minOccurs="0"/>
 *         &lt;element name="has_expanded_access" type="{}yes_no_enum" minOccurs="0"/>
 *         &lt;element name="condition_browse" type="{}browse_struct" minOccurs="0"/>
 *         &lt;element name="intervention_browse" type="{}browse_struct" minOccurs="0"/>
 *         &lt;element name="patient_data" type="{}patient_data_struct" minOccurs="0"/>
 *         &lt;element name="study_docs" type="{}study_docs_struct" minOccurs="0"/>
 *         &lt;element name="clinical_results" type="{}clinical_results_struct" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="rank" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "requiredHeader", "idInfo", "briefTitle","acronym", "officialTitle","source", 
     "briefSummary", "detailedDescription", "overallStatus", "startDate","completionDate", "overallOfficial"

})
@XmlRootElement(name = "clinical_study")
public class ClinicalStudy {

    @XmlElement(name = "required_header", required = true)
    protected RequiredHeaderStruct requiredHeader;
    @XmlElement(name = "id_info", required = true)
    protected IdInfoStruct idInfo;
    @XmlElement(name = "brief_title", required = true)
    protected String briefTitle;
    protected String acronym;
    @XmlElement(name = "official_title")
    protected String officialTitle;
    @XmlElement(required = true)
    protected String source;
    @XmlElement(name = "brief_summary")
    protected TextblockStruct briefSummary;
    @XmlElement(name = "detailed_description")
    protected TextblockStruct detailedDescription;
    @XmlElement(name = "overall_status", required = true)
    protected String overallStatus;
    @XmlElement(name = "start_date")
    protected String startDate;
    @XmlElement(name = "completion_date")
    protected YearMonthStruct completionDate;
    @XmlElement(name = "overall_official")
    protected List<InvestigatorStruct> overallOfficial;
   
    /**
     * Gets the value of the requiredHeader property.
     * 
     * @return
     *     possible object is
     *     {@link RequiredHeaderStruct }
     *     
     */
    public RequiredHeaderStruct getRequiredHeader() {
        return requiredHeader;
    }

    /**
     * Sets the value of the requiredHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequiredHeaderStruct }
     *     
     */
    public void setRequiredHeader(RequiredHeaderStruct value) {
        this.requiredHeader = value;
    }

    /**
     * Gets the value of the idInfo property.
     * 
     * @return
     *     possible object is
     *     {@link IdInfoStruct }
     *     
     */
    public IdInfoStruct getIdInfo() {
        return idInfo;
    }

    /**
     * Sets the value of the idInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdInfoStruct }
     *     
     */
    public void setIdInfo(IdInfoStruct value) {
        this.idInfo = value;
    }

    /**
     * Gets the value of the briefTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBriefTitle() {
        return briefTitle;
    }

    /**
     * Sets the value of the briefTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBriefTitle(String value) {
        this.briefTitle = value;
    }

    /**
     * Gets the value of the acronym property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAcronym() {
        return acronym;
    }

    /**
     * Sets the value of the acronym property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcronym(String value) {
        this.acronym = value;
    }

    /**
     * Gets the value of the officialTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOfficialTitle() {
        return officialTitle;
    }

    /**
     * Sets the value of the officialTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOfficialTitle(String value) {
        this.officialTitle = value;
    }

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Gets the value of the briefSummary property.
     * 
     * @return
     *     possible object is
     *     {@link TextblockStruct }
     *     
     */
    public TextblockStruct getBriefSummary() {
        return briefSummary;
    }

    /**
     * Sets the value of the briefSummary property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextblockStruct }
     *     
     */
    public void setBriefSummary(TextblockStruct value) {
        this.briefSummary = value;
    }

    /**
     * Gets the value of the detailedDescription property.
     * 
     * @return
     *     possible object is
     *     {@link TextblockStruct }
     *     
     */
    public TextblockStruct getDetailedDescription() {
        return detailedDescription;
    }

    /**
     * Sets the value of the detailedDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextblockStruct }
     *     
     */
    public void setDetailedDescription(TextblockStruct value) {
        this.detailedDescription = value;
    }

    /**
     * Gets the value of the overallStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOverallStatus() {
        return overallStatus;
    }

    /**
     * Sets the value of the overallStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOverallStatus(String value) {
        this.overallStatus = value;
    }

    /**
     * Gets the value of the startDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartDate(String value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the completionDate property.
     * 
     * @return
     *     possible object is
     *     {@link YearMonthStruct }
     *     
     */
    public YearMonthStruct getCompletionDate() {
        return completionDate;
    }

    /**
     * Sets the value of the completionDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link YearMonthStruct }
     *     
     */
    public void setCompletionDate(YearMonthStruct value) {
        this.completionDate = value;
    }



    /**
     * Gets the value of the overallOfficial property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the overallOfficial property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOverallOfficial().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InvestigatorStruct }
     * 
     * 
     */
    public List<InvestigatorStruct> getOverallOfficial() {
        if (overallOfficial == null) {
            overallOfficial = new ArrayList<InvestigatorStruct>();
        }
        return this.overallOfficial;
    }
    
    public InvestigatorStruct getPrincipalInvestigator()
    {

        if (overallOfficial != null)
        {
            for (InvestigatorStruct official : overallOfficial)
            {
                if (ModelConstants.PRINCIPAL_INVESTIGATOR.equals(official.getRole()))
                {
                    return official;
                }
            }
        }

        return null;
    }

}
