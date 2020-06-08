package gov.nih.cit.brics.file.mvc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import gov.nih.cit.brics.file.data.entity.UserToken;
import gov.nih.cit.brics.file.mvc.controller.swagger.StatusApi;

@RestController
@RequestScope
public class StatusApiController extends BaseFileRestController implements StatusApi {
	private static final Logger logger = LoggerFactory.getLogger(StatusApiController.class);

	public static final String STATUS_PAGE_HTML_STR =
			"<html><head><title>File Repository Status</title></head><body><h1>It's running</h1></body></html>";

	@Override
	public ResponseEntity<String> getRunningStatus() {
		UserToken token = getUserToken();

		if (token != null) {
			logger.info("Current logged in user: {}", token.getUsername());
		}

		return ResponseEntity.ok(STATUS_PAGE_HTML_STR);
	}

}
