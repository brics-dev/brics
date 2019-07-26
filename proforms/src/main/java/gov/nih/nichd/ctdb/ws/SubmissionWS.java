package gov.nih.nichd.ctdb.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.attachments.manager.AttachmentManager;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.ServerFileSystemException;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;

@Service
@Path("/submission")
public class SubmissionWS {
	private static final Logger LOGGER = Logger.getLogger(SubmissionWS.class);
	
	/**
	 * This class will be used to send file data to the client browser or server.
	 * 
	 * @author jeng
	 *
	 */
	private class StreamingFileOutput implements StreamingOutput {
		private File systemFile = null;
		
		/**
		 * Constructor that will store a reference to a Java file handler, which
		 * will be used later to send the file data to the client browser or server.
		 * 
		 * @param inFile - The Java file handler to be used later for uploading the file's data
		 * @throws WebApplicationException	Only if the file object parameter is null.
		 */
		public StreamingFileOutput(File inFile) throws WebApplicationException {
			if ( inFile != null ) {
				systemFile = inFile;
			}
			else {
				throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
			}
		}
		
		/**
		 * Writes the data from the stored file object out to the provided response output stream.
		 * 
		 * @param out - The response output stream
		 * @throws IOException	When an access error occurs from the server's file system.
		 * @throws WebApplicationException	When any other error occurs.
		 */
		@Override
		public void write(OutputStream out) throws IOException, WebApplicationException {
			FileInputStream in = new FileInputStream(systemFile);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			
			try {
				// Stream the file out to the user's browser
				bytesRead = in.read(buffer);
				
				while ( bytesRead > 0 ) {
					out.write(buffer, 0, bytesRead);
					out.flush();
					bytesRead = in.read(buffer);
				}
			}
			finally {
				in.close();
				out.close();
			}
		}
	}

