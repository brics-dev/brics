
package gov.nih.tbi.recaptcha.ws;

import javax.servlet.http.HttpServletRequest;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.recaptcha.model.ReCaptchaResponse;
import gov.nih.tbi.recaptcha.service.ReCaptcha;


@Path("/")
public class RecaptchaRestService extends AbstractRestService {

	private static final Logger logger = Logger.getLogger(RecaptchaRestService.class);
	

	@Autowired
	private ReCaptcha recaptcha;

	@Resource
	private WebServiceContext wsContext;

	/***************************************************************************************************/

	//



	
    /**
	 * 
	 * @param response - captcha response
	 * @return No content response.
	 * @throws WebApplicationException When an error response needs to be sent back to the client.
	 */
	@POST
    @Path("verify")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String verifyCaptcha(@FormParam("response") String response, @Context HttpServletRequest request) throws WebApplicationException {
		String remoteip = request.getRemoteAddr();
		try {
			recaptcha.setSecret("REPLACED");
			ReCaptchaResponse verifyResponse = recaptcha.checkAnswer(response, remoteip);
			return verifyResponse.getErrorMessage();
		} catch (Exception e) {
			String msg = "recaptcha error.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}
	}
	
	
	
}
