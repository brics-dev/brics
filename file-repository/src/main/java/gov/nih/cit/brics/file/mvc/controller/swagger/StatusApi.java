/**
 * NOTE: This class is auto generated by the swagger code generator program (3.0.8).
 * https://github.com/swagger-api/swagger-codegen Do not edit the class manually.
 */
package gov.nih.cit.brics.file.mvc.controller.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-19T16:34:07.992-05:00[America/New_York]")
@Api(value = "status", description = "the status API")
public interface StatusApi {

	@ApiOperation(value = "Produces a simple status page, which is used to quickly verify that the web service is running.", nickname = "getRunningStatus", notes = "", response = String.class, authorizations = {
			@Authorization(value = "bearerAuth")}, tags = {"status",})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Information about the running service.", response = String.class),
			@ApiResponse(code = 401, message = "When the user is not logged in.")})
	@RequestMapping(value = "/status", produces = "text/html", method = RequestMethod.GET)
	ResponseEntity<String> getRunningStatus();

}
