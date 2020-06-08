package gov.nih.tbi.api.query.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class QueryExceptionHandler extends ResponseEntityExceptionHandler {
	final Logger logger = LoggerFactory.getLogger(QueryExceptionHandler.class);

	@ExceptionHandler(ApiEntityNotFoundException.class)
	public ResponseEntity<QueryErrorResponse> apiEntityNotFound(Exception ex, WebRequest request) {
		logger.warn(request.getUserPrincipal().getName() + " got a 404 while calling " + request.getContextPath(), ex);
		QueryErrorResponse errors = new QueryErrorResponse();
		errors.setTimestamp(LocalDateTime.now());
		errors.setError(ex.getMessage());
		errors.setStatus(HttpStatus.NOT_FOUND.value());

		return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
	}
}
