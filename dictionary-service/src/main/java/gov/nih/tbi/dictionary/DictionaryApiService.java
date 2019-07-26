package gov.nih.tbi.dictionary;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.dictionary.model.MissingSemanticObjectException;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.restful.StructuralFormStructureListItem;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.commons.service.DictionaryToolManager;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AbstractRestService;

import gov.nih.tbi.common.exception.BricsWebApplicationException;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.service.DictionaryAPISearchManager;
import gov.nih.tbi.dictionary.util.SearchParamParserUtil;
import gov.nih.tbi.dictionary.xml.XmlGenerationUtil;
import gov.nih.tbi.dictionary.model.restful.EntityList;
import gov.nih.tbi.dictionary.model.restful.ListItem;

import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;

@Path("/v1/")
public class DictionaryApiService extends AbstractRestService {

	static Logger logger = Logger.getLogger(DictionaryApiService.class);

	final int apiVersion = 1;

	@Autowired
	DictionaryToolManager dictionaryToolManager;

	@Autowired
	DictionaryAPISearchManager dictionaryAPISearchManager;

	@Context
	MessageContext context;

    @GET
	@Path("/data_elements/{shortname}")
	@Produces("application/xml")
	public DataElement getDataElement(@PathParam("shortname") String shortName, @QueryParam("version") String version) {
    	
		// Manager call
		DataElement de = dictionaryAPISearchManager.getDataElement(shortName, version);
		if (de == null) {
			String v = "";
			if (version != null) {
				v = "with version " + version + " ";
			}
			String m = "The data element \"" + shortName + "\" " + v + "could not be found.";
			throw new BricsWebApplicationException(Response.Status.NOT_FOUND, m);
		}
    	
		// Permission check? (deny draft resources)
		if (DataElementStatus.DRAFT.equals(de.getStatus())) {
			String m = "The data element \"" + shortName + "\" is not public.";
			throw new BricsWebApplicationException(Response.Status.FORBIDDEN, m);
		}

		return de;
    }

	@GET
	@Path("/element/{title}")
	@Produces("application/xml")
	public Response getLatestElementByTitle(@PathParam("title") String title) {

		Response result = null;

		DataElement elemResult = dictionaryToolManager.getLatestDataElementByName(title);

		if(elemResult != null) {
			result = Response.status(200).entity(elemResult).build();
		} else {
			result = Response.status(404).build();
		}


		return result;
	}

	@GET
	@Path("/structure/{shortName}")
	@Produces("application/xml")
	public Response getLatestStructureByShortName(@PathParam("shortName") String shortName) {

		Response result = null;

		FormStructure structResult = dictionaryToolManager.getLatestDataStructure(shortName);

		if(structResult != null) {
			result = Response.status(200).entity(structResult).build();
		} else {
			result = Response.status(404).build();
		}

		return result;
	}

	@GET
	@Path("/structureByID/{id}")
	@Produces("application/xml")
	public Response getStructureByID(@PathParam("id") Long id) {

		Response result = null;

		FormStructure structResult = dictionaryToolManager.getDataStructure(id);

		if(structResult != null) {
			result = Response.status(200).entity(structResult).build();
		} else {
			result = Response.status(404).build();
		}

		return result;
	}

    @GET
	@Path("/form_structures/{shortname}")
	@Produces("application/xml")
	public Response getFormStructure(@PathParam("shortname") String shortName, @QueryParam("fields") String fieldInput,
			@QueryParam("version") String version) {
    	
    	// parse field list
		List<String> fields = null;
    	if (fieldInput != null)
    	{
    		fields = Arrays.asList(fieldInput.split(","));
    	}

    	// Manager call coming soon.
		FormStructure fs = dictionaryAPISearchManager.getFormStructure(shortName, version);
		if (fs == null) {
			String v = "";
			if (version != null) {
				v = "with version " + version + " ";
			}
			String m = "The form structure \"" + shortName + "\" " + v + "could not be found.";
			throw new BricsWebApplicationException(Response.Status.NOT_FOUND, m);
		}
    	
    	// Permission check?
		StatusType status = fs.getStatus();
		if (StatusType.DRAFT.equals(status) || StatusType.AWAITING_PUBLICATION.equals(status)) {
			String m = "The form structure \"" + shortName + "\" is not public.";
			throw new BricsWebApplicationException(Response.Status.FORBIDDEN, m);
		}
    	
    	// Generate XML doc
		StringWriter sw = null;
		try {
			sw = XmlGenerationUtil.generateFormStructureXml(fs, fields);
		} catch (ParserConfigurationException | TransformerException e) {
			String m = "An unexpected error has occured. Please contact the operations team.";
			throw new BricsWebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR, m);
		}
		
