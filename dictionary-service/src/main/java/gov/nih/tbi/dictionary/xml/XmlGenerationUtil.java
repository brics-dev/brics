package gov.nih.tbi.dictionary.xml;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

public class XmlGenerationUtil {

	private static Logger logger = Logger.getLogger(XmlGenerationUtil.class);

	public static StringWriter generateDataElementXml(DataElement dataElement, List<String> fields,
			HttpServletRequest req)
			throws ParserConfigurationException, TransformerException {

		Document doc = createDomDocument();

		Element root = doc.createElement("dataElement");
		doc.appendChild(root);

		Element name = doc.createElement("name");
		name.setTextContent(dataElement.getName());
		root.appendChild(name);

		Element version = doc.createElement("version");
		version.setTextContent(dataElement.getVersion());
		root.appendChild(version);

		if (fields == null || fields.contains("status")) {
			Element e = doc.createElement("status");
			e.setTextContent(dataElement.getStatus().getName().toUpperCase());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("category")) {
			Element e = doc.createElement("category");
			e.setTextContent(dataElement.getCategory().getName().toUpperCase());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("title")) {
			Element e = doc.createElement("title");
			e.setTextContent(dataElement.getTitle());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("shortDescription")) {
			Element e = doc.createElement("shortDescription");
			e.setTextContent(dataElement.getShortDescription());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("definition")) {
			Element e = doc.createElement("definition");
			e.setTextContent(dataElement.getDescription());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("notes")) {
			Element e = doc.createElement("notes");
			e.setTextContent(dataElement.getNotes());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("historicalNotes")) {
			Element e = doc.createElement("historicalNotes");
			e.setTextContent(dataElement.getHistoricalNotes());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("references")) {
			Element e = doc.createElement("references");
			e.setTextContent(dataElement.getReferences());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("shortDescription")) {
			Element e = doc.createElement("shortDescription");
			e.setTextContent(dataElement.getShortDescription());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("type")) {
			Element e = doc.createElement("type");
			e.setTextContent(dataElement.getType().getValue().toUpperCase());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("restrictions")) {
			Element e = doc.createElement("restrictions");
			e.setTextContent(dataElement.getRestrictions().getValue().toUpperCase());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("unit")) {
			Element e = doc.createElement("unit");
			e.setTextContent(
					dataElement.getMeasuringUnit() == null ? "" : dataElement.getMeasuringUnit().getDisplayName());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("maximumLength")) {
			Element e = doc.createElement("maximumLength");
			e.setTextContent(dataElement.getSize() == null ? "" : dataElement.getSize().toString());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("maximumValue")) {
			Element e = doc.createElement("maximumValue");
			e.setTextContent(dataElement.getMaximumValue() == null ? "" : dataElement.getMaximumValue().toString());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("minimumValue")) {
			Element e = doc.createElement("minimumValue");
			e.setTextContent(dataElement.getMinimumValue() == null ? "" : dataElement.getMinimumValue().toString());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("permissibleValues")) {
			Element list = doc.createElement("permissibleValues");

			if (dataElement.getValueRangeList() != null) {
				for (ValueRange vr : dataElement.getValueRangeList()) {
					Element permValue = doc.createElement("permissibleValue");
					Element value = doc.createElement("value");
					value.setTextContent(vr.getValueRange());
					Element description = doc.createElement("description");
					description.setTextContent(vr.getDescription() == null ? "" : vr.getDescription());

					permValue.appendChild(value);
					permValue.appendChild(description);
					list.appendChild(permValue);
				}
			}

			root.appendChild(list);
		}

		if (fields == null || fields.contains("population")) {
			Element e = doc.createElement("population");
			e.setTextContent(dataElement.getPopulation().getName().toUpperCase());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("guidelines")) {
			Element e = doc.createElement("guidelines");
			e.setTextContent(dataElement.getGuidelines());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("suggestedQuestion")) {
			Element e = doc.createElement("suggestedQuestion");
			e.setTextContent(dataElement.getSuggestedQuestion());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("submitter")) {
			Element parent = doc.createElement("submitter");

			Element orgName = doc.createElement("organizationName");
			orgName.setTextContent(
					dataElement.getSubmittingOrgName() == null ? "" : dataElement.getSubmittingOrgName());
			parent.appendChild(orgName);

			Element contactName = doc.createElement("contactName");
			contactName.setTextContent(
					dataElement.getSubmittingContactName() == null ? "" : dataElement.getSubmittingContactName());
			parent.appendChild(contactName);

			Element contactInfo = doc.createElement("contactInformation");
			contactInfo.setTextContent(
					dataElement.getSubmittingContactInfo() == null ? "" : dataElement.getSubmittingContactInfo());
			parent.appendChild(contactInfo);

			root.appendChild(parent);
		}

		if (fields == null || fields.contains("steward")) {
			Element parent = doc.createElement("steward");

			Element orgName = doc.createElement("organizationName");
			orgName.setTextContent(dataElement.getStewardOrgName() == null ? "" : dataElement.getStewardOrgName());
			parent.appendChild(orgName);

			Element contactName = doc.createElement("contactName");
			contactName.setTextContent(
					dataElement.getStewardContactName() == null ? "" : dataElement.getStewardContactName());
			parent.appendChild(contactName);

			Element contactInfo = doc.createElement("contactInformation");
			contactInfo.setTextContent(
					dataElement.getStewardContactInfo() == null ? "" : dataElement.getStewardContactInfo());
			parent.appendChild(contactInfo);

			root.appendChild(parent);
		}

		// None of the SubDomainElement objects in this list are guaranteed to be in any kind of
		// order!
		if (fields == null || fields.contains("diseases")) {
			Element diseaseRoot = doc.createElement("diseases");
			root.appendChild(diseaseRoot);

			// ALL THE ELEMENTS WE ARE GOING TO HAVE TO CHECK FOR EXISTANCE OR REFERENCE.
			// A map that maps disease names to the element tag for that DISEASE and another one for
			// that tag's domian list (so we don't have to look it up later).
			Map<String, Element> diseaseMap = new HashMap<String, Element>();
			Map<String, Element> domainListMap = new HashMap<String, Element>();
			// A map that maps a disease/domain combo to the element tag for that DOMAIN and another
			// one for that tag's subdomain list(so we don't have to look it up later).
			Map<String, Element> domainMap = new HashMap<String, Element>();
			Map<String, Element> subdomainListMap = new HashMap<String, Element>();

			// processing loop
			for (SubDomainElement sde : dataElement.getSubDomainElementList()) {


				String diseaseName = sde.getDisease().getName();
				String domainName = sde.getDomain().getName();
				String subdomainName = sde.getSubDomain().getName();

				// Get or create the disease element tag.
				Element diseaseElement = diseaseMap.get(diseaseName);
				if (diseaseElement == null) { // disease tag doesn't exist yet (need to create)
					diseaseElement = doc.createElement("disease");
					diseaseRoot.appendChild(diseaseElement);
					
					Element nameTag = doc.createElement("name");
					nameTag.setTextContent(diseaseName);
					diseaseElement.appendChild(nameTag);
					
					Element domainsTag = doc.createElement("domains");
					diseaseElement.appendChild(domainsTag);
					
					Element subgroupsTag = doc.createElement("subgroups");
					diseaseElement.appendChild(subgroupsTag);

					// Store in maps for lookup later.
					diseaseMap.put(diseaseName, diseaseElement);
					domainListMap.put(diseaseName, domainsTag);

				}

				// Get or create the the domain
				Element domainElement = domainMap.get(diseaseName + domainName);
				if(domainElement == null) { // domain tag doesn't exist yet (need to create)

					// Get the domains tag for this disease
					Element domainParent = domainListMap.get(diseaseName);

					// Create new domain tag
					domainElement = doc.createElement("domain");
					domainParent.appendChild(domainElement);

					Element nameTag = doc.createElement("name");
					nameTag.setTextContent(domainName);
					domainElement.appendChild(nameTag);

					Element subsTag = doc.createElement("subdomains");
					domainElement.appendChild(subsTag);

					domainMap.put(diseaseName + domainName, domainElement);
					subdomainListMap.put(diseaseName + domainName, subsTag);
				}
				
				// Create the subdomain tag
				Element subdomainTag = doc.createElement("subdomain");
				subdomainTag.setTextContent(subdomainName);

				Element subdomainParent = subdomainListMap.get(diseaseName + domainName);
				subdomainParent.appendChild(subdomainTag);

			}



		}

		// HATEOAS
		String scheme = req.getScheme();
		String serverName = req.getServerName();
		String contextPath = req.getContextPath();
		String servletPath = req.getServletPath();
		String pathInfo = req.getPathInfo();
		String queryString = req.getQueryString();

		StringBuilder self = new StringBuilder();
		self.append(scheme).append("://").append(serverName).append(contextPath).append(servletPath);

		if (pathInfo != null) {
			self.append(pathInfo);
		}
		if (queryString != null) {
			self.append("?").append(queryString);
		}

		Element linkTag = doc.createElement("link");
		linkTag.setAttribute("ref", "self");
		linkTag.setAttribute("href", self.toString());
		root.appendChild(linkTag);

		return transformDocument(doc);

	}

