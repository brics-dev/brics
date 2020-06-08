package gov.nih.tbi.ordermanager.iu;

import gov.nih.tbi.ordermanager.iu.Order.OrderMetadata;
import gov.nih.tbi.ordermanager.model.BioRepository;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Purpose of this class is to provide - Adapter to bridge BRICS and non-BRICS specific Order mappings - Marshalling &
 * Unmarshalling of objects to/from XML
 * 
 * @author blandi
 * 
 */

public class IUXMLWrapper
{

    // Coriell order
    private Order order;
    private BioRepository repo;

    private JAXBContext jaxbContext;
    private Marshaller jaxbMarshaller;
    private Unmarshaller jaxbUnmarshaller;

    public IUXMLWrapper(gov.nih.tbi.ordermanager.model.BiospecimenOrder bricsOrder) throws JAXBException
    {

        init();
        order = createSampleOrder(bricsOrder);
    }

    public IUXMLWrapper(gov.nih.tbi.ordermanager.model.BiospecimenOrder bricsOrder, BioRepository repo)
            throws JAXBException
    {

        init();
        this.repo = repo;
        order = createSampleOrder(bricsOrder);
    }

    
    public String toString() {
    	StringWriter writer = new StringWriter();
    	try {
			jaxbMarshaller.marshal(order, writer);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    	return writer.toString();
    	
    }

    
    public IUXMLWrapper(InputStream is) throws JAXBException
    {

        init();
        order = (Order) jaxbUnmarshaller.unmarshal(is);
    }

    private void init() throws JAXBException
    {

        jaxbContext = JAXBContext.newInstance(Order.class);
        jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }

    public Order createSampleOrder(gov.nih.tbi.ordermanager.model.BiospecimenOrder bricsOrder)
    {

        // Create the Order Meta
        order = new Order();
        order.setOrderMetadata(createOrderMetadata(bricsOrder));

        // Some pertinent information
        order.setUser(createIUlUser(bricsOrder));
        order.setInstitution(createInstitution(bricsOrder));
        order.setDate(new java.util.Date().toString());
        Grant grant = new Grant();
        grant.setAbstract(bricsOrder.getAbstractText());
        order.setGrant(grant);
        order.setPowerAnalysis("" + bricsOrder.getExperimentalDesignPowerAnalysis());

        // A mailing address
        order.setShippingAddress(createShippingAddress(bricsOrder));

        // Sample Request List Items
        SampleRequests reqsListElement = new SampleRequests();
        order.setSampleRequests(reqsListElement);
        for (gov.nih.tbi.ordermanager.model.BiospecimenItem bricsItem : bricsOrder.getRequestedItems())
        {
            if (repo == null || repo.getId().equals(bricsItem.getBioRepository().getId()))
                reqsListElement.getSampleRequest().add(createIUSampleRequest(bricsItem));
        }

        return order;
    }

    public Institution createInstitution(gov.nih.tbi.ordermanager.model.BiospecimenOrder bricsOrder)
    {

        Institution institution = new Institution();
        institution.setName("" + bricsOrder.getInstitution());

        return institution;
    }

    private User createIUlUser(gov.nih.tbi.ordermanager.model.BiospecimenOrder bricsOrder)
    {

        User user = new User();
        user.setEmail("" + bricsOrder.getUser().getEmail());
        user.setFirstName("" + bricsOrder.getUser().getFirstName());
        user.setLastName("" + bricsOrder.getUser().getLastName());
        user.setMiddleName(null);
        user.setPhone(bricsOrder.getPhone());
        user.setFax(null);
        user.setTitle(null);
        return user;
    }

    private OrderMetadata createOrderMetadata(gov.nih.tbi.ordermanager.model.BiospecimenOrder bricsOrder)
    {

        OrderMetadata metadata = new OrderMetadata();
        metadata.setOriginator("NINDS"); // XXX
        metadata.setOriginatorOrderId("" + bricsOrder.getId());
        metadata.getOriginatorEmail()
                .add(bricsOrder.getBracUser() != null ? bricsOrder.getBracUser().getEmail() : null);

        // Probably not
        // for(Comment comment: bricsOrder.getCommentList()) {
        // metadata.getOriginatorNote().add(comment.getDate()+":  "+comment.getMessage());
        // }
        return metadata;
    }

    private SampleRequest createIUSampleRequest(gov.nih.tbi.ordermanager.model.BiospecimenItem bricsRequestItem)
    {

        SampleRequest request = new SampleRequest();
        request.setCoriellId("" + bricsRequestItem.getCoriellId());
        request.setGUID("" + bricsRequestItem.getGuid());
        request.setSpecimenType("" + bricsRequestItem.getSampCollType());
        request.setOriginalContainerTypeReceived("" + bricsRequestItem.getOriginalContainerTypeReceived());
        // request.setSubcollection(null);
        request.setVisitDescription("" + bricsRequestItem.getVisitTypePDBP());
        request.setNumberOfAliquots("" + bricsRequestItem.getNumberOfAliquots());
        request.setRepository("" + bricsRequestItem.getBioRepository().getName()); 
        return request;
    }

    private ShippingAddress createShippingAddress(gov.nih.tbi.ordermanager.model.BiospecimenOrder bricsOrder)
    {

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setShipToName("" + bricsOrder.getShipToName());
        shippingAddress.setShipToOrganization("" + bricsOrder.getShipToInstitution());
        shippingAddress.setShipToPhone("" + bricsOrder.getPhone()); // TODO:
        shippingAddress.setShipToAffiliation("" + bricsOrder.getAffiliation());
        shippingAddress.setShipToAffiliationPhone("" + bricsOrder.getAffiliationPhone());
        shippingAddress.setShipToAffiliationEmail("" + bricsOrder.getAffiliationEmail());
        shippingAddress.setShipToAffiliationSpecialInstructions("" + bricsOrder.getAffiliationSpecialInstructions());
        shippingAddress.setShipToAddress("" + bricsOrder.getAddress().getAddress1());
        shippingAddress.setShipToAddress2("" + bricsOrder.getAddress().getAddress2());
        shippingAddress.setShipToCity("" + bricsOrder.getAddress().getCity());
        // shippingAddress.setShipToName(""+bricsOrder.getShipToName());
        shippingAddress.setShipToState("" + bricsOrder.getAddress().getOldState());
        shippingAddress.setShipToZip("" + bricsOrder.getAddress().getZipCode());
        return shippingAddress;
    }

    public void write(OutputStream out) throws JAXBException
    {

        jaxbMarshaller.marshal(order, out);
    }

    // public static void main(String[] args) {
    //
    // try {
    //
    // CoriellXMLWrapper testXML = new CoriellXMLWrapper();
    // // CoriellOrderTest testXML = new CoriellOrderTest(
    // // new FileInputStream("C:/BRICS/2.1/CoriellOrdering/Model/OrderSample.xml"));
    // testXML.dump(System.out);
    //
    // } catch (JAXBException e) {
    // e.printStackTrace();
    // // } catch (FileNotFoundException e) {
    // // e.printStackTrace();
    // }
    //
    //
    // }

}

/***
 * Sample File Content
 * ***/
// <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
// <Order>
// <Date>Friday, July 19, 2013</Date>
// <User>
// <FirstName>Joseph</FirstName>
// <MiddleName>Barry</MiddleName>
// <LastName>Landin</LastName>
// <Title>Researcher</Title>
// <Email>blandin@sapient.com</Email>
// <Phone>703-489-1028</Phone>
// <Fax>703-908-2401</Fax>
// </User>
// <Institution>
// <Name>NINDS</Name>
// </Institution>
// <Grant>
// <Title>Adverse Affects in Traumatized Elderly</Title>
// <Number>1209494</Number>
// <FundingStatus>
// <IsFunded>
// <FundingAgency>NIMH</FundingAgency>
// </IsFunded>
// <UnderReview>
// <FundingTimeline>N/A</FundingTimeline>
// <FundingAgency>N/A</FundingAgency>
// </UnderReview>
// </FundingStatus>
// </Grant>
// <SampleRequests>
// <SampleRequest>
// <CoriellId>FF_12345</CoriellId>
// <SampleRefId>ABC24499DF</SampleRefId>
// <SpecimenType>Blood</SpecimenType>
// <OriginalContainerTypeReceived>Vial</OriginalContainerTypeReceived>
// <Subcollection>N/A</Subcollection>
// <VisitDescription>N/A</VisitDescription>
// <NumberOfAliquots>3</NumberOfAliquots>
// </SampleRequest>
// <SampleRequest>
// <CoriellId>FG_22342</CoriellId>
// <SampleRefId>CDF24A99DF</SampleRefId>
// <SpecimenType>Blood</SpecimenType>
// <OriginalContainerTypeReceived>Vial</OriginalContainerTypeReceived>
// <Subcollection>N/A</Subcollection>
// <VisitDescription>N/A</VisitDescription>
// <NumberOfAliquots>1</NumberOfAliquots>
// </SampleRequest>
// </SampleRequests>
// </Order>