		return Response.ok(sw.toString(), MediaType.APPLICATION_XML_TYPE).build();
    
	}

	@GET
	@Path("/form_structures")
	@Produces("application/xml")
	public Response searchFormStructures(@QueryParam("page") Integer pageInput,
			@QueryParam("pageSize") Integer pageSizeInput, @QueryParam("sort") String sortInput) {

		// construct page data object
		// defaults
		boolean asc = true;
		String sort = "title";
		Integer page = 1;
		Integer pageSize = 100;
		// reading inputs
		if(pageInput != null)
		{
			if (pageInput.intValue() < 1) {
				String m = "Argument \"page\" must be greater than 0";
				throw new BricsWebApplicationException(Response.Status.BAD_REQUEST, m);
			}
			page = pageInput;
		}
		if(pageSizeInput != null)
		{
			if (pageSizeInput.intValue() > 1000 || pageSizeInput.intValue() < 1) {
				String m = "Argument \"pageSize\" must be between 1 and 1000 inclusive.";
				throw new BricsWebApplicationException(Response.Status.BAD_REQUEST, m);
			}
			pageSize = pageSizeInput;
		}
		if(sort != null)
		{
			// TODO: handle sorting
		}
		PaginationData pageData = new PaginationData(page, pageSize, asc, sort);
		
		// Query parameters
		Map<FormStructureFacet, Set<String>> facetMap =
				SearchParamParserUtil.generateFormStructureFacetMap(context.getHttpServletRequest().getParameterMap());
		
		// Correct status facet for only public entities
		if (facetMap.containsKey(FormStructureFacet.STATUS)) {
			Set<String> statusSet = facetMap.get(FormStructureFacet.STATUS);
			statusSet.remove(StatusType.DRAFT.getType());
			statusSet.remove(StatusType.AWAITING_PUBLICATION.getType());
		} else {
			facetMap.put(FormStructureFacet.STATUS,
					new HashSet<String>(Arrays.asList(new String[] {StatusType.PUBLISHED.getType(),
							StatusType.ARCHIVED.getType(), StatusType.SHARED_DRAFT.getType()})));
		}
		
		// Manager call
		StringWriter sw = null;
		try {
			sw = dictionaryAPISearchManager.searchFormStructure(facetMap, pageData);
		} catch (ParserConfigurationException | TransformerException e) {
			String m = "An unexpected error has occured. Please contact the operations team.";
			throw new BricsWebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR, m);
		}

		return Response.ok(sw.toString(), MediaType.APPLICATION_XML_TYPE).build();

	}

	@DELETE
	@Path("/structureById/{id}")
	@Produces("text/xml")
	public Response deleteFormStructure(@PathParam("id") Long id, @QueryParam("ticket") String proxyTicket) {

		Response result = null;
		Account account = null;

		try {
			account = getAuthenticatedAccount();
		} catch(Exception ex) {
			logger.error("Unable to authenticate account in deleteFormStructure");
		}

		try {


			FormStructure struct = this.dictionaryToolManager.getDataStructure(id);

			if (struct != null) {
				this.dictionaryToolManager.deleteDataStructure(account, struct, proxyTicket);
				result = Response.status(200).build();
			} else {
				result = Response.status(404).build();
			}
		} catch(MissingSemanticObjectException sparqlEx) {

			StructuralFormStructure sqlStruct = this.dictionaryToolManager.getSqlDataStructure(id);

			if(sqlStruct != null) {
				this.dictionaryToolManager.deleteSqlDataStructure(account, id, proxyTicket);
				result = Response.status(200).build();
			} else {
				result = Response.status(404).build();
			}
		} catch(Exception ex) {
			result = Response.status(500).build();
		}



		return result;
	}

	@DELETE
	@Path("/form_structures/{shortname}")
	@Produces("text/xml")
	public Response deleteFormStructure(@PathParam("shortname") String shortname, @QueryParam("ticket") String proxyTicket) {

		Response result = null;

		try {
			Account account = getAuthenticatedAccount();

			FormStructure structResult = dictionaryToolManager.getLatestDataStructure(shortname);

			if(structResult != null) {
				dictionaryToolManager.deleteDataStructure(account, structResult, proxyTicket);
				result = Response.status(200).build();
			} else {
				result = Response.status(404).build();
			}

		} catch(Exception ex) {
			result = Response.status(500).build();
		}



		return result;
	}

	@DELETE
	@Path("/data_elements/{shortname}")
	@Produces("text/xml")
	public Response deleteDataElement(@PathParam("shortname") String shortname, @QueryParam("component") String component) {

		Response result = null;

		if(component == null) {

			try {
				this.dictionaryToolManager.deleteDataElement(shortname);
				result = Response.status(200).build();
			} catch (Exception ex) {
				result = Response.status(500).build();
			}
		} else if(component.equals("structural")) {
			try {
				this.dictionaryToolManager.deleteStructuralDataElement(shortname);
				result = Response.status(200).build();
			} catch (Exception ex) {
				result = Response.status(500).build();
			}
		} else if(component.equals("semantic")) {

		} else {
			result = Response.status(500).build();
		}

		return result;
	}

	@POST
	@Path("DataElement/create")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces("text/xml")
	public Response createDataElement(DataElement element) {

		Response result = null;

		String elementName = element.getName();

		if(elementName == null) {
			result = Response.status(500).entity("Element name cannot be null").build();
			return result;
		}

		Date createDate = element.getDateCreated();

		if(createDate == null) {
			createDate = new Date();
			element.setDateCreated(createDate);
		}

		String version = element.getVersion();

		if(version == null) {
			String shortName = element.getName();
			String nextVersion = this.dictionaryToolManager.getNextMinorDataElementVersion(shortName);
			element.setVersion(nextVersion);
		}

		DataElementStatus status = element.getStatus();

		if(status == null) {
			status = DataElementStatus.DRAFT;
		}

		try {
			Account account = getAuthenticatedAccount();

			List<String> errors = new ArrayList<String>();
			List<String> warnings = new ArrayList<String>();

			//String[] proxyTix = { null, null };

			DataElement resultObj = dictionaryToolManager.saveDataElement(account, element, errors, warnings, SeverityLevel.MINOR, new String[]{ null, null }, null, false);

			if(!status.equals(DataElementStatus.DRAFT)) {
				resultObj = dictionaryToolManager.editDataElementStatus(account, resultObj, status, new String[]{ null, null, null });

			}

			result = Response.status(200).entity(resultObj).build();
		} catch(Exception anyEx) {

			anyEx.printStackTrace();
			result = Response.status(500).entity("Error processing your request").build();
		}

		return result;
	}

	@POST
	@Path("FormStructure/create")
	@Consumes(MediaType.APPLICATION_XML)
    @Produces("text/xml")
	public Response createFormStructure(FormStructure input) {

		Response result = null;

        try {
			Account account = getAuthenticatedAccount();

			PermissionType permType = PermissionType.OWNER;

			List<String> errors = new ArrayList<String>();
			List<String> warnings = new ArrayList<String>();

			//String[] proxyTix = { null, null };

			String version = input.getVersion();

			if(version == null) {
				String shortName = input.getShortName();
				String nextVersion = this.dictionaryToolManager.getNextMinorDataStructureVersion(shortName);
				input.setVersion(nextVersion);
			}

			Long modUserId = input.getModifiedUserId();

			if(modUserId == null) {
				modUserId = account.getUserId();
				input.setModifiedUserId(modUserId);
			}

			Date modDate = input.getModifiedDate();

			if(modDate == null) {
				modDate = new Date();
				input.setModifiedDate(modDate);
			}

			input.setCreatedBy(String.valueOf(modUserId));

			FormStructure saved = dictionaryToolManager.saveDataStructure(account, permType, input, errors, warnings, SeverityLevel.MINOR, null);
			long structID = saved.getId();

			FormStructure xmlReady = dictionaryToolManager.getDataStructure(structID);

			result = Response.status(200).entity(xmlReady).build();

		} catch(Exception anyEx) {
            anyEx.printStackTrace();
			result = Response.status(500).entity("Error processing your request").build();
		}

        return result;
	}

	@GET
	@Path("/allSqlFormStructures")
	@Produces("application/xml")
	public Response getAllSqlFormStructures() {

		Response result;

		List<StructuralFormStructure> nativeList = this.dictionaryToolManager.getAllSqlFormStructures();

		EntityList<StructuralFormStructureListItem> resultList = new EntityList<>("formStructure");

		for(StructuralFormStructure nativeItem : nativeList) {
			resultList.addEntity(nativeItem);
		}

		try {

			String xml = getDynamicallyMarshalledListXML(resultList, "formStructureList", StructuralFormStructureListItem.class);

			result = Response.status(200).entity(xml).build();
		} catch(JAXBException jaxbX) {
			result = Response.status(500).entity("Error processing your request").build();
		}

		return result;
	}

	private static String getDynamicallyMarshalledListXML(EntityList<?> list, String name, Class<? extends ListItem> itemClass)
			throws JAXBException {

		java.io.StringWriter sw = new StringWriter();

		JAXBContext context = JAXBContext.newInstance(EntityList.class, itemClass);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		QName qName = new QName(name);

		JAXBElement<EntityList> jaxbElement = new JAXBElement<>(qName, EntityList.class, list);

		marshaller.marshal(jaxbElement, sw);

		String result = sw.toString();

		return result;
	}
}

