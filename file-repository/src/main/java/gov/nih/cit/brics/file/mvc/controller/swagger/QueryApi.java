/**
 * NOTE: This class is auto generated by the swagger code generator program (3.0.8).
 * https://github.com/swagger-api/swagger-codegen Do not edit the class manually.
 */
package gov.nih.cit.brics.file.mvc.controller.swagger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nih.cit.brics.file.mvc.model.swagger.FileDetails;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;


@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-19T16:34:07.992-05:00[America/New_York]")
@Api(value = "query", description = "the query API")
public interface QueryApi {

	@ApiOperation(value = "Retrieves the file details from the database for the given file ID.", nickname = "getFileDetails", notes = "", response = FileDetails.class, authorizations = {
			@Authorization(value = "bearerAuth")}, tags = {"query",})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "A JSON object containing the files ID, name, instance ID, and group ID.", response = FileDetails.class),
			@ApiResponse(code = 404, message = "File ID is not in the system."),
			@ApiResponse(code = 401, message = "When the user is not logged in."),
			@ApiResponse(code = 403, message = "When there is a perimssions valiation on the file being accessed."),
			@ApiResponse(code = 500, message = "A database error or JSON parse or generation error.")})
	@RequestMapping(value = "/query/{fileId}", produces = "application/json", method = RequestMethod.GET)
	ResponseEntity<FileDetails> getFileDetails(
			@ApiParam(value = "The ID of the file that the client needs details for.", required = true) @PathVariable("fileId") String fileId);


	@ApiOperation(value = "Looks up the file size (in bytes) of the specified file.", nickname = "getFileSize", notes = "", response = String.class, authorizations = {
			@Authorization(value = "bearerAuth")}, tags = {"query",})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "The size of the requested file in bytes.", response = String.class),
			@ApiResponse(code = 401, message = "When the user is not logged in."),
			@ApiResponse(code = 403, message = "When there is a perimssions valiation on the file being accessed."),
			@ApiResponse(code = 404, message = "File ID is not in the system."),
			@ApiResponse(code = 500, message = "File system access error occured.")})
	@RequestMapping(value = "/query/fileSize/{fileId}", produces = "text/plain", method = RequestMethod.GET)
	ResponseEntity<String> getFileSize(
			@ApiParam(value = "The ID of the file for the requested file size.", required = true) @PathVariable("fileId") String fileId);

}