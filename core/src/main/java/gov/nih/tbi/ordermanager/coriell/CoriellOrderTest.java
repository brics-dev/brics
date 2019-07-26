package gov.nih.tbi.ordermanager.coriell;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class CoriellOrderTest {

	private Order order;
	private JAXBContext jaxbContext;
	private Marshaller jaxbMarshaller;
	private Unmarshaller jaxbUnmarshaller;
	 
	public CoriellOrderTest() throws JAXBException {
		setup();
		order = createSampleOrder();
	}
	
	public CoriellOrderTest(InputStream is) throws JAXBException {
		setup();
		order = (Order)jaxbUnmarshaller.unmarshal(is);
	}
	
	private void setup() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(Order.class);
		jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	}
	
	
	public Order createSampleOrder() {
		
		// Order Meta
		order = new Order();
		order.setUser(createSampleUser());
		order.setDate("5-5-12");
		order.setGrant(new Grant());
		order.setInstitution(new Institution());
		
		// Sample Requests
		SampleRequests reqsListElement = new SampleRequests();
		reqsListElement.getSampleRequest().addAll(Arrays.asList(
				createSampleRequest(),
				createSampleRequest(),
				createSampleRequest(),
				createSampleRequest()));
		order.setSampleRequests(reqsListElement);
		
		return order;
	}
	
	
	private User createSampleUser() {
		
		User user = new User();
		user.setEmail("blandin@email.com");
		user.setFirstName("Barry");
		user.setLastName("Landin");
		user.setMiddleName(null);
		user.setPhone("703-555-1212");
		user.setFax("703-555-1213");
		user.setTitle("Dotore");
		return user;
	}
	
	private SampleRequest createSampleRequest() {
		SampleRequest request = new SampleRequest();
		request.setCoriellId("C:12383442");
		request.setNumberOfAliquots("5");
		request.setOriginalContainerTypeReceived("Vial");
		request.setGUID("GUID:AAE33G5DDD");
		request.setSpecimenType("MySpecType");
		request.setSubcollection("SomeSubCollection");
		request.setVisitDescription("V01");
		return request;
	}
	
	
	public void dump(OutputStream out) throws JAXBException {
		jaxbMarshaller.marshal(order, out);
	}
	
	
	
	public static void main(String[] args) {

		try {
			
			CoriellOrderTest testXML = new CoriellOrderTest();
//			CoriellOrderTest testXML = new CoriellOrderTest(
//					new FileInputStream("C:/BRICS/2.1/CoriellOrdering/Model/OrderSample.xml"));
			testXML.dump(System.out);
			
		} catch (JAXBException e) {
			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
		}
		
		
	}

}


/***
 *  Sample File Content
 *  ***/
//<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
//<Order>
//    <Date>Friday, July 19, 2013</Date>
//    <User>
//        <FirstName>Joseph</FirstName>
//        <MiddleName>Barry</MiddleName>
//        <LastName>Landin</LastName>
//        <Title>Researcher</Title>
//        <Email>REPLACED</Email>
//        <Phone>703-489-1028</Phone>
//        <Fax>703-908-2401</Fax>
//    </User>
//    <Institution>
//        <Name>NINDS</Name>
//    </Institution>
//    <Grant>
//        <Title>Adverse Affects in Traumatized Elderly</Title>
//        <Number>1209494</Number>
//        <FundingStatus>
//            <IsFunded>
//                <FundingAgency>NIMH</FundingAgency>
//            </IsFunded>
//            <UnderReview>
//                <FundingTimeline>N/A</FundingTimeline>
//                <FundingAgency>N/A</FundingAgency>
//            </UnderReview>
//        </FundingStatus>
//    </Grant>
//    <SampleRequests>
//        <SampleRequest>
//            <CoriellId>FF_12345</CoriellId>
//            <SampleRefId>ABC24499DF</SampleRefId>
//            <SpecimenType>Blood</SpecimenType>
//            <OriginalContainerTypeReceived>Vial</OriginalContainerTypeReceived>
//            <Subcollection>N/A</Subcollection>
//            <VisitDescription>N/A</VisitDescription>
//            <NumberOfAliquots>3</NumberOfAliquots>
//        </SampleRequest>
//        <SampleRequest>
//            <CoriellId>FG_22342</CoriellId>
//            <SampleRefId>CDF24A99DF</SampleRefId>
//            <SpecimenType>Blood</SpecimenType>
//            <OriginalContainerTypeReceived>Vial</OriginalContainerTypeReceived>
//            <Subcollection>N/A</Subcollection>
//            <VisitDescription>N/A</VisitDescription>
//            <NumberOfAliquots>1</NumberOfAliquots>
//        </SampleRequest>
//    </SampleRequests>
//</Order>

