package gov.nih.tbi.semantic.model;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;

import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

public class DataElementRDFTest {
	@Test
	public void permissibleValueClassTest() {
		ValueRange vr = new ValueRange();
		vr.setValueRange("value");
		String expected = "http://ninds.nih.gov/dictionary/ibis/1.0/Element/DataElement/de/permissibleValue/value";
		String actual = DataElementRDF.createPermissibleValueResource("de", vr).toString();
		assertEquals(actual, expected);
	}

	@Test
	public void determineDataTypeTestAlphanumeric() {
		StructuralDataElement testDe = new StructuralDataElement();
		testDe.setType(DataType.ALPHANUMERIC);
		XSDDatatype actualXSDType = DataElementRDF.determineDataType(testDe);
		assertEquals(XSDDatatype.XSDstring, actualXSDType);
	}

	@Test
	public void determineDataTypeTestFile() {
		StructuralDataElement testDe = new StructuralDataElement();
		testDe.setType(DataType.FILE);
		XSDDatatype actualXSDType = DataElementRDF.determineDataType(testDe);
		assertEquals(XSDDatatype.XSDstring, actualXSDType);
	}

	@Test
	public void determineDataTypeTestThumbnail() {
		StructuralDataElement testDe = new StructuralDataElement();
		testDe.setType(DataType.THUMBNAIL);
		XSDDatatype actualXSDType = DataElementRDF.determineDataType(testDe);
		assertEquals(XSDDatatype.XSDstring, actualXSDType);
	}

	@Test
	public void determineDataTypeTestGUID() {
		StructuralDataElement testDe = new StructuralDataElement();
		testDe.setType(DataType.GUID);
		XSDDatatype actualXSDType = DataElementRDF.determineDataType(testDe);
		assertEquals(XSDDatatype.XSDstring, actualXSDType);
	}

	@Test
	public void determineDataTypeTestBiosample() {
		StructuralDataElement testDe = new StructuralDataElement();
		testDe.setType(DataType.BIOSAMPLE);
		XSDDatatype actualXSDType = DataElementRDF.determineDataType(testDe);
		assertEquals(XSDDatatype.XSDstring, actualXSDType);
	}

	@Test
	public void determineDataTypeTestNumericTest() {
		StructuralDataElement testDe = new StructuralDataElement();
		testDe.setRestrictions(InputRestrictions.FREE_FORM);
		testDe.setType(DataType.NUMERIC);
		XSDDatatype actualXSDType = DataElementRDF.determineDataType(testDe);
		assertEquals(XSDDatatype.XSDdecimal, actualXSDType);
	}

	@Test
	public void determineDataTypeTestNumericTest2() {
		StructuralDataElement testDe = new StructuralDataElement();
		testDe.setRestrictions(InputRestrictions.SINGLE);
		testDe.setType(DataType.NUMERIC);
		XSDDatatype actualXSDType = DataElementRDF.determineDataType(testDe);
		assertEquals(XSDDatatype.XSDdecimal, actualXSDType);
	}

	@Test
	public void determineDataTypeTestNumericTest3() {
		StructuralDataElement testDe = new StructuralDataElement();
		testDe.setRestrictions(InputRestrictions.MULTIPLE);
		testDe.setType(DataType.NUMERIC);
		XSDDatatype actualXSDType = DataElementRDF.determineDataType(testDe);
		assertEquals(XSDDatatype.XSDstring, actualXSDType);
	}

	@Test
	public void determineDataTypeTestDate() {
		StructuralDataElement testDe = new StructuralDataElement();
		testDe.setType(DataType.DATE);
		XSDDatatype actualXSDType = DataElementRDF.determineDataType(testDe);
		assertEquals(XSDDatatype.XSDdateTime, actualXSDType);
	}
}