	/**
	 * Handler for requests to the root path. Will try to generate CDISC compliant XML string for submissions to the data repository.
	 * 
	 * @return
	 * @throws WebApplicationException
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getSubmissionXML() throws WebApplicationException {
		StringBuffer xml = new StringBuffer(1000);
		
		try {
			ResponseManager rm = new ResponseManager();
			FormManager fm = new FormManager();
			ProtocolManager pm = new ProtocolManager();
			
			LOGGER.info("Creating the submission XML file...");

			// Create and format the creation time stamp.
			Date currentDate = new Date();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String calDate = df.format(currentDate);
			df = new SimpleDateFormat("HH:mm:ss");
			String timeDate = df.format(currentDate);
			String dateStr = calDate + "T" + timeDate;

			// Initialize the XML string
			xml.append("<?xml version=\"1.0\"?>");
			xml.append("<ODM xmlns=\"http://www.cdisc.org/ns/odm/v1.3\" FileOID=\"000-00-0000\" FileType=\"Snapshot\" CreationDateTime=\"" + dateStr + "\">");

			// Parse through all of the studies.
			Map<Integer, String> studyIdAndNamesMap = pm.getProtocolIds();
			
			for (Entry<Integer, String> studyEntry : studyIdAndNamesMap.entrySet()) {
				int studyId = studyEntry.getKey().intValue();
				String studyName = studyEntry.getValue();

				//gets list of all locked aform ids sorted in a hashmap based on patient id and visit type
				//
				//     key: patientId     value : Map  ->  (key: visittype, value: List<aformids>)
				//
				//
				Map<String, Map<String, List<Integer>>> outerMap = rm.getPatientIdLockedAformIdsHashMap(studyId);
				
				if (!outerMap.isEmpty()) {
					xml.append("<ClinicalData MetaDataVersionOID=\"000-00-0000\" StudyOID=\"" + StringEscapeUtils.escapeXml10(studyName) + "\">");
					
					for (Entry<String, Map<String, List<Integer>>> outMapEntry : outerMap.entrySet()) {
						String subjectId = outMapEntry.getKey();
						Map<String, List<Integer>> innerMap = outMapEntry.getValue();
						
						xml.append("<SubjectData SubjectKey=\"" + subjectId + "\">");
						
						for (Entry<String, List<Integer>> inMapEntry : innerMap.entrySet()) {
							String visitTypeName = inMapEntry.getKey();
							List<Integer> aformIdList = inMapEntry.getValue();
							
							xml.append("<StudyEventData StudyEventOID=\"" + StringEscapeUtils.escapeXml10(visitTypeName) + "\">");

							for (Integer aformIdInteger : aformIdList) {
								//gets the final answers
								xml.append(getFinalAnswersXML(rm, fm, aformIdInteger.intValue()));
							}

							xml.append("</StudyEventData>");
						}

						xml.append("</SubjectData>");
					}

					xml.append("</ClinicalData>");
				}
			} //end protocol ids iterator

			xml.append("</ODM>");
		}
		catch(CtdbException ce) {
			LOGGER.error("Could not create the submission XML due to a database error.", ce);
			throw new WebApplicationException(ce, Status.INTERNAL_SERVER_ERROR);
		}
		
		LOGGER.info("Submission XML has been created.");
		LOGGER.debug(xml.toString());
		LOGGER.info("Validating the submission XML...");

		// Validate this XML string against the CDISC schema
		try {
			// 1. Lookup a factory for the W3C XML Schema language
			SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			
			// 2. Get the CDISC schema and apply it to the factory.
			URL schemaLocation = new URL("https://dev.openclinica.com/tools/odm-doc/ODM1-3-0-foundation.xsd");
			Schema schema = factory.newSchema(schemaLocation);

			// 3. Get a validator from the schema.
			Validator validator = schema.newValidator();

			// 4. covert our generated XML to a Reader object
			Reader reader = new StringReader(xml.toString());

			// 5. Parse the document
			Source source = new StreamSource(reader);
			
			// 6. Check the document
			validator.validate(source);
			LOGGER.info("Submission XML passed validation.");
		}
		catch (MalformedURLException mue) {
			LOGGER.error("Could not access the OpenClinica/CDISC XSD file.", mue);
			throw new WebApplicationException(mue, Status.INTERNAL_SERVER_ERROR);
		}
		catch (SAXException | IOException e) {
			LOGGER.error("The sumbission XML failed cdisc schema validation.", e);
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
		
		return xml.toString();
	}
	
	/**
	 * Downloads a file from the server that is associated with a given attachment ID.
	 * 
	 * @param attachId - The ID of the file to download
	 * @return	A response object containing the binary file data of the specified attachment ID.
	 * @throws WebApplicationException	404 if the attachment ID is not in the database or if the file itself cannot be
	 * found in the server's file system.  500 for all other errors, which include database errors and file system access errors.
	 */
	@GET
	@Path("download/{fileId}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadFile(@PathParam("fileId") long attachId) throws WebApplicationException {
		File sysFile = null;
		Attachment a = null;
		
		// Get the file details from the database and the file data from the server's file system
		try {
			AttachmentManager attchMan = new AttachmentManager();
			a = attchMan.getAttachment(attachId);
			sysFile = attchMan.getFileFromSystem(a);
		}
		catch ( ObjectNotFoundException onfe ) {
			LOGGER.error("The attachment ID " + Long.toString(attachId) + " is not in the database.", onfe);
			throw new WebApplicationException(onfe, Status.NOT_FOUND);
		}
		catch ( CtdbException ce ) {
			LOGGER.error("A database error occurred for attachment ID " + Long.toString(attachId), ce);
			throw new WebApplicationException(ce, Status.INTERNAL_SERVER_ERROR);
		}
		catch ( ServerFileSystemException sfse ) {
			LOGGER.error("An error occurred while accessing the file from the file system.", sfse);
			throw new WebApplicationException(sfse, Status.INTERNAL_SERVER_ERROR);
		}
		
		LOGGER.info("Sending file data from attachement " + Long.toString(attachId) + " to the client.");
		
		// Create a streaming output object pass it the file data handle, then put the result in the Response object
		return Response.ok(new StreamingFileOutput(sysFile), MediaType.APPLICATION_OCTET_STREAM_TYPE)
				.header("Content-Disposition", "attachment; filename=\"" + a.getFileName() + "\"")
				.header("Content-Length", Long.toString(sysFile.length()))
				.build();
	}
	
