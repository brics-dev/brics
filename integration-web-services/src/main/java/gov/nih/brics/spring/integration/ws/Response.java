package gov.nih.brics.spring.integration.ws;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

public class Response {

	static Logger log = Logger.getLogger(Response.class);

	public String handleResponse(Message<?> s) {
		log.info("Handle Response: " + s);
		MessageHeaders mh = s.getHeaders();
		for (Object o : mh.entrySet()) {
			log.info("\tHeader: " + o.toString());
		}
		return s.toString();
	}

	public String handleResponse(Object s) {
		log.info("Handle Response: " + s);
		if (s instanceof List<?>) {
			List<?> objectList = (List<?>) s;
			for (Object o : objectList) {
				log.info("\tResponse List: " + ((File)o).getPath());
			}
			
		}
		return s.toString();
	}
}
