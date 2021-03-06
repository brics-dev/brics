//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.08.31 at 05:32:23 AM EDT 
//


package gov.nih.tbi.ordermanager.coriell;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the gov.nih.tbi.ordermanager.coriell package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ShipToAddress2_QNAME = new QName("", "ShipToAddress2");
    private final static QName _FundingTimeline_QNAME = new QName("", "FundingTimeline");
    private final static QName _ShipToState_QNAME = new QName("", "ShipToState");
    private final static QName _Phone_QNAME = new QName("", "Phone");
    private final static QName _GUID_QNAME = new QName("", "GUID");
    private final static QName _OriginatorOrderId_QNAME = new QName("", "OriginatorOrderId");
    private final static QName _CoriellId_QNAME = new QName("", "CoriellId");
    private final static QName _LastName_QNAME = new QName("", "LastName");
    private final static QName _VisitDescription_QNAME = new QName("", "VisitDescription");
    private final static QName _Date_QNAME = new QName("", "Date");
    private final static QName _ShipToZip_QNAME = new QName("", "ShipToZip");
    private final static QName _OriginatorOrderTitle_QNAME = new QName("", "OriginatorOrderTitle");
    private final static QName _FirstName_QNAME = new QName("", "FirstName");
    private final static QName _OriginatorNote_QNAME = new QName("", "OriginatorNote");
    private final static QName _SpecimenType_QNAME = new QName("", "SpecimenType");
    private final static QName _ShipToAddress_QNAME = new QName("", "ShipToAddress");
    private final static QName _NumberOfAliquots_QNAME = new QName("", "NumberOfAliquots");
    private final static QName _ShipToName_QNAME = new QName("", "ShipToName");
    private final static QName _Originator_QNAME = new QName("", "Originator");
    private final static QName _Number_QNAME = new QName("", "Number");
    private final static QName _Fax_QNAME = new QName("", "Fax");
    private final static QName _ShipToCity_QNAME = new QName("", "ShipToCity");
    private final static QName _Subcollection_QNAME = new QName("", "Subcollection");
    private final static QName _PowerAnalysis_QNAME = new QName("", "PowerAnalysis");
    private final static QName _Title_QNAME = new QName("", "Title");
    private final static QName _Name_QNAME = new QName("", "Name");
    private final static QName _MiddleName_QNAME = new QName("", "MiddleName");
    private final static QName _Email_QNAME = new QName("", "Email");
    private final static QName _OriginatorEmail_QNAME = new QName("", "OriginatorEmail");
    private final static QName _OriginalContainerTypeReceived_QNAME = new QName("", "OriginalContainerTypeReceived");
    private final static QName _FundingAgency_QNAME = new QName("", "FundingAgency");
    private final static QName _ShipToOrganization_QNAME = new QName("", "ShipToOrganization");
    private final static QName _Abstract_QNAME = new QName("", "Abstract");
    private final static QName _ShipToPhone_QNAME = new QName("", "ShipToPhone");
    private final static QName _ShipToAffiliation_QNAME = new QName("", "ShipToAffiliation");
    private final static QName _ShipToAffiliationPhone_QNAME = new QName("", "ShipToAffiliationPhone");
    private final static QName _ShipToAffiliationEmail_QNAME = new QName("", "ShipToAffiliationEmail");
    private final static QName _ShipToAffiliationSpecialInstructions_QNAME = new QName("", "ShipToAffiliationSpecialInstructions");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: gov.nih.tbi.ordermanager.coriell
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Order }
     * 
     */
    public Order createOrder() {
        return new Order();
    }

    /**
     * Create an instance of {@link User }
     * 
     */
    public User createUser() {
        return new User();
    }

    /**
     * Create an instance of {@link SampleRequest }
     * 
     */
    public SampleRequest createSampleRequest() {
        return new SampleRequest();
    }

    /**
     * Create an instance of {@link Order.OrderMetadata }
     * 
     */
    public Order.OrderMetadata createOrderOrderMetadata() {
        return new Order.OrderMetadata();
    }

    /**
     * Create an instance of {@link Institution }
     * 
     */
    public Institution createInstitution() {
        return new Institution();
    }

    /**
     * Create an instance of {@link Grant }
     * 
     */
    public Grant createGrant() {
        return new Grant();
    }

    /**
     * Create an instance of {@link FundingStatus }
     * 
     */
    public FundingStatus createFundingStatus() {
        return new FundingStatus();
    }

    /**
     * Create an instance of {@link IsFunded }
     * 
     */
    public IsFunded createIsFunded() {
        return new IsFunded();
    }

    /**
     * Create an instance of {@link UnderReview }
     * 
     */
    public UnderReview createUnderReview() {
        return new UnderReview();
    }

    /**
     * Create an instance of {@link SampleRequests }
     * 
     */
    public SampleRequests createSampleRequests() {
        return new SampleRequests();
    }

    /**
     * Create an instance of {@link ShippingAddress }
     * 
     */
    public ShippingAddress createShippingAddress() {
        return new ShippingAddress();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ShipToAddress2")
    public JAXBElement<String> createShipToAddress2(String value) {
        return new JAXBElement<String>(_ShipToAddress2_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "FundingTimeline")
    public JAXBElement<String> createFundingTimeline(String value) {
        return new JAXBElement<String>(_FundingTimeline_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ShipToState")
    public JAXBElement<String> createShipToState(String value) {
        return new JAXBElement<String>(_ShipToState_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Phone")
    public JAXBElement<String> createPhone(String value) {
        return new JAXBElement<String>(_Phone_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "GUID")
    public JAXBElement<String> createGUID(String value) {
        return new JAXBElement<String>(_GUID_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "OriginatorOrderId")
    public JAXBElement<String> createOriginatorOrderId(String value) {
        return new JAXBElement<String>(_OriginatorOrderId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "CoriellId")
    public JAXBElement<String> createCoriellId(String value) {
        return new JAXBElement<String>(_CoriellId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "LastName")
    public JAXBElement<String> createLastName(String value) {
        return new JAXBElement<String>(_LastName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "VisitDescription")
    public JAXBElement<String> createVisitDescription(String value) {
        return new JAXBElement<String>(_VisitDescription_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Date")
    public JAXBElement<String> createDate(String value) {
        return new JAXBElement<String>(_Date_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ShipToZip")
    public JAXBElement<String> createShipToZip(String value) {
        return new JAXBElement<String>(_ShipToZip_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "OriginatorOrderTitle")
    public JAXBElement<String> createOriginatorOrderTitle(String value) {
        return new JAXBElement<String>(_OriginatorOrderTitle_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "FirstName")
    public JAXBElement<String> createFirstName(String value) {
        return new JAXBElement<String>(_FirstName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "OriginatorNote")
    public JAXBElement<String> createOriginatorNote(String value) {
        return new JAXBElement<String>(_OriginatorNote_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "SpecimenType")
    public JAXBElement<String> createSpecimenType(String value) {
        return new JAXBElement<String>(_SpecimenType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ShipToAddress")
    public JAXBElement<String> createShipToAddress(String value) {
        return new JAXBElement<String>(_ShipToAddress_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "NumberOfAliquots")
    public JAXBElement<String> createNumberOfAliquots(String value) {
        return new JAXBElement<String>(_NumberOfAliquots_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ShipToName")
    public JAXBElement<String> createShipToName(String value) {
        return new JAXBElement<String>(_ShipToName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Originator")
    public JAXBElement<String> createOriginator(String value) {
        return new JAXBElement<String>(_Originator_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Number")
    public JAXBElement<String> createNumber(String value) {
        return new JAXBElement<String>(_Number_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Fax")
    public JAXBElement<String> createFax(String value) {
        return new JAXBElement<String>(_Fax_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ShipToCity")
    public JAXBElement<String> createShipToCity(String value) {
        return new JAXBElement<String>(_ShipToCity_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Subcollection")
    public JAXBElement<String> createSubcollection(String value) {
        return new JAXBElement<String>(_Subcollection_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "PowerAnalysis")
    public JAXBElement<String> createPowerAnalysis(String value) {
        return new JAXBElement<String>(_PowerAnalysis_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Title")
    public JAXBElement<String> createTitle(String value) {
        return new JAXBElement<String>(_Title_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Name")
    public JAXBElement<String> createName(String value) {
        return new JAXBElement<String>(_Name_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "MiddleName")
    public JAXBElement<String> createMiddleName(String value) {
        return new JAXBElement<String>(_MiddleName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Email")
    public JAXBElement<String> createEmail(String value) {
        return new JAXBElement<String>(_Email_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "OriginatorEmail")
    public JAXBElement<String> createOriginatorEmail(String value) {
        return new JAXBElement<String>(_OriginatorEmail_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "OriginalContainerTypeReceived")
    public JAXBElement<String> createOriginalContainerTypeReceived(String value) {
        return new JAXBElement<String>(_OriginalContainerTypeReceived_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "FundingAgency")
    public JAXBElement<String> createFundingAgency(String value) {
        return new JAXBElement<String>(_FundingAgency_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ShipToOrganization")
    public JAXBElement<String> createShipToOrganization(String value) {
        return new JAXBElement<String>(_ShipToOrganization_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Abstract")
    public JAXBElement<String> createAbstract(String value) {
        return new JAXBElement<String>(_Abstract_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ShipToPhone")
    public JAXBElement<String> createShipToPhone(String value) {
        return new JAXBElement<String>(_ShipToPhone_QNAME, String.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ShipToAffiliation")
    public JAXBElement<String> createShipToAffiliation(String value) {
        return new JAXBElement<String>(_ShipToAffiliation_QNAME, String.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ShipToAffiliationPhone")
    public JAXBElement<String> createShipToAffiliationPhone(String value) {
        return new JAXBElement<String>(_ShipToAffiliationPhone_QNAME, String.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ShipToAffiliationEmail")
    public JAXBElement<String> createShipToAffiliationEmail(String value) {
        return new JAXBElement<String>(_ShipToAffiliationEmail_QNAME, String.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ShipToAffiliationSpecialInstructions")
    public JAXBElement<String> createShipToAffiliationSpecialInstructions(String value) {
        return new JAXBElement<String>(_ShipToAffiliationSpecialInstructions_QNAME, String.class, null, value);
    }
}
