package gov.nih.tbi.dictionary.ws;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.service.EformManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.util.EformFormViewXmlUtil;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocument;

@Path("/eforms/")
public class PublicEformRestService extends AbstractRestService {
	private static Logger logger = Logger.getLogger(PublicEformRestService.class);

	@Autowired
	EformManager eformManager;

	@Autowired
	RepositoryManager repositoryManager;

	@Context
	private MessageContext context;

	@GET
	@Path("question/{id}/document/{fileName}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getQuestionDocumentFileStream(@PathParam("id") long questionId,
			@PathParam("fileName") String questionDocumentFileName)
			throws WebApplicationException, UnsupportedEncodingException {

		questionDocumentFileName = URLDecoder.decode(questionDocumentFileName, "UTF-8");
		String fileExtesion = FilenameUtils.getExtension(questionDocumentFileName);
		validateParamStringNotBlank(questionDocumentFileName, "fileName");

		Question documentQuestion = eformManager.getQuestion(questionId);

		QuestionDocument userFileDocument =
				eformManager.getQuestionDocument(documentQuestion, questionDocumentFileName);

		try {
			byte[] questionDocumentBytes = repositoryManager.getFileByteArray(userFileDocument.getUserFile());

			if (EformFormViewXmlUtil.QUESTION_FILE_TYPES.contains(fileExtesion.toLowerCase())) {
				ResponseBuilder response = Response.ok(questionDocumentBytes);
				response.type(MediaType.APPLICATION_OCTET_STREAM);
				response.header("Content-Disposition", "attachment; filename= \"" + questionDocumentFileName + "\"");
				return response.build();
			} else {

				ResponseBuilder response = Response.ok(questionDocumentBytes);
				response.header("Content-Disposition", "inline; filename=" + userFileDocument.getUserFile().getName());
				return response.build();
			}
		} catch (Exception e) {
			String errorMessage = "There was an error retrieving the question document.";
			logger.error(errorMessage, e);
			throw new InternalServerErrorException(errorMessage, e);
		}
	}

	private void validateParamStringNotBlank(String stringToCheck, String paramaterName) {
		if (StringUtils.isBlank(stringToCheck)) {
			String msg = "The " + paramaterName + " field is required.";
			logger.error(msg);

			throw new BadRequestException(msg);
		}
	}


}