	/**
	 * 
	 * @param rm
	 * @param fm
	 * @param aformId
	 * @return
	 * @throws CtdbException
	 */
	private StringBuffer getFinalAnswersXML(ResponseManager rm, FormManager fm, int aformId) throws CtdbException {
		StringBuffer xml = new StringBuffer(200);
		Map<String, Map<String, List<String>>> outerMap = new HashMap<String, Map<String, List<String>>>();
		int formId = rm.getFormId(aformId);
		String dataStructureName = fm.getDataStructureName(formId);
		
		xml.append("<FormData FormOID=\"" + dataStructureName + "\">");
		
		List<List<Section>> rowList = fm.getSections(formId);
		
		for (List<Section> aRow : rowList) {
			for (Section section : aRow) {
				if (section != null) {
					boolean isSectionRepeatable = section.isRepeatable();
					String repeatableGroupName = section.getRepeatableGroupName();
					
					// write out if section is repeatable
					if (isSectionRepeatable) {
						int sectionId = section.getId();
						List<Question> questions = section.getQuestionList();
						
						xml.append("<ItemGroupData ItemGroupOID=\"" + StringEscapeUtils.escapeXml10(repeatableGroupName) + "\">");
						
						for (Question question : questions) {
							int questionId = question.getId();
							String[] dataElementGroupAndName = fm.getDataElementGeoupAndName(sectionId, questionId);
							
							if (dataElementGroupAndName != null) {
								String dataElementName = dataElementGroupAndName[1];
								int responseId = rm.getResponseId(aformId, sectionId, questionId);
								List<String> finalAnswers = rm.getFinalAnswers(responseId);

								for (String answer : finalAnswers) {
									xml.append("<ItemData ItemOID=\"" + dataElementName + "\" Value=\"" + StringEscapeUtils.escapeXml10(answer) + "\"/>");
								}
							}
						}
						
						xml.append("</ItemGroupData>");
					}
					// section is not-repeatable, store values in hashmap with key as group name so we can write out based on group...
					// rather than section (since non-repeatable section can have de's from many groups)
					else {
						int sectionId = section.getId();
						List<Question> questions = section.getQuestionList();
						
						for (Question question : questions) {
							int questionId = question.getId();
							String[] dataElementGroupAndName = fm.getDataElementGeoupAndName(sectionId, questionId);
							
							if (dataElementGroupAndName != null) {
								String dataElementGroup = dataElementGroupAndName[0];
								String dataElementName = dataElementGroupAndName[1];
								int responseId = rm.getResponseId(aformId, sectionId, questionId);
								List<String> finalAnswers = rm.getFinalAnswers(responseId);
								
								if (!finalAnswers.isEmpty()) {
									Map<String, List<String>> innerMap = outerMap.get(dataElementGroup);
									
									if (innerMap == null) {
										innerMap = new HashMap<String, List<String>>();
										innerMap.put(dataElementName, finalAnswers);
										outerMap.put(dataElementGroup, innerMap);
									}
									else {
										innerMap.put(dataElementName, finalAnswers);
									}
								}
							}
						}
					}
				} 
			} 
		} // end sections iterator
		
		// now write out the non-repeatable stuff!!
		for (Entry<String, Map<String, List<String>>> outMapEntry : outerMap.entrySet()) {
			String groupName = outMapEntry.getKey();
			Map<String, List<String>> innerMap = outMapEntry.getValue();
			
			xml.append("<ItemGroupData ItemGroupOID=\"" + StringEscapeUtils.escapeXml10(groupName) + "\">");

			for (Entry<String, List<String>> inMapEntry : innerMap.entrySet()) {
				String dataElementName = inMapEntry.getKey();
				
				for (String answer : inMapEntry.getValue()) {
					xml.append("<ItemData ItemOID=\"" + dataElementName + "\" Value=\"" + StringEscapeUtils.escapeXml10(answer) + "\"/>");
				}
			}

			xml.append("</ItemGroupData>");
		}

		xml.append("</FormData>");

		return xml;
	}
}