	public static StringWriter generateFormStructureXml(FormStructure fs, List<String> fields)
			throws TransformerException, ParserConfigurationException {
		Document doc = createDomDocument();

		Element root = doc.createElement("formStructure");
		doc.appendChild(root);

		Element name = doc.createElement("name");
		name.setTextContent(fs.getShortName());
		root.appendChild(name);

		Element version = doc.createElement("version");
		version.setTextContent(fs.getVersion());
		root.appendChild(version);

		if (fields == null || fields.contains("status")) {
			Element e = doc.createElement("status");
			e.setTextContent(fs.getStatus().getType().toUpperCase());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("description")) {
			Element e = doc.createElement("description");
			e.setTextContent(fs.getDescription());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("organization")) {
			Element e = doc.createElement("organization");
			e.setTextContent(fs.getOrganization() == null ? "" : fs.getOrganization());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("fileType")) {
			Element e = doc.createElement("fileType");
			e.setTextContent(fs.getFileType().getType().toUpperCase());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("isCopyrighted")) {
			Element e = doc.createElement("isCopyrighted");
			e.setTextContent(fs.getIsCopyrighted().toString().toUpperCase());
			root.appendChild(e);
		}

		if (fields == null || fields.contains("diseases")) {
			Element list = doc.createElement("diseases");
			if (fs.getDiseases() != null) {
				for (Disease d : fs.getDiseases()) {
					Element disease = doc.createElement("disease");
					disease.setTextContent(d.getName());
					list.appendChild(disease);
				}
			}
			root.appendChild(list);
		}

		if (fields == null || fields.contains("elementGroups")) {
			Element elementGroupsTag = doc.createElement("elementGroups");
			if (fs.getRepeatableGroups() != null) { // this check should never fail
				// loop over groups
				for (RepeatableGroup rg : fs.getSortedRepeatableGroups()) {
					Element elementGroupTag = doc.createElement("elementGroup");

					Element groupNameTag = doc.createElement("name");
					groupNameTag.setTextContent(rg.getName());

					Element groupTypeTag = doc.createElement("type");
					groupTypeTag.setTextContent(rg.getType().getValue().toUpperCase());

					Element groupThresholdTag = doc.createElement("threshold");
					groupThresholdTag.setTextContent(rg.getThreshold().toString());

					Element elementsTag = doc.createElement("elements");
					if (rg.getMapElements() != null) { // this check should never fail
						// loop over elements in a group
						for (MapElement me : rg.getMapElements()) {
							Element requiredTypeTag = doc.createElement("requiredType");
							requiredTypeTag.setTextContent(me.getRequiredType().getValue().toUpperCase());

							Element linkTag = doc.createElement("link");
							linkTag.setAttribute("rel", "self");
							linkTag.setTextContent(me.getStructuralDataElement().getNameAndVersion());

							elementsTag.appendChild(requiredTypeTag);
							elementsTag.appendChild(linkTag);
						}

					}
					elementGroupTag.appendChild(groupNameTag);
					elementGroupTag.appendChild(groupTypeTag);
					elementGroupTag.appendChild(elementsTag);
				}
			}
			root.appendChild(elementGroupsTag);
		}

		Element fsLink = doc.createElement("link");
		fsLink.setAttribute("rel", "self");
		fsLink.setTextContent(fs.getShortNameAndVersion());
		root.appendChild(fsLink);



		return transformDocument(doc);
	}

