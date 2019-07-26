package gov.nih.tbi.dictionary.service.schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.util.XSDConstants;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public final class SchemaGenerationService {

	private static Logger logger = Logger.getLogger(SchemaGenerationService.class);
	
	private SchemaGenerationService() {
		throw new UnsupportedOperationException();
	}

	/**
	 * this takes a structural form structure and converts it into an XML DOC
	 * @param structure - structural form structure
	 * @return
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static byte[] writeStructureSchema(StructuralFormStructure structure)
			throws ParserConfigurationException, TransformerException, SAXException, IOException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		Element rootSchema = doc.createElement(XSDConstants.SCHEMA_TAG);
		addAttributeToElement(doc, rootSchema, XSDConstants.SOURCE_ATTR, XSDConstants.SOURCE_VAL);
		doc.appendChild(rootSchema);
		
		
		//TODO: This block is a quick addition to add the requirement of a Dataset root tag.
		//The Dataset root tag was originally left out to allow for more modular use of the
		//structure schema generation.
		//Further discussion should be had to finalize schema design.
		//This does not currently make use of the schema framework. -TT 10/16/15
		/////////////////////////////////////////////////////////////////////////////////////
		Element datasetElement = doc.createElement(XSDConstants.ELEMENT_TAG);
		addAttributeToElement(doc, datasetElement, XSDConstants.NAME_ATTR, XSDConstants.DATASET_VAL);
		rootSchema.appendChild(datasetElement);
		
		Element datasetType = doc.createElement(XSDConstants.COMPLEX_TYPE_TAG);
		datasetElement.appendChild(datasetType);
		
		Element datasetSequence = doc.createElement(XSDConstants.SEQUENCE_TAG);
		datasetType.appendChild(datasetSequence);
		
		Element datasetURIElement = doc.createElement(XSDConstants.ATTR_TAG);
		addAttributeToElement(doc, datasetURIElement, XSDConstants.NAME_ATTR, XSDConstants.URI_VAL);
		addAttributeToElement(doc, datasetURIElement, XSDConstants.TYPE_ATTR, XSDConstants.STRING_TYPE_VAL);
		datasetType.appendChild(datasetURIElement);
		/////////////////////////////////////////////////////////////////////////////////////

		
		Element structureElement = doc.createElement(XSDConstants.ELEMENT_TAG);
		addAttributeToElement(doc, structureElement, XSDConstants.NAME_ATTR,
				structure.getShortName());
//		rootSchema.appendChild(structureElement);
		datasetSequence.appendChild(structureElement);

		// write sequence of repeatable groups
		if (structure.getRepeatableGroups().size() > 0) {

			Element structureType = doc.createElement(XSDConstants.COMPLEX_TYPE_TAG);
			structureElement.appendChild(structureType);

			Element structureSequence = doc.createElement(XSDConstants.SEQUENCE_TAG);
			structureType.appendChild(structureSequence);

			for (RepeatableGroup group : structure.getRepeatableGroups()) {
				Element groupElement = doc.createElement(XSDConstants.ELEMENT_TAG);
				addAttributeToElement(doc, groupElement, XSDConstants.NAME_ATTR, group
						.getName().replaceAll("\\s", ""));

				if (group.getThreshold() == 0) {
					addAttributeToElement(doc, groupElement, XSDConstants.MIN_ATTR, group
							.getThreshold().toString());
					addAttributeToElement(doc, groupElement, XSDConstants.MAX_ATTR,
							XSDConstants.UNBOUNDED_VAL);
				} else if (group.getType() == RepeatableType.LESSTHAN) {
					addAttributeToElement(doc, groupElement, XSDConstants.MAX_ATTR, group
							.getThreshold().toString());
				} else if (group.getType() == RepeatableType.MORETHAN) {
					addAttributeToElement(doc, groupElement, XSDConstants.MIN_ATTR, group
							.getThreshold().toString());
					addAttributeToElement(doc, groupElement, XSDConstants.MAX_ATTR,
							XSDConstants.UNBOUNDED_VAL);
				} else if (group.getType() == RepeatableType.EXACTLY) {
					addAttributeToElement(doc, groupElement, XSDConstants.MIN_ATTR, group
							.getThreshold().toString());
					addAttributeToElement(doc, groupElement, XSDConstants.MAX_ATTR, group
							.getThreshold().toString());
				}

				// write sequence of data elements
				if (group.getDataElements().size() > 0) {

					structureSequence.appendChild(groupElement);

					Element groupTypeElement = doc
							.createElement(XSDConstants.COMPLEX_TYPE_TAG);
					groupElement.appendChild(groupTypeElement);
					Element groupSequenceElement = doc
							.createElement(XSDConstants.SEQUENCE_TAG);
					groupTypeElement.appendChild(groupSequenceElement);

					for (MapElement element : group.getDataElements()) {

						Element includeElement = buildElementBlock(doc,
								element.getStructuralDataElement());

						if (element.getRequiredType() == RequiredType.REQUIRED) {
							addAttributeToElement(doc, includeElement,
									XSDConstants.MIN_ATTR, "1");
						} else {
							addAttributeToElement(doc, includeElement,
									XSDConstants.MIN_ATTR, "0");
						}

						groupSequenceElement.appendChild(includeElement);
					}
				}
			}

			Element structureURIElement = doc.createElement(XSDConstants.ATTR_TAG);
			addAttributeToElement(doc, structureURIElement, XSDConstants.NAME_ATTR, XSDConstants.URI_VAL);
			addAttributeToElement(doc, structureURIElement, XSDConstants.TYPE_ATTR,
					XSDConstants.STRING_TYPE_VAL);
			structureType.appendChild(structureURIElement);
		}

		//format the document into a byte[] to return
		return formatXML(doc);
	}

	/*
	 * this method creates schema for a structural data element
	 */
	public static byte[] writeElementSchema(StructuralDataElement dataElement)
			throws ParserConfigurationException, TransformerException, SAXException, IOException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		Element rootSchema = doc.createElement(XSDConstants.SCHEMA_TAG);
		addAttributeToElement(doc, rootSchema, XSDConstants.SOURCE_ATTR, XSDConstants.SOURCE_VAL);

		Element fullElement = buildElementBlock(doc, dataElement);

		rootSchema.appendChild(fullElement);
		doc.appendChild(rootSchema);

		return formatXML(doc);
	}

	/*
	 * build schema for a structural data element
	 */
	private static Element buildElementBlock(Document doc, StructuralDataElement dataElement) throws ParserConfigurationException {

		Element rootElement = doc.createElement(XSDConstants.ELEMENT_TAG);
		addAttributeToElement(doc, rootElement, XSDConstants.NAME_ATTR,
				dataElement.getName());

		String type = determineRestrictionType(dataElement);

		Element restrictionElement = doc.createElement("Empty");

		if (dataElement.getRestrictions() == InputRestrictions.FREE_FORM) {

			Element simpleType = doc.createElement(XSDConstants.SIMPLE_TYPE_TAG);
			rootElement.appendChild(simpleType);

			if (dataElement.getType() == DataType.DATE) {
				restrictionElement = buildDateBlock(doc);
			}

			else if (dataElement.getType() == DataType.ALPHANUMERIC) {
				restrictionElement = buildStringBlock(doc, type,
						dataElement.getSize());
			}

			else if (dataElement.getType() == DataType.NUMERIC) {
				restrictionElement = buildNumericBlock(doc, type,
						dataElement.getMinimumValueString(),
						dataElement.getMaximumValueString());
			}

			else if (dataElement.getType() == DataType.BIOSAMPLE) {
				restrictionElement = buildBiosampleBlock(doc, type);
			}

			else if (dataElement.getType() == DataType.GUID) {
				restrictionElement = buildGuidBlock(doc, type);
			}

			else if (dataElement.getType() == DataType.FILE) {
				restrictionElement = buildFileBlock(doc, type);
			}
			
			else if (dataElement.getType() == DataType.TRIPLANAR) {
				restrictionElement = buildFileBlock(doc, type);
			}

			else if (dataElement.getType() == DataType.THUMBNAIL) {
				restrictionElement = buildThumbBlock(doc, type);
			}

			simpleType.appendChild(restrictionElement);

		}

		else {

			if (dataElement.getRestrictions() == InputRestrictions.MULTIPLE) {
				restrictionElement = buildMultiselectBlock(doc, type,
						dataElement.getValueRangeList());

				Element unique = doc.createElement(XSDConstants.UNIQUE_TAG);
				addAttributeToElement(doc, unique, XSDConstants.NAME_ATTR,
						dataElement.getName() + "Select");

				Element uniqueSelector = doc.createElement(XSDConstants.SELECTOR_TAG);
				addAttributeToElement(doc, uniqueSelector, XSDConstants.XPATH_ATTR,
						"value");
				Element uniqueField = doc.createElement(XSDConstants.FIELD_TAG);
				addAttributeToElement(doc, uniqueField, XSDConstants.XPATH_ATTR, ".");

				unique.appendChild(uniqueSelector);
				unique.appendChild(uniqueField);

				rootElement.appendChild(restrictionElement);
				rootElement.appendChild(unique);
			}

			else if (dataElement.getRestrictions() == InputRestrictions.SINGLE) {
				Element simpleType = doc.createElement(XSDConstants.SIMPLE_TYPE_TAG);
				rootElement.appendChild(simpleType);

				restrictionElement = buildEnumBlock(doc, type,
						dataElement.getValueRangeList());

				simpleType.appendChild(restrictionElement);
			}
		}

		return rootElement;

	}

	/*
	 * add attribute information for a data element
	 */
	private static void addAttributeToElement(Document doc, Element element,
			String attrName, String attrVal) {
		Attr attr = doc.createAttribute(attrName);
		attr.setValue(attrVal);
		element.setAttributeNode(attr);
	}

	/*
	 * creation of enum for multiselect data element
	 */
	private static Element buildMultiselectBlock(Document doc, String type,
			Set<ValueRange> valueRanges) {

		Element complexType = doc.createElement(XSDConstants.COMPLEX_TYPE_TAG);
		Element multiSequence = doc.createElement(XSDConstants.SEQUENCE_TAG);
		Element multiSelect = doc.createElement(XSDConstants.ELEMENT_TAG);
		addAttributeToElement(doc, multiSelect, XSDConstants.MIN_ATTR, "0");
		addAttributeToElement(doc, multiSelect, XSDConstants.MAX_ATTR,
				String.valueOf(valueRanges.size()));
		addAttributeToElement(doc, multiSelect, XSDConstants.NAME_ATTR, XSDConstants.MULTI_SELECT_TAG);
		Element simpleType = doc.createElement(XSDConstants.SIMPLE_TYPE_TAG);
		Element restrictionElement = buildEnumBlock(doc, type, valueRanges);

		complexType.appendChild(multiSequence);
		multiSequence.appendChild(multiSelect);
		multiSelect.appendChild(simpleType);
		simpleType.appendChild(restrictionElement);

		Element unique = doc.createElement(XSDConstants.UNIQUE_TAG);
		addAttributeToElement(doc, unique, XSDConstants.NAME_ATTR, "uniqueSelect");

		return complexType;

	}

	/*
	 * create schema for the enum values
	 */
	private static Element buildEnumBlock(Document doc, String type,
			Set<ValueRange> valueRanges) {
		Element restrictionElement = doc.createElement(XSDConstants.RESTRICTION_TAG);
		addAttributeToElement(doc, restrictionElement, XSDConstants.BASE_ATTR, type);

		for (ValueRange valueRange : valueRanges) {
			Element enumeration = doc.createElement(XSDConstants.ENUMERATION_TAG);
			addAttributeToElement(doc, enumeration, XSDConstants.VALUE_ATTR,
					valueRange.getValueRange());
			restrictionElement.appendChild(enumeration);
		}

		return restrictionElement;
	}

	/*
	 * create schema for a aplanumeric type data element
	 */
	private static Element buildStringBlock(Document doc, String type,
			Integer maxLength) {

		if (maxLength == null) {
			maxLength = XSDConstants.MAX_LENGTH_DEFAULT;
		}

		Element restrictionElement = doc.createElement(XSDConstants.RESTRICTION_TAG);
		addAttributeToElement(doc, restrictionElement, XSDConstants.BASE_ATTR, type);

		Element minLengthElement = doc.createElement(XSDConstants.MIN_LENGTH_TAG);
		addAttributeToElement(doc, minLengthElement, XSDConstants.VALUE_ATTR, "1");
		Element maxLengthElement = doc.createElement(XSDConstants.MAX_LENGTH_TAG);
		addAttributeToElement(doc, maxLengthElement, XSDConstants.VALUE_ATTR, maxLength.toString());

		restrictionElement.appendChild(minLengthElement);
		restrictionElement.appendChild(maxLengthElement);

		return restrictionElement;
	}

	/*
	 * create schema for a numeric data element
	 */
	private static Element buildNumericBlock(Document doc, String type,
			String minVal, String maxVal) {
		Element restrictionElement = doc.createElement(XSDConstants.RESTRICTION_TAG);
		addAttributeToElement(doc, restrictionElement, XSDConstants.BASE_ATTR, type);

		if(minVal != null && !minVal.equals("")){
			Element minElement = doc.createElement(XSDConstants.MIN_TAG);
			addAttributeToElement(doc, minElement, XSDConstants.VALUE_ATTR, minVal);
			restrictionElement.appendChild(minElement);
		}
		
		if(minVal != null && !minVal.equals("")){
			Element maxElement = doc.createElement(XSDConstants.MAX_TAG);
			addAttributeToElement(doc, maxElement, XSDConstants.VALUE_ATTR, maxVal);
			restrictionElement.appendChild(maxElement);
		}

		return restrictionElement;
	}

	/*
	 * create schema for a date data element
	 * This validates with date AND time. time can be 00:00, but it has to be there
	 */
	private static Element buildDateBlock(Document doc) {

		Element restrictionElement = doc.createElement(XSDConstants.RESTRICTION_TAG);
		addAttributeToElement(doc, restrictionElement, XSDConstants.BASE_ATTR,
				XSDConstants.DATE_TIME_TYPE_VAL);

		return restrictionElement;
	}

	/*
	 * create schema for a GUID data element
	 */
	private static Element buildGuidBlock(Document doc, String type) {
		Element restrictionElement = doc.createElement(XSDConstants.RESTRICTION_TAG);
		addAttributeToElement(doc, restrictionElement, XSDConstants.BASE_ATTR, type);

		Element patternElement = doc.createElement(XSDConstants.PATTERN_TAG);
		addAttributeToElement(doc, patternElement, XSDConstants.VALUE_ATTR, XSDConstants.GUID_PATTERN);

		restrictionElement.appendChild(patternElement);

		return restrictionElement;
	}

	/*
	 * create schema for a file OR triplanar data element
	 */
	private static Element buildFileBlock(Document doc, String type) {
		Element restrictionElement = doc.createElement(XSDConstants.RESTRICTION_TAG);
		addAttributeToElement(doc, restrictionElement, XSDConstants.BASE_ATTR, type);

		Element patternElement = doc.createElement(XSDConstants.PATTERN_TAG);
		addAttributeToElement(doc, patternElement, XSDConstants.VALUE_ATTR, XSDConstants.FILE_PATTERN);

		restrictionElement.appendChild(patternElement);

		return restrictionElement;
	} 

	/*
	 * create schema for a thumbnail
	 */
	private static Element buildThumbBlock(Document doc, String type) {
		Element restrictionElement = doc.createElement(XSDConstants.RESTRICTION_TAG);
		addAttributeToElement(doc, restrictionElement, XSDConstants.BASE_ATTR, type);

		Element patternElement = doc.createElement(XSDConstants.PATTERN_TAG);
		addAttributeToElement(doc, patternElement, XSDConstants.VALUE_ATTR, XSDConstants.THUMB_PATTERN);

		restrictionElement.appendChild(patternElement);

		return restrictionElement;
	}

	/*
	 * create biosample element type. min = 1, max == 100
	 */
	private static Element buildBiosampleBlock(Document doc, String type) {
		Element restrictionElement = doc.createElement(XSDConstants.RESTRICTION_TAG);
		addAttributeToElement(doc, restrictionElement, XSDConstants.BASE_ATTR, type);
		
		Element maxLengthElement = doc.createElement(XSDConstants.MAX_LENGTH_TAG);
		addAttributeToElement(doc, maxLengthElement, XSDConstants.VALUE_ATTR, Integer.toString(XSDConstants.MAX_BIOSAMPLE_LENGTH));
		Element minElement = doc.createElement(XSDConstants.MIN_LENGTH_TAG);
		addAttributeToElement(doc, minElement, XSDConstants.VALUE_ATTR, Integer.toString(XSDConstants.MIN_BIOSAMPLE_LENGTH));

		restrictionElement.appendChild(maxLengthElement);
		restrictionElement.appendChild(minElement);
		
		return restrictionElement;
	}

	/*
	 * create schema rule for data element restriction type
	 */
	private static String determineRestrictionType(StructuralDataElement element) {
		String type = "";

		if (element.getType() == DataType.NUMERIC) {
			type = XSDConstants.DECIMAL_TYPE_VAL;
		} else if (element.getType() == DataType.DATE) {
			type = XSDConstants.DATE_TIME_TYPE_VAL;
		} else {
			type = XSDConstants.STRING_TYPE_VAL;
		}

		return type;

	}

	/*
	 * This method takes a W3C document and converts it into a byte []
	 */
	private static byte[] formatXML(Document doc) throws TransformerException, ParserConfigurationException, SAXException, IOException {
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(bos);
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		byte[] schemaDocument = bos.toByteArray();
		
		//verify document is well formed
		wellFormed(schemaDocument);
		
		return schemaDocument;
	}
	
	/*
	 * This document verifies the document is well formed
	 * any exception is thrown to be handled by the manager
	 */
	private static void wellFormed(byte[] schemaDocument) throws TransformerException, ParserConfigurationException, SAXException, IOException {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		docBuilder.parse(new ByteArrayInputStream(schemaDocument));
	}
}