	public static StringWriter generateFormStructureSearchXml(ResultSet rs)
			throws ParserConfigurationException, TransformerException {

		Document doc = createDomDocument();
		
		Element root = doc.createElement("formStructures");
		doc.appendChild(root);
		
		// iterate over each row of results (each row is a fs)
		while(rs.hasNext()) {
			QuerySolution q = rs.next();
			Element fsTag = doc.createElement("formStructure");

			// These are fetched first because they are used twice (for HATEOAS)
			String name = getStringValue(q.get(RDFConstants.SHORT_NAME_VARIABLE.getName()));
			String version = getStringValue(q.get(RDFConstants.VERSION_VARIABLE.getName()));

			Element nameTag = doc.createElement("name");
			nameTag.setTextContent(name);
			fsTag.appendChild(nameTag);

			Element versionTag = doc.createElement("version");
			versionTag.setTextContent(version);
			fsTag.appendChild(versionTag);

			Element titleTag = doc.createElement("title");
			titleTag.setTextContent(getStringValue(q.get(RDFConstants.TITLE_VARIABLE.getName())));
			fsTag.appendChild(titleTag);

			Element statusTag = doc.createElement("status");
			statusTag.setTextContent(getStringValue(q.get(RDFConstants.STATUS_VARIABLE.getName())).toUpperCase());
			fsTag.appendChild(statusTag);

			root.appendChild(fsTag);
		}
		

		return transformDocument(doc);
	}

	/**
	 * Uses the same method used in the core module to convert the RDFNode into a string
	 * representation of the actaul data.
	 * 
	 * @param node
	 * @return
	 */
	private static String getStringValue(RDFNode node) {

		return (node == null ? null : node.toString());

	}

	/**
	 * A helper funtion that initializes a response xml document. Doing this in one place will allow
	 * any DOM configuration (such as how to handle empty elements) to be consistent.
	 * 
	 * @return
	 * @throws ParserConfigurationException
	 */
	private static Document createDomDocument() throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		return docBuilder.newDocument();
	}

	/**
	 * A helper funtion to finalize a document into a form which can be understood by the http
	 * response.
	 * 
	 * @param doc
	 * @return
	 * @throws TransformerException
	 */
	private static StringWriter transformDocument(Document doc) throws TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		StringWriter sw = new StringWriter();
		t.transform(new DOMSource(doc), new StreamResult(sw));
		return sw;
	}
}
